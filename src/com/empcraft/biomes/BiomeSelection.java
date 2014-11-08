package com.empcraft.biomes;

import org.bukkit.Location;
import org.bukkit.World;

public class BiomeSelection {
    public final Location pos1;
    public final Location pos2;
    public final int      height;
    public final World    world;

    public BiomeSelection(final World world, final Location pos1, final Location pos2, final int height) {
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.height = height;
        this.world = world;
    }
}
