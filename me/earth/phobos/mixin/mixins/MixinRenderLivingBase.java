package me.earth.phobos.mixin.mixins;

import java.awt.Color;
import me.earth.phobos.event.events.RenderEntityModelEvent;
import me.earth.phobos.features.modules.client.Colors;
import me.earth.phobos.features.modules.render.Chams;
import me.earth.phobos.features.modules.render.ESP;
import me.earth.phobos.features.modules.render.Skeleton;
import me.earth.phobos.util.EntityUtil;
import me.earth.phobos.util.RenderUtil;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({RenderLivingBase.class})
public abstract class MixinRenderLivingBase<T extends EntityLivingBase> extends Render<T> {
   public MixinRenderLivingBase(RenderManager renderManagerIn, ModelBase modelBaseIn, float shadowSizeIn) {
      super(renderManagerIn);
   }

   @Redirect(
      method = {"renderModel"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/model/ModelBase;render(Lnet/minecraft/entity/Entity;FFFFFF)V"
)
   )
   private void renderModelHook(ModelBase modelBase, Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
      boolean cancel = false;
      if (Skeleton.getInstance().isEnabled() || ESP.getInstance().isEnabled()) {
         RenderEntityModelEvent event = new RenderEntityModelEvent(0, modelBase, entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
         if (Skeleton.getInstance().isEnabled()) {
            Skeleton.getInstance().onRenderModel(event);
         }

         if (ESP.getInstance().isEnabled()) {
            ESP.getInstance().onRenderModel(event);
            if (event.isCanceled()) {
               cancel = true;
            }
         }
      }

      if (Chams.getInstance().isEnabled() && entityIn instanceof EntityPlayer && (Boolean)Chams.getInstance().colored.getValue()) {
         GL11.glPushAttrib(1048575);
         GL11.glDisable(3008);
         GL11.glDisable(3553);
         GL11.glDisable(2896);
         GL11.glEnable(3042);
         GL11.glBlendFunc(770, 771);
         GL11.glLineWidth(1.5F);
         GL11.glEnable(2960);
         Color visibleColor;
         Color hiddenColor;
         if ((Boolean)Chams.getInstance().rainbow.getValue()) {
            hiddenColor = (Boolean)Chams.getInstance().colorSync.getValue() ? Colors.INSTANCE.getCurrentColor() : new Color(RenderUtil.getRainbow((Integer)Chams.getInstance().speed.getValue() * 100, 0, (float)(Integer)Chams.getInstance().saturation.getValue() / 100.0F, (float)(Integer)Chams.getInstance().brightness.getValue() / 100.0F));
            visibleColor = EntityUtil.getColor(entityIn, hiddenColor.getRed(), hiddenColor.getGreen(), hiddenColor.getBlue(), (Integer)Chams.getInstance().alpha.getValue(), true);
            GL11.glDisable(2929);
            GL11.glDepthMask(false);
            GL11.glEnable(10754);
            GL11.glColor4f((float)visibleColor.getRed() / 255.0F, (float)visibleColor.getGreen() / 255.0F, (float)visibleColor.getBlue() / 255.0F, (float)(Integer)Chams.getInstance().alpha.getValue() / 255.0F);
            modelBase.func_78088_a(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            GL11.glEnable(2929);
            GL11.glDepthMask(true);
         } else if ((Boolean)Chams.getInstance().xqz.getValue()) {
            hiddenColor = (Boolean)Chams.getInstance().colorSync.getValue() ? EntityUtil.getColor(entityIn, (Integer)Chams.getInstance().red.getValue(), (Integer)Chams.getInstance().green.getValue(), (Integer)Chams.getInstance().blue.getValue(), (Integer)Chams.getInstance().alpha.getValue(), true) : EntityUtil.getColor(entityIn, (Integer)Chams.getInstance().red.getValue(), (Integer)Chams.getInstance().green.getValue(), (Integer)Chams.getInstance().blue.getValue(), (Integer)Chams.getInstance().alpha.getValue(), true);
            visibleColor = (Boolean)Chams.getInstance().colorSync.getValue() ? EntityUtil.getColor(entityIn, (Integer)Chams.getInstance().red.getValue(), (Integer)Chams.getInstance().green.getValue(), (Integer)Chams.getInstance().blue.getValue(), (Integer)Chams.getInstance().alpha.getValue(), true) : EntityUtil.getColor(entityIn, (Integer)Chams.getInstance().red.getValue(), (Integer)Chams.getInstance().green.getValue(), (Integer)Chams.getInstance().blue.getValue(), (Integer)Chams.getInstance().alpha.getValue(), true);
            GL11.glDisable(2929);
            GL11.glDepthMask(false);
            GL11.glEnable(10754);
            GL11.glColor4f((float)hiddenColor.getRed() / 255.0F, (float)hiddenColor.getGreen() / 255.0F, (float)hiddenColor.getBlue() / 255.0F, (float)(Integer)Chams.getInstance().alpha.getValue() / 255.0F);
            modelBase.func_78088_a(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            GL11.glEnable(2929);
            GL11.glDepthMask(true);
            GL11.glColor4f((float)visibleColor.getRed() / 255.0F, (float)visibleColor.getGreen() / 255.0F, (float)visibleColor.getBlue() / 255.0F, (float)(Integer)Chams.getInstance().alpha.getValue() / 255.0F);
            modelBase.func_78088_a(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
         } else {
            hiddenColor = (Boolean)Chams.getInstance().colorSync.getValue() ? Colors.INSTANCE.getCurrentColor() : EntityUtil.getColor(entityIn, (Integer)Chams.getInstance().red.getValue(), (Integer)Chams.getInstance().green.getValue(), (Integer)Chams.getInstance().blue.getValue(), (Integer)Chams.getInstance().alpha.getValue(), true);
            GL11.glDisable(2929);
            GL11.glDepthMask(false);
            GL11.glEnable(10754);
            GL11.glColor4f((float)hiddenColor.getRed() / 255.0F, (float)hiddenColor.getGreen() / 255.0F, (float)hiddenColor.getBlue() / 255.0F, (float)(Integer)Chams.getInstance().alpha.getValue() / 255.0F);
            modelBase.func_78088_a(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            GL11.glEnable(2929);
            GL11.glDepthMask(true);
         }

         GL11.glEnable(3042);
         GL11.glEnable(2896);
         GL11.glEnable(3553);
         GL11.glEnable(3008);
         GL11.glPopAttrib();
      } else if (!cancel) {
         modelBase.func_78088_a(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
      }

   }

   @Inject(
      method = {"doRender"},
      at = {@At("HEAD")}
   )
   public void doRenderPre(T entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo info) {
      if (Chams.getInstance().isEnabled() && !(Boolean)Chams.getInstance().colored.getValue() && entity != null) {
         GL11.glEnable(32823);
         GL11.glPolygonOffset(1.0F, -1100000.0F);
      }

   }

   @Inject(
      method = {"doRender"},
      at = {@At("RETURN")}
   )
   public void doRenderPost(T entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo info) {
      if (Chams.getInstance().isEnabled() && !(Boolean)Chams.getInstance().colored.getValue() && entity != null) {
         GL11.glPolygonOffset(1.0F, 1000000.0F);
         GL11.glDisable(32823);
      }

   }
}
