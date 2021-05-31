package me.earth.phobos.mixin.mixins;

import javax.annotation.Nullable;
import me.earth.phobos.Phobos;
import me.earth.phobos.features.gui.custom.GuiCustomMainScreen;
import me.earth.phobos.features.modules.client.Managers;
import me.earth.phobos.features.modules.client.Screens;
import me.earth.phobos.features.modules.player.MultiTask;
import me.earth.phobos.features.modules.render.NoRender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.crash.CrashReport;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin({Minecraft.class})
public abstract class MixinMinecraft {
   @Shadow
   public abstract void func_147108_a(@Nullable GuiScreen var1);

   @Inject(
      method = {"runTickKeyboard"},
      at = {@At(
   value = "FIELD",
   target = "Lnet/minecraft/client/Minecraft;currentScreen:Lnet/minecraft/client/gui/GuiScreen;",
   ordinal = 0
)},
      locals = LocalCapture.CAPTURE_FAILSOFT
   )
   private void onRunTickKeyboard(CallbackInfo ci, int i) {
      if (Keyboard.getEventKeyState() && Phobos.moduleManager != null) {
         Phobos.moduleManager.onKeyPressed(i);
      }

   }

   @Inject(
      method = {"getLimitFramerate"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void getLimitFramerateHook(CallbackInfoReturnable<Integer> callbackInfoReturnable) {
      try {
         if ((Boolean)Managers.getInstance().unfocusedCpu.getValue() && !Display.isActive()) {
            callbackInfoReturnable.setReturnValue(Managers.getInstance().cpuFPS.getValue());
         }
      } catch (NullPointerException var3) {
      }

   }

   @Redirect(
      method = {"runGameLoop"},
      at = @At(
   value = "INVOKE",
   target = "Lorg/lwjgl/opengl/Display;sync(I)V",
   remap = false
)
   )
   public void syncHook(int maxFps) {
      if ((Boolean)Managers.getInstance().betterFrames.getValue()) {
         Display.sync((Integer)Managers.getInstance().betterFPS.getValue());
      } else {
         Display.sync(maxFps);
      }

   }

   @Inject(
      method = {"runTick()V"},
      at = {@At("RETURN")}
   )
   private void runTick(CallbackInfo callbackInfo) {
      if (Minecraft.func_71410_x().field_71462_r instanceof GuiMainMenu && (Boolean)Screens.INSTANCE.mainScreen.getValue()) {
         Minecraft.func_71410_x().func_147108_a(new GuiCustomMainScreen());
      }

   }

   @Inject(
      method = {"displayGuiScreen"},
      at = {@At("HEAD")}
   )
   private void displayGuiScreen(GuiScreen screen, CallbackInfo ci) {
      if (screen instanceof GuiMainMenu) {
         this.func_147108_a(new GuiCustomMainScreen());
      }

   }

   @Redirect(
      method = {"run"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/Minecraft;displayCrashReport(Lnet/minecraft/crash/CrashReport;)V"
)
   )
   public void displayCrashReportHook(Minecraft minecraft, CrashReport crashReport) {
      this.unload();
   }

   @Redirect(
      method = {"runTick"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/multiplayer/WorldClient;doVoidFogParticles(III)V"
)
   )
   public void doVoidFogParticlesHook(WorldClient world, int x, int y, int z) {
      NoRender.getInstance().doVoidFogParticles(x, y, z);
   }

   @Inject(
      method = {"shutdown"},
      at = {@At("HEAD")}
   )
   public void shutdownHook(CallbackInfo info) {
      this.unload();
   }

   private void unload() {
      System.out.println("Shutting down: saving configuration");
      Phobos.onUnload();
      System.out.println("Configuration saved.");
   }

   @Redirect(
      method = {"sendClickBlockToController"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/entity/EntityPlayerSP;isHandActive()Z"
)
   )
   private boolean isHandActiveWrapper(EntityPlayerSP playerSP) {
      return !MultiTask.getInstance().isOn() && playerSP.func_184587_cr();
   }

   @Redirect(
      method = {"rightClickMouse"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;getIsHittingBlock()Z",
   ordinal = 0
),
      require = 1
   )
   private boolean isHittingBlockHook(PlayerControllerMP playerControllerMP) {
      return !MultiTask.getInstance().isOn() && playerControllerMP.func_181040_m();
   }
}
