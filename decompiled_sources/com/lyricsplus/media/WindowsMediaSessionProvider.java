package com.lyricsplus.media;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lyricsplus.lyrics.LrclibClient;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.ptr.IntByReference;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.loader.api.FabricLoader;

public class WindowsMediaSessionProvider implements MediaProvider {
   private static final byte VK_MEDIA_NEXT_TRACK = -80;
   private static final byte VK_MEDIA_PREV_TRACK = -79;
   private static final byte VK_MEDIA_PLAY_PAUSE = -77;
   private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(4L)).build();
   private final AtomicReference<TrackInfo> currentTrack = new AtomicReference<>(null);
   private Thread workerThread;
   private Thread gsmtcThread;
   private volatile Process gsmtcProcess = null;
   private volatile boolean running = false;
   private volatile long lastGsmtcUpdateMs = 0L;
   private String lastDetectedKey = "";
   private long trackStartTimeMs = 0L;
   private long cachedDurationMs = 0L;
   private byte[] cachedArtBytes = null;
   private String cachedAlbumName = "";
   private static final String GSMTC_SCRIPT = "[Console]::OutputEncoding = [System.Text.Encoding]::UTF8\nAdd-Type -AssemblyName System.Runtime.WindowsRuntime\n$asTaskGeneric = [System.WindowsRuntimeSystemExtensions].GetMethods() | ? { $_.Name -eq 'AsTask' -and $_.GetParameters().Count -eq 1 -and $_.GetParameters()[0].ParameterType.Name -eq 'IAsyncOperation`1' }[0]\nfunction Await-WinRT($winRtAsync, $returnType) {\n    if (-not $winRtAsync) { return $null }\n    try {\n        $asTask = $asTaskGeneric.MakeGenericMethod($returnType)\n        $task = $asTask.Invoke($null, @($winRtAsync))\n        if ($task.Wait(1200)) { return $task.Result }\n    } catch {}\n    return $null\n}\n[Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager, Windows.Media, ContentType = WindowsRuntime] | Out-Null\n$manager = Await-WinRT ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager]::RequestAsync()) ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager])\nif (-not $manager) { exit 1 }\nfunction Get-ActiveMediaSession($mgr) {\n    try {\n        $sessions = $mgr.GetSessions()\n        foreach ($s in $sessions) {\n            try {\n                $pInfo = $s.GetPlaybackInfo()\n                if ($pInfo -and $pInfo.PlaybackStatus -eq [Windows.Media.Control.GlobalSystemMediaTransportControlsSessionPlaybackStatus]::Playing) {\n                    $m = Await-WinRT ($s.TryGetMediaPropertiesAsync()) ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionMediaProperties])\n                    if ($m -and $m.Title -and $m.Title -notmatch 'TikTok|GDI\\+|Default IME|MSCTFIME') {\n                        return @($s, $pInfo, $m)\n                    }\n                }\n            } catch {}\n        }\n        foreach ($s in $sessions) {\n            try {\n                $pInfo = $s.GetPlaybackInfo()\n                if ($pInfo -and $pInfo.PlaybackStatus -ne [Windows.Media.Control.GlobalSystemMediaTransportControlsSessionPlaybackStatus]::Closed) {\n                    $m = Await-WinRT ($s.TryGetMediaPropertiesAsync()) ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionMediaProperties])\n                    if ($m -and $m.Title -and $m.Artist -and $m.Title -notmatch 'TikTok|GDI\\+|Default IME|MSCTFIME') {\n                        return @($s, $pInfo, $m)\n                    }\n                }\n            } catch {}\n        }\n        $curr = $mgr.GetCurrentSession()\n        if ($curr) {\n            $pInfo = $curr.GetPlaybackInfo()\n            $m = Await-WinRT ($curr.TryGetMediaPropertiesAsync()) ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionMediaProperties])\n            if ($m -and $m.Title -and $m.Title -notmatch 'TikTok|GDI\\+|Default IME|MSCTFIME') {\n                return @($curr, $pInfo, $m)\n            }\n        }\n    } catch {}\n    return $null\n}\nwhile ($true) {\n    try {\n        $active = Get-ActiveMediaSession $manager\n        if ($active) {\n            $session = $active[0]\n            $info = $active[1]\n            $media = $active[2]\n            $tl = $session.GetTimelineProperties()\n            $status = if ($info) { $info.PlaybackStatus.ToString() } else { 'Closed' }\n            $pos = if ($tl) {\n                if ($status -eq 'Playing' -and $tl.LastUpdatedTime) {\n                    $elapsed = ([DateTimeOffset]::UtcNow - $tl.LastUpdatedTime).TotalMilliseconds\n                    $rate = if ($info.PlaybackRate) { [double]$info.PlaybackRate } else { 1.0 }\n                    [long]($tl.Position.TotalMilliseconds + ($elapsed * $rate))\n                } else {\n                    [long]$tl.Position.TotalMilliseconds\n                }\n            } else { 0 }\n            $dur = if ($tl) { [long]$tl.EndTime.TotalMilliseconds } else { 0 }\n            $title = if ($media -and $media.Title) { $media.Title.Trim() } else { '' }\n            $artist = if ($media -and $media.Artist) { $media.Artist.Trim() } else { '' }\n            $album = if ($media -and $media.AlbumTitle) { $media.AlbumTitle.Trim() } else { '' }\n            [Console]::Out.WriteLine(\"LP|$status|$pos|$dur|$artist|$title|$album\")\n            [Console]::Out.Flush()\n        } else {\n            [Console]::Out.WriteLine('LP|NONE|0|0|||')\n            [Console]::Out.Flush()\n        }\n    } catch {}\n    Start-Sleep -Milliseconds 150\n}\n";

   @Override
   public void init() {
      if (!this.running) {
         this.running = true;
         this.startGsmtcThread();
         this.workerThread = new Thread(() -> {
            while (this.running) {
               try {
                  if (System.currentTimeMillis() - this.lastGsmtcUpdateMs > 800L) {
                     this.pollWindowsMedia();
                  }

                  Thread.sleep(150L);
               } catch (InterruptedException var2) {
                  break;
               } catch (Throwable var3) {
               }
            }
         }, "LyricsPlus-NativeMedia-Worker");
         this.workerThread.setDaemon(true);
         this.workerThread.start();
      }
   }

   private void startGsmtcThread() {
      this.gsmtcThread = new Thread(
         () -> {
            File var1 = this.ensureGsmtcScript();
            if (var1 != null && var1.exists()) {
               while (this.running) {
                  try {
                     ProcessBuilder var2 = new ProcessBuilder(
                        "powershell.exe", "-NoProfile", "-NonInteractive", "-ExecutionPolicy", "Bypass", "-File", var1.getAbsolutePath()
                     );
                     var2.redirectErrorStream(true);
                     this.gsmtcProcess = var2.start();

                     String var4;
                     try (BufferedReader var3 = new BufferedReader(new InputStreamReader(this.gsmtcProcess.getInputStream(), StandardCharsets.UTF_8))) {
                        while (this.running && (var4 = var3.readLine()) != null) {
                           var4 = var4.trim();
                           if (var4.startsWith("LP|")) {
                              String[] var5 = var4.split("\\|", -1);
                              if (var5.length >= 7) {
                                 String var6 = var5[1];
                                 long var7 = this.parseLongSafe(var5[2]);
                                 long var9 = this.parseLongSafe(var5[3]);
                                 String var11 = var5[4].trim();
                                 String var12 = var5[5].trim();
                                 String var13 = var5[6].trim();
                                 if (!var12.isEmpty()) {
                                    this.lastGsmtcUpdateMs = System.currentTimeMillis();
                                    boolean var14 = "Playing".equalsIgnoreCase(var6);
                                    this.handleTrackUpdate(var12, var11, var13, var9, var7, var14);
                                 } else if ("NONE".equals(var6)) {
                                    this.lastGsmtcUpdateMs = 0L;
                                 }
                              }
                           }
                        }
                     }
                  } catch (Throwable var29) {
                  } finally {
                     if (this.gsmtcProcess != null) {
                        try {
                           this.gsmtcProcess.destroyForcibly();
                        } catch (Throwable var25) {
                        }

                        this.gsmtcProcess = null;
                     }
                  }

                  try {
                     Thread.sleep(2000L);
                  } catch (InterruptedException var27) {
                     break;
                  }
               }
            }
         },
         "LyricsPlus-GSMTC-Bridge"
      );
      this.gsmtcThread.setDaemon(true);
      this.gsmtcThread.start();
   }

   private File ensureGsmtcScript() {
      try {
         File var1 = new File(FabricLoader.getInstance().getConfigDir().toFile(), "lyricsplus");
         if (!var1.exists()) {
            var1.mkdirs();
         }

         File var2 = new File(var1, "gsmtc_bridge.ps1");

         try (FileWriter var3 = new FileWriter(var2, StandardCharsets.UTF_8)) {
            var3.write(
               "[Console]::OutputEncoding = [System.Text.Encoding]::UTF8\nAdd-Type -AssemblyName System.Runtime.WindowsRuntime\n$asTaskGeneric = [System.WindowsRuntimeSystemExtensions].GetMethods() | ? { $_.Name -eq 'AsTask' -and $_.GetParameters().Count -eq 1 -and $_.GetParameters()[0].ParameterType.Name -eq 'IAsyncOperation`1' }[0]\nfunction Await-WinRT($winRtAsync, $returnType) {\n    if (-not $winRtAsync) { return $null }\n    try {\n        $asTask = $asTaskGeneric.MakeGenericMethod($returnType)\n        $task = $asTask.Invoke($null, @($winRtAsync))\n        if ($task.Wait(1200)) { return $task.Result }\n    } catch {}\n    return $null\n}\n[Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager, Windows.Media, ContentType = WindowsRuntime] | Out-Null\n$manager = Await-WinRT ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager]::RequestAsync()) ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager])\nif (-not $manager) { exit 1 }\nfunction Get-ActiveMediaSession($mgr) {\n    try {\n        $sessions = $mgr.GetSessions()\n        foreach ($s in $sessions) {\n            try {\n                $pInfo = $s.GetPlaybackInfo()\n                if ($pInfo -and $pInfo.PlaybackStatus -eq [Windows.Media.Control.GlobalSystemMediaTransportControlsSessionPlaybackStatus]::Playing) {\n                    $m = Await-WinRT ($s.TryGetMediaPropertiesAsync()) ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionMediaProperties])\n                    if ($m -and $m.Title -and $m.Title -notmatch 'TikTok|GDI\\+|Default IME|MSCTFIME') {\n                        return @($s, $pInfo, $m)\n                    }\n                }\n            } catch {}\n        }\n        foreach ($s in $sessions) {\n            try {\n                $pInfo = $s.GetPlaybackInfo()\n                if ($pInfo -and $pInfo.PlaybackStatus -ne [Windows.Media.Control.GlobalSystemMediaTransportControlsSessionPlaybackStatus]::Closed) {\n                    $m = Await-WinRT ($s.TryGetMediaPropertiesAsync()) ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionMediaProperties])\n                    if ($m -and $m.Title -and $m.Artist -and $m.Title -notmatch 'TikTok|GDI\\+|Default IME|MSCTFIME') {\n                        return @($s, $pInfo, $m)\n                    }\n                }\n            } catch {}\n        }\n        $curr = $mgr.GetCurrentSession()\n        if ($curr) {\n            $pInfo = $curr.GetPlaybackInfo()\n            $m = Await-WinRT ($curr.TryGetMediaPropertiesAsync()) ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionMediaProperties])\n            if ($m -and $m.Title -and $m.Title -notmatch 'TikTok|GDI\\+|Default IME|MSCTFIME') {\n                return @($curr, $pInfo, $m)\n            }\n        }\n    } catch {}\n    return $null\n}\nwhile ($true) {\n    try {\n        $active = Get-ActiveMediaSession $manager\n        if ($active) {\n            $session = $active[0]\n            $info = $active[1]\n            $media = $active[2]\n            $tl = $session.GetTimelineProperties()\n            $status = if ($info) { $info.PlaybackStatus.ToString() } else { 'Closed' }\n            $pos = if ($tl) {\n                if ($status -eq 'Playing' -and $tl.LastUpdatedTime) {\n                    $elapsed = ([DateTimeOffset]::UtcNow - $tl.LastUpdatedTime).TotalMilliseconds\n                    $rate = if ($info.PlaybackRate) { [double]$info.PlaybackRate } else { 1.0 }\n                    [long]($tl.Position.TotalMilliseconds + ($elapsed * $rate))\n                } else {\n                    [long]$tl.Position.TotalMilliseconds\n                }\n            } else { 0 }\n            $dur = if ($tl) { [long]$tl.EndTime.TotalMilliseconds } else { 0 }\n            $title = if ($media -and $media.Title) { $media.Title.Trim() } else { '' }\n            $artist = if ($media -and $media.Artist) { $media.Artist.Trim() } else { '' }\n            $album = if ($media -and $media.AlbumTitle) { $media.AlbumTitle.Trim() } else { '' }\n            [Console]::Out.WriteLine(\"LP|$status|$pos|$dur|$artist|$title|$album\")\n            [Console]::Out.Flush()\n        } else {\n            [Console]::Out.WriteLine('LP|NONE|0|0|||')\n            [Console]::Out.Flush()\n        }\n    } catch {}\n    Start-Sleep -Milliseconds 150\n}\n"
            );
         }

         return var2;
      } catch (Exception var8) {
         return null;
      }
   }

   private void handleTrackUpdate(String var1, String var2, String var3, long var4, long var6, boolean var8) {
      String var9 = var2 + "_" + var1;
      if (!var9.equals(this.lastDetectedKey)) {
         this.lastDetectedKey = var9;
         this.trackStartTimeMs = System.currentTimeMillis() - var6;
         this.cachedDurationMs = var4 > 0L ? var4 : 180000L;
         this.cachedArtBytes = null;
         this.cachedAlbumName = var3;
         this.fetchMetadataAsync(var1, var2);
      } else {
         if (var4 > 0L) {
            this.cachedDurationMs = var4;
         }

         if (!var3.isEmpty() && this.cachedAlbumName.isEmpty()) {
            this.cachedAlbumName = var3;
         }
      }

      long var10 = this.cachedDurationMs > 0L ? this.cachedDurationMs : 180000L;
      long var12 = Math.min(Math.max(0L, var6), var10);
      TrackInfo var14 = new TrackInfo(var1, var2, this.cachedAlbumName, var10, var12, var8, this.cachedArtBytes, null, System.currentTimeMillis());
      this.currentTrack.set(var14);
   }

   private void pollWindowsMedia() {
      AtomicReference var1 = new AtomicReference(null);
      AtomicReference var2 = new AtomicReference(null);
      User32.INSTANCE
         .EnumWindows(
            (var2x, var3x) -> {
               char[] var4x = new char[512];
               int var5x = User32.INSTANCE.GetWindowText(var2x, var4x, 512);
               if (var5x <= 0) {
                  return true;
               }

               String var6 = Native.toString(var4x).trim();
               if (var6.isEmpty()) {
                  return true;
               }

               IntByReference var7x = new IntByReference();
               User32.INSTANCE.GetWindowThreadProcessId(var2x, var7x);
               int var8 = var7x.getValue();
               ProcessHandle.of(var8)
                  .ifPresent(
                     var3xx -> {
                        String var4xx = var3xx.info().command().orElse("");
                        String var5xx = var4xx.contains("\\") ? var4xx.substring(var4xx.lastIndexOf(92) + 1).toLowerCase() : var4xx.toLowerCase();
                        if (var5xx.equals("spotify.exe")) {
                           if (var6.contains(" - ")
                              && !var6.equalsIgnoreCase("spotify")
                              && !var6.equalsIgnoreCase("spotify free")
                              && !var6.equalsIgnoreCase("spotify premium")
                              && !var6.contains("GDI+")
                              && !var6.startsWith("Angle")
                              && !var6.startsWith("Default IME")) {
                              String[] var6x = var6.split(" - ", 2);
                              var2.set(var6x[0].trim());
                              var1.set(var6x[1].trim());
                           }
                        } else if (var5xx.equals("chrome.exe")
                           || var5xx.equals("msedge.exe")
                           || var5xx.equals("brave.exe")
                           || var5xx.equals("firefox.exe")
                           || var5xx.equals("opera.exe")
                           || var5xx.equals("vivaldi.exe")
                           || var5xx.equals("arc.exe")
                           || var5xx.equals("thorium.exe")
                           || var5xx.equals("floorp.exe")) {
                           int var11 = var6.indexOf("- YouTube Music");
                           if (var11 > 0) {
                              String var7xx = var6.substring(0, var11).trim();
                              if (var7xx.contains(" • ")) {
                                 String[] var8x = var7xx.split(" • ", 2);
                                 var1.set(var8x[0].trim());
                                 var2.set(var8x[1].trim());
                              } else if (var7xx.contains(" - ")) {
                                 String[] var16 = var7xx.split(" - ", 2);
                                 var1.set(var16[0].trim());
                                 var2.set(var16[1].trim());
                              } else if (!var7xx.isBlank()) {
                                 var1.set(var7xx);
                                 var2.set("");
                              }
                           } else if (var6.contains("- Spotify")) {
                              int var12 = var6.indexOf("- Spotify");
                              String var17 = var6.substring(0, var12).trim();
                              if (var17.contains(" - ")) {
                                 String[] var9 = var17.split(" - ", 2);
                                 var2.set(var9[0].trim());
                                 var1.set(var9[1].trim());
                              }
                           } else if (var6.contains("| SoundCloud") || var6.contains("- SoundCloud")) {
                              String var15 = var6.contains("| SoundCloud") ? "| SoundCloud" : "- SoundCloud";
                              String var20 = var6.substring(0, var6.indexOf(var15)).trim();
                              if (var20.contains(" by ")) {
                                 String[] var22 = var20.split(" by ", 2);
                                 var1.set(var22[0].trim());
                                 var2.set(var22[1].trim());
                              } else if (var20.contains(" - ")) {
                                 String[] var23 = var20.split(" - ", 2);
                                 var2.set(var23[0].trim());
                                 var1.set(var23[1].trim());
                              }
                           } else if (var6.contains("- TIDAL")) {
                              String var13 = var6.substring(0, var6.indexOf("- TIDAL")).trim();
                              if (var13.contains(" - ")) {
                                 String[] var18 = var13.split(" - ", 2);
                                 var1.set(var18[0].trim());
                                 var2.set(var18[1].trim());
                              }
                           } else if (var6.contains("- YouTube")) {
                              int var14 = var6.indexOf("- YouTube");
                              String var19 = var6.substring(0, var14).trim();
                              if (var19.contains(" - ")) {
                                 String[] var21 = var19.split(" - ", 2);
                                 var2.set(var21[0].trim());
                                 var1.set(var21[1].trim());
                              } else if (!var19.isBlank()) {
                                 var1.set(var19);
                                 var2.set("");
                              }
                           }
                        } else if (var5xx.equals("applemusic.exe") && var6.contains(" - ") && !var6.equalsIgnoreCase("apple music")) {
                           String[] var10 = var6.split(" - ", 2);
                           var1.set(var10[0].trim());
                           var2.set(var10[1].trim());
                        }
                     }
                  );
               return var1.get() == null;
            },
            null
         );
      String var3 = (String)var1.get();
      String var4 = var2.get() != null ? (String)var2.get() : "";
      if (var3 != null && !var3.isBlank()) {
         long var5 = System.currentTimeMillis();
         long var7 = Math.max(0L, var5 - this.trackStartTimeMs);
         this.handleTrackUpdate(var3, var4, "", this.cachedDurationMs, var7, true);
      } else if (!this.lastDetectedKey.isEmpty() && System.currentTimeMillis() - this.lastGsmtcUpdateMs > 3000L) {
         this.lastDetectedKey = "";
         this.currentTrack.set(null);
      }
   }

   private void fetchMetadataAsync(String var1, String var2) {
      String var3 = LrclibClient.cleanTitle(var1);
      String var4 = LrclibClient.cleanTitle(var2);
      String var5 = !var4.isBlank() ? var4 + " " + var3 : var3;
      String var6 = "https://itunes.apple.com/search?term=" + URLEncoder.encode(var5, StandardCharsets.UTF_8) + "&entity=song&limit=1";
      HttpRequest var7 = HttpRequest.newBuilder()
         .uri(URI.create(var6))
         .header("User-Agent", "LyricsPlus-Mod/1.0.0")
         .timeout(Duration.ofSeconds(4L))
         .GET()
         .build();
      HTTP_CLIENT.sendAsync(var7, BodyHandlers.ofString()).thenAccept(var2x -> {
         boolean var3x = false;
         if (var2x.statusCode() == 200) {
            try {
               JsonObject var4x = new JsonParser().parse(var2x.body()).getAsJsonObject();
               if (var4x.has("results")) {
                  JsonArray var5x = var4x.getAsJsonArray("results");
                  if (var5x.size() > 0) {
                     JsonObject var6x = var5x.get(0).getAsJsonObject();
                     if (var6x.has("trackTimeMillis")) {
                        this.cachedDurationMs = var6x.get("trackTimeMillis").getAsLong();
                     }

                     if (var6x.has("collectionName")) {
                        this.cachedAlbumName = var6x.get("collectionName").getAsString();
                     }

                     if (var6x.has("artworkUrl100")) {
                        String var7x = var6x.get("artworkUrl100").getAsString().replace("100x100bb.jpg", "600x600bb.jpg");
                        this.fetchCoverArtBytes(var7x);
                     }

                     var3x = true;
                  }
               }
            } catch (Exception var8) {
            }
         }

         if (!var3x) {
            this.fetchDeezerMetadataFallback(var5);
         }
      }).exceptionally(var2x -> {
         this.fetchDeezerMetadataFallback(var5);
         return null;
      });
   }

   private void fetchDeezerMetadataFallback(String var1) {
      try {
         String var2 = "https://api.deezer.com/search?q=" + URLEncoder.encode(var1, StandardCharsets.UTF_8) + "&limit=1";
         HttpRequest var3 = HttpRequest.newBuilder()
            .uri(URI.create(var2))
            .header("User-Agent", "LyricsPlus-Mod/1.0.0")
            .timeout(Duration.ofSeconds(4L))
            .GET()
            .build();
         HTTP_CLIENT.sendAsync(var3, BodyHandlers.ofString()).thenAccept(var1x -> {
            if (var1x.statusCode() == 200) {
               try {
                  JsonObject var2x = new JsonParser().parse(var1x.body()).getAsJsonObject();
                  if (var2x.has("data")) {
                     JsonArray var3x = var2x.getAsJsonArray("data");
                     if (var3x.size() > 0) {
                        JsonObject var4x = var3x.get(0).getAsJsonObject();
                        if (var4x.has("duration") && this.cachedDurationMs == 180000L) {
                           this.cachedDurationMs = var4x.get("duration").getAsLong() * 1000L;
                        }

                        if (var4x.has("album")) {
                           JsonObject var5 = var4x.getAsJsonObject("album");
                           if (var5.has("title") && this.cachedAlbumName.isEmpty()) {
                              this.cachedAlbumName = var5.get("title").getAsString();
                           }

                           String var6 = null;
                           if (var5.has("cover_big") && !var5.get("cover_big").isJsonNull()) {
                              var6 = var5.get("cover_big").getAsString();
                           } else if (var5.has("cover_medium") && !var5.get("cover_medium").isJsonNull()) {
                              var6 = var5.get("cover_medium").getAsString();
                           }

                           if (var6 != null && !var6.isBlank()) {
                              this.fetchCoverArtBytes(var6);
                           }
                        }
                     }
                  }
               } catch (Exception var7) {
               }
            }
         }).exceptionally(var0 -> null);
      } catch (Exception var4) {
      }
   }

   private void fetchCoverArtBytes(String var1) {
      HttpRequest var2 = HttpRequest.newBuilder()
         .uri(URI.create(var1))
         .header("User-Agent", "LyricsPlus-Mod/1.0.0")
         .timeout(Duration.ofSeconds(4L))
         .GET()
         .build();
      HTTP_CLIENT.sendAsync(var2, BodyHandlers.ofByteArray()).thenAccept(var1x -> {
         if (var1x.statusCode() == 200) {
            this.cachedArtBytes = var1x.body();
         }
      }).exceptionally(var0 -> null);
   }

   private long parseLongSafe(String var1) {
      try {
         return Long.parseLong(var1.trim());
      } catch (Exception var3) {
         return 0L;
      }
   }

   @Override
   public TrackInfo pollTrack() {
      return this.currentTrack.get();
   }

   @Override
   public void skipNext() {
      this.sendMediaKey((byte)-80);
   }

   @Override
   public void skipPrevious() {
      this.sendMediaKey((byte)-79);
   }

   @Override
   public void togglePlayPause() {
      this.sendMediaKey((byte)-77);
   }

   private void sendMediaKey(byte var1) {
      new Thread(() -> {
         try {
            WindowsMediaSessionProvider.User32Media.INSTANCE.keybd_event(var1, (byte)0, 0, 0);
            Thread.sleep(20L);
            WindowsMediaSessionProvider.User32Media.INSTANCE.keybd_event(var1, (byte)0, 2, 0);
         } catch (Throwable var2) {
         }
      }).start();
   }

   @Override
   public void close() {
      this.running = false;
      if (this.gsmtcProcess != null) {
         try {
            this.gsmtcProcess.destroyForcibly();
         } catch (Throwable var2) {
         }

         this.gsmtcProcess = null;
      }

      if (this.workerThread != null) {
         this.workerThread.interrupt();
      }

      if (this.gsmtcThread != null) {
         this.gsmtcThread.interrupt();
      }
   }

   public interface User32Media extends Library {
      WindowsMediaSessionProvider.User32Media INSTANCE = (WindowsMediaSessionProvider.User32Media)Native.load(
         "user32", WindowsMediaSessionProvider.User32Media.class
      );

      void keybd_event(byte var1, byte var2, int var3, int var4);
   }
}
