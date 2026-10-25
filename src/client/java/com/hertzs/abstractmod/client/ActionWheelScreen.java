package com.hertzs.abstractmod.client;

import com.hertzs.abstractmod.PlayerAction;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * 动作轮盘：按方向排布四个动作，鼠标悬停 + 点击（或按 1-4）选择。
 *
 * 布局：
 *        [1 吓唬]
 * [4 躺在地上]  ●  [2 挥手]
 *        [3 坐在地上]
 */
public class ActionWheelScreen extends Screen {
	private static final int CARD_W = 96;
	private static final int CARD_H = 26;
	private static final int PICK_RADIUS = 64;

	private record Option(PlayerAction action, String labelKey, int accent) {
	}

	private static final List<Option> OPTIONS = List.of(
			new Option(PlayerAction.SCARE, "hertzsabstract.action.scare", 0xFF5555),
			new Option(PlayerAction.WAVE, "hertzsabstract.action.wave", 0x55FF55),
			new Option(PlayerAction.SIT, "hertzsabstract.action.sit", 0x66CCFF),
			new Option(PlayerAction.LIE, "hertzsabstract.action.lie", 0xCC88FF)
	);

	private final List<int[]> centers = new ArrayList<>();
	private int hovered = -1;

	public ActionWheelScreen() {
		super(Component.translatable("hertzsabstract.wheel.title"));
	}

	@Override
	protected void init() {
		centers.clear();
		int cx = this.width / 2;
		int cy = this.height / 2;
		// 上、右、下、左（与 OPTIONS 顺序一致）
		centers.add(new int[]{cx, cy - 72});
		centers.add(new int[]{cx + 96, cy});
		centers.add(new int[]{cx, cy + 72});
		centers.add(new int[]{cx - 96, cy});
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		int cx = this.width / 2;
		int cy = this.height / 2;

		this.hovered = pick(mouseX, mouseY);

		// 四个动作卡片
		for (int i = 0; i < OPTIONS.size(); i++) {
			int[] center = centers.get(i);
			Option option = OPTIONS.get(i);
			boolean hover = i == this.hovered;

			int x0 = center[0] - CARD_W / 2;
			int y0 = center[1] - CARD_H / 2;

			// 底板 + 描边
			graphics.fill(x0 - 1, y0 - 1, x0 + CARD_W + 1, y0 + CARD_H + 1,
					hover ? option.accent : 0x60FFFFFF);
			graphics.fill(x0, y0, x0 + CARD_W, y0 + CARD_H,
					hover ? 0xF03A3A50 : 0xD0181824);

			String label = (i + 1) + ". " + Component.translatable(option.labelKey()).getString();
			int textColor = hover ? 0xFFFFF2A8 : 0xFFE8E8F0;
			graphics.drawCenteredString(this.font, label, center[0],
					center[1] - this.font.lineHeight / 2, textColor);
		}

		// 中央标题盘
		graphics.fill(cx - 42, cy - 14, cx + 42, cy + 14, 0xE0121220);
		graphics.fill(cx - 42, cy - 14, cx + 42, cy - 13, 0x80FFFFFF);
		graphics.fill(cx - 42, cy + 13, cx + 42, cy + 14, 0x80FFFFFF);
		graphics.drawCenteredString(this.font, this.title, cx, cy - 4, 0xFFFFD980);
	}

	@Override
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		// 自己画半透明遮罩，避免默认模糊/变暗处理影响轮盘观感
		graphics.fill(0, 0, this.width, this.height, 0x70000000);
	}

	private int pick(int mouseX, int mouseY) {
		int best = -1;
		double bestDist = PICK_RADIUS * PICK_RADIUS;
		for (int i = 0; i < centers.size(); i++) {
			int[] center = centers.get(i);
			double dx = mouseX - center[0];
			double dy = mouseY - center[1];
			double dist = dx * dx + dy * dy;
			if (dist <= bestDist) {
				bestDist = dist;
				best = i;
			}
		}
		return best;
	}

	private void select(int index) {
		if (index < 0 || index >= OPTIONS.size()) return;
		PlayerAction action = OPTIONS.get(index).action();
		this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
		HertzsAbstractModClient.sendAction(action);
		this.onClose();
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
		int index = pick((int) event.x(), (int) event.y());
		if (index >= 0) {
			select(index);
			return true;
		}
		return super.mouseClicked(event, doubled);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		int key = event.key();
		if (key >= GLFW.GLFW_KEY_1 && key <= GLFW.GLFW_KEY_4) {
			select(key - GLFW.GLFW_KEY_1);
			return true;
		}
		if (key == GLFW.GLFW_KEY_G) {
			this.onClose();
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
