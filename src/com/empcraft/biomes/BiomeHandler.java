package com.empcraft.biomes;

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import com.empcraft.biomes.generators.BiomeGenerator_17R4;
import com.empcraft.biomes.generators.BiomeGenerator_18R1;
import com.empcraft.biomes.generators.BiomeGenerator_18R2;

public class BiomeHandler {
private static BiomeGenerator generator = null;
    
    public static boolean generate(final BiomeSelection selection, final Player player, Runnable whenDone) {
        if (generator == null) {
            return false;
        }
        final String name;
        if (player != null) {
            name = player.getName();
        } else {
            name = "";
        }

        if (isRunning) {
            Main.sendMessage(player, "&cSome user is already executing a biome conversion. We will remind you when this finishes");
            if (runner.equals(name) && !runners.contains(name)) {
                runners.add(name);
            }
            return false;
        }

        final boolean sameProvider;

        if (Main.world.equals(selection.world.getName()) || Main.world.equals("")) {
            sameProvider = true;
        } else {
            sameProvider = false;
        }
        runner = name;
        isRunning = true;
        return BiomeHandler.generator.generate(selection, player, sameProvider, whenDone);
    }
    
    public static void notifyPlayers(Runnable whenDone) {
        runners.add(runner);
        for (final String name : runners) {
            final Player player = Bukkit.getPlayer(name);
            if (player != null) {
                Main.sendMessage(player, "&6The biome conversion by &a" + runner + " &6has finished!");
            }
        }
        runners = new HashSet<String>();
        runner = "";
        isRunning = false;
        counter = 0;
        if (whenDone != null) {
            Bukkit.getScheduler().runTask(Main.plugin, whenDone);
        }
    }
    
    public static void getNewGenerator(Biome biome, long seed) {
        try {
            BiomeHandler.generator = new BiomeGenerator_17R4(biome, seed);
            return;
        }
        catch (Throwable e) { }
        try {
            BiomeHandler.generator = new BiomeGenerator_18R1(biome, seed);
            return;
        }
        catch (Throwable e) {}
        try {
            BiomeHandler.generator = new BiomeGenerator_18R2(biome, seed);
            return;
        }
        catch (Throwable e) { 
            e.printStackTrace();
        }
    }
    
    public static long state = 157;

    public static long nextLong() {
        final long a = state;
        state = xorShift64(a);
        return a;
    }

    public static long xorShift64(long a) {
        a ^= (a << 21);
        a ^= (a >>> 35);
        a ^= (a << 4);
        return a;
    }

    public static int random(final int n) {
        if (n == 1) {
            return 0;
        }
        final long r = ((nextLong() >>> 32) * n) >> 32;
        return (int) r;
    }
    
    public static boolean isRunning = false;
    
    public static String runner = null;
    
    public static HashSet<String> runners = new HashSet<>();
    
    public static int counter = 0;
}
