package com.empcraft.biomes.generators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import net.minecraft.server.v1_8_R1.BiomeBase;
import net.minecraft.server.v1_8_R1.Block;
import net.minecraft.server.v1_8_R1.BlockFalling;
import net.minecraft.server.v1_8_R1.BlockPosition;
import net.minecraft.server.v1_8_R1.ChunkProviderGenerate;
import net.minecraft.server.v1_8_R1.ChunkSnapshot;
import net.minecraft.server.v1_8_R1.World;
import net.minecraft.server.v1_8_R1.WorldGenCanyon;
import net.minecraft.server.v1_8_R1.WorldGenCaves;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_8_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.empcraft.biomes.BBC;
import com.empcraft.biomes.BiomeGenerator;
import com.empcraft.biomes.BiomeHandler;
import com.empcraft.biomes.BiomeSelection;
import com.empcraft.biomes.Main;
import com.empcraft.biomes.ReflectionUtils;
import com.empcraft.biomes.SendChunk_;

/**
 * Created a random vanilla-style world using a specific biome.
 */
public class BiomeGenerator_18R1 extends BiomeGenerator {
    private Biome biome;
    private BiomeBase   biomeBase;

    private final long  seed;

    public BiomeGenerator_18R1(final Biome biome, final long seed) {
        this.biome = biome;
        this.seed = seed;
        try {
            this.biomeBase = ReflectionUtils.getFieldValue(BiomeBase.class, biome.name(), BiomeBase.class, null);
        }
        catch (final NoSuchFieldException e) {
            this.biome = Biome.FOREST;
            try {
                this.biomeBase = ReflectionUtils.getFieldValue(BiomeBase.class, Biome.FOREST.name(), BiomeBase.class, null);
            }
            catch (SecurityException | NoSuchFieldException e2) {
                e2.printStackTrace();
            }
        }
    }

    private ChunkSnapshot createChunk(final ChunkProviderGenerate generator, final World world, final int x, final int z, final BiomeBase biomeBase) {
        final BiomeBase[] biomeBases = new BiomeBase[256];
        Arrays.fill(biomeBases, biomeBase);

//        final Block[] blocks = new Block[65536];
//        final byte[] bytes = new byte[65536];

        ChunkSnapshot blocks = new ChunkSnapshot();
        // create new chunk
        
        
        generator.a(x, z, blocks);
        generator.a(x, z, blocks, biomeBases);

        try {
            ReflectionUtils.getFieldValue(ChunkProviderGenerate.class, "u", WorldGenCaves.class, generator).a(generator, world, x, z, blocks);
            ReflectionUtils.getFieldValue(ChunkProviderGenerate.class, "z", WorldGenCanyon.class, generator).a(generator, world, x, z, blocks);
        }
        catch (final NoSuchFieldException e) {
            e.printStackTrace();
        }

        return blocks;
    }

    public boolean generate(final BiomeSelection selection, final Player player, boolean sameProvider, final Runnable whenDone) {
        final CraftWorld cw2 = (CraftWorld) selection.world;
        final World w2 = cw2.getHandle();

        final CraftWorld craftWorld;

        if (sameProvider) {
            craftWorld = cw2;
        }
        else {
            craftWorld = (CraftWorld) Bukkit.getWorld(Main.world);
        }

        final World world = craftWorld.getHandle();

        final ChunkProviderGenerate generator = new ChunkProviderGenerate(world, this.seed, false, "");

        final org.bukkit.World w = selection.world;

        int length = 0;

        final ArrayList<Chunk> cs = new ArrayList<Chunk>();

        final Location pos1 = selection.pos1;
        final Location pos2 = selection.pos2;
        for (int i = ((pos1.getBlockX() / 16) * 16) - 16; i <= (16 + ((pos2.getBlockX() / 16) * 16)); i += 16) {
            for (int j = ((pos1.getBlockZ() / 16) * 16) - 16; j <= (16 + ((pos2.getBlockZ() / 16) * 16)); j += 16) {
                final Chunk chunk = w.getChunkAt(i / 16, j / 16);
                cs.add(chunk);
                final boolean result = chunk.load(false);
                if (!result) {
                    Main.sendMessage(player, BBC.UNEXPLORED.s);
                    BiomeHandler.notifyPlayers(whenDone);
                    return false;
                }
                length++;
            }
        }

        BBC.sendMessage(player, BBC.ESTIMATE, ((Main.interval * length) / 10));

        final int worldHeight = craftWorld.getMaxHeight();
        final int seaHeight = craftWorld.getSeaLevel();
        final int gridHeight = selection.height;

        final int x1 = pos1.getBlockX();
        final int z1 = pos1.getBlockZ();
        final int x2 = pos2.getBlockX();
        final int z2 = pos2.getBlockZ();

        final int[] selectionArray = new int[] { x1, z1, x2, z2 };

        final Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("BiomeGenerator");
        BiomeHandler.counter = 1;

        for (int xv = selectionArray[0]; xv <= selectionArray[2]; xv += 16) {
            for (int zv = selectionArray[1]; zv <= selectionArray[3]; zv += 16) {

                final int x = xv;
                final int z = zv;

                Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                    @SuppressWarnings("deprecation")
                    @Override
                    public void run() {
                        ChunkSnapshot chunk = createChunk(generator, world, x >> 4, z >> 4, BiomeGenerator_18R1.this.biomeBase);
                        
                        long init_state = BiomeHandler.state;
                        
                        for (int cx = 0; cx < 16; ++cx) {
                            for (int cz = 0; cz < 16; ++cz) {
                                final int wx = x + cx;
                                final int wz = z + cz;

                                if ((wx >= selectionArray[0]) && (wz >= selectionArray[1]) && (wx <= selectionArray[2]) && (wz <= selectionArray[3])) {
                                    craftWorld.setBiome(wx, wz, BiomeGenerator_18R1.this.biome);
                                    if (BiomeGenerator_18R1.this.biome == Biome.MESA) {
                                        Random r = new Random();
                                        Integer[] ids = new Integer[] { 0, 1, 4, 7, 8, 12};
                                        for (int y = 0; y < 256; ++y) {
                                            final int wy = y - (seaHeight - gridHeight);

                                            if ((wy > 0) && (wy < worldHeight)) {
                                                Block block = chunk.a((((cx * 16) + cz) * 256) + y).getBlock();
                                                short id = (short) Block.getId(block);
                                                if (id != 0) {
                                                    int data = 0;
                                                    if (id == 12) {
                                                        data = 1;
                                                    }
                                                    else if (id == 159) {
                                                        int scy = y;
                                                        
                                                        if (r.nextInt(15) == 1) {
                                                            scy++;
                                                        }
                                                        
                                                        scy = scy/3;
                                                        
                                                        BiomeHandler.state = init_state + scy;
                                                        
                                                        long num = BiomeHandler.nextLong();
//                                                        num = num * num + 3 * num + 2 * num * scy + scy + scy * scy;
                                                        data = ids[(int) (Math.abs(num)%5)];
                                                    }
                                                    Main.setBlock(w.getBlockAt(wx, wy, wz), id, (byte) data);
                                                }
                                                else if (wy <= (gridHeight + 32)) {
                                                    Main.setBlock(w.getBlockAt(wx, wy, wz), (short) 0, (byte) 0);
                                                }
                                            }
                                        }
                                    }
                                    else {
                                        for (int y = 0; y < 256; ++y) {
                                            final int wy = y - (seaHeight - gridHeight);
    
                                            if ((wy > 0) && (wy < worldHeight)) {
                                                Block block = chunk.a((((cx * 16) + cz) * 256) + y).getBlock();
                                                short id = (short) Block.getId(block);
                                                if (id != 0) {
                                                    Main.setBlock(w.getBlockAt(wx, wy, wz), id, (byte) 0);
                                                }
                                                else if (wy <= (gridHeight + 32)) {
                                                    Main.setBlock(w.getBlockAt(wx, wy, wz), (short) 0, (byte) 0);
                                                }
                                            }
                                        }
                                    }
                                    Main.setBlock(w.getBlockAt(wx, 0, wz), (short) 7, (byte) 0);
                                }
                            }
                        }
                    }

                }, Main.interval * BiomeHandler.counter);
                BiomeHandler.counter++;
            }
        }

        BlockFalling.instaFall = true;

        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {

            @Override
            public void run() {
                try {
                    for (int x = selectionArray[0] + Main.inset; x <= (selectionArray[2] - Main.inset); x += 16) {
                        for (int z = selectionArray[1] + Main.inset; z <= (selectionArray[3] - Main.inset); z += 16) {
                            try {
                                BlockPosition pos = new BlockPosition((x >> 4) * 16, 0, (z >> 4) * 16);
                                BiomeGenerator_18R1.this.biomeBase.a(w2, ReflectionUtils.getFieldValue(ChunkProviderGenerate.class, "h", Random.class, generator), pos);
                            }
                            catch (final NoSuchFieldException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    BlockFalling.instaFall = true;
                    BiomeHandler.notifyPlayers(whenDone);
                }
                catch (final Throwable e) {
                    final Player player = Bukkit.getPlayerExact(BiomeHandler.runner);
                    if (player != null) {
                        Main.sendMessage(player, BBC.INTERRUPTED.s);
                    }
                    BiomeHandler.notifyPlayers(whenDone);
                }
                if (Main.canSetFast) {
                    SendChunk_.sendChunk(cs);
                }
            }
        }, (Main.interval * BiomeHandler.counter) + 20);

        return true;
    }
}
