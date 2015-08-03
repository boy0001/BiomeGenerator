package com.empcraft.biomes;

import static com.empcraft.biomes.ReflectionUtils.getRefClass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;

import com.empcraft.biomes.ReflectionUtils.RefClass;
import com.empcraft.biomes.ReflectionUtils.RefConstructor;
import com.empcraft.biomes.ReflectionUtils.RefField;
import com.empcraft.biomes.ReflectionUtils.RefMethod;

public class SendChunk_ {

    // Ref Class
    private static final RefClass classWorld             = getRefClass("{nms}.World");
    private static final RefClass classEntityPlayer      = getRefClass("{nms}.EntityPlayer");
    private static final RefClass classChunkCoordIntPair = getRefClass("{nms}.ChunkCoordIntPair");
    private static final RefClass classCraftChunk        = getRefClass("{cb}.CraftChunk");
    private static final RefClass classChunk             = getRefClass("{nms}.Chunk");

    // Ref Method
    private static RefMethod      methodGetHandle;

    // Ref Field
    private static RefField       chunkCoordIntPairQueue;
    private static RefField       players;
    private static RefField       locX;
    private static RefField       locZ;
    private static RefField       world;

    // Ref Constructor
    private static RefConstructor ChunkCoordIntPairCon;

    /**
     * Constructor
     *
     * @throws NoSuchMethodException
     */
    public SendChunk_() throws NoSuchMethodException {
        methodGetHandle = classCraftChunk.getMethod("getHandle");
        chunkCoordIntPairQueue = classEntityPlayer.getField("chunkCoordIntPairQueue");

        players = classWorld.getField("players");
        locX = classEntityPlayer.getField("locX");
        locZ = classEntityPlayer.getField("locZ");

        world = classChunk.getField("world");

        ChunkCoordIntPairCon = classChunkCoordIntPair.getConstructor(int.class, int.class);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static void sendChunk(final Collection<Chunk> chunks) {
        int diffx, diffz;
        final int view = Bukkit.getServer().getViewDistance() << 4;
        for (final Chunk chunk : chunks) {
            System.out.print(chunk);
            System.out.print(chunk == null);
            final Object c = methodGetHandle.of(chunk).call();

            final Object w = world.of(c).get();
            final Object p = players.of(w).get();

            for (final Object ep : (List<Object>) p) {
                final int x = ((Double) locX.of(ep).get()).intValue();
                final int z = ((Double) locZ.of(ep).get()).intValue();
                diffx = Math.abs(x - (chunk.getX() << 4));
                diffz = Math.abs(z - (chunk.getZ() << 4));
                if ((diffx <= view) && (diffz <= view)) {
                    final Object pair = ChunkCoordIntPairCon.create(chunk.getX(), chunk.getZ());
                    final Object pq = chunkCoordIntPairQueue.of(ep).get();
                    ((List) pq).add(pair);
                }
            }
        }
    }
}
