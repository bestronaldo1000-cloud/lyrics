package com.lyricsplus.media;

public interface MediaProvider {
   void init();

   TrackInfo pollTrack();

   void skipNext();

   void skipPrevious();

   void togglePlayPause();

   void close();
}
