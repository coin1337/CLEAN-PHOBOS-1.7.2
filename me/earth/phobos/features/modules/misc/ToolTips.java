package me.earth.phobos.features.modules.misc;

import java.awt.Color;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import me.earth.phobos.event.events.Render2DEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Bind;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.ColorUtil;
import me.earth.phobos.util.EntityUtil;
import me.earth.phobos.util.RenderUtil;
import me.earth.phobos.util.Timer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityShulkerBox;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.client.event.RenderTooltipEvent.PostText;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

public class ToolTips extends Module {
   public Setting<Boolean> maps = this.register(new Setting("Maps", true));
   public Setting<Boolean> shulkers = this.register(new Setting("ShulkerViewer", true));
   public Setting<Bind> peek = this.register(new Setting("Peek", new Bind(-1)));
   public Setting<Boolean> shulkerSpy = this.register(new Setting("ShulkerSpy", true));
   public Setting<Boolean> render = this.register(new Setting("Render", true, (v) -> {
      return (Boolean)this.shulkerSpy.getValue();
   }));
   public Setting<Boolean> own = this.register(new Setting("OwnShulker", true, (v) -> {
      return (Boolean)this.shulkerSpy.getValue();
   }));
   public Setting<Integer> cooldown = this.register(new Setting("ShowForS", 2, 0, 5, (v) -> {
      return (Boolean)this.shulkerSpy.getValue();
   }));
   public Setting<Boolean> textColor = this.register(new Setting("TextColor", false, (v) -> {
      return (Boolean)this.shulkers.getValue();
   }));
   private final Setting<Integer> red = this.register(new Setting("Red", 255, 0, 255, (v) -> {
      return (Boolean)this.textColor.getValue();
   }));
   private final Setting<Integer> green = this.register(new Setting("Green", 0, 0, 255, (v) -> {
      return (Boolean)this.textColor.getValue();
   }));
   private final Setting<Integer> blue = this.register(new Setting("Blue", 0, 0, 255, (v) -> {
      return (Boolean)this.textColor.getValue();
   }));
   private final Setting<Integer> alpha = this.register(new Setting("Alpha", 255, 0, 255, (v) -> {
      return (Boolean)this.textColor.getValue();
   }));
   public Setting<Boolean> offsets = this.register(new Setting("Offsets", false));
   private final Setting<Integer> yPerPlayer = this.register(new Setting("Y/Player", 18, (v) -> {
      return (Boolean)this.offsets.getValue();
   }));
   private final Setting<Integer> xOffset = this.register(new Setting("XOffset", 4, (v) -> {
      return (Boolean)this.offsets.getValue();
   }));
   private final Setting<Integer> yOffset = this.register(new Setting("YOffset", 2, (v) -> {
      return (Boolean)this.offsets.getValue();
   }));
   private final Setting<Integer> trOffset = this.register(new Setting("TROffset", 2, (v) -> {
      return (Boolean)this.offsets.getValue();
   }));
   public Setting<Integer> invH = this.register(new Setting("InvH", 3, (v) -> {
      return (Boolean)this.offsets.getValue();
   }));
   private static final ResourceLocation MAP = new ResourceLocation("textures/map/map_background.png");
   private static final ResourceLocation SHULKER_GUI_TEXTURE = new ResourceLocation("textures/gui/container/shulker_box.png");
   private static ToolTips INSTANCE = new ToolTips();
   public Map<EntityPlayer, ItemStack> spiedPlayers = new ConcurrentHashMap();
   public Map<EntityPlayer, Timer> playerTimers = new ConcurrentHashMap();
   private int textRadarY = 0;

   public ToolTips() {
      super("ToolTips", "Several tweaks for tooltips.", Module.Category.MISC, true, false, false);
      this.setInstance();
   }

   private void setInstance() {
      INSTANCE = this;
   }

   public static ToolTips getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new ToolTips();
      }

      return INSTANCE;
   }

   public void onUpdate() {
      if (!fullNullCheck() && (Boolean)this.shulkerSpy.getValue()) {
         if (((Bind)this.peek.getValue()).getKey() != -1 && mc.field_71462_r instanceof GuiContainer && Keyboard.isKeyDown(((Bind)this.peek.getValue()).getKey())) {
            Slot slot = ((GuiContainer)mc.field_71462_r).getSlotUnderMouse();
            if (slot != null) {
               ItemStack stack = slot.func_75211_c();
               if (stack != null && stack.func_77973_b() instanceof ItemShulkerBox) {
                  displayInv(stack, (String)null);
               }
            }
         }

         Iterator var4 = mc.field_71441_e.field_73010_i.iterator();

         while(true) {
            EntityPlayer player;
            do {
               do {
                  do {
                     do {
                        do {
                           if (!var4.hasNext()) {
                              return;
                           }

                           player = (EntityPlayer)var4.next();
                        } while(player == null);
                     } while(player.func_184614_ca() == null);
                  } while(!(player.func_184614_ca().func_77973_b() instanceof ItemShulkerBox));
               } while(EntityUtil.isFakePlayer(player));
            } while(!(Boolean)this.own.getValue() && mc.field_71439_g.equals(player));

            ItemStack stack = player.func_184614_ca();
            this.spiedPlayers.put(player, stack);
         }
      }
   }

   public void onRender2D(Render2DEvent event) {
      if (!fullNullCheck() && (Boolean)this.shulkerSpy.getValue() && (Boolean)this.render.getValue()) {
         int x = -4 + (Integer)this.xOffset.getValue();
         int y = 10 + (Integer)this.yOffset.getValue();
         this.textRadarY = 0;
         Iterator var4 = mc.field_71441_e.field_73010_i.iterator();

         while(true) {
            EntityPlayer player;
            Timer playerTimer;
            do {
               do {
                  if (!var4.hasNext()) {
                     return;
                  }

                  player = (EntityPlayer)var4.next();
               } while(this.spiedPlayers.get(player) == null);

               if (player.func_184614_ca() != null && player.func_184614_ca().func_77973_b() instanceof ItemShulkerBox) {
                  if (player.func_184614_ca().func_77973_b() instanceof ItemShulkerBox) {
                     playerTimer = (Timer)this.playerTimers.get(player);
                     if (playerTimer != null) {
                        playerTimer.reset();
                        this.playerTimers.put(player, playerTimer);
                     }
                  }
                  break;
               }

               playerTimer = (Timer)this.playerTimers.get(player);
               if (playerTimer == null) {
                  Timer timer = new Timer();
                  timer.reset();
                  this.playerTimers.put(player, timer);
                  break;
               }
            } while(playerTimer.passedS((double)(Integer)this.cooldown.getValue()));

            ItemStack stack = (ItemStack)this.spiedPlayers.get(player);
            this.renderShulkerToolTip(stack, x, y, player.func_70005_c_());
            y += (Integer)this.yPerPlayer.getValue() + 60;
            this.textRadarY = y - 10 - (Integer)this.yOffset.getValue() + (Integer)this.trOffset.getValue();
         }
      }
   }

   public int getTextRadarY() {
      return this.textRadarY;
   }

   @SubscribeEvent(
      priority = EventPriority.HIGHEST
   )
   public void makeTooltip(ItemTooltipEvent event) {
   }

   @SubscribeEvent
   public void renderTooltip(PostText event) {
      if ((Boolean)this.maps.getValue() && !event.getStack().func_190926_b() && event.getStack().func_77973_b() instanceof ItemMap) {
         MapData mapData = Items.field_151098_aY.func_77873_a(event.getStack(), mc.field_71441_e);
         if (mapData != null) {
            GlStateManager.func_179094_E();
            GlStateManager.func_179124_c(1.0F, 1.0F, 1.0F);
            RenderHelper.func_74518_a();
            mc.func_110434_K().func_110577_a(MAP);
            Tessellator instance = Tessellator.func_178181_a();
            BufferBuilder buffer = instance.func_178180_c();
            int n = 7;
            float n2 = 135.0F;
            float n3 = 0.5F;
            GlStateManager.func_179109_b((float)event.getX(), (float)event.getY() - n2 * n3 - 5.0F, 0.0F);
            GlStateManager.func_179152_a(n3, n3, n3);
            buffer.func_181668_a(7, DefaultVertexFormats.field_181707_g);
            buffer.func_181662_b((double)(-n), (double)n2, 0.0D).func_187315_a(0.0D, 1.0D).func_181675_d();
            buffer.func_181662_b((double)n2, (double)n2, 0.0D).func_187315_a(1.0D, 1.0D).func_181675_d();
            buffer.func_181662_b((double)n2, (double)(-n), 0.0D).func_187315_a(1.0D, 0.0D).func_181675_d();
            buffer.func_181662_b((double)(-n), (double)(-n), 0.0D).func_187315_a(0.0D, 0.0D).func_181675_d();
            instance.func_78381_a();
            mc.field_71460_t.func_147701_i().func_148250_a(mapData, false);
            GlStateManager.func_179145_e();
            GlStateManager.func_179121_F();
         }
      }

   }

   public void renderShulkerToolTip(ItemStack stack, int x, int y, String name) {
      NBTTagCompound tagCompound = stack.func_77978_p();
      if (tagCompound != null && tagCompound.func_150297_b("BlockEntityTag", 10)) {
         NBTTagCompound blockEntityTag = tagCompound.func_74775_l("BlockEntityTag");
         if (blockEntityTag.func_150297_b("Items", 9)) {
            GlStateManager.func_179098_w();
            GlStateManager.func_179140_f();
            GlStateManager.func_179131_c(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.func_179147_l();
            GlStateManager.func_187428_a(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
            mc.func_110434_K().func_110577_a(SHULKER_GUI_TEXTURE);
            RenderUtil.drawTexturedRect(x, y, 0, 0, 176, 16, 500);
            RenderUtil.drawTexturedRect(x, y + 16, 0, 16, 176, 54 + (Integer)this.invH.getValue(), 500);
            RenderUtil.drawTexturedRect(x, y + 16 + 54, 0, 160, 176, 8, 500);
            GlStateManager.func_179097_i();
            Color color = new Color(0, 0, 0, 255);
            if ((Boolean)this.textColor.getValue()) {
               color = new Color((Integer)this.red.getValue(), (Integer)this.green.getValue(), (Integer)this.blue.getValue(), (Integer)this.alpha.getValue());
            }

            this.renderer.drawStringWithShadow(name == null ? stack.func_82833_r() : name, (float)(x + 8), (float)(y + 6), ColorUtil.toRGBA(color));
            GlStateManager.func_179126_j();
            RenderHelper.func_74520_c();
            GlStateManager.func_179091_B();
            GlStateManager.func_179142_g();
            GlStateManager.func_179145_e();
            NonNullList<ItemStack> nonnulllist = NonNullList.func_191197_a(27, ItemStack.field_190927_a);
            ItemStackHelper.func_191283_b(blockEntityTag, nonnulllist);

            for(int i = 0; i < nonnulllist.size(); ++i) {
               int iX = x + i % 9 * 18 + 8;
               int iY = y + i / 9 * 18 + 18;
               ItemStack itemStack = (ItemStack)nonnulllist.get(i);
               mc.func_175599_af().field_77023_b = 501.0F;
               RenderUtil.itemRender.func_180450_b(itemStack, iX, iY);
               RenderUtil.itemRender.func_180453_a(mc.field_71466_p, itemStack, iX, iY, (String)null);
               mc.func_175599_af().field_77023_b = 0.0F;
            }

            GlStateManager.func_179140_f();
            GlStateManager.func_179084_k();
            GlStateManager.func_179131_c(1.0F, 1.0F, 1.0F, 1.0F);
         }
      }

   }

   public static void displayInv(ItemStack stack, String name) {
      try {
         Item item = stack.func_77973_b();
         TileEntityShulkerBox entityBox = new TileEntityShulkerBox();
         ItemShulkerBox shulker = (ItemShulkerBox)item;
         entityBox.field_145854_h = shulker.func_179223_d();
         entityBox.func_145834_a(mc.field_71441_e);
         ItemStackHelper.func_191283_b(stack.func_77978_p().func_74775_l("BlockEntityTag"), entityBox.field_190596_f);
         entityBox.func_145839_a(stack.func_77978_p().func_74775_l("BlockEntityTag"));
         entityBox.func_190575_a(name == null ? stack.func_82833_r() : name);
         (new Thread(() -> {
            try {
               Thread.sleep(200L);
            } catch (InterruptedException var2) {
            }

            mc.field_71439_g.func_71007_a(entityBox);
         })).start();
      } catch (Exception var5) {
      }

   }
}
