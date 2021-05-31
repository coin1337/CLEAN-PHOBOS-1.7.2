package me.earth.phobos.features.modules.misc;

import me.earth.phobos.DiscordPresence;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;

public class RPC extends Module {
   public static RPC INSTANCE;
   public Setting<Boolean> showIP = this.register(new Setting("ShowIP", true, "Shows the server IP in your discord presence."));
   public Setting<String> state = this.register(new Setting("State", "3arthh4ck 1.7.2", "Sets the state of the DiscordRPC."));

   public RPC() {
      super("RPC", "Discord rich presence", Module.Category.MISC, false, false, false);
      INSTANCE = this;
   }

   public void onEnable() {
      DiscordPresence.start();
   }

   public void onDisable() {
      DiscordPresence.stop();
   }
}
