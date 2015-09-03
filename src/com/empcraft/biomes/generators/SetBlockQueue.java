package com.empcraft.biomes.generators;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.World;

import com.empcraft.biomes.BlockWrapper;
import com.empcraft.biomes.ChunkLoc;
import com.empcraft.biomes.SetBlockFast_1_8;
import com.empcraft.biomes.TaskManager;

public class SetBlockQueue {

    private volatile static HashMap<ChunkWrapper, BlockWrapper[][]> blocks;
    private volatile static int allocate = 25;
    private volatile static boolean running = false;
    private volatile static boolean locked = false;
    private volatile static ArrayDeque<Runnable> runnables;
    private static long last;
    private static int lastInt = 0;
    private static BlockWrapper lastBlock = new BlockWrapper((short) 0, (byte) 0);

    private static short[][] x_loc;
    private static short[][] y_loc;
    private static short[][] z_loc;

    public static void initCache() {
        if (x_loc == null) {
            x_loc = new short[16][4096];
            y_loc = new short[16][4096];
            z_loc = new short[16][4096];
            for (int i = 0; i < 16; i++) {
                final int i4 = i << 4;
                for (int j = 0; j < 4096; j++) {
                    final int y = (i4) + (j >> 8);
                    final int a = (j - ((y & 0xF) << 8));
                    final int z1 = (a >> 4);
                    final int x1 = a - (z1 << 4);
                    x_loc[i][j] = (short) x1;
                    y_loc[i][j] = (short) y;
                    z_loc[i][j] = (short) z1;
                }
            }
        }
    }

    public synchronized static void allocate(final int t) {
        allocate = t;
    }

    public static int getAllocate() {
        return allocate;
    }

    public synchronized static boolean addNotify(final Runnable whenDone) {
        if (runnables == null) {
            if ((blocks == null) || (blocks.size() == 0)) {
                if (whenDone != null) {
                    whenDone.run();
                }
                locked = false;
                return true;
            }
            runnables = new ArrayDeque<>();
        }
        if (whenDone != null) {
            init();
            runnables.add(whenDone);
        }
        if ((blocks == null) || (blocks.size() == 0) || !blocks.entrySet().iterator().hasNext()) {
            final ArrayDeque<Runnable> tasks = runnables;
            lastInt = -1;
            lastBlock = null;
            runnables = null;
            running = false;
            blocks = null;
            if (tasks != null) {
                for (final Runnable runnable : tasks) {
                    runnable.run();
                }
            }
        }
        return false;
    }

    public synchronized static void init() {
        if (blocks == null) {
            if (x_loc == null) {
                initCache();
            }
            blocks = new HashMap<>();
            runnables = new ArrayDeque<>();
        }
        if (!running) {
            TaskManager.index.increment();
            final int current = TaskManager.index.intValue();
            final int task = TaskManager.taskRepeat(new Runnable() {
                @Override
                public void run() {
                    if (locked) {
                        return;
                    }
                    if ((blocks == null) || (blocks.size() == 0)) {
                        Bukkit.getScheduler().cancelTask(TaskManager.tasks.get(current));
                        final ArrayDeque<Runnable> tasks = runnables;
                        lastInt = -1;
                        lastBlock = null;
                        runnables = null;
                        running = false;
                        blocks = null;
                        if (tasks != null) {
                            for (final Runnable runnable : tasks) {
                                runnable.run();
                            }
                        }
                        return;
                    }
                    final long newLast = System.currentTimeMillis();
                    last = Math.max(newLast - 50, last);
                    while ((blocks.size() > 0) && ((System.currentTimeMillis() - last) < (50 + allocate))) {
                        if (locked) {
                            return;
                        }
                        final Iterator<Entry<ChunkWrapper, BlockWrapper[][]>> iter = blocks.entrySet().iterator();
                        if (!iter.hasNext()) {
                            Bukkit.getScheduler().cancelTask(TaskManager.tasks.get(current));
                            final ArrayDeque<Runnable> tasks = runnables;
                            lastInt = -1;
                            lastBlock = null;
                            runnables = null;
                            running = false;
                            blocks = null;
                            if (tasks != null) {
                                for (final Runnable runnable : tasks) {
                                    runnable.run();
                                }
                            }
                            return;
                        }
                        final Entry<ChunkWrapper, BlockWrapper[][]> n = iter.next();
                        final ChunkWrapper chunk = n.getKey();
                        final BlockWrapper[][] blocks = n.getValue();
                        final int X = chunk.x << 4;
                        final int Z = chunk.z << 4;
                        final World world = Bukkit.getWorld(chunk.world);
                        SetBlockQueue.blocks.remove(n.getKey());
                        for (int j = 0; j < blocks.length; j++) {
                            final BlockWrapper[] blocksj = blocks[j];
                            if (blocksj != null) {
                                for (int k = 0; k < blocksj.length; k++) {
                                    final BlockWrapper block = blocksj[k];
                                    if (block != null) {
                                        final int x = x_loc[j][k];
                                        final int y = y_loc[j][k];
                                        final int z = z_loc[j][k];
                                        SetBlockFast_1_8.set(world, X + x, y, Z + z, block.id, (byte) block.data);
                                    }
                                }
                            }
                        }
                    }
                }
            }, 1);
            TaskManager.tasks.put(current, task);
            running = true;
        }
    }

    public static void setChunk(final String world, final ChunkLoc loc, final BlockWrapper[][] result) {
        locked = true;
        if (!running) {
            init();
        }
        final ChunkWrapper wrap = new ChunkWrapper(world, loc.x, loc.z);
        blocks.put(wrap, result);
        locked = false;
    }

    public static void setBlock(final String world, int x, final int y, int z, final BlockWrapper block) {
        locked = true;
        if (!running) {
            init();
        }
        final int X = x >> 4;
                                final int Z = z >> 4;
                        x -= X << 4;
                        z -= Z << 4;

                        final ChunkWrapper wrap = new ChunkWrapper(world, X, Z);
                        BlockWrapper[][] result;
                        result = blocks.get(wrap);
                        if (!blocks.containsKey(wrap)) {
                            result = new BlockWrapper[16][];
                            blocks.put(wrap, result);
                        }
                        if ((y > 255) || (y < 0)) {
                            locked = false;
                            return;
                        }
                        if (result[y >> 4] == null) {
                            result[y >> 4] = new BlockWrapper[4096];
                        }
                        result[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = block;
                        locked = false;
    }

    public static void setData(final String world, int x, final int y, int z, final byte data) {
        locked = true;
        if (!running) {
            init();
        }
        final int X = x >> 4;
                        final int Z = z >> 4;
                        x -= X << 4;
                        z -= Z << 4;
                        final ChunkWrapper wrap = new ChunkWrapper(world, X, Z);
                        BlockWrapper[][] result;
                        result = blocks.get(wrap);
                        if (!blocks.containsKey(wrap)) {
                            result = new BlockWrapper[16][];
                            blocks.put(wrap, result);
                        }
                        if ((y > 255) || (y < 0)) {
                            locked = false;
                            return;
                        }
                        if (result[y >> 4] == null) {
                            result[y >> 4] = new BlockWrapper[4096];
                        }
                        result[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = new BlockWrapper((short) -1, data);
                        locked = false;
    }

    public static void setBlock(final String world, int x, final int y, int z, final int id) {
        locked = true;
        if (!running) {
            init();
        }
        final int X = x >> 4;
                        final int Z = z >> 4;
                        x -= X << 4;
                        z -= Z << 4;
                        final ChunkWrapper wrap = new ChunkWrapper(world, X, Z);
                        BlockWrapper[][] result;
                        result = blocks.get(wrap);
                        if (!blocks.containsKey(wrap)) {
                            result = new BlockWrapper[16][];
                            blocks.put(wrap, result);
                        }
                        if ((y > 255) || (y < 0)) {
                            locked = false;
                            return;
                        }
                        if (result[y >> 4] == null) {
                            result[y >> 4] = new BlockWrapper[4096];
                        }
                        if (id == lastInt) {
                            result[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = lastBlock;
                        } else {
                            lastInt = id;
                            lastBlock = new BlockWrapper((short) id, (byte) 0);
                        }
                        result[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = lastBlock;
                        locked = false;
    }

    public static class ChunkWrapper {
        public final int x;
        public final int z;
        public final String world;

        public ChunkWrapper(final String world, final int x, final int z) {
            this.world = world;
            this.x = x;
            this.z = z;
        }

        @Override
        public int hashCode() {
            int result;
            if (this.x >= 0) {
                if (this.z >= 0) {
                    result = (this.x * this.x) + (3 * this.x) + (2 * this.x * this.z) + this.z + (this.z * this.z);
                } else {
                    final int y1 = -this.z;
                    result = (this.x * this.x) + (3 * this.x) + (2 * this.x * y1) + y1 + (y1 * y1) + 1;
                }
            } else {
                final int x1 = -this.x;
                if (this.z >= 0) {
                    result = -((x1 * x1) + (3 * x1) + (2 * x1 * this.z) + this.z + (this.z * this.z));
                } else {
                    final int y1 = -this.z;
                    result = -((x1 * x1) + (3 * x1) + (2 * x1 * y1) + y1 + (y1 * y1) + 1);
                }
            }
            result = (result * 31) + this.world.hashCode();
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ChunkWrapper other = (ChunkWrapper) obj;
            return ((this.x == other.x) && (this.z == other.z) && (this.world.equals(other.world)));
        }
    }
}
