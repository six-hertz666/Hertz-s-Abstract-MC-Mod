package com.hertzs.abstractmod.client;

import com.hertzs.abstractmod.HertzsAbstractMod;
import com.hertzs.abstractmod.PlayerAction;
import com.hertzs.abstractmod.networking.ActionStatePayload;
import com.hertzs.abstractmod.networking.ActionPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.UUID;

public class HertzsAbstractModClient implements ClientModInitializer {
	public static KeyMapping openWheel;

	@Override
	public void onInitializeClient() {
		// 注册按键分类 + G 键
		KeyMapping.Category category = KeyMapping.Category.register(
				Identifier.fromNamespaceAndPath(HertzsAbstractMod.MOD_ID, "main"));
		openWheel = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.hertzsabstract.wheel",
				GLFW.GLFW_KEY_G,
				category));

		// 按 G 打开动作轮盘
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openWheel.consumeClick()) {
				if (client.player != null && client.screen == null) {
					client.setScreen(new ActionWheelScreen());
				}
			}
		});

		// 服务端广播的动作状态
		ClientPlayNetworking.registerGlobalReceiver(ActionStatePayload.TYPE, (payload, context) ->
				context.client().execute(() ->
						ClientActionManager.set(UUID.fromString(payload.playerId()),
								PlayerAction.byId((byte) payload.actionId()))));

		// 断线清空缓存
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientActionManager.clear());

		HertzsAbstractMod.LOGGER.info("[Hertzs Abstract] 客户端动作轮盘已注册（G 键）");
	}

	/** 轮盘选择后发送到服务端 */
	public static void sendAction(PlayerAction action) {
		ClientPlayNetworking.send(new ActionPayload(action.getId()));
	}
}
