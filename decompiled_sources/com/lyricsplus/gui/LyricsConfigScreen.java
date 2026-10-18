package com.lyricsplus.gui;

import com.lyricsplus.config.LyricsConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class LyricsConfigScreen extends Screen {
   private final Screen parent;
   private final LyricsConfig config;
   private static int activeTab = 0;

   public LyricsConfigScreen(Screen var1) {
      super(Text.literal("LyricsPlus Settings"));
      this.parent = var1;
      this.config = LyricsConfig.getInstance();
   }

   protected void init() {
      int var1 = this.width / 2;
      byte var2 = 76;
      int var3 = var2 * 4 + 12;
      int var4 = var1 - var3 / 2;
      String[] var5 = new String[]{"Display", "Appearance", "Lyrics", "3D & Flight"};

      for (int var6 = 0; var6 < 4; var6++) {
         int var7 = var6;
         boolean var8 = activeTab == var7;
         String var9 = (var8 ? "§a§l" : "§7") + var5[var6];
         this.addDrawableChild(ButtonWidget.builder(Text.literal(var9), var2x -> {
            activeTab = var7;
            this.clearChildren();
            this.init();
         }).dimensions(var4 + var6 * (var2 + 4), 12, var2, 18).build());
      }

      byte var12 = 38;
      byte var13 = 22;
      short var14 = 280;
      int var15 = (var14 - 6) / 2;
      int var10 = var1 - var14 / 2;
      if (activeTab == 0) {
         int var11 = var12;
         this.addDrawableChild(ButtonWidget.builder(Text.literal("Style: §e" + this.config.hudStyle), var1x -> {
            if ("BORDERLESS".equalsIgnoreCase(this.config.hudStyle)) {
               this.config.hudStyle = "BORDERED";
               this.config.compactMode = false;
            } else if ("BORDERED".equalsIgnoreCase(this.config.hudStyle)) {
               this.config.hudStyle = "COMPACT";
               this.config.compactMode = true;
            } else {
               this.config.hudStyle = "BORDERLESS";
               this.config.compactMode = false;
            }

            LyricsConfig.save();
            var1x.setMessage(Text.literal("Style: §e" + this.config.hudStyle));
         }).dimensions(var10, var11, var14, 20).build());
         var11 += var13;
         this.addDrawableChild(ButtonWidget.builder(Text.literal("Player Widget: " + (this.config.showPlayerWidget ? "§aON" : "§cOFF")), var1x -> {
            this.config.showPlayerWidget = !this.config.showPlayerWidget;
            LyricsConfig.save();
            var1x.setMessage(Text.literal("Player Widget: " + (this.config.showPlayerWidget ? "§aON" : "§cOFF")));
         }).dimensions(var10, var11, var15, 20).build());
         this.addDrawableChild(ButtonWidget.builder(Text.literal("Lyrics HUD: " + (this.config.showLyricsHud ? "§aON" : "§cOFF")), var1x -> {
            this.config.showLyricsHud = !this.config.showLyricsHud;
            LyricsConfig.save();
            var1x.setMessage(Text.literal("Lyrics HUD: " + (this.config.showLyricsHud ? "§aON" : "§cOFF")));
         }).dimensions(var10 + var15 + 6, var11, var15, 20).build());
         var11 += var13;
         this.addDrawableChild(ButtonWidget.builder(Text.literal("Widget Pos: §b" + this.config.playerWidgetPosition), var1x -> {
            LyricsConfig.WidgetPosition[] var2x = LyricsConfig.WidgetPosition.values();
            int var3x = (this.config.playerWidgetPosition.ordinal() + 1) % var2x.length;
            this.config.playerWidgetPosition = var2x[var3x];
            LyricsConfig.save();
            var1x.setMessage(Text.literal("Widget Pos: §b" + this.config.playerWidgetPosition));
         }).dimensions(var10, var11, var14, 20).build());
      } else if (activeTab == 1) {
         int var18 = var12;
         this.addDrawableChild(ButtonWidget.builder(Text.literal("Accent: §d" + this.config.accentColor), var1x -> {
            String[] var2x = new String[]{"ALBUM", "GREEN", "BLUE", "PURPLE", "GOLD", "RED", "WHITE"};
            int var3x = 0;

            for (int var4x = 0; var4x < var2x.length; var4x++) {
               if (var2x[var4x].equalsIgnoreCase(this.config.accentColor)) {
                  var3x = var4x;
                  break;
               }
            }

            this.config.accentColor = var2x[(var3x + 1) % var2x.length];
            LyricsConfig.save();
            var1x.setMessage(Text.literal("Accent: §d" + this.config.accentColor));
         }).dimensions(var10, var18, var14, 20).build());
         var18 += var13;
         this.addDrawableChild(ButtonWidget.builder(Text.literal("Scale: §f" + String.format("%.1fx", this.config.playerWidgetScale)), var1x -> {
            float var2x = this.config.playerWidgetScale + 0.1F;
            if (var2x > 1.55F) {
               var2x = 0.6F;
            }

            this.config.playerWidgetScale = Math.round(var2x * 10.0F) / 10.0F;
            LyricsConfig.save();
            var1x.setMessage(Text.literal("Scale: §f" + String.format("%.1fx", this.config.playerWidgetScale)));
         }).dimensions(var10, var18, var15, 20).build());
         this.addDrawableChild(ButtonWidget.builder(Text.literal("Opacity: §f" + (int)(this.config.playerWidgetAlpha * 100.0F) + "%"), var1x -> {
            float var2x = this.config.playerWidgetAlpha + 0.15F;
            if (var2x > 1.05F) {
               var2x = 0.2F;
            }

            this.config.playerWidgetAlpha = Math.round(var2x * 100.0F) / 100.0F;
            LyricsConfig.save();
            var1x.setMessage(Text.literal("Opacity: §f" + (int)(this.config.playerWidgetAlpha * 100.0F) + "%"));
         }).dimensions(var10 + var15 + 6, var18, var15, 20).build());
         var18 += var13;
         this.addDrawableChild(ButtonWidget.builder(Text.literal("Auto-Hide Paused: " + (this.config.autoHide ? "§aON" : "§cOFF")), var1x -> {
            this.config.autoHide = !this.config.autoHide;
            LyricsConfig.save();
            var1x.setMessage(Text.literal("Auto-Hide Paused: " + (this.config.autoHide ? "§aON" : "§cOFF")));
         }).dimensions(var10, var18, var14, 20).build());
      } else if (activeTab == 2) {
         byte var21 = var12;
         this.addDrawableChild(ButtonWidget.builder(Text.literal("Lines: §e" + this.config.lyricsLineCount), var1x -> {
            int var2x = this.config.lyricsLineCount + 1;
            if (var2x > 6) {
               var2x = 2;
            }

            this.config.lyricsLineCount = var2x;
            LyricsConfig.save();
            var1x.setMessage(Text.literal("Lines: §e" + this.config.lyricsLineCount));
         }).dimensions(var10, var21, var15, 20).build());
         this.addDrawableChild(ButtonWidget.builder(Text.literal("Sync: §b" + this.config.timeOffsetMs + "ms"), var1x -> {
            this.config.timeOffsetMs += 100L;
            if (this.config.timeOffsetMs > 1000L) {
               this.config.timeOffsetMs = -1000L;
            }

            LyricsConfig.save();
            var1x.setMessage(Text.literal("Sync: §b" + this.config.timeOffsetMs + "ms"));
         }).dimensions(var10 + var15 + 6, var21, var15, 20).build());
      } else if (activeTab == 3) {
         int var22 = var12;
         this.addDrawableChild(ButtonWidget.builder(Text.literal("3D World Lyrics: " + (this.config.renderIn3D ? "§aON" : "§cOFF")), var1x -> {
            this.config.renderIn3D = !this.config.renderIn3D;
            LyricsConfig.save();
            var1x.setMessage(Text.literal("3D World Lyrics: " + (this.config.renderIn3D ? "§aON" : "§cOFF")));
         }).dimensions(var10, var22, var14, 20).build());
         var22 += var13;
         this.addDrawableChild(ButtonWidget.builder(Text.literal("Elytra Flight Assist: " + (this.config.elytraFlightAssist ? "§aON" : "§cOFF")), var1x -> {
            this.config.elytraFlightAssist = !this.config.elytraFlightAssist;
            LyricsConfig.save();
            var1x.setMessage(Text.literal("Elytra Flight Assist: " + (this.config.elytraFlightAssist ? "§aON" : "§cOFF")));
         }).dimensions(var10, var22, var14, 20).build());
      }

      this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), var1x -> this.close()).dimensions(var1 - 100, this.height - 28, 200, 20).build());
   }

   public void render(DrawContext var1, int var2, int var3, float var4) {
      this.renderBackground(var1, var2, var3, var4);
      super.render(var1, var2, var3, var4);
   }

   public void close() {
      if (this.client != null) {
         this.client.setScreen(this.parent);
      }
   }
}
