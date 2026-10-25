package com.hertzs.abstractmod.mixin;

import com.hertzs.abstractmod.ActionStateManager;
import com.hertzs.abstractmod.PlayerAction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 强制玩家姿势：躺下时返回 SLEEPING（服务端逻辑与客户端渲染均生效）。
 */
@Mixin(Entity.class)
public class EntityPoseMixin {
	@Inject(method = "getPose", at = @At("HEAD"), cancellable = true)
	private void hertzsabstract$forcedPose(CallbackInfoReturnable<Pose> cir) {
		Entity self = (Entity) (Object) this;
		if (self instanceof Player && self.isAlive()) {
			PlayerAction action = ActionStateManager.get(self.level().isClientSide(), self.getUUID());
			Pose forced = action.getForcedPose();
			if (forced != null) {
				cir.setReturnValue(forced);
			}
		}
	}
}
