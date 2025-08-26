package net.skymoe.enchaddons.impl.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.skymoe.enchaddons.feature.dungeon.partyfinder.PartyFinderStats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(GuiScreen.class)
public abstract class GuiScreenMixin {
    @Shadow protected abstract void drawHoveringText(List<String> textLines, int x, int y);

    @Inject(method = "renderToolTip", at = @At("HEAD"), cancellable = true)
    public void renderTooltipPre(ItemStack stack, int x, int y, CallbackInfo ci) {
        Minecraft mc = Minecraft.getMinecraft();
        List<String> original = stack.getTooltip(mc.thePlayer, mc.gameSettings.advancedItemTooltips);

        ArrayList<String> colored = new ArrayList<>(original.size());
        for (int i = 0; i < original.size(); ++i) {
            String s = original.get(i);
            if (i == 0) {
                s = stack.getRarity().rarityColor + s;
            } else {
                s = EnumChatFormatting.GRAY + s;
            }
            colored.add(s);
        }

        try {
            PartyFinderStats.INSTANCE.onTooltip(colored);
        } catch (Throwable ignored) {}

        this.drawHoveringText(colored, x, y);
        ci.cancel();
    }
}

