package me.earth.phobos.mixin.mixins;

import me.earth.phobos.features.modules.misc.Bypass;
import net.minecraft.network.NettyCompressionDecoder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin({NettyCompressionDecoder.class})
public abstract class MixinNettyCompressionDecoder {
   @ModifyConstant(
      method = {"decode"},
      constant = {@Constant(
   intValue = 2097152
)}
   )
   private int decodeHook(int n) {
      return Bypass.getInstance().isOn() && (Boolean)Bypass.getInstance().packets.getValue() && (Boolean)Bypass.getInstance().noLimit.getValue() ? Integer.MAX_VALUE : n;
   }
}
