package com.lyricsplus.keybinds;

import com.lyricsplus.config.LyricsConfig;
import com.lyricsplus.gui.LyricsConfigScreen;
import com.lyricsplus.media.MediaManager;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.InputUtil.Type;
import net.minecraft.text.Text;

public class LyricsKeybinds {
   public static final String CATEGORY = "category.lyricsplus.main";
   public static KeyBinding toggleHudKey;
   public static KeyBinding openConfigKey;
   public static KeyBinding playPauseKey;
   public static KeyBinding nextTrackKey;
   public static KeyBinding prevTrackKey;
   public static KeyBinding seekBackKey;
   public static KeyBinding seekForwardKey;
   private static boolean wasUpPressed = false;
   private static boolean wasDownPressed = false;
   private static boolean wasLeftPressed = false;
   private static boolean wasRightPressed = false;
   private static boolean wasBackslashPressed = false;

   public static void register() {
      toggleHudKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.lyricsplus.toggle_hud", Type.KEYSYM, 265, "category.lyricsplus.main"));
      openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.lyricsplus.open_config", Type.KEYSYM, 264, "category.lyricsplus.main"));
      prevTrackKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.lyricsplus.prev_track", Type.KEYSYM, 263, "category.lyricsplus.main"));
      nextTrackKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.lyricsplus.next_track", Type.KEYSYM, 262, "category.lyricsplus.main"));
      playPauseKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.lyricsplus.play_pause", Type.KEYSYM, 92, "category.lyricsplus.main"));
      seekBackKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.lyricsplus.seek_back", Type.KEYSYM, 91, "category.lyricsplus.main"));
      seekForwardKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.lyricsplus.seek_forward", Type.KEYSYM, 93, "category.lyricsplus.main"));
   }

   public static void handleInput(MinecraftClient var0) {
      if (var0 != null && var0.player != null) {
         boolean var1 = toggleHudKey != null && toggleHudKey.wasPressed();
         boolean var2 = openConfigKey != null && openConfigKey.wasPressed();
         boolean var3 = prevTrackKey != null && prevTrackKey.wasPressed();
         boolean var4 = nextTrackKey != null && nextTrackKey.wasPressed();
         boolean var5 = playPauseKey != null && playPauseKey.wasPressed();
         if (var0.currentScreen == null && var0.getWindow() != null) {
            long var6 = var0.getWindow().getHandle();
            boolean var8 = var0.mouse == null || var0.mouse.isCursorLocked();
            if (var8) {
               boolean var9 = InputUtil.isKeyPressed(var6, 265);
               if (var9 && !wasUpPressed) {
                  var1 = true;
               }

               wasUpPressed = var9;
               boolean var10 = InputUtil.isKeyPressed(var6, 264);
               if (var10 && !wasDownPressed) {
                  var2 = true;
               }

               wasDownPressed = var10;
               boolean var11 = InputUtil.isKeyPressed(var6, 263);
               if (var11 && !wasLeftPressed) {
                  var3 = true;
               }

               wasLeftPressed = var11;
               boolean var12 = InputUtil.isKeyPressed(var6, 262);
               if (var12 && !wasRightPressed) {
                  var4 = true;
               }

               wasRightPressed = var12;
               boolean var13 = InputUtil.isKeyPressed(var6, 92);
               if (var13 && !wasBackslashPressed) {
                  var5 = true;
               }

               wasBackslashPressed = var13;
            } else {
               wasUpPressed = false;
               wasDownPressed = false;
               wasLeftPressed = false;
               wasRightPressed = false;
               wasBackslashPressed = false;
            }
         }

         if (var1) {
            LyricsConfig var14 = LyricsConfig.getInstance();
            var14.showLyricsHud = !var14.showLyricsHud;
            LyricsConfig.save();
         }

         if (var2 && var0.currentScreen == null) {
            var0.setScreen(new LyricsConfigScreen(null));
         }

         if (var3) {
            MediaManager.getInstance().skipPrevious();
         }

         if (var4) {
            MediaManager.getInstance().skipNext();
         }

         if (var5) {
            MediaManager.getInstance().togglePlayPause();
         }

         if (seekBackKey != null && seekBackKey.wasPressed()) {
            MediaManager.getInstance().seekRelative(-5000L);
            if (var0.player != null) {
               var0.player.sendMessage(Text.literal("§6[LyricsPlus] §f⏪ -5s"), true);
            }
         }

         if (seekForwardKey != null && seekForwardKey.wasPressed()) {
            MediaManager.getInstance().seekRelative(5000L);
            if (var0.player != null) {
               var0.player.sendMessage(Text.literal("§6[LyricsPlus] §f⏩ +5s"), true);
            }
         }
      }
   }
}
