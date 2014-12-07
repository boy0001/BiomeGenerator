package com.empcraft.biomes;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.commands.SubCommand;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.generator.DefaultPlotWorld;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.PlayerFunctions;
import com.intellectualcrafters.plot.util.PlotHelper;

public class GenerateBiomeCommand extends SubCommand {

    public GenerateBiomeCommand() {
        super("generatebiome", "plots.generatebiome", "Generate an authentic biome in your plot", "generatebiome", "gb", CommandCategory.ACTIONS, true);
    }

    @Override
    public boolean execute(final Player player, final String... args) {
        if (args.length == 0) {
            PlayerFunctions.sendMessage(player, "&7Use: &c/plot generatebiome <biome>");
            return false;
        }
        if (!PlayerFunctions.isInPlot(player)) {
            sendMessage(player, C.NOT_IN_PLOT);
            return false;
        }
        final Plot plot = PlayerFunctions.getCurrentPlot(player);
        if (!plot.hasRights(player)) {
            sendMessage(player, C.NO_PLOT_PERMS);
            return false;
        }

        Biome biome = null;

        if (args[0].equalsIgnoreCase("auto")) {
            biome = PlotHelper.getPlotBottomLoc(player.getWorld(), plot.id).add(1, 0, 1).getBlock().getBiome();
        }
        else {
            try {
                final String match = new StringComparsion(args[0], Biome.values()).getBestMatch();
                if (!match.equalsIgnoreCase(args[0])) {
                    PlayerFunctions.sendMessage(player, C.DID_YOU_MEAN, match);
                    return false;
                }
                biome = Biome.valueOf(match);
            }
            catch (final Exception e) {
                PlayerFunctions.sendMessage(player, C.DID_YOU_MEAN, "FOREST");
                return false;
            }
        }

        if (!PlotMain.hasPermission(player, "plots.generatebiome." + biome.name())) {
            PlayerFunctions.sendMessage(player, C.NO_PERMISSION, "plots.generatebiome." + biome.name());
            return false;
        }
        
        if (BiomeGenerator.running) {
            String name = player.getName();
            Main.sendMessage(player, "&cSome user is already executing a biome conversion. We will remind you when this finishes");
            if (BiomeGenerator.runner.equals(name) && !BiomeGenerator.runners.contains(name)) {
                BiomeGenerator.runners.add(name);
            }
            return false;
        }

        final BiomeGenerator bu = new BiomeGenerator(biome, new Random(System.nanoTime()).nextLong());

        final World world = player.getWorld();

        int height = 64;
        final PlotWorld plotworld = PlotMain.getWorldSettings(world);
        if (plotworld instanceof DefaultPlotWorld) {
            height = ((DefaultPlotWorld) plotworld).PLOT_HEIGHT;
        }
        final Location pos1 = PlotHelper.getPlotBottomLoc(world, plot.id).add(1, 0, 1);
        final Location pos2 = PlotHelper.getPlotTopLoc(world, plot.id);
        final BiomeSelection selection = new BiomeSelection(world, pos1, pos2, height);
        bu.generate(selection, player);
        return true;
    }
}
