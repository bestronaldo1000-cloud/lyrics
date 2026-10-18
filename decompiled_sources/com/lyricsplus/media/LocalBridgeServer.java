package com.lyricsplus.media;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class LocalBridgeServer implements MediaProvider {
   public static final int PORT = 8974;
   private HttpServer server;
   private final AtomicReference<TrackInfo> currentTrack = new AtomicReference<>(null);

   @Override
   public void init() {
      try {
         this.server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8974), 0);
         this.server.createContext("/update", new LocalBridgeServer.UpdateHandler());
         this.server.createContext("/status", var0 -> {
            String var1 = "{\"status\":\"ok\",\"mod\":\"LyricsPlus\"}";
            var0.getResponseHeaders().set("Content-Type", "application/json");
            var0.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            var0.sendResponseHeaders(200, var1.getBytes(StandardCharsets.UTF_8).length);

            try (OutputStream var2x = var0.getResponseBody()) {
               var2x.write(var1.getBytes(StandardCharsets.UTF_8));
            }
         });
         this.server.setExecutor(Executors.newSingleThreadExecutor(var0 -> {
            Thread var1 = new Thread(var0, "LyricsPlus-Bridge");
            var1.setDaemon(true);
            return var1;
         }));
         this.server.start();
      } catch (IOException var2) {
         System.err.println("[LyricsPlus] Failed to start LocalBridgeServer: " + var2.getMessage());
      }
   }

   @Override
   public TrackInfo pollTrack() {
      return this.currentTrack.get();
   }

   @Override
   public void skipNext() {
   }

   @Override
   public void skipPrevious() {
   }

   @Override
   public void togglePlayPause() {
   }

   @Override
   public void close() {
      if (this.server != null) {
         this.server.stop(0);
      }
   }

   private class UpdateHandler implements HttpHandler {
      @Override
      public void handle(HttpExchange var1) throws IOException {
         if ("OPTIONS".equalsIgnoreCase(var1.getRequestMethod())) {
            var1.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            var1.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, OPTIONS");
            var1.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
            var1.sendResponseHeaders(204, -1L);
         } else {
            if ("POST".equalsIgnoreCase(var1.getRequestMethod())) {
               try {
                  try (InputStream var2 = var1.getRequestBody()) {
                     String var3 = new String(var2.readAllBytes(), StandardCharsets.UTF_8);
                     JsonObject var4 = new JsonParser().parse(var3).getAsJsonObject();
                     String var5 = var4.has("title") ? var4.get("title").getAsString() : "";
                     String var6 = var4.has("artist") ? var4.get("artist").getAsString() : "";
                     String var7 = var4.has("album") ? var4.get("album").getAsString() : "";
                     long var8 = var4.has("durationMs") ? var4.get("durationMs").getAsLong() : 0L;
                     long var10 = var4.has("positionMs") ? var4.get("positionMs").getAsLong() : 0L;
                     boolean var12 = var4.has("isPlaying") && var4.get("isPlaying").getAsBoolean();
                     String var13 = var4.has("artUrl") ? var4.get("artUrl").getAsString() : null;
                     if (!var5.isBlank()) {
                        TrackInfo var14 = new TrackInfo(var5, var6, var7, var8, var10, var12, null, var13, 0L);
                        LocalBridgeServer.this.currentTrack.set(var14);
                     } else {
                        LocalBridgeServer.this.currentTrack.set(null);
                     }

                     String var23 = "{\"success\":true}";
                     var1.getResponseHeaders().set("Content-Type", "application/json");
                     var1.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                     var1.sendResponseHeaders(200, var23.getBytes(StandardCharsets.UTF_8).length);

                     try (OutputStream var15 = var1.getResponseBody()) {
                        var15.write(var23.getBytes(StandardCharsets.UTF_8));
                     }
                  }

                  return;
               } catch (Exception var22) {
                  var22.printStackTrace();
               }
            }

            var1.sendResponseHeaders(400, -1L);
         }
      }
   }
}
