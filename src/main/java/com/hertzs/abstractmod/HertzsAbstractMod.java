package com.hertzs.abstractmod;

import com.hertzs.abstractmod.networking.ActionPayload;
import com.hertzs.abstractmod.networking.ActionStatePayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HertzsAbstractMod implements ModInitializer {
	public static final String MOD_ID = "hertzsabstract";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// 注册网络包
		PayloadTypeRegistry.playC2S().register(ActionPayload.TYPE, ActionPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(ActionStatePayload.TYPE, ActionStatePayload.CODEC);

		// 客户端 -> 服务端：选择动作
		ServerPlayNetworking.registerGlobalReceiver(ActionPayload.TYPE, (payload, context) -> {
			context.server().execute(() ->
					ServerActionManager.perform(context.player(), PlayerAction.byId((byte) payload.actionId())));
		});

		// 服务端每 tick 维护坐/躺/挥手状态
		ServerTickEvents.END_SERVER_TICK.register(ServerActionManager::tick);

		// 玩家断线时清理
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
				ServerActionManager.clear(handler.player));

		LOGGER.info("[Hertzs Abstract] 动作系统已加载：按 G 打开动作轮盘");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
