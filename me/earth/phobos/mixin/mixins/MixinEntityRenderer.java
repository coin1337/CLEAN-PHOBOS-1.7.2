package me.earth.phobos.mixin.mixins;

import com.google.common.base.Predicate;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import me.earth.phobos.features.modules.client.Notifications;
import me.earth.phobos.features.modules.player.Speedmine;
import me.earth.phobos.features.modules.render.CameraClip;
import me.earth.phobos.features.modules.render.NoRender;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({EntityRenderer.class})
public abstract class MixinEntityRenderer {
   private boolean injection = true;
   @Shadow
   public ItemStack field_190566_ab;
   @Shadow
   @Final
   public Minecraft field_78531_r;

   @Shadow
   public abstract void func_78473_a(float var1);

   @Inject(
      method = {"renderItemActivation"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void renderItemActivationHook(CallbackInfo info) {
      if (this.field_190566_ab != null && NoRender.getInstance().isOn() && (Boolean)NoRender.getInstance().totemPops.getValue() && this.field_190566_ab.func_77973_b() == Items.field_190929_cY) {
         info.cancel();
      }

   }

   @Inject(
      method = {"updateLightmap"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void updateLightmap(float partialTicks, CallbackInfo info) {
      if (NoRender.getInstance().isOn() && (NoRender.getInstance().skylight.getValue() == NoRender.Skylight.ENTITY || NoRender.getInstance().skylight.getValue() == NoRender.Skylight.ALL)) {
         info.cancel();
      }

   }

   @Inject(
      method = {"getMouseOver(F)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void getMouseOverHook(float partialTicks, CallbackInfo info) {
      if (this.injection) {
         info.cancel();
         this.injection = false;

         try {
            this.func_78473_a(partialTicks);
         } catch (Exception var4) {
            var4.printStackTrace();
            if (Notifications.getInstance().isOn() && (Boolean)Notifications.getInstance().crash.getValue()) {
               Notifications.displayCrash(var4);
            }
         }

         this.injection = true;
      }

   }

   @Redirect(
      method = {"setupCameraTransform"},
      at = @At(
   value = "FIELD",
   target = "Lnet/minecraft/client/entity/EntityPlayerSP;prevTimeInPortal:F"
)
   )
   public float prevTimeInPortalHook(EntityPlayerSP entityPlayerSP) {
      return NoRender.getInstance().isOn() && (Boolean)NoRender.getInstance().nausea.getValue() ? -3.4028235E38F : entityPlayerSP.field_71080_cy;
   }

   @Inject(
      method = {"setupFog"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void setupFogHook(int startCoords, float partialTicks, CallbackInfo info) {
      if (NoRender.getInstance().isOn() && NoRender.getInstance().fog.getValue() == NoRender.Fog.NOFOG) {
         info.cancel();
      }

   }

   @Redirect(
      method = {"setupFog"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/renderer/ActiveRenderInfo;getBlockStateAtEntityViewpoint(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;F)Lnet/minecraft/block/state/IBlockState;"
)
   )
   public IBlockState getBlockStateAtEntityViewpointHook(World worldIn, Entity entityIn, float p_186703_2_) {
      return NoRender.getInstance().isOn() && NoRender.getInstance().fog.getValue() == NoRender.Fog.AIR ? Blocks.field_150350_a.field_176228_M : ActiveRenderInfo.func_186703_a(worldIn, entityIn, p_186703_2_);
   }

   @Inject(
      method = {"hurtCameraEffect"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void hurtCameraEffectHook(float ticks, CallbackInfo info) {
      if (NoRender.getInstance().isOn() && (Boolean)NoRender.getInstance().hurtcam.getValue()) {
         info.cancel();
      }

   }

   @Redirect(
      method = {"getMouseOver"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/multiplayer/WorldClient;getEntitiesInAABBexcluding(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;Lcom/google/common/base/Predicate;)Ljava/util/List;"
)
   )
   public List<Entity> getEntitiesInAABBexcludingHook(WorldClient worldClient, @Nullable Entity entityIn, AxisAlignedBB boundingBox, @Nullable Predicate<? super Entity> predicate) {
      return (List)(!Speedmine.getInstance().isOn() || !(Boolean)Speedmine.getInstance().noTrace.getValue() || (Boolean)Speedmine.getInstance().pickaxe.getValue() && !(this.field_78531_r.field_71439_g.func_184614_ca().func_77973_b() instanceof ItemPickaxe) ? worldClient.func_175674_a(entityIn, boundingBox, predicate) : new ArrayList());
   }

   @ModifyVariable(
      method = {"orientCamera"},
      ordinal = 3,
      at = @At(
   value = "STORE",
   ordinal = 0
),
      require = 1
   )
   public double changeCameraDistanceHook(double range) {
      return CameraClip.getInstance().isEnabled() && (Boolean)CameraClip.getInstance().extend.getValue() ? (Double)CameraClip.getInstance().distance.getValue() : range;
   }

   @ModifyVariable(
      method = {"orientCamera"},
      ordinal = 7,
      at = @At(
   value = "STORE",
   ordinal = 0
),
      require = 1
   )
   public double orientCameraHook(double range) {
      return CameraClip.getInstance().isEnabled() && (Boolean)CameraClip.getInstance().extend.getValue() ? (Double)CameraClip.getInstance().distance.getValue() : (CameraClip.getInstance().isEnabled() && !(Boolean)CameraClip.getInstance().extend.getValue() ? 4.0D : range);
   }
}
