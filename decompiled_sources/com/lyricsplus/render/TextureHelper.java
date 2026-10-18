package com.lyricsplus.render;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

public class TextureHelper {
   private static byte[] lastArtBytes = null;
   private static final Identifier COVER_IDENTIFIER = Identifier.of("lyricsplus", "cover_art_texture");
   private static NativeImageBackedTexture dynamicTexture = null;
   private static boolean hasCoverTexture = false;
   private static int cachedMoodColor = 16777215;
   private static String cachedMoodKey = "";

   public static Identifier getCoverArtTexture(byte[] var0, String var1) {
      if (var0 != null && var0.length != 0) {
         if (lastArtBytes == null || !Arrays.equals(var0, lastArtBytes)) {
            lastArtBytes = (byte[])var0.clone();

            try {
               NativeImage var2 = NativeImage.read(new ByteArrayInputStream(var0));
               MinecraftClient var3 = MinecraftClient.getInstance();
               if (dynamicTexture != null) {
                  try {
                     dynamicTexture.close();
                  } catch (Exception var5) {
                  }

                  dynamicTexture = null;
               }

               dynamicTexture = new NativeImageBackedTexture(var2);
               var3.getTextureManager().registerTexture(COVER_IDENTIFIER, dynamicTexture);
               hasCoverTexture = true;
            } catch (Exception var6) {
               var6.printStackTrace();
            }
         }

         return hasCoverTexture ? COVER_IDENTIFIER : null;
      } else {
         return null;
      }
   }

   public static int getMoodColorForTrack(byte[] var0, String var1, String var2) {
      String var3 = var1 + "_" + var2 + "_" + (var0 != null ? var0.length : 0);
      if (var3.equals(cachedMoodKey)) {
         return cachedMoodColor;
      }

      cachedMoodKey = var3;
      if (var0 != null && var0.length > 0) {
         try {
            NativeImage var4 = NativeImage.read(new ByteArrayInputStream(var0));
            if (var4 != null) {
               int var5 = var4.getWidth();
               int var6 = var4.getHeight();
               long var7 = 0L;
               long var9 = 0L;
               long var11 = 0L;
               int var13 = 0;
               int var14 = Math.max(1, Math.min(var5, var6) / 16);
               int var15 = -1;
               float var16 = 0.0F;

               for (int var17 = 0; var17 < var5; var17 += var14) {
                  for (int var18 = 0; var18 < var6; var18 += var14) {
                     int var19 = var4.getColorArgb(var17, var18);
                     int var20 = var19 >> 24 & 0xFF;
                     if (var20 >= 128) {
                        int var21 = var19 >> 16 & 0xFF;
                        int var22 = var19 >> 8 & 0xFF;
                        int var23 = var19 & 0xFF;
                        var7 += var21;
                        var9 += var22;
                        var11 += var23;
                        var13++;
                        float[] var24 = Color.RGBtoHSB(var21, var22, var23, null);
                        float var25 = var24[1];
                        float var26 = var24[2];
                        if (var26 > 0.3F && var26 < 0.95F && var25 > var16) {
                           var16 = var25;
                           var15 = var21 << 16 | var22 << 8 | var23;
                        }
                     }
                  }
               }

               if (var16 >= 0.25F && var15 != -1) {
                  cachedMoodColor = var15;
               } else if (var13 > 0) {
                  int var28 = (int)(var7 / var13);
                  int var29 = (int)(var9 / var13);
                  int var30 = (int)(var11 / var13);
                  cachedMoodColor = var28 << 16 | var29 << 8 | var30;
               } else {
                  cachedMoodColor = 3066993;
               }

               var4.close();
               return cachedMoodColor;
            }
         } catch (Exception var27) {
         }
      }

      cachedMoodColor = 3066993;
      return cachedMoodColor;
   }
}
