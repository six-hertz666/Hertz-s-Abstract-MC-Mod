package com.hertzs.abstractmod;

import com.hertzs.abstractmod.networking.ActionStatePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * 服务端动作管理：执行动作、维护坐/躺/挥手状态、广播给所有客户端。
 */
public final class ServerActionManager {
	/** 躺姿取消阈值：一 tick 内位移超过该值视为玩家开始移动 */
	private static final double MOVE_CANCEL_SQR = 0.0025D;
	/** 挥手总时长（tick） */
	private static final int WAVE_DURATION = 21;
	/** 挥手摆臂间隔（tick） */
	private static final int WAVE_INTERVAL = 6;

	private static final Map<UUID, Vec3> LAST_POS = new HashMap<>();
	private static final Map<UUID, ArmorStand> SIT_STANDS = new HashMap<>();
	private static final Map<UUID, Integer> WAVE_TICKS = new HashMap<>();

	private ServerActionManager() {
	}

	public static void perform(ServerPlayer player, PlayerAction action) {
		if (action == PlayerAction.NONE || !player.isAlive()) return;

		// 先清掉旧状态（例如坐着时改选躺下）
		clear(player, false);

		switch (action) {
			case SCARE -> doScare(player);
			case WAVE -> startWave(player);
			case SIT -> startSit(player);
			case LIE -> startLie(player);
			default -> {
			}
		}
	}

	// ---------- 各动作 ----------

	private static void doScare(ServerPlayer player) {
		ServerLevel level = player.level();
		// 未日影人尖啸 + 黑烟，够吓人
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.ENDERMAN_SCREAM, SoundSource.HOSTILE, 1.0F, 1.0F);
		level.sendParticles(ParticleTypes.LARGE_SMOKE,
				player.getX(), player.getY() + 1.0D, player.getZ(),
				40, 0.4D, 0.8D, 0.4D, 0.02D);
		level.sendParticles(ParticleTypes.SMOKE,
				player.getX(), player.getY() + 1.2D, player.getZ(),
				30, 0.5D, 0.9D, 0.5D, 0.01D);
	}

	private static void startWave(ServerPlayer player) {
		WAVE_TICKS.put(player.getUUID(), WAVE_DURATION);
		setAndBroadcast(player, PlayerAction.WAVE);
		player.swing(InteractionHand.MAIN_HAND, true);
	}

	private static void startSit(ServerPlayer player) {
		ServerLevel level = player.level();
		ArmorStand stand = new ArmorStand(level, player.getX(), player.getY(), player.getZ());
		stand.setInvisible(true);
		stand.setMarker(true);
		stand.setNoGravity(true);
		stand.setInvulnerable(true);
		stand.setSilent(true);
		stand.setYRot(player.getYRot());
		level.addFreshEntity(stand);

		player.startRiding(stand, true, true);
		SIT_STANDS.put(player.getUUID(), stand);
		setAndBroadcast(player, PlayerAction.SIT);
	}

	private static void startLie(ServerPlayer player) {
		LAST_POS.put(player.getUUID(), player.position());
		setAndBroadcast(player, PlayerAction.LIE);
	}

	// ---------- 每 tick 维护 ----------

	public static void tick(MinecraftServer server) {
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			PlayerAction action = ActionStateManager.get(false, player.getUUID());
			if (action == PlayerAction.NONE) continue;

			if (!player.isAlive() || player.isRemoved()) {
				clear(player, true);
				continue;
			}

			switch (action) {
				case WAVE -> tickWave(player);
				case SIT -> tickSit(player);
				case LIE -> tickLie(player);
				default -> {
				}
			}
		}
	}

	private static void tickWave(ServerPlayer player) {
		Iterator<Map.Entry<UUID, Integer>> it = WAVE_TICKS.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<UUID, Integer> entry = it.next();
			if (!entry.getKey().equals(player.getUUID())) continue;
			int ticks = entry.getValue() - 1;
			if (ticks <= 0) {
				it.remove();
				// 挥手没有强制姿势，但状态要同步清除
				ActionStateManager.set(false, player.getUUID(), PlayerAction.NONE);
				broadcastState(player.level().getServer(), player.getUUID(), PlayerAction.NONE);
			} else {
				entry.setValue(ticks);
				if (ticks % WAVE_INTERVAL == 0) {
					player.swing(InteractionHand.MAIN_HAND, true);
				}
			}
		}
	}

	private static void tickSit(ServerPlayer player) {
		ArmorStand stand = SIT_STANDS.get(player.getUUID());
		// 玩家自行下坐（Shift）或盔甲架消失 => 取消
		if (stand == null || stand.isRemoved() || player.getVehicle() != stand) {
			clear(player, true);
		}
	}

	private static void tickLie(ServerPlayer player) {
		Vec3 last = LAST_POS.get(player.getUUID());
		if (last == null) {
			LAST_POS.put(player.getUUID(), player.position());
			return;
		}
		// 移动 / 下水 / 被弹起 => 取消躺姿
		if (player.position().distanceToSqr(last) > MOVE_CANCEL_SQR
				|| player.isInWater() || player.isFallFlying() || player.isPassenger()) {
			clear(player, true);
		} else {
			LAST_POS.put(player.getUUID(), player.position());
		}
	}

	// ---------- 清理与广播 ----------

	public static void clear(ServerPlayer player) {
		clear(player, true);
	}

	/** 清理玩家的动作状态；sync 为 false 用于“换个动作前”的内部清理 */
	private static void clear(ServerPlayer player, boolean sync) {
		UUID id = player.getUUID();
		ArmorStand stand = SIT_STANDS.remove(id);
		if (stand != null) {
			player.stopRiding();
			stand.discard();
		}
		LAST_POS.remove(id);
		WAVE_TICKS.remove(id);

		if (sync && ActionStateManager.get(false, id) != PlayerAction.NONE) {
			ActionStateManager.set(false, id, PlayerAction.NONE);
			broadcastState(player.level().getServer(), id, PlayerAction.NONE);
		}
	}

	private static void setAndBroadcast(ServerPlayer player, PlayerAction action) {
		ActionStateManager.set(false, player.getUUID(), action);
		broadcastState(player.level().getServer(), player.getUUID(), action);
	}

	private static void broadcastState(MinecraftServer server, UUID playerId, PlayerAction action) {
		ActionStatePayload payload = new ActionStatePayload(playerId.toString(), action.getId());
		for (ServerPlayer other : server.getPlayerList().getPlayers()) {
			ServerPlayNetworking.send(other, payload);
		}
	}
}
