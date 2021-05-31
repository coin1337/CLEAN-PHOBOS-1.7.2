package me.earth.phobos.mixin.mixins;

import me.earth.phobos.features.modules.client.ServerModule;
import me.earth.phobos.mixin.mixins.accessors.IServerAddress;
import net.minecraft.client.multiplayer.ServerAddress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({ServerAddress.class})
public abstract class MixinServerAddress {
   @Redirect(
      method = {"fromString"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/multiplayer/ServerAddress;getServerAddress(Ljava/lang/String;)[Ljava/lang/String;"
)
   )
   private static String[] getServerAddressHook(String ip) {
      if (ip.equals(ServerModule.getInstance().ip.getValue())) {
         ServerModule module = ServerModule.getInstance();
         int port = module.getPort();
         if (port != -1) {
            return new String[]{(String)ServerModule.getInstance().ip.getValue(), Integer.toString(port)};
         }
      }

      return IServerAddress.getServerAddress(ip);
   }
}
