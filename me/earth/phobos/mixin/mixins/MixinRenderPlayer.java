package me.earth.phobos.mixin.mixins;

import java.awt.Color;
import me.earth.phobos.features.modules.client.Colors;
import me.earth.phobos.features.modules.render.HandColor;
import me.earth.phobos.features.modules.render.Nametags;
import me.earth.phobos.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderPlayer;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({RenderPlayer.class})
public class MixinRenderPlayer {
   @Inject(
      method = {"renderEntityName"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void renderEntityNameHook(AbstractClientPlayer entityIn, double x, double y, double z, String name, double distanceSq, CallbackInfo info) {
      if (Nametags.getInstance().isOn()) {
         info.cancel();
      }

   }

   @Inject(
      method = {"renderRightArm"},
      at = {@At(
   value = "FIELD",
   target = "Lnet/minecraft/client/model/ModelPlayer;swingProgress:F",
   opcode = 181
)},
      cancellable = true
   )
   public void renderRightArmBegin(AbstractClientPlayer clientPlayer, CallbackInfo ci) {
      if (clientPlayer == Minecraft.func_71410_x().field_71439_g && HandColor.INSTANCE.isEnabled()) {
         GL11.glPushAttrib(1048575);
         GL11.glDisable(3008);
         GL11.glDisable(3553);
         GL11.glDisable(2896);
         GL11.glEnable(3042);
         GL11.glBlendFunc(770, 771);
         GL11.glLineWidth(1.5F);
         GL11.glEnable(2960);
         GL11.glEnable(10754);
         OpenGlHelper.func_77475_a(OpenGlHelper.field_77476_b, 240.0F, 240.0F);
         Color rainbowColor;
         if ((Boolean)HandColor.INSTANCE.rainbow.getValue()) {
            rainbowColor = (Boolean)HandColor.INSTANCE.colorSync.getValue() ? Colors.INSTANCE.getCurrentColor() : new Color(RenderUtil.getRainbow((Integer)HandColor.INSTANCE.speed.getValue() * 100, 0, (float)(Integer)HandColor.INSTANCE.saturation.getValue() / 100.0F, (float)(Integer)HandColor.INSTANCE.brightness.getValue() / 100.0F));
            GL11.glColor4f((float)rainbowColor.getRed() / 255.0F, (float)rainbowColor.getGreen() / 255.0F, (float)rainbowColor.getBlue() / 255.0F, (float)(Integer)HandColor.INSTANCE.alpha.getValue() / 255.0F);
         } else {
            rainbowColor = (Boolean)HandColor.INSTANCE.colorSync.getValue() ? new Color(Colors.INSTANCE.getCurrentColor().getRed(), Colors.INSTANCE.getCurrentColor().getBlue(), Colors.INSTANCE.getCurrentColor().getGreen(), (Integer)HandColor.INSTANCE.alpha.getValue()) : new Color((Integer)HandColor.INSTANCE.red.getValue(), (Integer)HandColor.INSTANCE.green.getValue(), (Integer)HandColor.INSTANCE.blue.getValue(), (Integer)HandColor.INSTANCE.alpha.getValue());
            GL11.glColor4f((float)rainbowColor.getRed() / 255.0F, (float)rainbowColor.getGreen() / 255.0F, (float)rainbowColor.getBlue() / 255.0F, (float)rainbowColor.getAlpha() / 255.0F);
         }
      }

   }

   @Inject(
      method = {"renderRightArm"},
      at = {@At("RETURN")},
      cancellable = true
   )
   public void renderRightArmReturn(AbstractClientPlayer clientPlayer, CallbackInfo ci) {
      if (clientPlayer == Minecraft.func_71410_x().field_71439_g && HandColor.INSTANCE.isEnabled()) {
         GL11.glEnable(3042);
         GL11.glEnable(2896);
         GL11.glEnable(3553);
         GL11.glEnable(3008);
         GL11.glPopAttrib();
      }

   }

   @Inject(
      method = {"renderLeftArm"},
      at = {@At(
   value = "FIELD",
   target = "Lnet/minecraft/client/model/ModelPlayer;swingProgress:F",
   opcode = 181
)},
      cancellable = true
   )
   public void renderLeftArmBegin(AbstractClientPlayer clientPlayer, CallbackInfo ci) {
      if (clientPlayer == Minecraft.func_71410_x().field_71439_g && HandColor.INSTANCE.isEnabled()) {
         GL11.glPushAttrib(1048575);
         GL11.glDisable(3008);
         GL11.glDisable(3553);
         GL11.glDisable(2896);
         GL11.glEnable(3042);
         GL11.glBlendFunc(770, 771);
         GL11.glLineWidth(1.5F);
         GL11.glEnable(2960);
         GL11.glEnable(10754);
         OpenGlHelper.func_77475_a(OpenGlHelper.field_77476_b, 240.0F, 240.0F);
         Color rainbowColor;
         if ((Boolean)HandColor.INSTANCE.rainbow.getValue()) {
            rainbowColor = (Boolean)HandColor.INSTANCE.colorSync.getValue() ? Colors.INSTANCE.getCurrentColor() : new Color(RenderUtil.getRainbow((Integer)HandColor.INSTANCE.speed.getValue() * 100, 0, (float)(Integer)HandColor.INSTANCE.saturation.getValue() / 100.0F, (float)(Integer)HandColor.INSTANCE.brightness.getValue() / 100.0F));
            GL11.glColor4f((float)rainbowColor.getRed() / 255.0F, (float)rainbowColor.getGreen() / 255.0F, (float)rainbowColor.getBlue() / 255.0F, (float)(Integer)HandColor.INSTANCE.alpha.getValue() / 255.0F);
         } else {
            rainbowColor = (Boolean)HandColor.INSTANCE.colorSync.getValue() ? Colors.INSTANCE.getCurrentColor() : new Color(RenderUtil.getRainbow((Integer)HandColor.INSTANCE.speed.getValue() * 100, 0, (float)(Integer)HandColor.INSTANCE.saturation.getValue() / 100.0F, (float)(Integer)HandColor.INSTANCE.brightness.getValue() / 100.0F));
            GL11.glColor4f((float)rainbowColor.getRed() / 255.0F, (float)rainbowColor.getGreen() / 255.0F, (float)rainbowColor.getBlue() / 255.0F, (float)(Integer)HandColor.INSTANCE.alpha.getValue() / 255.0F);
         }
      }

   }

   @Inject(
      method = {"renderLeftArm"},
      at = {@At("RETURN")},
      cancellable = true
   )
   public void renderLeftArmReturn(AbstractClientPlayer clientPlayer, CallbackInfo ci) {
      if (clientPlayer == Minecraft.func_71410_x().field_71439_g && HandColor.INSTANCE.isEnabled()) {
         GL11.glEnable(3042);
         GL11.glEnable(2896);
         GL11.glEnable(3553);
         GL11.glEnable(3008);
         GL11.glPopAttrib();
      }

   }
}
