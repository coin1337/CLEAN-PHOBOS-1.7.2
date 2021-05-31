package me.earth.phobos.mixin.mixins;

import java.net.UnknownHostException;
import me.earth.phobos.Phobos;
import me.earth.phobos.features.modules.client.ServerModule;
import me.earth.phobos.features.modules.player.NoDDoS;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.network.ServerPinger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ServerPinger.class})
public class MixinServerPinger {
   @Inject(
      method = {"ping"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void pingHook(ServerData server, CallbackInfo info) throws UnknownHostException {
      if (server.field_78845_b.equalsIgnoreCase((String)ServerModule.getInstance().ip.getValue())) {
         info.cancel();
      } else if (NoDDoS.getInstance().shouldntPing(server.field_78845_b)) {
         Phobos.LOGGER.info("NoDDoS preventing Ping to: " + server.field_78845_b);
         info.cancel();
      }

   }

   @Inject(
      method = {"tryCompatibilityPing"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void tryCompatibilityPingHook(ServerData server, CallbackInfo info) {
      if (server.field_78845_b.equalsIgnoreCase((String)ServerModule.getInstance().ip.getValue())) {
         info.cancel();
      } else if (NoDDoS.getInstance().shouldntPing(server.field_78845_b)) {
         Phobos.LOGGER.info("NoDDoS preventing Compatibility Ping to: " + server.field_78845_b);
         info.cancel();
      }

   }
}
