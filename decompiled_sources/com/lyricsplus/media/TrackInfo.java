package com.lyricsplus.media;

import java.util.Objects;

public class TrackInfo {
   private final String title;
   private final String artist;
   private final String album;
   private final long durationMs;
   private final long positionMs;
   private final long sampledAtSystemMs;
   private final boolean isPlaying;
   private final byte[] albumArtBytes;
   private final String albumArtUrl;
   private static long lastCalculatedPos = 0L;
   private static long lastCalculatedSysTime = 0L;
   private static String lastTrackKey = "";

   public TrackInfo(String var1, String var2, String var3, long var4, long var6, boolean var8, byte[] var9, String var10, long var11) {
      this.title = var1 != null ? var1 : "";
      this.artist = var2 != null ? var2 : "";
      this.album = var3 != null ? var3 : "";
      this.durationMs = Math.max(0L, var4);
      this.positionMs = Math.max(0L, var6);
      this.isPlaying = var8;
      this.albumArtBytes = var9;
      this.albumArtUrl = var10;
      this.sampledAtSystemMs = var11 > 0L ? var11 : System.currentTimeMillis();
   }

   public String getTitle() {
      return this.title;
   }

   public String getArtist() {
      return this.artist;
   }

   public String getAlbum() {
      return this.album;
   }

   public long getDurationMs() {
      return this.durationMs;
   }

   public long getPositionMs() {
      return this.positionMs;
   }

   public boolean isPlaying() {
      return this.isPlaying;
   }

   public byte[] getAlbumArtBytes() {
      return this.albumArtBytes;
   }

   public String getAlbumArtUrl() {
      return this.albumArtUrl;
   }

   public synchronized long getCurrentEstimatedPositionMs(long var1) {
      String var3 = this.getTrackKey();
      long var4 = System.currentTimeMillis();
      if (!var3.equals(lastTrackKey)) {
         lastTrackKey = var3;
         lastCalculatedPos = this.positionMs;
         lastCalculatedSysTime = var4;
         MediaManager.resetManualSeekOffset();
      }

      long var6 = MediaManager.getManualSeekOffsetMs();
      if (!this.isPlaying) {
         lastCalculatedPos = this.positionMs;
         lastCalculatedSysTime = var4;
         return Math.max(0L, this.positionMs + var1 + var6);
      }

      long var8 = Math.max(0L, var4 - this.sampledAtSystemMs);
      long var10 = this.positionMs + var8;
      lastCalculatedPos = var10;
      lastCalculatedSysTime = var4;
      long var12 = lastCalculatedPos + var1 + var6;
      if (this.durationMs > 0L && var12 > this.durationMs) {
         var12 = this.durationMs;
      }

      return Math.max(0L, var12);
   }

   public boolean isValidTrack() {
      return !this.title.isBlank();
   }

   public String getTrackKey() {
      return (this.artist + " - " + this.title).toLowerCase().trim();
   }

   @Override
   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else {
         return !(var1 instanceof TrackInfo var2)
            ? false
            : this.durationMs == var2.durationMs && Objects.equals(this.title, var2.title) && Objects.equals(this.artist, var2.artist);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.title, this.artist, this.durationMs);
   }

   @Override
   public String toString() {
      return "TrackInfo{title='"
         + this.title
         + "', artist='"
         + this.artist
         + "', pos="
         + this.positionMs
         + ", dur="
         + this.durationMs
         + ", playing="
         + this.isPlaying
         + "}";
   }
}
