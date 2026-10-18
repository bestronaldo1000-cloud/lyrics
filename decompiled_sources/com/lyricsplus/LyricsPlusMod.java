package com.lyricsplus;

import com.lyricsplus.config.LyricsConfig;
import com.lyricsplus.keybinds.LyricsKeybinds;
import com.lyricsplus.lyrics.LyricsManager;
import com.lyricsplus.media.MediaManager;
import com.lyricsplus.media.TrackInfo;
import com.lyricsplus.render.LyricsHudRenderer;
import com.lyricsplus.render.PlayerWidgetRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LyricsPlusMod implements ClientModInitializer {
   public static final String MOD_ID = "lyricsplus";
   public static final Logger LOGGER = LoggerFactory.getLogger("lyricsplus");
   private int tickCounter = 0;
   private String lastLoggedTrack = "";

   public void onInitializeClient() {
      LOGGER.info("[LyricsPlus] Initializing synced lyrics & media HUD mod for Minecraft 1.21.x...");
      LyricsConfig.load();
      MediaManager.getInstance().init();
      LyricsKeybinds.register();
      HudRenderCallback.EVENT.register((HudRenderCallback)(var0, var1) -> {
         TrackInfo var2 = MediaManager.getInstance().getCurrentTrack();
         LyricsConfig var3 = LyricsConfig.getInstance();
         float var4 = var1 != null ? var1.getTickDelta(true) : 0.0F;
         PlayerWidgetRenderer.render(var0, var2, var3, var4);
         LyricsHudRenderer.render(var0, var2, var3, var4);
      });
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)var1 -> {
         LyricsKeybinds.handleInput(var1);
         this.tickCounter++;
         if (this.tickCounter % 5 == 0) {
            MediaManager.getInstance().tick();
            TrackInfo var2 = MediaManager.getInstance().getCurrentTrack();
            if (var2 != null && var2.isValidTrack()) {
               String var3 = var2.getTitle() + " - " + var2.getArtist();
               if (!var3.equals(this.lastLoggedTrack)) {
                  this.lastLoggedTrack = var3;
                  LOGGER.info("[LyricsPlus] Track active: " + var3);
               }

               LyricsManager.getInstance().updateTrack(var2);
            } else {
               this.lastLoggedTrack = "";
            }
         }
      });
      LOGGER.info("[LyricsPlus] Successfully loaded for Minecraft 1.21.x!");
   }
}
