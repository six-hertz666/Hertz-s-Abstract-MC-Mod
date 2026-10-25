package com.hertzs.abstractmod.networking;

import com.hertzs.abstractmod.HertzsAbstractMod;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * 服务端 -> 客户端：广播某玩家的动作状态（UUID 以字符串传输）。
 */
public record ActionStatePayload(String playerId, int actionId) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<ActionStatePayload> TYPE =
			new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(HertzsAbstractMod.MOD_ID, "action_state"));

	public static final StreamCodec<io.netty.buffer.ByteBuf, ActionStatePayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8, ActionStatePayload::playerId,
			ByteBufCodecs.VAR_INT, ActionStatePayload::actionId,
			ActionStatePayload::new);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
