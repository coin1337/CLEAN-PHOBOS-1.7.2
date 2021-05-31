package me.earth.phobos.features.modules.misc;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import me.earth.phobos.event.events.ConnectionEvent;
import me.earth.phobos.event.events.DeathEvent;
import me.earth.phobos.event.events.PacketEvent;
import me.earth.phobos.event.events.UpdateWalkingPlayerEvent;
import me.earth.phobos.features.command.Command;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.modules.combat.AntiTrap;
import me.earth.phobos.features.modules.combat.AutoCrystal;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.EntityUtil;
import me.earth.phobos.util.TextUtil;
import me.earth.phobos.util.Timer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Tracker extends Module {
   public Setting<TextUtil.Color> color;
   public Setting<Boolean> autoEnable;
   public Setting<Boolean> autoDisable;
   private EntityPlayer trackedPlayer;
   private static Tracker instance;
   private int usedExp;
   private int usedStacks;
   private int usedCrystals;
   private int usedCStacks;
   private boolean shouldEnable;
   private final Timer timer;
   private final Set<BlockPos> manuallyPlaced;

   public Tracker() {
      super("Tracker", "Tracks players in 1v1s. Only good in duels tho!", Module.Category.MISC, true, false, true);
      this.color = this.register(new Setting("Color", TextUtil.Color.RED));
      this.autoEnable = this.register(new Setting("AutoEnable", false));
      this.autoDisable = this.register(new Setting("AutoDisable", true));
      this.usedExp = 0;
      this.usedStacks = 0;
      this.usedCrystals = 0;
      this.usedCStacks = 0;
      this.shouldEnable = false;
      this.timer = new Timer();
      this.manuallyPlaced = new HashSet();
      instance = this;
   }

   @SubscribeEvent
   public void onPacketReceive(PacketEvent.Receive event) {
      if (!fullNullCheck() && ((Boolean)this.autoEnable.getValue() || (Boolean)this.autoDisable.getValue()) && event.getPacket() instanceof SPacketChat) {
         SPacketChat packet = (SPacketChat)event.getPacket();
         String message = packet.func_148915_c().func_150254_d();
         if ((Boolean)this.autoEnable.getValue() && (message.contains("has accepted your duel request") || message.contains("Accepted the duel request from")) && !message.contains("<")) {
            Command.sendMessage("Tracker will enable in 5 seconds.");
            this.timer.reset();
            this.shouldEnable = true;
         } else if ((Boolean)this.autoDisable.getValue() && message.contains("has defeated") && message.contains(mc.field_71439_g.func_70005_c_()) && !message.contains("<")) {
            this.disable();
         }
      }

   }

   @SubscribeEvent
   public void onPacketSend(PacketEvent.Send event) {
      if (!fullNullCheck() && this.isOn() && event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock) {
         CPacketPlayerTryUseItemOnBlock packet = (CPacketPlayerTryUseItemOnBlock)event.getPacket();
         if (mc.field_71439_g.func_184586_b(packet.field_187027_c).func_77973_b() == Items.field_185158_cP && !AntiTrap.placedPos.contains(packet.field_179725_b) && !AutoCrystal.placedPos.contains(packet.field_179725_b)) {
            this.manuallyPlaced.add(packet.field_179725_b);
         }
      }

   }

   @SubscribeEvent
   public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
      if (this.shouldEnable && this.timer.passedS(5.0D) && this.isOff()) {
         this.enable();
      }

   }

   public void onUpdate() {
      if (!this.isOff()) {
         if (this.trackedPlayer == null) {
            this.trackedPlayer = EntityUtil.getClosestEnemy(1000.0D);
         } else {
            if (this.usedStacks != this.usedExp / 64) {
               this.usedStacks = this.usedExp / 64;
               Command.sendMessage(TextUtil.coloredString(this.trackedPlayer.func_70005_c_() + " used: " + this.usedStacks + " Stacks of EXP.", (TextUtil.Color)this.color.getValue()));
            }

            if (this.usedCStacks != this.usedCrystals / 64) {
               this.usedCStacks = this.usedCrystals / 64;
               Command.sendMessage(TextUtil.coloredString(this.trackedPlayer.func_70005_c_() + " used: " + this.usedCStacks + " Stacks of Crystals.", (TextUtil.Color)this.color.getValue()));
            }
         }

      }
   }

   public void onSpawnEntity(Entity entity) {
      if (!this.isOff()) {
         if (entity instanceof EntityExpBottle && Objects.equals(mc.field_71441_e.func_72890_a(entity, 3.0D), this.trackedPlayer)) {
            ++this.usedExp;
         }

         if (entity instanceof EntityEnderCrystal) {
            if (AntiTrap.placedPos.contains(entity.func_180425_c().func_177977_b())) {
               AntiTrap.placedPos.remove(entity.func_180425_c().func_177977_b());
            } else if (this.manuallyPlaced.contains(entity.func_180425_c().func_177977_b())) {
               this.manuallyPlaced.remove(entity.func_180425_c().func_177977_b());
            } else if (!AutoCrystal.placedPos.contains(entity.func_180425_c().func_177977_b())) {
               ++this.usedCrystals;
            }
         }

      }
   }

   @SubscribeEvent
   public void onConnection(ConnectionEvent event) {
      if (!this.isOff() && event.getStage() == 1) {
         String name = event.getName();
         if (this.trackedPlayer != null && name != null && name.equals(this.trackedPlayer.func_70005_c_()) && (Boolean)this.autoDisable.getValue()) {
            Command.sendMessage(name + " logged, Tracker disableing.");
            this.disable();
         }

      }
   }

   public void onToggle() {
      this.manuallyPlaced.clear();
      AntiTrap.placedPos.clear();
      this.shouldEnable = false;
      this.trackedPlayer = null;
      this.usedExp = 0;
      this.usedStacks = 0;
      this.usedCrystals = 0;
      this.usedCStacks = 0;
   }

   public void onLogout() {
      if ((Boolean)this.autoDisable.getValue()) {
         this.disable();
      }

   }

   @SubscribeEvent
   public void onDeath(DeathEvent event) {
      if (this.isOn() && (event.player.equals(this.trackedPlayer) || event.player.equals(mc.field_71439_g))) {
         this.usedExp = 0;
         this.usedStacks = 0;
         this.usedCrystals = 0;
         this.usedCStacks = 0;
         if ((Boolean)this.autoDisable.getValue()) {
            this.disable();
         }
      }

   }

   public String getDisplayInfo() {
      return this.trackedPlayer != null ? this.trackedPlayer.func_70005_c_() : null;
   }

   public static Tracker getInstance() {
      if (instance == null) {
         instance = new Tracker();
      }

      return instance;
   }
}
