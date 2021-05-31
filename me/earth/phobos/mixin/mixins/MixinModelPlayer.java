package me.earth.phobos.mixin.mixins;

import net.minecraft.client.model.ModelPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({ModelPlayer.class})
public class MixinModelPlayer {
   @Redirect(
      method = {"renderCape"},
      at = @At("HEAD")
   )
   public void renderCape(float scale) {
   }
}
