package com.lyricsplus.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import net.fabricmc.loader.api.FabricLoader;

public class LyricsConfig {
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "lyricsplus.json");
   private static LyricsConfig INSTANCE;
   public String hudStyle = "BORDERLESS";
   public LyricsConfig.WidgetPosition playerWidgetPosition = LyricsConfig.WidgetPosition.TOP_LEFT;
   public boolean showAlbumArt = true;
   public boolean showArtist = true;
   public boolean showProgressBar = true;
   public boolean showTime = true;
   public boolean compactMode = false;
   public boolean autoHide = false;
   public boolean showPlayerWidget = true;
   public float playerWidgetAlpha = 0.95F;
   public float playerWidgetScale = 1.0F;
   public String accentColor = "ALBUM";
   public boolean showShadow = true;
   public boolean smoothAnimation = true;
   public boolean showEqualizer = true;
   public boolean showLyricsHud = true;
   public int lyricsLineCount = 4;
   public String lyricsSpacing = "NORMAL";
   public float lyricsBgAlpha = 0.9F;
   public String lyricsPositionMode = "BELOW_HUD";
   public boolean matchAlbumColor = false;
   public String activeLyricColor = "GREEN";
   public boolean boldActiveLyric = true;
   public boolean lyricsUppercase = false;
   public boolean showNextLinePreview = true;
   public boolean renderIn3D = false;
   public double textDistance3D = 18.0;
   public double textHeightOffset3D = 2.5;
   public boolean elytraFlightAssist = true;
   public boolean twoSided3D = true;
   public long timeOffsetMs = 120L;
   public int playerWidgetCustomX = 10;
   public int playerWidgetCustomY = 10;
   public int lyricsCustomX = 10;
   public int lyricsCustomY = 10;
   public float lyricsScale = 1.0F;
   public int lyricsColorHex = 5635925;
   public boolean lyricsDropShadow = true;
   public boolean lyricsGlowEffect = true;
   public boolean showNowPlayingToast = true;
   public boolean beatPulseEffect = true;
   public boolean smartCombatFade = true;
   public float combatFadeAlpha = 0.15F;
   public boolean showInstrumentalCountdown = true;
   public boolean scrollingMarquee = true;
   public boolean showLyricsLoading = true;
   public boolean smoothLyricTransition = true;
   public boolean autoHideInScreens = true;
   public int hudMargin = 10;
   public String toastDuration = "NORMAL";
   public float lyricsFontScale = 1.0F;
   public boolean activeLineHighlight = true;
   public boolean idleAutoDim = false;

   public static LyricsConfig getInstance() {
      if (INSTANCE == null) {
         load();
      }

      return INSTANCE;
   }

   public static void load() {
      if (CONFIG_FILE.exists()) {
         try (FileReader var0 = new FileReader(CONFIG_FILE)) {
            INSTANCE = (LyricsConfig)GSON.fromJson(var0, LyricsConfig.class);
         } catch (Exception var5) {
            var5.printStackTrace();
         }
      }

      if (INSTANCE == null) {
         INSTANCE = new LyricsConfig();
         save();
      }
   }

   public static void save() {
      if (INSTANCE == null) {
         INSTANCE = new LyricsConfig();
      }

      try {
         if (!CONFIG_FILE.getParentFile().exists()) {
            CONFIG_FILE.getParentFile().mkdirs();
         }

         try (FileWriter var0 = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(INSTANCE, var0);
         }
      } catch (IOException var5) {
         var5.printStackTrace();
      }
   }

   public enum LyricsPosition {
      TOP_LEFT,
      ABOVE_CROSSHAIR,
      CENTER,
      TOP,
      BOTTOM,
      CUSTOM;
   }

   public enum WidgetPosition {
      TOP_LEFT,
      TOP_RIGHT,
      BOTTOM_LEFT,
      BOTTOM_RIGHT,
      CENTER,
      CUSTOM;
   }
}
