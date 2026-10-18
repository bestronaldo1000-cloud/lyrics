package com.lyricsplus.lyrics;

public record LyricLine(long timestampMs, String text) implements Comparable<LyricLine> {
   public int compareTo(LyricLine var1) {
      return Long.compare(this.timestampMs, var1.timestampMs);
   }
}
