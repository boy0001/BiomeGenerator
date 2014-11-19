package com.empcraft.biomes;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    public static boolean                  canSetFast;
    public static boolean                  updateLazy;
    public static int                      inset;
    public String                          version;
    public Main                            plugin;
    public static String                   world;
    public static FileConfiguration        config;
    public static HashMap<String, Integer> worldChanged = new HashMap<String, Integer>();
    public static int interval;

    private static String colorise(final String mystring) {
        return ChatColor.translateAlternateColorCodes('&', mystring);
    }

    public static void sendMessage(final Player player, final String mystring) {
        if (ChatColor.stripColor(mystring).equals("")) {
            return;
        }
        if (player == null) {
            Bukkit.getServer().getConsoleSender().sendMessage(colorise(mystring));
        } else {
            player.sendMessage(colorise(mystring));
        }
    }

    public static boolean setBlock(final Block block, final short id, final byte data) {
        if (canSetFast) {
            if ((block.getTypeId() != id) || (data != block.getData())) {
                try {
                    SetBlockFast.set(block.getWorld(), block.getX(), block.getY(), block.getZ(), id, data);
                    return true;
                } catch (final Throwable e) {
                    canSetFast = false;
                }
            }
            return false;
        }
        if (block.getData() == data) {
            if (block.getTypeId() != id) {
                block.setTypeId(id);
            }
        } else {
            if (block.getTypeId() == id) {
                block.setData(data);
            } else {
                block.setTypeIdAndData(id, data, false);
            }
        }
        return false;
    }

    @Override
    public void onEnable() {
        this.version = getDescription().getVersion();
        this.plugin = this;
        setupPlotSquared();
        setupWorldEdit();
        setupConfig();
        Main.config = this.getConfig();

        try {
            new SetBlockFast();
            Main.canSetFast = true;
        } catch (final Exception e) {
            Main.canSetFast = false;
        }

    }

    private void setupConfig() {
        getConfig().options().copyDefaults(true);
        final Map<String, Object> options = new HashMap<String, Object>();
        getConfig().set("version", this.version);
        options.put("base-generation-off-world", "");
        options.put("populator-inset", 4);
        options.put("ticks-per-chunk", 1);
        options.put("lazy-chunk-updates", false);
        for (final Entry<String, Object> node : options.entrySet()) {
            if (!getConfig().contains(node.getKey())) {
                getConfig().set(node.getKey(), node.getValue());
            }
        }
        saveConfig();
        Main.world = getConfig().getString("base-generation-off-world");
        Main.inset = getConfig().getInt("populator-inset");
        Main.interval = getConfig().getInt("ticks-per-chunk");
        Main.updateLazy = getConfig().getBoolean("lazy-chunk-updates");
    }

    private void setupPlotSquared() {
        final Plugin plotsquared = Bukkit.getServer().getPluginManager().getPlugin("PlotSquared");
        if ((plotsquared == null) || !plotsquared.isEnabled()) {
            sendMessage(null, "&c[PlotBiomes] Could not find PlotSquared! Additional features have been disabled");
            return;
        }
        new PlotSquaredFeature();
    }

    private void setupWorldEdit() {
        final Plugin worldedit = Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        if ((worldedit == null) || !worldedit.isEnabled()) {
            sendMessage(null, "&c[PlotBiomes] Could not find WorldEdit! Additional features have been disabled");
            return;
        }
        final WorldEditListener myclass = new WorldEditListener();
        Bukkit.getServer().getPluginManager().registerEvents(myclass, this);
    }
}
