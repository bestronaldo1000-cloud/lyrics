package com.lyricsplus.lyrics;

import com.lyricsplus.media.TrackInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class LyricsManager {
   private static LyricsManager INSTANCE;
   private final List<LyricLine> currentLyrics = new CopyOnWriteArrayList<>();
   private volatile String lastTrackKey = "";
   private volatile boolean loading = false;
   private volatile boolean fetchFailed = false;
   private volatile boolean cached = false;

   public static synchronized LyricsManager getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new LyricsManager();
      }

      return INSTANCE;
   }

   public void updateTrack(TrackInfo var1) {
      if (var1 != null && var1.isValidTrack()) {
         String var2 = var1.getTrackKey();
         if (!Objects.equals(var2, this.lastTrackKey)) {
            this.lastTrackKey = var2;
            this.currentLyrics.clear();
            this.loading = true;
            this.fetchFailed = false;
            this.cached = false;
            String var3 = var2;
            LrclibClient.fetchSyncedLyrics(var1.getTitle(), var1.getArtist(), var1.getAlbum(), var1.getDurationMs() / 1000.0).thenAccept(var3x -> {
               if (Objects.equals(this.lastTrackKey, var3)) {
                  this.currentLyrics.clear();
                  if (var3x != null && !var3x.isBlank()) {
                     List var4 = LrcParser.parse(var3x);
                     this.currentLyrics.addAll(var4);
                     this.cached = true;
                     System.out.println("[LyricsPlus] Loaded " + var4.size() + " synced lines for: " + var1.getTitle());
                  } else {
                     this.fetchFailed = true;
                  }

                  this.loading = false;
               }
            }).exceptionally(var2x -> {
               if (Objects.equals(this.lastTrackKey, var3)) {
                  this.loading = false;
                  this.fetchFailed = true;
               }

               return null;
            });
         }
      } else {
         this.currentLyrics.clear();
         this.lastTrackKey = "";
      }
   }

   public LyricLine getCurrentLine(long var1) {
      if (this.currentLyrics.isEmpty()) {
         return null;
      }

      LyricLine var3 = null;

      for (LyricLine var5 : this.currentLyrics) {
         if (var5.timestampMs() > var1) {
            break;
         }

         var3 = var5;
      }

      return var3;
   }

   public LyricLine getNextLine(long var1) {
      if (this.currentLyrics.isEmpty()) {
         return null;
      }

      for (LyricLine var4 : this.currentLyrics) {
         if (var4.timestampMs() > var1) {
            return var4;
         }
      }

      return null;
   }

   public List<LyricLine> getUpcomingLines(long var1, int var3) {
      if (!this.currentLyrics.isEmpty() && var3 > 0) {
         ArrayList var4 = new ArrayList(var3);

         for (LyricLine var6 : this.currentLyrics) {
            if (var6.timestampMs() > var1) {
               var4.add(var6);
               if (var4.size() >= var3) {
                  break;
               }
            }
         }

         return var4;
      } else {
         return Collections.emptyList();
      }
   }

   public boolean hasLyrics() {
      return !this.currentLyrics.isEmpty();
   }

   public boolean isLoading() {
      return this.loading;
   }

   public boolean isFetchFailed() {
      return this.fetchFailed;
   }

   public boolean isCached() {
      return this.cached;
   }

   public List<LyricLine> getLyrics() {
      return Collections.unmodifiableList(this.currentLyrics);
   }
}
