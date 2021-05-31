package me.earth.phobos.mixin.mixins;

import me.earth.phobos.features.modules.render.NoRender;
import me.earth.phobos.features.modules.render.SmallShield;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ItemRenderer.class})
public abstract class MixinItemRenderer {
   private boolean injection = true;

   @Shadow
   public abstract void func_187457_a(AbstractClientPlayer var1, float var2, float var3, EnumHand var4, float var5, ItemStack var6, float var7);

   @Inject(
      method = {"renderItemInFirstPerson(Lnet/minecraft/client/entity/AbstractClientPlayer;FFLnet/minecraft/util/EnumHand;FLnet/minecraft/item/ItemStack;F)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void renderItemInFirstPersonHook(AbstractClientPlayer player, float p_187457_2_, float p_187457_3_, EnumHand hand, float p_187457_5_, ItemStack stack, float p_187457_7_, CallbackInfo info) {
      if (this.injection) {
         info.cancel();
         SmallShield offset = SmallShield.getINSTANCE();
         float xOffset = 0.0F;
         float yOffset = 0.0F;
         this.injection = false;
         if (hand == EnumHand.MAIN_HAND) {
            if (offset.isOn() && player.func_184614_ca() != ItemStack.field_190927_a) {
               xOffset = (Float)offset.mainX.getValue();
               yOffset = (Float)offset.mainY.getValue();
            }
         } else if (!(Boolean)offset.normalOffset.getValue() && offset.isOn() && player.func_184592_cb() != ItemStack.field_190927_a) {
            xOffset = (Float)offset.offX.getValue();
            yOffset = (Float)offset.offY.getValue();
         }

         this.func_187457_a(player, p_187457_2_, p_187457_3_, hand, p_187457_5_ + xOffset, stack, p_187457_7_ + yOffset);
         this.injection = true;
      }

   }

   @Redirect(
      method = {"renderArmFirstPerson"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/renderer/GlStateManager;translate(FFF)V",
   ordinal = 0
)
   )
   public void translateHook(float x, float y, float z) {
      SmallShield offset = SmallShield.getINSTANCE();
      boolean shiftPos = Minecraft.func_71410_x().field_71439_g != null && Minecraft.func_71410_x().field_71439_g.func_184614_ca() != ItemStack.field_190927_a && offset.isOn();
      GlStateManager.func_179109_b(x + (shiftPos ? (Float)offset.mainX.getValue() : 0.0F), y + (shiftPos ? (Float)offset.mainY.getValue() : 0.0F), z);
   }

   @Inject(
      method = {"renderFireInFirstPerson"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void renderFireInFirstPersonHook(CallbackInfo info) {
      if (NoRender.getInstance().isOn() && (Boolean)NoRender.getInstance().fire.getValue()) {
         info.cancel();
      }

   }

   @Inject(
      method = {"renderSuffocationOverlay"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void renderSuffocationOverlay(CallbackInfo ci) {
      if (NoRender.getInstance().isOn() && (Boolean)NoRender.getInstance().blocks.getValue()) {
         ci.cancel();
      }

   }
}
