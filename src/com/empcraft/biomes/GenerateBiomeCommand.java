package com.empcraft.biomes;

import java.util.HashMap;
import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.commands.SubCommand;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.generator.HybridPlotWorld;
import com.intellectualcrafters.plot.object.BukkitPlayer;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.util.BlockManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.bukkit.BukkitChunkManager;
import com.intellectualcrafters.plot.util.bukkit.BukkitUtil;

public class GenerateBiomeCommand extends SubCommand {

    public GenerateBiomeCommand() {
        super("generatebiome", "plots.generatebiome", "Generate an authentic biome in your plot", "generatebiome", "gb", CommandCategory.ACTIONS, true);
    }

    @Override
    public boolean execute(final PlotPlayer pp, final String... args) {
        if (args.length == 0) {
            MainUtil.sendMessage(pp, "&7Use: &c/plot generatebiome <biome>");
            return false;
        }
        Location loc = pp.getLocation();
        Plot plot = MainUtil.getPlot(loc);
        if (plot == null) {
            sendMessage(pp, C.NOT_IN_PLOT);
            return false;
        }
        if (!plot.isAdded(pp.getUUID())) {
            sendMessage(pp, C.NO_PLOT_PERMS);
            return false;
        }

        Biome biome = null;

        if (args[0].equalsIgnoreCase("auto")) {
            biome = Biome.valueOf(BlockManager.manager.getBiome(MainUtil.getPlotBottomLoc(loc.getWorld(), plot.id).add(1, 0, 1)));
        }
        else {
            try {
                final String match = new StringComparsion(args[0], Biome.values()).getBestMatch();
                if (!match.equalsIgnoreCase(args[0])) {
                    sendMessage(pp, C.DID_YOU_MEAN, match);
                    return false;
                }
                biome = Biome.valueOf(match);
            }
            catch (final Exception e) {
                sendMessage(pp, C.DID_YOU_MEAN, "FOREST");
                return false;
            }
        }

        if (!Permissions.hasPermission(pp, "plots.generatebiome." + biome.name())) {
            sendMessage(pp, C.NO_PERMISSION, "plots.generatebiome." + biome.name());
            return false;
        }
        
        if (BiomeHandler.isRunning) {
            String name = pp.getName();
            MainUtil.sendMessage(pp, "&cSome user is already executing a biome conversion. We will remind you when this finishes");
            if (BiomeHandler.runner.equals(name) && !BiomeHandler.runners.contains(name)) {
                BiomeHandler.runners.add(name);
            }
            return false;
        }
        
        BiomeHandler.getNewGenerator(biome, new Random(System.nanoTime()).nextLong());

        int height = 64;
        final PlotWorld plotworld = PlotSquared.getPlotWorld(plot.world);
        if (plotworld instanceof HybridPlotWorld) {
            height = ((HybridPlotWorld) plotworld).PLOT_HEIGHT;
        }
        final Location p1 = MainUtil.getPlotBottomLoc(plot.world, plot.id).add(1, 0, 1);
        final Location p2 = MainUtil.getPlotTopLoc(plot.world, plot.id);
        final World world = BukkitUtil.getWorld(plot.world);
        org.bukkit.Location pos1 = new org.bukkit.Location(world, p1.getX(), p1.getZ(), p1.getZ());
        org.bukkit.Location pos2 = new org.bukkit.Location(world, p2.getX(), p2.getZ(), p2.getZ());
        final BiomeSelection selection = new BiomeSelection(world, pos1, pos2, height);
        // STORE blocks
        final int bxo = pos1.getBlockX();
        final int exo = pos2.getBlockX();
        final int bzo = pos1.getBlockZ();
        final int ezo = pos2.getBlockZ();
        final int bx = bxo >> 4;
        final int ex = exo >> 4;
        final int bz = bzo >> 4;
        final int ez = ezo >> 4;
        final HashMap<Chunk, Short[][][]> ids = new HashMap<>();
        final HashMap<Chunk, Byte[][][]> datas = new HashMap<>();
        for (int x = bx; x <= ex; x++) {
            for (int z = bz; z <= ez; z++) {
                if (x != bx && x != ex && z != bz && z != ez) {
                    continue;
                }
                Short[][][] id = new Short[16][16][128];
                Byte[][][] data = new Byte[16][16][128];
                for (int i = 0; i < 16; i++) {
                    for (int j = 0; j < 16; j++) {
                        for (int y = 0; y < 128; y++) {
                            int X = (x << 4) + i;
                            int Z = (z << 4) + j;
                            if (X >= bxo && X <= exo && Z >= bzo && Z <= ezo) {
                                continue;
                            }
                            Block block = world.getBlockAt(X, y, Z);
                            short cid = (short) block.getTypeId();
                            id[i][j][y] = cid;
                            data[i][j][y] = block.getData();
                        }
                    }
                }
                Chunk chunk = world.getChunkAt(x, z);
                ids.put(chunk, id);
                datas.put(chunk, data);
            }
        }
        BiomeHandler.generate(selection, ((BukkitPlayer) pp).player, new Runnable() {
            @Override
            public void run() {
                TaskManager.runTaskLater(new Runnable() {
                    @Override
                    public void run() {
                        for (int x = bx; x <= ex; x++) {
                            for (int z = bz; z <= ez; z++) {
                                if (x != bx && x != ex && z != bz && z != ez) {
                                    continue;
                                }
                                Chunk chunk = world.getChunkAt(x, z);
                                Short[][][] id = ids.get(chunk);
                                Byte[][][] data = datas.get(chunk);
                                for (int i = 0; i < 16; i++) {
                                    for (int j = 0; j < 16; j++) {
                                        for (int y = 0; y < 128; y++) {
                                            Short cid = id[i][j][y];
                                            Byte cdata = data[i][j][y];
                                            if (cid == null) {
                                                continue;
                                            }
                                            Main.setBlock(world.getBlockAt((x << 4) + i, y, (z << 4) + j), cid, cdata);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }, 20);
            }
        });
        return true;
    }
}
