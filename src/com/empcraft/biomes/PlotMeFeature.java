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

import com.worldcretornica.plotme.Plot;
import com.worldcretornica.plotme.PlotManager;

public class PlotMeFeature implements Listener {

    public static List<String> commands = Arrays.asList(new String[] { "/plot", "/plotme", "/p", "/plotme:plot", "/plotme:p", "/plotme:plotme" });

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

                final Location loc = player.getLocation();
                final Plot plotid = PlotManager.getPlotById(loc);

                if (plotid == null) {
                    Main.sendMessage(player, "&cYou are not in a plot");
                    event.setCancelled(true);
                    return;
                }

                final UUID owner = PlotManager.getPlotById(loc).getOwnerId();
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

                final Location pos1 = new Location(loc.getWorld(), PlotManager.bottomX(plotid.id, player.getWorld()), 64, PlotManager.bottomZ(plotid.id, player.getWorld()));
                final Location pos2 = new Location(loc.getWorld(), PlotManager.topX(plotid.id, player.getWorld()), 64, PlotManager.topZ(plotid.id, player.getWorld()));

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
                final World world = player.getWorld();
                final int height = 64;
                final BiomeSelection selection = new BiomeSelection(world, pos1, pos2, height);
                BiomeHandler.generate(selection, player);

                event.setCancelled(true);
            }
        }
    }
}
