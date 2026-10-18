package com.lyricsplus.lyrics;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.regex.Pattern;
import net.fabricmc.loader.api.FabricLoader;

public class LrclibClient {
   private static final String API_BASE = "https://lrclib.net/api";
   private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(6L)).build();
   private static final File CACHE_DIR = new File(FabricLoader.getInstance().getConfigDir().toFile(), "lyricsplus/cache");
   private static final Pattern CLEAN_PATTERN = Pattern.compile(
      "\\(.*?((?i)official|video|audio|lyrics|hd|4k|remix|feat|ft\\.).*?\\)|\\[.*?((?i)official|video|audio|lyrics|hd|4k|remix|feat|ft\\.).*?\\]|\\|.*$", 2
   );

   public static CompletableFuture<String> fetchSyncedLyrics(String var0, String var1, String var2, double var3) {
      if (var0 != null && !var0.isBlank()) {
         String var5 = cleanTitle(var0);
         String var6 = cleanTitle(var1);
         if (var6.isBlank() && var5.contains(" - ")) {
            String[] var7 = var5.split(" - ", 2);
            var6 = var7[0].trim();
            var5 = var7[1].trim();
         }

         String var14 = sanitizeFileName(var6 + "_" + var5 + ".lrc");
         File var8 = new File(CACHE_DIR, var14);
         if (var8.exists()) {
            try {
               String var9 = Files.readString(var8.toPath(), StandardCharsets.UTF_8);
               if (!var9.isBlank()) {
                  return CompletableFuture.completedFuture(var9);
               }
            } catch (Exception var13) {
            }
         }

         StringBuilder var15 = new StringBuilder("https://lrclib.net/api").append("/get?track_name=").append(URLEncoder.encode(var5, StandardCharsets.UTF_8));
         if (!var6.isBlank()) {
            var15.append("&artist_name=").append(URLEncoder.encode(var6, StandardCharsets.UTF_8));
         }

         HttpRequest var10 = HttpRequest.newBuilder()
            .uri(URI.create(var15.toString()))
            .header("User-Agent", "LyricsPlus-Minecraft-Mod/1.0.0")
            .timeout(Duration.ofSeconds(6L))
            .GET()
            .build();
         String var11 = var5;
         String var12 = var6;
         return HTTP_CLIENT.sendAsync(var10, BodyHandlers.ofString()).thenCompose(var5x -> {
            if (var5x.statusCode() == 200) {
               try {
                  JsonObject var6x = new JsonParser().parse(var5x.body()).getAsJsonObject();
                  if (var6x.has("syncedLyrics") && !var6x.get("syncedLyrics").isJsonNull()) {
                     String var7x = var6x.get("syncedLyrics").getAsString();
                     if (!var7x.isBlank()) {
                        saveToCache(var8, var7x);
                        return CompletableFuture.completedFuture(var7x);
                     }
                  }
               } catch (Exception var8x) {
               }
            }

            return !var12.isBlank() ? trySwappedExactGet(var12, var11, var3, var8) : searchRankedFallback(var11, var12, var3, var8);
         }).exceptionally(var0x -> null);
      } else {
         return CompletableFuture.completedFuture(null);
      }
   }

   private static CompletableFuture<String> trySwappedExactGet(String var0, String var1, double var2, File var4) {
      StringBuilder var5 = new StringBuilder("https://lrclib.net/api")
         .append("/get?track_name=")
         .append(URLEncoder.encode(var0, StandardCharsets.UTF_8))
         .append("&artist_name=")
         .append(URLEncoder.encode(var1, StandardCharsets.UTF_8));
      HttpRequest var6 = HttpRequest.newBuilder()
         .uri(URI.create(var5.toString()))
         .header("User-Agent", "LyricsPlus-Minecraft-Mod/1.0.0")
         .timeout(Duration.ofSeconds(4L))
         .GET()
         .build();
      return HTTP_CLIENT.sendAsync(var6, BodyHandlers.ofString()).handle((var5x, var6x) -> {
         if (var5x != null && var5x.statusCode() == 200) {
            try {
               JsonObject var7 = new JsonParser().parse(var5x.body()).getAsJsonObject();
               if (var7.has("syncedLyrics") && !var7.get("syncedLyrics").isJsonNull()) {
                  String var8 = var7.get("syncedLyrics").getAsString();
                  if (!var8.isBlank()) {
                     saveToCache(var4, var8);
                     return CompletableFuture.completedFuture(var8);
                  }
               }
            } catch (Exception var9) {
            }
         }

         return searchRankedFallback(var1, var0, var2, var4);
      }).thenCompose(var0x -> (CompletionStage<String>)var0x);
   }

   private static CompletableFuture<String> searchRankedFallback(String var0, String var1, double var2, File var4) {
      String var5 = !var1.isBlank() ? var0 + " " + var1 : var0;
      String var6 = "https://lrclib.net/api/search?q=" + URLEncoder.encode(var5, StandardCharsets.UTF_8);
      HttpRequest var7 = HttpRequest.newBuilder()
         .uri(URI.create(var6))
         .header("User-Agent", "LyricsPlus-Minecraft-Mod/1.0.0")
         .timeout(Duration.ofSeconds(6L))
         .GET()
         .build();
      return HTTP_CLIENT.sendAsync(var7, BodyHandlers.ofString()).thenApply(var5x -> {
         if (var5x.statusCode() == 200) {
            try {
               JsonArray var6x = new JsonParser().parse(var5x.body()).getAsJsonArray();
               JsonObject var7x = null;
               double var8 = -999999.0;
               String var10 = var0.toLowerCase();
               String var11 = var1.toLowerCase();

               for (JsonElement var13 : var6x) {
                  if (var13.isJsonObject()) {
                     JsonObject var14 = var13.getAsJsonObject();
                     if (var14.has("syncedLyrics") && !var14.get("syncedLyrics").isJsonNull()) {
                        double var15 = 100.0;
                        double var17 = var14.has("duration") ? var14.get("duration").getAsDouble() : 0.0;
                        if (var2 > 0.0 && var17 > 0.0) {
                           double var19 = Math.abs(var17 - var2);
                           if (var19 < 4.0) {
                              var15 += 30.0;
                           } else if (var19 < 12.0) {
                              var15 += 15.0;
                           } else {
                              var15 -= Math.min(25.0, var19 * 0.8);
                           }
                        }

                        String var23 = var14.has("trackName") ? var14.get("trackName").getAsString().toLowerCase() : "";
                        String var20 = var14.has("artistName") ? var14.get("artistName").getAsString().toLowerCase() : "";
                        if (var23.equals(var10) || var23.equals(var11)) {
                           var15 += 100.0;
                        } else if (var23.contains(var10) || var10.contains(var23) || var23.contains(var11) || var11.contains(var23)) {
                           var15 += 60.0;
                        }

                        if (!var1.isBlank()) {
                           if (var20.equals(var11) || var20.equals(var10)) {
                              var15 += 80.0;
                           } else if (var20.contains(var11) || var11.contains(var20) || var20.contains(var10) || var10.contains(var20)) {
                              var15 += 40.0;
                           }
                        }

                        if (var15 > var8) {
                           var8 = var15;
                           var7x = var14;
                        }
                     }
                  }
               }

               if (var7x != null && var7x.has("syncedLyrics")) {
                  String var22 = var7x.get("syncedLyrics").getAsString();
                  if (!var22.isBlank()) {
                     saveToCache(var4, var22);
                     return var22;
                  }
               }
            } catch (Exception var21) {
            }
         }

         return null;
      }).exceptionally(var0x -> null);
   }

   public static String cleanTitle(String var0) {
      if (var0 == null) {
         return "";
      }

      String var1 = CLEAN_PATTERN.matcher(var0).replaceAll("");
      return var1.trim();
   }

   private static void saveToCache(File var0, String var1) {
      try (FileWriter var2 = new FileWriter(var0, StandardCharsets.UTF_8)) {
         var2.write(var1);
      } catch (Exception var7) {
      }
   }

   private static String sanitizeFileName(String var0) {
      return var0.replaceAll("[^a-zA-Z0-9._-]", "_");
   }

   static {
      if (!CACHE_DIR.exists()) {
         CACHE_DIR.mkdirs();
      }
   }
}
