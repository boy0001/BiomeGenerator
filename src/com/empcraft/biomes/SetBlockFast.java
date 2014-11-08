package com.empcraft.biomes;

import static com.empcraft.biomes.ReflectionUtils.getRefClass;

import org.bukkit.Bukkit;

import com.empcraft.biomes.ReflectionUtils.RefClass;
import com.empcraft.biomes.ReflectionUtils.RefMethod;

/**
 * SetBlockFast class<br>
 * Used to do fast world editing
 */
public class SetBlockFast {

    private static final RefClass classBlock      = getRefClass("{nms}.Block");
    private static final RefClass classChunk      = getRefClass("{nms}.Chunk");
    private static final RefClass classWorld      = getRefClass("{nms}.World");
    private static final RefClass classCraftWorld = getRefClass("{cb}.CraftWorld");

    private static RefMethod      methodGetHandle;
    private static RefMethod      methodGetChunkAt;
    private static RefMethod      methodA;
    private static RefMethod      methodGetById;

    public static boolean set(final org.bukkit.World world, final int x, final int y, final int z, final int blockId, final byte data) throws NoSuchMethodException {

        final Object w = methodGetHandle.of(world).call();
        final Object chunk = methodGetChunkAt.of(w).call(x >> 4, z >> 4);
        final Object block = methodGetById.of(null).call(blockId);
        methodA.of(chunk).call(x & 0x0f, y, z & 0x0f, block, data);
        return true;
    }

    public SetBlockFast() throws NoSuchMethodException {
        methodGetHandle = classCraftWorld.getMethod("getHandle");
        methodGetChunkAt = classWorld.getMethod("getChunkAt", int.class, int.class);
        methodA = classChunk.getMethod("a", int.class, int.class, int.class, classBlock, int.class);
        methodGetById = classBlock.getMethod("getById", int.class);
    }
}
