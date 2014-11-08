package com.empcraft.biomes;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import com.intellectualcrafters.plot.C;
import com.intellectualcrafters.plot.PlayerFunctions;
import com.intellectualcrafters.plot.Plot;
import com.intellectualcrafters.plot.PlotHelper;
import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.PlotWorld;
import com.intellectualcrafters.plot.commands.SubCommand;
import com.intellectualcrafters.plot.generator.DefaultPlotWorld;

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

        Biome biome = Biome.valueOf(args[0].toUpperCase());
        if (biome == null) {
            biome = Biome.FOREST;
        }

        if (!PlotMain.hasPermission(player, "plots.generatebiome." + biome.name())) {
            PlayerFunctions.sendMessage(player, C.NO_PERMISSION, "plots.generatebiome." + biome.name());
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
