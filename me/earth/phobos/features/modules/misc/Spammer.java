package me.earth.phobos.features.modules.misc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.FileUtil;
import me.earth.phobos.util.Timer;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.util.StringUtils;

public class Spammer extends Module {
   public Setting<Spammer.Mode> mode;
   public Setting<Spammer.PwordMode> type;
   public Setting<Spammer.DelayType> delayType;
   public Setting<Integer> delay;
   public Setting<Integer> delayDS;
   public Setting<Integer> delayMS;
   public Setting<String> msgTarget;
   public Setting<Boolean> greentext;
   public Setting<Boolean> random;
   public Setting<Boolean> loadFile;
   private final Timer timer;
   private final List<String> sendPlayers;
   private static final String fileName = "phobos/util/Spammer.txt";
   private static final String defaultMessage = "gg";
   private static final List<String> spamMessages = new ArrayList();
   private static final Random rnd = new Random();

   public Spammer() {
      super("Spammer", "Spams stuff.", Module.Category.MISC, true, false, false);
      this.mode = this.register(new Setting("Mode", Spammer.Mode.PWORD));
      this.type = this.register(new Setting("Pword", Spammer.PwordMode.CHAT, (v) -> {
         return this.mode.getValue() == Spammer.Mode.PWORD;
      }));
      this.delayType = this.register(new Setting("DelayType", Spammer.DelayType.S));
      this.delay = this.register(new Setting("DelayS", 10, 1, 20, (v) -> {
         return this.delayType.getValue() == Spammer.DelayType.S;
      }));
      this.delayDS = this.register(new Setting("DelayDS", 10, 1, 500, (v) -> {
         return this.delayType.getValue() == Spammer.DelayType.DS;
      }));
      this.delayMS = this.register(new Setting("DelayDS", 10, 1, 1000, (v) -> {
         return this.delayType.getValue() == Spammer.DelayType.MS;
      }));
      this.msgTarget = this.register(new Setting("MsgTarget", "Target...", (v) -> {
         return this.mode.getValue() == Spammer.Mode.PWORD && this.type.getValue() == Spammer.PwordMode.MSG;
      }));
      this.greentext = this.register(new Setting("Greentext", false, (v) -> {
         return this.mode.getValue() == Spammer.Mode.FILE;
      }));
      this.random = this.register(new Setting("Random", false, (v) -> {
         return this.mode.getValue() == Spammer.Mode.FILE;
      }));
      this.loadFile = this.register(new Setting("LoadFile", false, (v) -> {
         return this.mode.getValue() == Spammer.Mode.FILE;
      }));
      this.timer = new Timer();
      this.sendPlayers = new ArrayList();
   }

   public void onLoad() {
      this.readSpamFile();
      this.disable();
   }

   public void onEnable() {
      if (fullNullCheck()) {
         this.disable();
      } else {
         this.readSpamFile();
      }
   }

   public void onLogin() {
      this.disable();
   }

   public void onLogout() {
      this.disable();
   }

   public void onDisable() {
      spamMessages.clear();
      this.timer.reset();
   }

   public void onUpdate() {
      if (fullNullCheck()) {
         this.disable();
      } else {
         if ((Boolean)this.loadFile.getValue()) {
            this.readSpamFile();
            this.loadFile.setValue(false);
         }

         switch((Spammer.DelayType)this.delayType.getValue()) {
         case MS:
            if (!this.timer.passedMs((long)(Integer)this.delayMS.getValue())) {
               return;
            }
            break;
         case S:
            if (!this.timer.passedS((double)(Integer)this.delay.getValue())) {
               return;
            }
            break;
         case DS:
            if (!this.timer.passedDs((double)(Integer)this.delayDS.getValue())) {
               return;
            }
         }

         String msg;
         if (this.mode.getValue() != Spammer.Mode.PWORD) {
            if (spamMessages.size() > 0) {
               if ((Boolean)this.random.getValue()) {
                  int index = rnd.nextInt(spamMessages.size());
                  msg = (String)spamMessages.get(index);
                  spamMessages.remove(index);
               } else {
                  msg = (String)spamMessages.get(0);
                  spamMessages.remove(0);
               }

               spamMessages.add(msg);
               if ((Boolean)this.greentext.getValue()) {
                  msg = "> " + msg;
               }

               mc.field_71439_g.field_71174_a.func_147297_a(new CPacketChatMessage(msg.replaceAll("§", "")));
            }
         } else {
            msg = "  ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒\n ███▒█▒█▒███▒███▒███▒███\n █▒█▒█▒█▒█▒█▒█▒█▒█▒█▒█▒▒\n ███▒███▒█▒█▒███▒█▒█▒███\n █▒▒▒█▒█▒█▒█▒█▒█▒█▒█▒▒▒█\n █▒▒▒█▒█▒███▒███▒███▒███\n ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒";
            switch((Spammer.PwordMode)this.type.getValue()) {
            case MSG:
               msg = "/msg " + (String)this.msgTarget.getValue() + msg;
               break;
            case EVERYONE:
               String target = null;
               if (mc.func_147114_u() == null || mc.func_147114_u().func_175106_d() == null) {
                  return;
               }

               Iterator var3 = mc.func_147114_u().func_175106_d().iterator();

               while(var3.hasNext()) {
                  NetworkPlayerInfo info = (NetworkPlayerInfo)var3.next();
                  if (info != null && info.func_178854_k() != null) {
                     try {
                        String str = info.func_178854_k().func_150254_d();
                        String name = StringUtils.func_76338_a(str);
                        if (!name.equals(mc.field_71439_g.func_70005_c_()) && !this.sendPlayers.contains(name)) {
                           target = name;
                           this.sendPlayers.add(name);
                           break;
                        }
                     } catch (Exception var7) {
                     }
                  }
               }

               if (target == null) {
                  this.sendPlayers.clear();
                  return;
               }

               msg = "/msg " + target + msg;
            }

            mc.field_71439_g.func_71165_d(msg);
         }

         this.timer.reset();
      }
   }

   private void readSpamFile() {
      List<String> fileInput = FileUtil.readTextFileAllLines("phobos/util/Spammer.txt");
      Iterator<String> i = fileInput.iterator();
      spamMessages.clear();

      while(i.hasNext()) {
         String s = (String)i.next();
         if (!s.replaceAll("\\s", "").isEmpty()) {
            spamMessages.add(s);
         }
      }

      if (spamMessages.size() == 0) {
         spamMessages.add("gg");
      }

   }

   public static enum DelayType {
      MS,
      DS,
      S;
   }

   public static enum PwordMode {
      MSG,
      EVERYONE,
      CHAT;
   }

   public static enum Mode {
      FILE,
      PWORD;
   }
}
