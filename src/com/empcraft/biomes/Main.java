package com.empcraft.biomes;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {

    public static boolean                  canSetFast;
    public static int                      inset;
    public String                          version;
    public static Main                     plugin;
    public static String                   world;
    public static FileConfiguration        config;
    public static HashMap<String, Integer> worldChanged = new HashMap<String, Integer>();
    public static int                      interval;

    private static String colorise(final String mystring) {
        return ChatColor.translateAlternateColorCodes('&', mystring);
    }

    public static void sendMessage(final Player player, final String mystring) {
        if (ChatColor.stripColor(mystring).equals("")) {
            return;
        }
        if (player == null) {
            Bukkit.getServer().getConsoleSender().sendMessage(colorise(mystring));
        }
        else {
            player.sendMessage(colorise(mystring));
        }
    }

    @SuppressWarnings("deprecation")
    public static boolean setBlock(final Block block, final short id, final byte data) {
        if (canSetFast) {
            if ((block.getTypeId() != id) || (data != block.getData())) {
                try {
                    SetBlockFast.set(block.getWorld(), block.getX(), block.getY(), block.getZ(), id, data);
                    return true;
                }
                catch (final Throwable e) {
                    canSetFast = false;
                }
            }
            return false;
        }
        if (block.getData() == data) {
            if (block.getTypeId() != id) {
                block.setTypeId(id);
            }
        }
        else {
            if (block.getTypeId() == id) {
                block.setData(data);
            }
            else {
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
        setupPlotMe();
        setupWorldEdit();
        setupConfig();
        Main.config = this.getConfig();

        try {
            new SetBlockFast();
            new SendChunk();
            Main.canSetFast = true;
        }
        catch (final Exception e) {
            Main.canSetFast = false;
        }
        
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
    }

    private void setupConfig() {
        getConfig().options().copyDefaults(true);
        final Map<String, Object> options = new HashMap<String, Object>();
        getConfig().set("version", this.version);
        options.put("base-generation-off-world", "");
        options.put("populator-inset", 4);
        options.put("ticks-per-chunk", 1);
        for (final Entry<String, Object> node : options.entrySet()) {
            if (!getConfig().contains(node.getKey())) {
                getConfig().set(node.getKey(), node.getValue());
            }
        }
        saveConfig();
        Main.world = getConfig().getString("base-generation-off-world");
        Main.inset = getConfig().getInt("populator-inset");
        Main.interval = getConfig().getInt("ticks-per-chunk");
    }

    private void setupPlotSquared() {
        final Plugin plotsquared = Bukkit.getServer().getPluginManager().getPlugin("PlotSquared");
        if ((plotsquared == null) || !plotsquared.isEnabled()) {
            return;
        }
        sendMessage(null, "&8===&3[&7BiomeGenerator hooked into PlotSquared&3]&8===");
        new PlotSquaredFeature();
    }

    private void setupPlotMe() {
        final Plugin plotme = Bukkit.getServer().getPluginManager().getPlugin("PlotMe");
        if ((plotme == null) || !plotme.isEnabled()) {
            return;
        }
        sendMessage(null, "&8===&3[&7BiomeGenerator hooked into PlotMe&3]&8===");
        Bukkit.getServer().getPluginManager().registerEvents(new PlotMeFeature(plotme), this);
    }

    private void setupWorldEdit() {
        final Plugin worldedit = Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        if ((worldedit == null) || !worldedit.isEnabled()) {
            return;
        }
        sendMessage(null, "&8===&3[&7BiomeGenerator hooked into WorldEdit&3]&8===");
        final WorldEditListener myclass = new WorldEditListener();
        Bukkit.getServer().getPluginManager().registerEvents(myclass, this);
    }
    
    @EventHandler
    public void onChunkUnloaded(ChunkUnloadEvent event) {
        if (BiomeHandler.isRunning) {
            event.setCancelled(true);
        }
    }
}
