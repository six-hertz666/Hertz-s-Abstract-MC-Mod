package com.hertzs.abstractmod.networking;

import com.hertzs.abstractmod.HertzsAbstractMod;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * 客户端 -> 服务端：动作轮盘选择了某个动作。
 */
public record ActionPayload(int actionId) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<ActionPayload> TYPE =
			new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(HertzsAbstractMod.MOD_ID, "action"));

	public static final StreamCodec<io.netty.buffer.ByteBuf, ActionPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, ActionPayload::actionId,
			ActionPayload::new);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
