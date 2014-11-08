package com.empcraft.biomes;

import com.intellectualcrafters.plot.commands.MainCommand;

public class PlotSquaredFeature {
    public PlotSquaredFeature() {
        MainCommand.subCommands.add(new GenerateBiomeCommand());
    }
}
