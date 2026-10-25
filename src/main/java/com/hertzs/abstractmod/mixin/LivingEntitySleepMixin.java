package com.hertzs.abstractmod.mixin;

import com.hertzs.abstractmod.ActionStateManager;
import com.hertzs.abstractmod.PlayerAction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 躺地只是摆姿势，不能让游戏认为玩家真的在睡觉
 * （否则会误触发睡眠计时、夜晚跳过等逻辑）。
 */
@Mixin(LivingEntity.class)
public class LivingEntitySleepMixin {
	@Inject(method = "isSleeping", at = @At("HEAD"), cancellable = true)
	private void hertzsabstract$notReallySleeping(CallbackInfoReturnable<Boolean> cir) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (self instanceof Player && self.isAlive()) {
			PlayerAction action = ActionStateManager.get(self.level().isClientSide(), self.getUUID());
			if (action == PlayerAction.LIE) {
				cir.setReturnValue(false);
			}
		}
	}
}
