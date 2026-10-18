package com.lyricsplus.render;

import com.lyricsplus.config.LyricsConfig;
import com.lyricsplus.media.TrackInfo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;

public class PlayerWidgetRenderer {
   public static final int BASE_WIDGET_WIDTH = 250;
   public static final int BASE_WIDGET_HEIGHT = 46;

   public static void render(DrawContext var0, TrackInfo var1, LyricsConfig var2, float var3) {
      if (var2.showPlayerWidget && var1 != null && var1.isValidTrack()) {
         if (!var2.autoHide || var1.isPlaying()) {
            MinecraftClient var4 = MinecraftClient.getInstance();
            TextRenderer var5 = var4.textRenderer;
            int var6 = var4.getWindow().getScaledWidth();
            int var7 = var4.getWindow().getScaledHeight();
            long var8 = System.currentTimeMillis();
            String var10 = var2.hudStyle != null ? var2.hudStyle.toUpperCase() : "BORDERLESS";
            boolean var11 = "COMPACT".equals(var10) || var2.compactMode;
            float var12 = var2.playerWidgetScale > 0.4F ? var2.playerWidgetScale : 1.0F;
            int var13 = (int)(250.0F * var12);
            int var14 = (int)((var11 ? 18 : 46) * var12);
            byte var15 = 10;
            LyricsConfig.WidgetPosition var18 = var2.playerWidgetPosition != null ? var2.playerWidgetPosition : LyricsConfig.WidgetPosition.TOP_LEFT;
            int var16;
            int var17;
            switch (var18) {
               case TOP_LEFT:
                  var16 = var15;
                  var17 = var15;
                  break;
               case TOP_RIGHT:
                  var16 = var6 - var13 - var15;
                  var17 = var15;
                  break;
               case BOTTOM_LEFT:
                  var16 = var15;
                  var17 = var7 - var14 - var15;
                  break;
               case BOTTOM_RIGHT:
                  var16 = var6 - var13 - var15;
                  var17 = var7 - var14 - var15;
                  break;
               case CENTER:
                  var16 = (var6 - var13) / 2;
                  var17 = var15;
                  break;
               default:
                  var16 = var15;
                  var17 = var15;
            }

            int var19 = resolveAccentColor(var2, var1);
            int var20 = var19 & 16777215;
            if ("BORDERLESS".equals(var10) && !var11) {
               byte var37 = 36;
               int var39 = var16 + 2;
               int var41 = var17 + 2;
               Identifier var43 = TextureHelper.getCoverArtTexture(var1.getAlbumArtBytes(), var1.getAlbumArtUrl());
               if (var43 != null) {
                  var0.fill(var39 + 1, var41 + 1, var39 + var37 + 2, var41 + var37 + 2, 1426063360);
                  var0.drawTexture(RenderLayer::getGuiTextured, var43, var39, var41, 0.0F, 0.0F, var37, var37, var37, var37);
               } else {
                  var0.fill(var39, var41, var39 + var37, var41 + var37, -1726933470);
                  var0.fill(var39, var41, var39 + 2, var41 + var37, 0xFF000000 | var20);
                  var0.drawTextWithShadow(var5, "♫", var39 + 13, var41 + 14, 0xFF000000 | var20);
               }

               int var45 = var39 + var37 + 8;
               int var47 = var13 - (var37 + 12);
               String var49 = var1.getTitle();
               int var51 = var5.getWidth(var49);
               if (var51 > var47) {
                  var49 = var5.trimToWidth(var49, var47 - 8) + "…";
               }

               var0.drawTextWithShadow(var5, var49, var45, var41 + 2, -1);
               String var53 = var1.getArtist();
               int var55 = var5.getWidth(var53);
               if (var55 > var47 - 32) {
                  var53 = var5.trimToWidth(var53, var47 - 38) + "…";
               }

               var0.drawTextWithShadow(var5, var53, var45, var41 + 15, -5195580);
               int var57 = var16 + var13 - 22;
               int var59 = var41 + 13;
               renderEqualizer(var0, var1.isPlaying(), var8, var57, var59, var20);
               int var60 = var45;
               int var61 = var41 + 27;
               int var62 = var13 - (var37 + 14);
               renderProgressBar(var0, var1, var2, var60, var61, var62, var20);
            } else if (var11) {
               byte var36 = 14;
               int var38 = var16;
               int var40 = var17 + 1;
               Identifier var42 = TextureHelper.getCoverArtTexture(var1.getAlbumArtBytes(), var1.getAlbumArtUrl());
               if (var42 != null) {
                  var0.drawTexture(RenderLayer::getGuiTextured, var42, var38, var40, 0.0F, 0.0F, var36, var36, var36, var36);
               } else {
                  var0.fill(var38, var40, var38 + var36, var40 + var36, -1441720798);
                  var0.drawTextWithShadow(var5, "♫", var38 + 3, var40 + 3, 0xFF000000 | var20);
               }

               int var44 = var38 + var36 + 5;
               int var46 = var13 - (var36 + 44);
               String var48 = var1.getTitle() + " §7• " + var1.getArtist();
               if (var5.getWidth(var48) > var46) {
                  var48 = var5.trimToWidth(var48, var46 - 6) + "…";
               }

               var0.drawTextWithShadow(var5, var48, var44, var17 + 3, -1);
               int var50 = var16 + var13 - 16;
               int var52 = var17 + 2;
               renderEqualizer(var0, var1.isPlaying(), var8, var50, var52, var20);
               int var54 = var44;
               int var56 = var17 + 14;
               int var58 = var13 - (var36 + 22);
               renderProgressBar(var0, var1, var2, var54, var56, var58, var20);
            } else {
               int var21 = Math.max(10, Math.min(255, (int)(var2.playerWidgetAlpha * 255.0F)));
               int var22 = var21 << 24 | 1185312;
               var0.fill(var16, var17, var16 + var13, var17 + var14, var22);
               var0.fill(var16, var17, var16 + 3, var17 + var14, 0xFF000000 | var20);
               var0.fill(var16 + 3, var17, var16 + var13, var17 + 1, 872415231);
               var0.fill(var16 + 3, var17 + var14 - 1, var16 + var13, var17 + var14, 570425344);
               byte var23 = 34;
               int var24 = var16 + 8;
               int var25 = var17 + (var14 - var23) / 2;
               Identifier var26 = TextureHelper.getCoverArtTexture(var1.getAlbumArtBytes(), var1.getAlbumArtUrl());
               if (var26 != null) {
                  var0.drawTexture(RenderLayer::getGuiTextured, var26, var24, var25, 0.0F, 0.0F, var23, var23, var23, var23);
               } else {
                  var0.fill(var24, var25, var24 + var23, var25 + var23, -1725946315);
                  var0.drawTextWithShadow(var5, "♫", var24 + 12, var25 + 13, 0xFF000000 | var20);
               }

               int var27 = var24 + var23 + 8;
               int var28 = var13 - (var23 + 22);
               String var29 = var1.getTitle();
               if (var5.getWidth(var29) > var28) {
                  var29 = var5.trimToWidth(var29, var28 - 8) + "…";
               }

               var0.drawTextWithShadow(var5, var29, var27, var25 + 2, -1);
               String var30 = var1.getArtist();
               if (var5.getWidth(var30) > var28 - 30) {
                  var30 = var5.trimToWidth(var30, var28 - 36) + "…";
               }

               var0.drawTextWithShadow(var5, var30, var27, var25 + 14, -6377793);
               int var31 = var16 + var13 - 20;
               int var32 = var25 + 12;
               renderEqualizer(var0, var1.isPlaying(), var8, var31, var32, var20);
               int var33 = var27;
               int var34 = var25 + 26;
               int var35 = var13 - (var23 + 20);
               renderProgressBar(var0, var1, var2, var33, var34, var35, var20);
            }
         }
      }
   }

   private static void renderEqualizer(DrawContext var0, boolean var1, long var2, int var4, int var5, int var6) {
      int var7 = 0xFF000000 | var6;

      for (int var8 = 0; var8 < 3; var8++) {
         int var9 = var1 ? (int)(3.0 + Math.abs(Math.sin(var2 / 160.0 + var8 * 1.2)) * 8.0) : 2;
         int var10 = var4 + var8 * 4;
         int var11 = var5 + (10 - var9);
         var0.fill(var10, var11, var10 + 2, var11 + var9, var7);
      }
   }

   private static void renderProgressBar(DrawContext var0, TrackInfo var1, LyricsConfig var2, int var3, int var4, int var5, int var6) {
      if (var5 > 0) {
         long var7 = var1.getDurationMs();
         long var9 = var1.getCurrentEstimatedPositionMs(var2.timeOffsetMs);
         float var11 = var7 > 0L ? Math.min(1.0F, (float)var9 / (float)var7) : 0.0F;
         var0.fill(var3, var4, var3 + var5, var4 + 2, 1157627903);
         int var12 = (int)(var5 * var11);
         if (var12 > 0) {
            var0.fill(var3, var4, var3 + var12, var4 + 2, 0xFF000000 | var6);
         }
      }
   }

   public static int resolveAccentColor(LyricsConfig var0, TrackInfo var1) {
      String var2 = var0.accentColor != null ? var0.accentColor.toUpperCase() : "ALBUM";
      if ("ALBUM".equals(var2) && var1 != null) {
         int var5 = TextureHelper.getMoodColorForTrack(var1.getAlbumArtBytes(), var1.getTitle(), var1.getArtist());
         return 0xFF000000 | var5 & 16777215;
      }

      return switch (var2) {
         case "WHITE" -> -1;
         case "CYAN" -> -16718337;
         case "GOLD" -> -19712;
         case "PURPLE" -> -2817799;
         case "RED" -> -59580;
         default -> -13710223;
      };
   }
}
