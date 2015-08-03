package com.empcraft.biomes.generators;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import net.minecraft.server.v1_8_R3.BiomeBase;
import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.BlockFalling;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.ChunkProviderGenerate;
import net.minecraft.server.v1_8_R3.ChunkSnapshot;
import net.minecraft.server.v1_8_R3.World;
import net.minecraft.server.v1_8_R3.WorldGenCanyon;
import net.minecraft.server.v1_8_R3.WorldGenCaves;
import net.minecraft.server.v1_8_R3.WorldGenLargeFeature;
import net.minecraft.server.v1_8_R3.WorldGenMineshaft;
import net.minecraft.server.v1_8_R3.WorldGenMonument;
import net.minecraft.server.v1_8_R3.WorldGenStronghold;
import net.minecraft.server.v1_8_R3.WorldGenVillage;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Player;

import com.empcraft.biomes.BBC;
import com.empcraft.biomes.BiomeGenerator;
import com.empcraft.biomes.BiomeHandler;
import com.empcraft.biomes.BiomeSelection;
import com.empcraft.biomes.BlockWrapper;
import com.empcraft.biomes.ChunkLoc;
import com.empcraft.biomes.Main;
import com.empcraft.biomes.ReflectionUtils;
import com.empcraft.biomes.TaskManager;

/**
 * Created a random vanilla-style world using a specific biome.
 */
public class BiomeGenerator_18R3 extends BiomeGenerator {
    private Biome biome;
    private BiomeBase   biomeBase;

    private final long  seed;

    public BiomeGenerator_18R3(final Biome biome, final long seed) {
        this.biome = biome;
        this.seed = seed;
        try {
            this.biomeBase = ReflectionUtils.getFieldValue(BiomeBase.class, biome.name(), BiomeBase.class, null);
        }
        catch (final NoSuchFieldException e) {
            e.printStackTrace();
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
            ReflectionUtils.getFieldValue(ChunkProviderGenerate.class, "v", WorldGenStronghold.class, generator).a(generator, world, x, z, blocks);
            ReflectionUtils.getFieldValue(ChunkProviderGenerate.class, "w", WorldGenVillage.class, generator).a(generator, world, x, z, blocks);
            ReflectionUtils.getFieldValue(ChunkProviderGenerate.class, "x", WorldGenMineshaft.class, generator).a(generator, world, x, z, blocks);
            ReflectionUtils.getFieldValue(ChunkProviderGenerate.class, "y", WorldGenLargeFeature.class, generator).a(generator, world, x, z, blocks);
            ReflectionUtils.getFieldValue(ChunkProviderGenerate.class, "z", WorldGenCanyon.class, generator).a(generator, world, x, z, blocks);
            ReflectionUtils.getFieldValue(ChunkProviderGenerate.class, "A", WorldGenMonument.class, generator).a(generator, world, x, z, blocks);
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
        final Location pos1 = selection.pos1;
        final Location pos2 = selection.pos2;

        final int worldHeight = craftWorld.getMaxHeight();
        final int seaHeight = craftWorld.getSeaLevel();
        final int gridHeight = selection.height;

        final int x1 = pos1.getBlockX();
        final int z1 = pos1.getBlockZ();
        final int x2 = pos2.getBlockX();
        final int z2 = pos2.getBlockZ();

        final int[] selectionArray = new int[] { x1, z1, x2, z2 };

        BiomeHandler.counter = 1;
        final ArrayList<Chunk> finished = new ArrayList<Chunk>();
        final ArrayList<ChunkLoc> toGen = new ArrayList<>();
        
        final int bcx = x1 >> 4;
        final int bcz = z1 >> 4;
        final int tcx = x2 >> 4;
        final int tcz = z2 >> 4;
        
        for (int x = bcx; x <= tcx; x++) {
            for (int z = bcz; z <= tcz; z++) {
                toGen.add(new ChunkLoc(x, z));
            }
        }
        
        final boolean snow;
        switch (biome) {
            case COLD_TAIGA:
            case COLD_TAIGA_HILLS:
            case COLD_TAIGA_MOUNTAINS:
            case FROZEN_RIVER:
            case ICE_MOUNTAINS:
            case ICE_PLAINS:
            case ICE_PLAINS_SPIKES:
            case TAIGA:
            case TAIGA_HILLS:
            case TAIGA_MOUNTAINS:
                snow = true;
                break;
            default:
                snow = false;
                break;
        }
        
        BBC.sendMessage(player, BBC.ESTIMATE, ((Main.interval * toGen.size()) / 10));
        
        TaskManager.task(new Runnable() {
            @Override
            public void run() {
                if (toGen.size() == 0) {
                    BlockFalling.instaFall = true;
                    // TODO
                    SetBlockQueue.addNotify(new Runnable() {
                        @Override
                        public void run() {
                            TaskManager.task(new Runnable() {
                                @Override
                                public void run() {
                                    if (finished.size() == 0) {
                                        BiomeHandler.notifyPlayers(whenDone);
                                        return;
                                    }
                                    Chunk chunk = finished.remove(0);
                                    try {
                                        if (chunk.load(false)) {
                                            int x = chunk.getX();
                                            int z = chunk.getZ();
                                            generator.getChunkAt(generator, x, z);
                                            BlockPosition pos = new BlockPosition((x >> 4) * 16, 0, (z >> 4) * 16);
                                            Random r = ReflectionUtils.getFieldValue(ChunkProviderGenerate.class, "h", Random.class, generator);
                                            BiomeGenerator_18R3.this.biomeBase.a(w2, r, pos);
                                        }
                                    }
                                    catch (final Throwable e) {
                                        e.printStackTrace();
                                    }
                                    TaskManager.taskLater(this, Main.interval);
                                }
                            });
                        }
                    });
                    return;
                }
                final ChunkLoc loc = toGen.remove(0);
                Chunk c = w.getChunkAt(loc.x, loc.z);
                if (!c.load(false)) {
                    TaskManager.taskLater(this, Main.interval);
                    return;
                }
                
                finished.add(c);
                final int bx = loc.x << 4;
                final int bz = loc.z << 4;
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        w.setBiome(x + bx, z + bz, BiomeGenerator_18R3.this.biome);
                    }
                }
                final ChunkSnapshot chunk = createChunk(generator, world, loc.x, loc.z, BiomeGenerator_18R3.this.biomeBase);
                final boolean check = bx < selectionArray[0] || bx + 15 > selectionArray[2] || bz < selectionArray[1] || bz + 15 > selectionArray[3];
                final long init_state = BiomeHandler.state;
                final String worldname = w.getName();
                final Runnable task = this;
                TaskManager.taskAsync(new Runnable() {
                    @Override
                    public void run() {
                        for (int cx = 0; cx < 16; ++cx) {
                            int wx = bx + cx;
                            if (check) {
                                if (wx < selectionArray[0] || wx > selectionArray[2]) {
                                    continue;
                                }
                            }
                            for (int cz = 0; cz < 16; ++cz) {
                                final int wz = bz + cz;
                                if (check) {
                                    if (wz < selectionArray[1] || wz > selectionArray[3]) {
                                        continue;
                                    }
                                }
                                craftWorld.setBiome(wx, wz, BiomeGenerator_18R3.this.biome);
                                if (BiomeGenerator_18R3.this.biome == Biome.MESA) {
                                    Random r = new Random(System.currentTimeMillis());
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
                                                    
                                                    BiomeHandler.state = scy;
                                                    
                                                    long num = BiomeHandler.nextLong();
//                                                                num = num * num + 3 * num + 2 * num * scy + scy + scy * scy;
                                                    data = ids[(int) (Math.abs(num)%5)];
                                                }
                                                SetBlockQueue.setBlock(worldname, wx, wy, wz, new BlockWrapper(id, (byte) data));
                                            }
                                            else if (wy <= (gridHeight + 32)) {
                                                SetBlockQueue.setBlock(worldname, wx, wy, wz, 0);
                                            }
                                        }
                                    }
                                }
                                else {
                                    int wy = - seaHeight + gridHeight;
                                    int heighest = 0;
                                    for (int y = Math.max(0, seaHeight - gridHeight); y < Math.min(256, 255 + seaHeight - gridHeight); y++) {
                                        wy++;
                                        Block block = chunk.a((((cx * 16) + cz) * 256) + y).getBlock();
                                        short id = (short) Block.getId(block);
                                        switch (id) {
                                            case 0:
                                                SetBlockQueue.setBlock(worldname, wx, wy, wz, 0);
                                                break;
                                            case 2:
                                                heighest = wy;
                                            default:
                                                SetBlockQueue.setBlock(worldname, wx, wy, wz, id);
                                                break;
                                        }
                                    }
                                    if (snow && heighest != 0 && heighest < 255) {
                                        SetBlockQueue.setBlock(worldname, wx, heighest + 1, wz, 78);
                                    }
                                }
                                SetBlockQueue.setBlock(worldname, wx, 0, wz, 7);
                            }
                        }
                        TaskManager.taskLater(task, Main.interval);
                    }
                });
            }
        });
        return true;
    }
}
