package com.empcraft.biomes;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.Plugin;

import com.worldcretornica.plotme_core.Plot;
import com.worldcretornica.plotme_core.PlotMeCoreManager;
import com.worldcretornica.plotme_core.PlotMe_Core;
import com.worldcretornica.plotme_core.api.IPlotMe_GeneratorManager;
import com.worldcretornica.plotme_core.bukkit.PlotMe_CorePlugin;
import com.worldcretornica.plotme_core.bukkit.api.BukkitPlayer;
import com.worldcretornica.plotme_core.bukkit.api.BukkitWorld;

public class PlotMeFeature implements Listener {
    public PlotMe_Core plotme;
    public static List<String> commands = Arrays.asList(new String[] { "/plot", "/plotme", "/p", "/plotme:plot", "/plotme:p", "/plotme:plotme" });

    
    
    public PlotMeFeature(Plugin plotme) {
        this.plotme = ((PlotMe_CorePlugin) plotme).getAPI();
    }

    @EventHandler
    public void onPlayerCommandPreprocessEvent(final PlayerCommandPreprocessEvent event) {
        final Player player = event.getPlayer();
        final String message = event.getMessage().toLowerCase();
        final String[] split = message.split(" ", 2);
        if (split.length < 2) {
            return;
        }
        final String cmd = split[0];
        if (commands.contains(cmd)) {
            final String[] args = split[1].split(" ");
            if (args[0].equals("gb") || args[0].equals("generatebiome")) {
                if (!player.hasPermission("plotme.generatebiome")) {
                    Main.sendMessage(player, "&7You are lacking the permission node: &cplotme.generatebiome");
                    event.setCancelled(true);
                    return;
                }

                final Plot plot = PlotMeCoreManager.getInstance().getPlotById(new BukkitPlayer(player));

                if (plot == null) {
                    Main.sendMessage(player, "&cYou are not in a plot");
                    event.setCancelled(true);
                    return;
                }

                final UUID owner = plot.getOwnerId();
                if ((owner == null) || !owner.equals(player.getUniqueId())) {
                    Main.sendMessage(player, "&cYou do not own this plot");
                    event.setCancelled(true);
                    return;
                }

                if (args.length != 2) {
                    Main.sendMessage(player, "&7Invalid syntax, use: &c/plotme gb <biome>");
                    event.setCancelled(true);
                    return;
                }
                
                if (BiomeHandler.isRunning) {
                    String name = player.getName();
                    Main.sendMessage(player, "&cSome user is already executing a biome conversion. We will remind you when this finishes");
                    if (BiomeHandler.runner.equals(name) && !BiomeHandler.runners.contains(name)) {
                        BiomeHandler.runners.add(name);
                    }
                    return;
                }

                Biome biome = null;
                final World world = player.getWorld();
                String worldname = world.getName();
                String id = plot.getId();
                BukkitWorld bukkitWorld = new BukkitWorld(player.getWorld());
                IPlotMe_GeneratorManager genMan = plotme.getGenManager(worldname);
                Location pos1 = new Location(world, genMan.bottomX(id, bukkitWorld), 256, genMan.bottomZ(id, bukkitWorld));
                Location pos2 = new Location(world, genMan.topX(id, bukkitWorld), 256, genMan.topZ(id, bukkitWorld));

                if (args[0].equalsIgnoreCase("auto")) {
                    biome = pos1.getBlock().getBiome();
                }
                else {
                    try {
                        final String match = new StringComparsion(args[1], Biome.values()).getBestMatch();
                        if (!match.equalsIgnoreCase(args[1])) {
                            Main.sendMessage(player, "&7Did you mean: &c" + match);
                            event.setCancelled(true);
                            return;
                        }
                        biome = Biome.valueOf(match);
                    }
                    catch (final Exception e) {
                        e.printStackTrace();
                        Main.sendMessage(player, "&7Did you mean: &cFOREST");
                        event.setCancelled(true);
                        return;
                    }
                }

                final String perm = "plotme.generatebiome." + biome.name().toLowerCase();
                if (!player.hasPermission(perm)) {
                    Main.sendMessage(player, "&7You are lacking the permission node: &c" + perm);
                    event.setCancelled(true);
                    return;
                }
                BiomeHandler.getNewGenerator(biome, new Random(System.nanoTime()).nextLong());
                final int height = 64;
                final BiomeSelection selection = new BiomeSelection(world, pos1, pos2, height);
                BiomeHandler.generate(selection, player, null);

                event.setCancelled(true);
            }
        }
    }
}
