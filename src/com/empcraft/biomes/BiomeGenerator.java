package com.empcraft.biomes;

import org.bukkit.entity.Player;

public abstract class BiomeGenerator {

    public abstract boolean generate(BiomeSelection selection, Player player, boolean sameProvider);
    
}
