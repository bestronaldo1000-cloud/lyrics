package com.lyricsplus.render;

import com.lyricsplus.config.LyricsConfig;
import com.lyricsplus.lyrics.LyricLine;
import com.lyricsplus.lyrics.LyricsManager;
import com.lyricsplus.media.TrackInfo;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class LyricsHudRenderer {
   public static void render(DrawContext var0, TrackInfo var1, LyricsConfig var2, float var3) {
      if (var2.showLyricsHud && var1 != null && var1.isValidTrack()) {
         if (!var2.autoHide || var1.isPlaying()) {
            if (!var2.renderIn3D) {
               MinecraftClient var4 = MinecraftClient.getInstance();
               TextRenderer var5 = var4.textRenderer;
               int var6 = var4.getWindow().getScaledWidth();
               int var7 = var4.getWindow().getScaledHeight();
               long var8 = var1.getCurrentEstimatedPositionMs(var2.timeOffsetMs);
               LyricsManager var10 = LyricsManager.getInstance();
               if (!var10.hasLyrics()) {
                  if (var10.isLoading()) {
                     renderSingleLine(var0, var5, "§8Fetching lyrics...", var6, var7, var2);
                  }
               } else {
                  LyricLine var11 = var10.getCurrentLine(var8);
                  if (var11 != null && !var11.text().isBlank()) {
                     String var12 = var2.hudStyle != null ? var2.hudStyle.toUpperCase() : "BORDERLESS";
                     boolean var13 = "COMPACT".equals(var12) || var2.compactMode;
                     int var14 = var13 ? Math.min(2, var2.lyricsLineCount) : Math.max(2, Math.min(6, var2.lyricsLineCount));
                     List var15 = var10.getUpcomingLines(var8, var14 - 1);
                     float var16 = var2.playerWidgetScale > 0.4F ? var2.playerWidgetScale : 1.0F;
                     int var17 = (int)(250.0F * var16);
                     int var18 = (int)((var13 ? 18 : 46) * var16);
                     int var19 = var13 ? 10 : 13;
                     int var20 = (int)((var14 * var19 + 4) * var16);
                     byte var21 = 10;
                     int var23 = var21 + var18 + 2;
                     LyricsConfig.WidgetPosition var24 = var2.playerWidgetPosition != null ? var2.playerWidgetPosition : LyricsConfig.WidgetPosition.TOP_LEFT;
                     int var22;
                     switch (var24) {
                        case TOP_LEFT:
                           var22 = var21;
                           var23 = var21 + var18 + 2;
                           break;
                        case TOP_RIGHT:
                           var22 = var6 - var17 - var21;
                           var23 = var21 + var18 + 2;
                           break;
                        case BOTTOM_LEFT:
                           var22 = var21;
                           var23 = var7 - var18 - var21 - var20 - 2;
                           break;
                        case BOTTOM_RIGHT:
                           var22 = var6 - var17 - var21;
                           var23 = var7 - var18 - var21 - var20 - 2;
                           break;
                        case CENTER:
                           var22 = (var6 - var17) / 2;
                           var23 = (var7 - var18) / 2 + var18 + 2;
                           break;
                        default:
                           var22 = var21;
                           var23 = var21 + var18 + 2;
                     }

                     int var25 = PlayerWidgetRenderer.resolveAccentColor(var2, var1);
                     int var26 = var25 & 16777215;
                     if ("BORDERED".equals(var12) && !var13) {
                        int var27 = Math.max(10, Math.min(255, (int)(var2.playerWidgetAlpha * 255.0F)));
                        int var28 = var27 << 24 | 1185312;
                        var0.fill(var22, var23, var22 + var17, var23 + var20, var28);
                        var0.fill(var22, var23, var22 + 3, var23 + var20, 0xFF000000 | var26);
                        var23 += 4;
                        var22 += 8;
                     }

                     String var32 = var11.text();
                     var0.drawTextWithShadow(var5, "§l" + var32, var22, var23, 0xFF000000 | var26);
                     int var33 = var23 + var19;

                     for (LyricLine var30 : var15) {
                        var0.drawTextWithShadow(var5, var30.text(), var22, var33, -2001684284);
                        var33 += var19;
                     }
                  }
               }
            }
         }
      }
   }

   private static void renderSingleLine(DrawContext var0, TextRenderer var1, String var2, int var3, int var4, LyricsConfig var5) {
      byte var6 = 10;
      byte var7 = 62;
      var0.drawTextWithShadow(var1, var2, var6, var7, -7829368);
   }
}
