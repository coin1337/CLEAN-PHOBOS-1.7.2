package me.earth.phobos.features.modules.misc;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import me.earth.phobos.event.events.PacketEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.MathUtil;
import me.earth.phobos.util.Timer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketKeepAlive;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PingSpoof extends Module {
   private Setting<Boolean> seconds = this.register(new Setting("Seconds", false));
   private Setting<Integer> delay = this.register(new Setting("DelayMS", 20, 0, 1000, (v) -> {
      return !(Boolean)this.seconds.getValue();
   }));
   private Setting<Integer> secondDelay = this.register(new Setting("DelayS", 5, 0, 30, (v) -> {
      return (Boolean)this.seconds.getValue();
   }));
   private Setting<Boolean> offOnLogout = this.register(new Setting("Logout", false));
   private Queue<Packet<?>> packets = new ConcurrentLinkedQueue();
   private Timer timer = new Timer();
   private boolean receive = true;

   public PingSpoof() {
      super("PingSpoof", "Spoofs your ping!", Module.Category.MISC, true, false, false);
   }

   public void onLoad() {
      if ((Boolean)this.offOnLogout.getValue()) {
         this.disable();
      }

   }

   public void onLogout() {
      if ((Boolean)this.offOnLogout.getValue()) {
         this.disable();
      }

   }

   public void onUpdate() {
      this.clearQueue();
   }

   public void onDisable() {
      this.clearQueue();
   }

   @SubscribeEvent
   public void onPacketSend(PacketEvent.Send event) {
      if (this.receive && mc.field_71439_g != null && !mc.func_71356_B() && mc.field_71439_g.func_70089_S() && event.getStage() == 0 && event.getPacket() instanceof CPacketKeepAlive) {
         this.packets.add(event.getPacket());
         event.setCanceled(true);
      }

   }

   public void clearQueue() {
      if (mc.field_71439_g != null && !mc.func_71356_B() && mc.field_71439_g.func_70089_S() && (!(Boolean)this.seconds.getValue() && this.timer.passedMs((long)(Integer)this.delay.getValue()) || (Boolean)this.seconds.getValue() && this.timer.passedS((double)(Integer)this.secondDelay.getValue()))) {
         double limit = MathUtil.getIncremental(Math.random() * 10.0D, 1.0D);
         this.receive = false;

         for(int i = 0; (double)i < limit; ++i) {
            Packet<?> packet = (Packet)this.packets.poll();
            if (packet != null) {
               mc.field_71439_g.field_71174_a.func_147297_a(packet);
            }
         }

         this.timer.reset();
         this.receive = true;
      }

   }
}
