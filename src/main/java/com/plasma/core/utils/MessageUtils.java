package com.plasma.core.utils;

import com.plasma.core.PlasmaCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

public class MessageUtils {

    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    public static Component color(String message) {
        return SERIALIZER.deserialize(message);
    }

    public static String colorString(String message) {
        return message.replace("&", "§");
    }

    public static void send(Player player, String key, String... replacements) {
        String prefix = PlasmaCore.getInstance().getConfig().getString("messages.prefix", "");
        String message = PlasmaCore.getInstance().getConfig().getString("messages." + key, "&cMessage not found: " + key);
        
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace("{" + replacements[i] + "}", replacements[i + 1]);
            }
        }
        
        player.sendMessage(color(prefix + message));
    }

    public static void sendRaw(Player player, String message) {
        player.sendMessage(color(message));
    }

    public static String get(String key, String... replacements) {
        String message = PlasmaCore.getInstance().getConfig().getString("messages." + key, "");
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace("{" + replacements[i] + "}", replacements[i + 1]);
            }
        }
        return message;
    }
}
