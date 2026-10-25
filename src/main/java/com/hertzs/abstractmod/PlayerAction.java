package com.hertzs.abstractmod;

import net.minecraft.world.entity.Pose;

/**
 * 玩家可选动作。
 * SIT 通过骑乘隐形盔甲架实现（骑乘姿势 = 坐姿），
 * LIE 通过强制 Pose.SLEEPING 实现。
 */
public enum PlayerAction {
	NONE((byte) 0, null),
	SCARE((byte) 1, null),
	WAVE((byte) 2, null),
	SIT((byte) 3, null),
	LIE((byte) 4, Pose.SLEEPING);

	private final byte id;
	private final Pose forcedPose;

	PlayerAction(byte id, Pose forcedPose) {
		this.id = id;
		this.forcedPose = forcedPose;
	}

	public byte getId() {
		return this.id;
	}

	/** 需要强制覆盖的实体姿势（null 表示不强制） */
	public Pose getForcedPose() {
		return this.forcedPose;
	}

	public static PlayerAction byId(byte id) {
		for (PlayerAction action : values()) {
			if (action.id == id) return action;
		}
		return NONE;
	}
}
