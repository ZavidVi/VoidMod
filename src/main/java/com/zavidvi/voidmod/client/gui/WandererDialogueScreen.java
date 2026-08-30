package com.zavidvi.voidmod.client.gui;

import com.zavidvi.voidmod.entity.wanderer.WandererEntity;
import com.zavidvi.voidmod.network.WandererDialogueActionPayload;
import com.zavidvi.voidmod.registry.ModEntities;
import com.zavidvi.voidmod.world.progression.ClientProgressionData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class WandererDialogueScreen extends Screen {
    private static final int PANEL_WIDTH = 280;
    private static final int PANEL_HEIGHT = 130;
    private static final int PADDING = 10;
    private static final int PORTRAIT_WIDTH = 80;

    private static final int BUTTON_MIN_WIDTH = 160;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_PADDING = 10;

    private static final Component[] INTRO_LINES = {
            Component.translatable("voidmod.wanderer.intro.1"),
            Component.translatable("voidmod.wanderer.intro.2"),
            Component.translatable("voidmod.wanderer.intro.3",
                    Component.translatable("voidmod.wanderer.distorter").withStyle(ChatFormatting.ITALIC))
    };

    private static final Component[] INTRO_ANSWERS = {
            Component.translatable("voidmod.wanderer.answer.1"),
            Component.translatable("voidmod.wanderer.answer.2"),
            Component.translatable("voidmod.wanderer.answer.3")
    };

    private static final int GIVE_STEP = INTRO_LINES.length - 2;

    private static final int NO_INTRO = -1;

    private static final Component REPEAT_LINE = Component.translatable("voidmod.wanderer.repeat");
    private static final Component REPEAT_ANSWER = Component.translatable("voidmod.wanderer.repeat.answer");

    private WandererEntity dummyWanderer;
    private Component dialogueText;
    private Button actionButton;

    private final int introStepAtOpen;
    private int introStep;

    private int panelX;
    private int panelY;

    public WandererDialogueScreen() {
        super(Component.literal("Wanderer Dialogue"));
        this.introStepAtOpen = ClientProgressionData.wandererTalked ? NO_INTRO : 0;
        this.introStep = this.introStepAtOpen;
    }

    @Override
    protected void init() {
        super.init();

        this.panelX = this.width / 2 - PANEL_WIDTH / 2;
        this.panelY = this.height / 2 - PANEL_HEIGHT / 2;

        if (this.minecraft != null && this.minecraft.level != null && this.dummyWanderer == null) {
            this.dummyWanderer = new WandererEntity(ModEntities.WANDERER.get(), this.minecraft.level);
        }

        if (this.dialogueText == null) {
            this.dialogueText = this.introStep == NO_INTRO ? REPEAT_LINE : INTRO_LINES[this.introStep];
        }

        this.actionButton = Button.builder(Component.empty(), button -> onAnswer())
                .bounds(this.width / 2 - BUTTON_MIN_WIDTH / 2, this.panelY + PANEL_HEIGHT + 8,
                        BUTTON_MIN_WIDTH, BUTTON_HEIGHT)
                .build();
        this.addRenderableWidget(this.actionButton);

        setAnswer(this.introStep == NO_INTRO ? REPEAT_ANSWER : INTRO_ANSWERS[this.introStep]);
    }

    private void setAnswer(Component answer) {
        int width = Mth.clamp(this.font.width(answer) + BUTTON_PADDING * 2,
                BUTTON_MIN_WIDTH, Math.max(BUTTON_MIN_WIDTH, this.width - BUTTON_PADDING * 2));

        this.actionButton.setMessage(answer);
        this.actionButton.setWidth(width);
        this.actionButton.setX(this.width / 2 - width / 2);
    }

    private void onAnswer() {
        if (this.introStep == NO_INTRO) {
            ClientPacketDistributor.sendToServer(new WandererDialogueActionPayload());
            return;
        }

        if (this.introStep == GIVE_STEP) {
            ClientPacketDistributor.sendToServer(new WandererDialogueActionPayload());
        }

        if (this.introStep + 1 >= INTRO_LINES.length) {
            onClose();
            return;
        }

        this.introStep++;
        this.dialogueText = INTRO_LINES[this.introStep];
        setAnswer(INTRO_ANSWERS[this.introStep]);
    }

    public void updateResponse(String key, int arg) {
        ClientProgressionData.wandererTalked = true;

        if (this.introStepAtOpen != NO_INTRO) return;

        this.dialogueText = Component.translatable(key, arg);
        if (this.actionButton != null) {
            setAnswer(REPEAT_ANSWER);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.extractTransparentBackground(guiGraphics);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.extractBackground(guiGraphics, mouseX, mouseY, partialTick);

        int left = this.panelX;
        int top = this.panelY;
        int right = left + PANEL_WIDTH;
        int bottom = top + PANEL_HEIGHT;

        guiGraphics.fill(left, top, right, bottom, 0xE0100010);
        guiGraphics.outline(left, top, PANEL_WIDTH, PANEL_HEIGHT, 0xFFAA88CC);

        if (this.dummyWanderer != null) {
            int px1 = left + PADDING;
            int py1 = top + PADDING;
            int px2 = px1 + PORTRAIT_WIDTH;
            int py2 = bottom - PADDING;

            guiGraphics.fill(px1, py1, px2, py2, 0x40000000);
            guiGraphics.enableScissor(px1, py1, px2, py2);
            renderWandererFollowingMouse(guiGraphics, px1, py1, px2, py2, mouseX, mouseY);
            guiGraphics.disableScissor();
        }

        int textX = left + PADDING + PORTRAIT_WIDTH + PADDING;
        int textY = top + PADDING + 4;
        int maxTextWidth = right - PADDING - textX;

        var font = this.font;
        var lines = font.split(this.dialogueText, maxTextWidth);
        for (int i = 0; i < lines.size(); i++) {
            guiGraphics.text(font, lines.get(i), textX, textY + i * (font.lineHeight + 1), 0xFFE8E8E8, true);
        }

        for (var renderable : this.renderables) {
            renderable.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    private void renderWandererFollowingMouse(GuiGraphicsExtractor guiGraphics, int x1, int y1, int x2, int y2, int mouseX, int mouseY) {
        int scale = (int) ((y2 - y1) / 2.6F);

        InventoryScreen.extractEntityInInventoryFollowsMouse(
                guiGraphics,
                x1, y1, x2, y2,
                scale,
                0.0625F,
                mouseX,
                mouseY,
                this.dummyWanderer
        );
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
