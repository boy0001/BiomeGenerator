package com.empcraft.biomes;

import org.bukkit.entity.Player;

public enum BBC {
    FINISHED_NOTIFY("&6The biome conversion by &a%s0 &6has finished!"),
    INTERRUPTED("&aBiome conversion was interrupted!"),
    IN_PROGRESS("&cSome user is already executing a biome conversion. We will remind you when this finishes"),
    ESTIMATE("&6Estimated time: &a%s0 seconds"),
    UNEXPLORED("&cPlease explore the selection fully.");

    public String s;

    BBC(final String s) {
        this.s = s;
    }

    public static void sendMessage(final Player player, final BBC c) {
        Main.sendMessage(player, c.s);
    }

    public static void sendMessage(final Player player, final BBC c, final Object... args) {
        Main.sendMessage(player, format(c, args));
    }

    public static String format(final BBC c, final Object... args) {
        String m = c.s;
        for (int i = args.length - 1; i >= 0; i--) {
            if (args[i] == null) {
                continue;
            }
            m = m.replaceAll("%s" + i, args[i].toString());
        }
        return m;
    }
}
