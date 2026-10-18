package com.lyricsplus.lyrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LrcParser {
   private static final Pattern TIMESTAMP_PATTERN = Pattern.compile("\\[(\\d{1,2}):(\\d{2})(?:[.:](\\d{1,3}))?\\]");
   private static final Pattern OFFSET_PATTERN = Pattern.compile("\\[offset:\\s*([+-]?\\d+)\\]", 2);

   public static List<LyricLine> parse(String var0) {
      if (var0 != null && !var0.isBlank()) {
         ArrayList var1 = new ArrayList();
         long var2 = 0L;
         String[] var4 = var0.split("\\r?\\n");

         for (String var8 : var4) {
            String var9 = var8.trim();
            if (!var9.isEmpty()) {
               Matcher var10 = OFFSET_PATTERN.matcher(var9);
               if (var10.find()) {
                  try {
                     var2 = Long.parseLong(var10.group(1));
                  } catch (NumberFormatException var23) {
                  }
               } else {
                  Matcher var11 = TIMESTAMP_PATTERN.matcher(var9);
                  ArrayList var12 = new ArrayList();

                  int var13;
                  for (var13 = 0; var11.find(); var13 = var11.end()) {
                     long var14 = Long.parseLong(var11.group(1));
                     long var16 = Long.parseLong(var11.group(2));
                     long var18 = 0L;
                     String var20 = var11.group(3);
                     if (var20 != null) {
                        if (var20.length() == 1) {
                           var18 = Long.parseLong(var20) * 100L;
                        } else if (var20.length() == 2) {
                           var18 = Long.parseLong(var20) * 10L;
                        } else {
                           var18 = Long.parseLong(var20.substring(0, 3));
                        }
                     }

                     long var21 = var14 * 60L * 1000L + var16 * 1000L + var18;
                     var12.add(var21);
                  }

                  if (!var12.isEmpty()) {
                     String var24 = var9.substring(var13).trim();

                     for (long var25 : var12) {
                        var1.add(new LyricLine(Math.max(0L, var25 + var2), var24));
                     }
                  }
               }
            }
         }

         Collections.sort(var1);
         return var1;
      } else {
         return Collections.emptyList();
      }
   }
}
