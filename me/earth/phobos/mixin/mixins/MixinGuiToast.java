package me.earth.phobos.mixin.mixins;

import me.earth.phobos.features.modules.render.NoRender;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.toasts.GuiToast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({GuiToast.class})
public class MixinGuiToast {
   @Inject(
      method = {"drawToast"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void drawToastHook(ScaledResolution resolution, CallbackInfo info) {
      if (NoRender.getInstance().isOn() && (Boolean)NoRender.getInstance().advancements.getValue()) {
         info.cancel();
      }

   }
}
