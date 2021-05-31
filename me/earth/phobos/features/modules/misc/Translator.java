package me.earth.phobos.features.modules.misc;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import me.earth.phobos.event.events.PacketEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.Util;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Translator extends Module {
   private final Setting<Translator.Language> sourceLanguage;
   private final Setting<Translator.Language> targetLanguage;
   public static final String API_KEY = "trnsl.1.1.20200403T133250Z.c0062863622d7503.ca7fca44b9d2259ba3dadd61ddf7c15a2c9f3876";
   public Translator.Translate translate;

   public Translator() {
      super("Translator", "Translates text to a different language", Module.Category.MISC, true, false, false);
      this.sourceLanguage = this.register(new Setting("SourceLanguage", Translator.Language.English));
      this.targetLanguage = this.register(new Setting("TargetLanguage", Translator.Language.Spanish));
   }

   @SubscribeEvent
   public void onSendPacket(PacketEvent.Send event) {
      if (event.getPacket() instanceof CPacketChatMessage) {
         event.setCanceled(true);
         this.translate = new Translator.Translate(((CPacketChatMessage)event.getPacket()).func_149439_c(), (Translator.Language)this.sourceLanguage.getValue(), (Translator.Language)this.targetLanguage.getValue());
         this.translate.start();
      }

   }

   private JsonObject request(String URL) throws IOException {
      URL url = new URL(URL);
      URLConnection urlConn = url.openConnection();
      urlConn.addRequestProperty("User-Agent", "Mozilla");
      InputStream inStream = urlConn.getInputStream();
      JsonParser jp = new JsonParser();
      JsonElement root = jp.parse(new InputStreamReader((InputStream)urlConn.getContent()));
      inStream.close();
      return root.getAsJsonObject();
   }

   public static enum Language {
      Azerbaijan("az"),
      Albanian("sq"),
      Amharic("am"),
      English("en"),
      Arabic("ar"),
      Armenian("hy"),
      Afrikaans("af"),
      Basque("eu"),
      Bashkir("ba"),
      Belarusian("be"),
      Bengali("bn"),
      Burmese("my"),
      Bulgarian("bg"),
      Bosnian("bs"),
      Welsh("cy"),
      Hungarian("hu"),
      Vietnamese("vi"),
      Haitian("ht"),
      Galician("gl"),
      Dutch("nl"),
      HillMari("mrj"),
      Greek("el"),
      Georgian("ka"),
      Gujarati("gu"),
      Danish("da"),
      Hebrew("he"),
      Yiddish("yi"),
      Indonesian("id"),
      Irish("ga"),
      Italian("it"),
      Icelandic("is"),
      Spanish("es"),
      Kazakh("kk"),
      Kannada("kn"),
      Catalan("ca"),
      Kyrgyz("ky"),
      Chinese("zh"),
      Korean("ko"),
      Xhosa("xh"),
      Khmer("km"),
      Laotian("lo"),
      Latin("la"),
      Latvian("lv"),
      Lithuanian("lt"),
      Luxembourgish("lb"),
      Malagasy("mg"),
      Malay("ms"),
      Malayalam("ml"),
      Maltese("mt"),
      Macedonian("mk"),
      Maori("mi"),
      Marathi("mr"),
      Mari("mhr"),
      Mongolian("mn"),
      German("de"),
      Nepali("ne"),
      Norwegian("no"),
      Russian("ru"),
      Punjabi("pa"),
      Papiamento("pap"),
      Persian("fa"),
      Polish("pl"),
      Portuguese("pt"),
      Romanian("ro"),
      Cebuano("ceb"),
      Serbian("sr"),
      Sinhala("si"),
      Slovakian("sk"),
      Slovenian("sl"),
      Swahili("sw"),
      Sundanese("su"),
      Tajik("tg"),
      Thai("th"),
      Tagalog("tl"),
      Tamil("ta"),
      Tatar("tt"),
      Telugu("te"),
      Turkish("tr"),
      Udmurt("udm"),
      Uzbek("uz"),
      Ukrainian("uk"),
      Urdu("ur"),
      Finnish("fi"),
      French("fr"),
      Hindi("hi"),
      Croatian("hr"),
      Czech("cs"),
      Swedish("sv"),
      Scottish("gd"),
      Estonian("et"),
      Esperanto("eo"),
      Javanese("jv"),
      Japanese("ja");

      private String code;

      private Language(String code) {
         this.code = code;
      }

      public String getCode() {
         return this.code;
      }

      public static Translator.Language getByCode(String code) {
         Translator.Language[] var1 = values();
         int var2 = var1.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            Translator.Language language = var1[var3];
            if (language.code.equals(code)) {
               return language;
            }
         }

         return null;
      }
   }

   public static class Translate extends Thread {
      Thread thread;
      public String message;
      public Translator.Language sourceLang;
      public Translator.Language lang;
      public String finalMessage = null;

      public Translate(String message, Translator.Language sourceLang, Translator.Language lang) {
         super("Translate");
         this.message = message;
         this.sourceLang = sourceLang;
         this.lang = lang;
      }

      public void run() {
         try {
            this.finalMessage = this.request("https://translate.yandex.net/api/v1.5/tr.json/translate?key=trnsl.1.1.20200403T133250Z.c0062863622d7503.ca7fca44b9d2259ba3dadd61ddf7c15a2c9f3876&text=" + this.message.replace(" ", "%20") + "&lang=" + this.sourceLang.getCode() + "-" + this.lang.getCode()).get("text").getAsString();
            Util.mc.field_71439_g.field_71174_a.func_147297_a(new CPacketChatMessage(this.finalMessage));
         } catch (IOException var2) {
            var2.printStackTrace();
         }

      }

      public void start() {
         if (this.thread == null) {
            this.thread = new Thread(this, "Translate");
            this.thread.start();
         }

      }

      private JsonObject request(String URL) throws IOException {
         URL url = new URL(URL);
         URLConnection urlConn = url.openConnection();
         urlConn.addRequestProperty("User-Agent", "Mozilla");
         InputStream inStream = urlConn.getInputStream();
         JsonParser jp = new JsonParser();
         JsonElement root = jp.parse(new InputStreamReader((InputStream)urlConn.getContent()));
         inStream.close();
         return root.getAsJsonObject();
      }
   }
}
