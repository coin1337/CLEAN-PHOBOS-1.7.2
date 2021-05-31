package me.earth.phobos.features.modules.misc;

import java.util.ArrayList;
import java.util.List;
import me.earth.phobos.Phobos;
import me.earth.phobos.event.events.BlockEvent;
import me.earth.phobos.event.events.UpdateWalkingPlayerEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.BlockUtil;
import me.earth.phobos.util.MathUtil;
import me.earth.phobos.util.Timer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Nuker extends Module {
   public Setting<Boolean> rotate = this.register(new Setting("Rotate", false));
   public Setting<Float> distance = this.register(new Setting("Range", 6.0F, 0.1F, 10.0F));
   public Setting<Integer> blockPerTick = this.register(new Setting("Blocks/Attack", 50, 1, 100));
   public Setting<Integer> delay = this.register(new Setting("Delay/Attack", 50, 1, 1000));
   public Setting<Boolean> nuke = this.register(new Setting("Nuke", false));
   public Setting<Nuker.Mode> mode;
   public Setting<Boolean> antiRegear;
   public Setting<Boolean> hopperNuker;
   private Setting<Boolean> autoSwitch;
   private int oldSlot;
   private boolean isMining;
   private final Timer timer;
   private Block selected;

   public Nuker() {
      super("Nuker", "Mines many blocks", Module.Category.MISC, true, false, false);
      this.mode = this.register(new Setting("Mode", Nuker.Mode.NUKE, (v) -> {
         return (Boolean)this.nuke.getValue();
      }));
      this.antiRegear = this.register(new Setting("AntiRegear", false));
      this.hopperNuker = this.register(new Setting("HopperAura", false));
      this.autoSwitch = this.register(new Setting("AutoSwitch", false));
      this.oldSlot = -1;
      this.isMining = false;
      this.timer = new Timer();
   }

   public void onToggle() {
      this.selected = null;
   }

   @SubscribeEvent
   public void onClickBlock(BlockEvent event) {
      if (event.getStage() == 3 && (this.mode.getValue() == Nuker.Mode.SELECTION || this.mode.getValue() == Nuker.Mode.NUKE)) {
         Block block = mc.field_71441_e.func_180495_p(event.pos).func_177230_c();
         if (block != null && block != this.selected) {
            this.selected = block;
            event.setCanceled(true);
         }
      }

   }

   @SubscribeEvent
   public void onUpdateWalkingPlayerPre(UpdateWalkingPlayerEvent event) {
      if (event.getStage() == 0) {
         if ((Boolean)this.nuke.getValue()) {
            BlockPos pos = null;
            switch((Nuker.Mode)this.mode.getValue()) {
            case SELECTION:
            case NUKE:
               pos = this.getClosestBlockSelection();
               break;
            case ALL:
               pos = this.getClosestBlockAll();
            }

            if (pos != null) {
               if (this.mode.getValue() != Nuker.Mode.SELECTION && this.mode.getValue() != Nuker.Mode.ALL) {
                  for(int i = 0; i < (Integer)this.blockPerTick.getValue(); ++i) {
                     pos = this.getClosestBlockSelection();
                     if (pos != null) {
                        if ((Boolean)this.rotate.getValue()) {
                           float[] angle = MathUtil.calcAngle(mc.field_71439_g.func_174824_e(mc.func_184121_ak()), new Vec3d((double)((float)pos.func_177958_n() + 0.5F), (double)((float)pos.func_177956_o() + 0.5F), (double)((float)pos.func_177952_p() + 0.5F)));
                           Phobos.rotationManager.setPlayerRotations(angle[0], angle[1]);
                        }

                        if (this.timer.passedMs((long)(Integer)this.delay.getValue())) {
                           mc.field_71442_b.func_180512_c(pos, mc.field_71439_g.func_174811_aO());
                           mc.field_71439_g.func_184609_a(EnumHand.MAIN_HAND);
                           this.timer.reset();
                        }
                     }
                  }
               } else {
                  if ((Boolean)this.rotate.getValue()) {
                     float[] angle = MathUtil.calcAngle(mc.field_71439_g.func_174824_e(mc.func_184121_ak()), new Vec3d((double)((float)pos.func_177958_n() + 0.5F), (double)((float)pos.func_177956_o() + 0.5F), (double)((float)pos.func_177952_p() + 0.5F)));
                     Phobos.rotationManager.setPlayerRotations(angle[0], angle[1]);
                  }

                  if (this.canBreak(pos)) {
                     mc.field_71442_b.func_180512_c(pos, mc.field_71439_g.func_174811_aO());
                     mc.field_71439_g.func_184609_a(EnumHand.MAIN_HAND);
                  }
               }
            }
         }

         if ((Boolean)this.antiRegear.getValue()) {
            this.breakBlocks(BlockUtil.shulkerList);
         }

         if ((Boolean)this.hopperNuker.getValue()) {
            List<Block> blocklist = new ArrayList();
            blocklist.add(Blocks.field_150438_bZ);
            this.breakBlocks(blocklist);
         }
      }

   }

   public void breakBlocks(List<Block> blocks) {
      BlockPos pos = this.getNearestBlock(blocks);
      if (pos != null) {
         if (!this.isMining) {
            this.oldSlot = mc.field_71439_g.field_71071_by.field_70461_c;
            this.isMining = true;
         }

         if ((Boolean)this.rotate.getValue()) {
            float[] angle = MathUtil.calcAngle(mc.field_71439_g.func_174824_e(mc.func_184121_ak()), new Vec3d((double)((float)pos.func_177958_n() + 0.5F), (double)((float)pos.func_177956_o() + 0.5F), (double)((float)pos.func_177952_p() + 0.5F)));
            Phobos.rotationManager.setPlayerRotations(angle[0], angle[1]);
         }

         if (this.canBreak(pos)) {
            if ((Boolean)this.autoSwitch.getValue()) {
               int newSlot = -1;

               for(int i = 0; i < 9; ++i) {
                  ItemStack stack = mc.field_71439_g.field_71071_by.func_70301_a(i);
                  if (stack != ItemStack.field_190927_a && stack.func_77973_b() instanceof ItemPickaxe) {
                     newSlot = i;
                     break;
                  }
               }

               if (newSlot != -1) {
                  mc.field_71439_g.field_71071_by.field_70461_c = newSlot;
               }
            }

            mc.field_71442_b.func_180512_c(pos, mc.field_71439_g.func_174811_aO());
            mc.field_71439_g.func_184609_a(EnumHand.MAIN_HAND);
         }
      } else if ((Boolean)this.autoSwitch.getValue() && this.oldSlot != -1) {
         mc.field_71439_g.field_71071_by.field_70461_c = this.oldSlot;
         this.oldSlot = -1;
         this.isMining = false;
      }

   }

   private boolean canBreak(BlockPos pos) {
      IBlockState blockState = mc.field_71441_e.func_180495_p(pos);
      Block block = blockState.func_177230_c();
      return block.func_176195_g(blockState, mc.field_71441_e, pos) != -1.0F;
   }

   private BlockPos getNearestBlock(List<Block> blocks) {
      double maxDist = MathUtil.square((Float)this.distance.getValue());
      BlockPos ret = null;

      for(double x = maxDist; x >= -maxDist; --x) {
         for(double y = maxDist; y >= -maxDist; --y) {
            for(double z = maxDist; z >= -maxDist; --z) {
               BlockPos pos = new BlockPos(mc.field_71439_g.field_70165_t + x, mc.field_71439_g.field_70163_u + y, mc.field_71439_g.field_70161_v + z);
               double dist = mc.field_71439_g.func_70092_e((double)pos.func_177958_n(), (double)pos.func_177956_o(), (double)pos.func_177952_p());
               if (dist <= maxDist && blocks.contains(mc.field_71441_e.func_180495_p(pos).func_177230_c()) && this.canBreak(pos)) {
                  maxDist = dist;
                  ret = pos;
               }
            }
         }
      }

      return ret;
   }

   private BlockPos getClosestBlockAll() {
      float maxDist = (Float)this.distance.getValue();
      BlockPos ret = null;

      for(float x = maxDist; x >= -maxDist; --x) {
         for(float y = maxDist; y >= -maxDist; --y) {
            for(float z = maxDist; z >= -maxDist; --z) {
               BlockPos pos = new BlockPos(mc.field_71439_g.field_70165_t + (double)x, mc.field_71439_g.field_70163_u + (double)y, mc.field_71439_g.field_70161_v + (double)z);
               double dist = mc.field_71439_g.func_70011_f((double)pos.func_177958_n(), (double)pos.func_177956_o(), (double)pos.func_177952_p());
               if (dist <= (double)maxDist && mc.field_71441_e.func_180495_p(pos).func_177230_c() != Blocks.field_150350_a && !(mc.field_71441_e.func_180495_p(pos).func_177230_c() instanceof BlockLiquid) && this.canBreak(pos) && (double)pos.func_177956_o() >= mc.field_71439_g.field_70163_u) {
                  maxDist = (float)dist;
                  ret = pos;
               }
            }
         }
      }

      return ret;
   }

   private BlockPos getClosestBlockSelection() {
      float maxDist = (Float)this.distance.getValue();
      BlockPos ret = null;

      for(float x = maxDist; x >= -maxDist; --x) {
         for(float y = maxDist; y >= -maxDist; --y) {
            for(float z = maxDist; z >= -maxDist; --z) {
               BlockPos pos = new BlockPos(mc.field_71439_g.field_70165_t + (double)x, mc.field_71439_g.field_70163_u + (double)y, mc.field_71439_g.field_70161_v + (double)z);
               double dist = mc.field_71439_g.func_70011_f((double)pos.func_177958_n(), (double)pos.func_177956_o(), (double)pos.func_177952_p());
               if (dist <= (double)maxDist && mc.field_71441_e.func_180495_p(pos).func_177230_c() != Blocks.field_150350_a && !(mc.field_71441_e.func_180495_p(pos).func_177230_c() instanceof BlockLiquid) && mc.field_71441_e.func_180495_p(pos).func_177230_c() == this.selected && this.canBreak(pos) && (double)pos.func_177956_o() >= mc.field_71439_g.field_70163_u) {
                  maxDist = (float)dist;
                  ret = pos;
               }
            }
         }
      }

      return ret;
   }

   public static enum Mode {
      SELECTION,
      ALL,
      NUKE;
   }
}
