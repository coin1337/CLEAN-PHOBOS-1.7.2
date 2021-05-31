package me.earth.phobos.mixin.mixins;

import java.util.List;
import javax.annotation.Nullable;
import me.earth.phobos.features.modules.movement.Flight;
import me.earth.phobos.features.modules.movement.Phase;
import me.earth.phobos.features.modules.player.Freecam;
import me.earth.phobos.features.modules.player.Jesus;
import me.earth.phobos.features.modules.render.XRay;
import me.earth.phobos.util.EntityUtil;
import me.earth.phobos.util.Util;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Block.class})
public class MixinBlock {
   @Inject(
      method = {"addCollisionBoxToList(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/List;Lnet/minecraft/entity/Entity;Z)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void addCollisionBoxToListHook(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState, CallbackInfo info) {
      if (entityIn != null && Util.mc.field_71439_g != null && (entityIn.equals(Util.mc.field_71439_g) || Util.mc.field_71439_g.func_184187_bx() != null && entityIn.equals(Util.mc.field_71439_g.func_184187_bx())) && (Flight.getInstance().isOn() && (Flight.getInstance().mode.getValue() == Flight.Mode.PACKET && (Boolean)Flight.getInstance().better.getValue() && (Boolean)Flight.getInstance().phase.getValue() || Flight.getInstance().mode.getValue() == Flight.Mode.DAMAGE && (Boolean)Flight.getInstance().noClip.getValue()) || Phase.getInstance().isOn() && Phase.getInstance().mode.getValue() == Phase.Mode.PACKETFLY && Phase.getInstance().type.getValue() == Phase.PacketFlyMode.SETBACK && (Boolean)Phase.getInstance().boundingBox.getValue())) {
         info.cancel();
      }

      try {
         if (Freecam.getInstance().isOff() && Jesus.getInstance().isOn() && Jesus.getInstance().mode.getValue() == Jesus.Mode.TRAMPOLINE && Util.mc.field_71439_g != null && state != null && state.func_177230_c() instanceof BlockLiquid && !(entityIn instanceof EntityBoat) && !Util.mc.field_71439_g.func_70093_af() && Util.mc.field_71439_g.field_70143_R < 3.0F && !EntityUtil.isAboveLiquid(Util.mc.field_71439_g) && EntityUtil.checkForLiquid(Util.mc.field_71439_g, false) || EntityUtil.checkForLiquid(Util.mc.field_71439_g, false) && Util.mc.field_71439_g.func_184187_bx() != null && Util.mc.field_71439_g.func_184187_bx().field_70143_R < 3.0F && EntityUtil.isAboveBlock(Util.mc.field_71439_g, pos)) {
            AxisAlignedBB offset = Jesus.offset.func_186670_a(pos);
            if (entityBox.func_72326_a(offset)) {
               collidingBoxes.add(offset);
            }

            info.cancel();
         }
      } catch (Exception var10) {
      }

   }

   @Inject(
      method = {"isFullCube"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void isFullCubeHook(IBlockState blockState, CallbackInfoReturnable<Boolean> info) {
      try {
         if (XRay.getInstance().isOn()) {
            info.setReturnValue(XRay.getInstance().shouldRender((Block)Block.class.cast(this)));
            info.cancel();
         }
      } catch (Exception var4) {
      }

   }
}
