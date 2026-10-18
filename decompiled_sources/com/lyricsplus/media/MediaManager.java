package com.lyricsplus.media;

import java.util.ArrayList;
import java.util.List;

public class MediaManager {
   private static MediaManager INSTANCE;
   private final List<MediaProvider> providers = new ArrayList<>();
   private TrackInfo currentTrack = null;
   private MediaProvider activeProvider = null;
   private static volatile long manualSeekOffsetMs = 0L;

   private MediaManager() {
      this.providers.add(new LocalBridgeServer());
      if (System.getProperty("os.name", "").toLowerCase().contains("win")) {
         this.providers.add(new WindowsMediaSessionProvider());
      }
   }

   public static synchronized MediaManager getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new MediaManager();
      }

      return INSTANCE;
   }

   public void init() {
      for (MediaProvider var2 : this.providers) {
         try {
            var2.init();
         } catch (Exception var4) {
            var4.printStackTrace();
         }
      }
   }

   public void tick() {
      TrackInfo var1 = null;

      for (MediaProvider var3 : this.providers) {
         try {
            TrackInfo var4 = var3.pollTrack();
            if (var4 != null && var4.isValidTrack()) {
               var1 = var4;
               this.activeProvider = var3;
               break;
            }
         } catch (Exception var5) {
         }
      }

      this.currentTrack = var1;
   }

   public TrackInfo getCurrentTrack() {
      return this.currentTrack;
   }

   public void skipNext() {
      if (this.activeProvider != null) {
         this.activeProvider.skipNext();
      }
   }

   public void skipPrevious() {
      if (this.activeProvider != null) {
         this.activeProvider.skipPrevious();
      }
   }

   public void togglePlayPause() {
      if (this.activeProvider != null) {
         this.activeProvider.togglePlayPause();
      }
   }

   public void seekRelative(long var1) {
      manualSeekOffsetMs += var1;
   }

   public static long getManualSeekOffsetMs() {
      return manualSeekOffsetMs;
   }

   public static void resetManualSeekOffset() {
      manualSeekOffsetMs = 0L;
   }

   public void close() {
      for (MediaProvider var2 : this.providers) {
         try {
            var2.close();
         } catch (Exception var4) {
         }
      }
   }
}
