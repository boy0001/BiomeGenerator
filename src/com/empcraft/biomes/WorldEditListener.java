package com.empcraft.biomes;

import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldedit.function.mask.Mask;

public class WorldEditListener implements Listener {

    final WorldEditPlugin we;

    public WorldEditListener() {
        this.we = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
    }

    @EventHandler
    public void PlayerCommand(final PlayerCommandPreprocessEvent e) {
        final Player p = e.getPlayer();

        final String cmd = e.getMessage().toLowerCase();
        final String[] args = cmd.split(" ");

        if (cmd.startsWith("/biomegen") || cmd.startsWith("//biomegen") || cmd.startsWith("/worldedit:biomegen")) {
            if (p.hasPermission("worldedit.biome.generate") || p.isOp()) {
                if (args.length > 1) {
                    final String sb = args[1];
                    try {

                        int height = 64;
                        if (args.length > 2) {
                            try {
                                height = Integer.parseInt(args[2]);
                            }
                            catch (final Exception ex2) {
                                Main.sendMessage(p, "&cInvalid integer height: &a" + args[2] + "\n&6Use &7//biomegen <biome> [height]");
                                return;
                            }
                        }

                        long seed;
                        if (args.length > 3) {
                            final World world = Bukkit.getWorld(args[3]);
                            if (world == null) {
                                seed = Long.valueOf(args[3]);
                            }
                            else {
                                seed = world.getSeed();
                            }
                        }
                        else {
                            seed = (new Random(System.nanoTime())).nextLong();
                        }

                        final Selection sel = this.we.getSelection(p);

                        if (sel instanceof CuboidSelection) {
                            final Vector min = sel.getNativeMinimumPoint();
                            final Vector max = sel.getNativeMaximumPoint();
                            final World world = p.getWorld();

                            final LocalSession session = this.we.getSession(p);
                            final Mask mask = session.getMask();
                            if (mask != null) {
                                final boolean result1 = mask.test(min);
                                if (!result1) {
                                    Main.sendMessage(p, "&cpos1 is outside your current WorldEdit mask. Please make a valid selection first.");
                                    return;
                                }
                                final boolean result2 = mask.test(max);
                                if (!result2) {
                                    Main.sendMessage(p, "&cpos2 is outside your current WorldEdit mask. Please make a valid selection first.");
                                    return;
                                }
                            }

                            final Location pos1 = new Location(world, min.getX(), 0, min.getZ());
                            final Location pos2 = new Location(world, max.getX(), world.getMaxHeight(), max.getZ());

                            Biome biome;

                            if (sb.equalsIgnoreCase("auto")) {
                                biome = p.getLocation().getBlock().getBiome();
                            }
                            else {
                                try {
                                    biome = Biome.valueOf(new StringComparsion(sb, Biome.values()).getBestMatch());
                                }
                                catch (final Exception ex) {
                                    biome = Biome.FOREST;
                                }
                            }
                            
                            if (BiomeGenerator.running) {
                                String name = p.getName();
                                Main.sendMessage(p, "&cSome user is already executing a biome conversion. We will remind you when this finishes");
                                if (BiomeGenerator.runner.equals(name) && !BiomeGenerator.runners.contains(name)) {
                                    BiomeGenerator.runners.add(name);
                                }
                                return;
                            }

                            final BiomeSelection s = new BiomeSelection(world, pos1, pos2, height);
                            final BiomeGenerator bu = new BiomeGenerator(biome, seed);
                            bu.generate(s, p);
                        }
                        else {
                            Main.sendMessage(p, "&cThis command currently only supports cuboid selections");
                            return;
                        }
                    }
                    catch (final Exception ex) {
                        Main.sendMessage(p, "&cInvalid biome &a" + args[1] + "&7. &6Possible options&f: " + StringUtils.join(Biome.values(), ", "));
                    }
                }
                else {
                    Main.sendMessage(p, "&cInvalid number of arguments:\n&6Use &7//biomegen <biome> [height]");
                    e.setCancelled(true);
                }
            }
            else {
                Main.sendMessage(p, "&cYou do not have permission to execute this command.");
            }
            e.setCancelled(true);
        }
    }
}
