package com.hertzs.abstractmod.client;

import com.hertzs.abstractmod.ActionStateManager;
import com.hertzs.abstractmod.PlayerAction;

import java.util.UUID;

/**
 * 客户端动作状态缓存（由 S2C 广包维护），供姿势 mixin 读取。
 */
public final class ClientActionManager {
	private ClientActionManager() {
	}

	public static void set(UUID playerId, PlayerAction action) {
		ActionStateManager.set(true, playerId, action);
	}

	public static void clear() {
		ActionStateManager.clearClient();
	}
}
