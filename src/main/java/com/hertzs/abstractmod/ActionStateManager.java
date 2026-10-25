package com.hertzs.abstractmod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 动作状态存储。服务端与客户端各持一份：
 * - 服务端：网络包写入，tick 循环维护；
 * - 客户端：S2C 广播写入，供 mixin 读取并渲染姿势。
 */
public final class ActionStateManager {
	private static final Map<UUID, PlayerAction> SERVER = new HashMap<>();
	private static final Map<UUID, PlayerAction> CLIENT = new HashMap<>();

	private ActionStateManager() {
	}

	public static Map<UUID, PlayerAction> map(boolean clientSide) {
		return clientSide ? CLIENT : SERVER;
	}

	public static PlayerAction get(boolean clientSide, UUID playerId) {
		return map(clientSide).getOrDefault(playerId, PlayerAction.NONE);
	}

	public static void set(boolean clientSide, UUID playerId, PlayerAction action) {
		if (action == PlayerAction.NONE) {
			map(clientSide).remove(playerId);
		} else {
			map(clientSide).put(playerId, action);
		}
	}

	public static void clearClient() {
		CLIENT.clear();
	}
}
