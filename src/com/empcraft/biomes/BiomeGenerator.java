package com.empcraft.biomes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

import net.minecraft.server.v1_7_R4.BiomeBase;
import net.minecraft.server.v1_7_R4.Block;
import net.minecraft.server.v1_7_R4.BlockFalling;
import net.minecraft.server.v1_7_R4.ChunkProviderGenerate;
import net.minecraft.server.v1_7_R4.IChunkProvider;
import net.minecraft.server.v1_7_R4.World;
import net.minecraft.server.v1_7_R4.WorldGenCanyon;
import net.minecraft.server.v1_7_R4.WorldGenCaves;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Created a random vanilla-style world using a specific biome.
 */
public class BiomeGenerator {

    private static long           counter = 0;
    private static boolean        running = false;
    public static String          runner  = "";
    public static HashSet<String> runners = new HashSet<String>();

    public static void notifyPlayers() {
        for (final String name : runners) {
            final Player player = Bukkit.getPlayer(name);
            if (player != null) {
                Main.sendMessage(player, "&6The biome conversion by &a" + runner + " &6has finished!");
            }
        }
        runners = new HashSet<String>();
        runner = "";
        running = false;
        counter = 0;
    }

    private final Biome biome;
    private BiomeBase   biomeBase;

    private final long  seed;

    public BiomeGenerator(final Biome biome, long seed) {
        this.biome = biome;
        this.seed = seed;
//        this.seed = (new Random()).nextLong();

        try {
            this.biomeBase = ReflectionUtils.getFieldValue(BiomeBase.class, biome.name(), BiomeBase.class, null);
        } catch (final NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    private Block[] createChunk(final ChunkProviderGenerate generator, final World world, final int x, final int z, final BiomeBase biomeBase) {
        final BiomeBase[] biomeBases = new BiomeBase[256];
        Arrays.fill(biomeBases, biomeBase);

        final Block[] blocks = new Block[65536];
        final byte[] bytes = new byte[65536];

        generator.a(x, z, blocks);
        generator.a(x, z, blocks, bytes, biomeBases);

        try {
            ReflectionUtils.getFieldValue(ChunkProviderGenerate.class, "t", WorldGenCaves.class, generator).a(generator, world, x, z, blocks);
            ReflectionUtils.getFieldValue(ChunkProviderGenerate.class, "y", WorldGenCanyon.class, generator).a(generator, world, x, z, blocks);
        } catch (final NoSuchFieldException e) {
            e.printStackTrace();
        }

        return blocks;
    }

    public boolean generate(final BiomeSelection selection, final Player player) {
        final String name;
        if (player != null) {
            name = player.getName();
        } else {
            name = "";
        }

        if (running) {
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

        running = true;

        final CraftWorld cw2 = (CraftWorld) selection.world;
        final World w2 = cw2.getHandle();

        final CraftWorld craftWorld;

        if (sameProvider) {
            craftWorld = cw2;
        } else {
            craftWorld = (CraftWorld) Bukkit.getWorld(Main.world);
        }

        final World world = craftWorld.getHandle();

        final ChunkProviderGenerate generator = new ChunkProviderGenerate(world, this.seed, false);

        final org.bukkit.World w = selection.world;

        int length = 0;

        final ArrayList<Chunk> cs = new ArrayList<Chunk>();
        
        final Location pos1 = selection.pos1;
        final Location pos2 = selection.pos2;
        for (int i = (pos1.getBlockX() / 16) * 16; i < (16 + ((pos2.getBlockX() / 16) * 16)); i += 16) {
            for (int j = (pos1.getBlockZ() / 16) * 16; j < (16 + ((pos2.getBlockZ() / 16) * 16)); j += 16) {
                final Chunk chunk = w.getChunkAt(i, j);
                cs.add(chunk);
                final boolean result = chunk.load(false);
                if (!result) {
                    Main.sendMessage(player, "&cPlease explore the selection fully.");
                    notifyPlayers();
                    return false;
                }
                length++;
            }
        }

        Main.sendMessage(player, "&6Estimated time: &a" + (Main.interval * length / 10) + " seconds");

        final int worldHeight = craftWorld.getMaxHeight();
        final int seaHeight = craftWorld.getSeaLevel();
        final int gridHeight = selection.height;

        final int x1 = pos1.getBlockX();
        final int z1 = pos1.getBlockZ();
        final int x2 = pos2.getBlockX();
        final int z2 = pos2.getBlockZ();

        final int[] selectionArray = new int[] { x1, z1, x2, z2 };

        final long chunks = length;

        final Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("BiomeGenerator");
        counter = 1;

        for (int xv = selectionArray[0]; xv <= selectionArray[2]; xv += 16) {
            for (int zv = selectionArray[1]; zv <= selectionArray[3]; zv += 16) {

                final int x = xv;
                final int z = zv;

                final long index = counter;

                Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                    @SuppressWarnings("deprecation")
                    @Override
                    public void run() {
                        if ((index != 0) && ((index % 10) == 0)) {
                            Main.sendMessage(Bukkit.getPlayer(runner), "&7 - chunk &6" + index + "&7/&6" + chunks);
                        }
                        final Block[] chunk = createChunk(generator, world, x >> 4, z >> 4, BiomeGenerator.this.biomeBase);
                        for (int cx = 0; cx < 16; ++cx) {
                            for (int cz = 0; cz < 16; ++cz) {
                                final int wx = x + cx;
                                final int wz = z + cz;

                                if ((wx >= selectionArray[0]) && (wz >= selectionArray[1]) && (wx <= selectionArray[2]) && (wz <= selectionArray[3])) {
                                    craftWorld.setBiome(wx, wz, BiomeGenerator.this.biome);

                                    for (int y = 0; y < 256; ++y) {
                                        final int wy = y - (seaHeight - gridHeight);

                                        if ((wy > 0) && (wy < worldHeight)) {
                                            Material type = Material.getMaterial(Block.REGISTRY.b(chunk[(((cx * 16) + cz) * 256) + y]));
                                            if (type == null) {
                                                type = Material.AIR;
                                            }
                                            final short id = (short) type.getId();
                                            if (id != 0) {
                                                Main.setBlock(w.getBlockAt(wx, wy, wz), id, (byte) 0);
                                            } else if (wy <= (gridHeight + 32)) {
                                                Main.setBlock(w.getBlockAt(wx, wy, wz), (short) 0, (byte) 0);
                                            }
                                        }
                                    }
                                    Main.setBlock(w.getBlockAt(wx, 0, wz), (short) 7, (byte) 0);
                                }
                            }
                        }
                    }

                }, Main.interval * counter);
                counter++;
            }
        }

        BlockFalling.instaFall = true;

        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {

            @SuppressWarnings("deprecation")
            @Override
            public void run() {
                try {
                    final IChunkProvider currentChunkProvider = world.chunkProvider;

                    for (int x = selectionArray[0] + Main.inset; x <= (selectionArray[2] - Main.inset); x += 16) {
                        for (int z = selectionArray[1] + Main.inset; z <= (selectionArray[3] - Main.inset); z += 16) {
                            if (!sameProvider) {
                                world.chunkProvider = new BCP(currentChunkProvider, selectionArray);
                            }
                            try {
                                BiomeGenerator.this.biomeBase.a(w2, ReflectionUtils.getFieldValue(ChunkProviderGenerate.class, "i", Random.class, generator), (x >> 4) * 16, (z >> 4) * 16);
                            } catch (final NoSuchFieldException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    final Player player = Bukkit.getPlayer(runner);
                    Main.sendMessage(player, "&aBiome conversion finished!");
                    world.chunkProvider = currentChunkProvider;
                    BlockFalling.instaFall = true;
                    notifyPlayers();
                } catch (final Throwable e) {
                    final Player player = Bukkit.getPlayer(runner);
                    Main.sendMessage(player, "&aBiome conversion was interrupted!");
                    notifyPlayers();
                }
                if (Main.canSetFast) {
                    SendChunk.sendChunk(cs);
                }
            }
        }, Main.interval * counter + 20);

        return true;
    }

}
