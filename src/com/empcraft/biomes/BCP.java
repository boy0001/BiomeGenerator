package com.empcraft.biomes;

import java.util.List;

import net.minecraft.server.v1_7_R4.Chunk;
import net.minecraft.server.v1_7_R4.ChunkPosition;
import net.minecraft.server.v1_7_R4.EnumCreatureType;
import net.minecraft.server.v1_7_R4.IChunkProvider;
import net.minecraft.server.v1_7_R4.IProgressUpdate;
import net.minecraft.server.v1_7_R4.World;

public class BCP implements IChunkProvider {

    private final IChunkProvider cp;
    private final int[]          selection;

    public BCP(final IChunkProvider chunkProvider, final int[] selection) {
        this.cp = chunkProvider;
        this.selection = selection;
    }

    @Override
    public void c() {
        this.cp.c();
    }

    @Override
    public boolean canSave() {
        return this.cp.canSave();
    }

    @Override
    public ChunkPosition findNearestMapFeature(final World a, final String b, final int c, final int d, final int e) {
        return this.cp.findNearestMapFeature(a, b, c, d, e);
    }

    @Override
    public void getChunkAt(final IChunkProvider a, final int b, final int c) {
        this.cp.getChunkAt(a, b, c);
    }

    @Override
    public Chunk getChunkAt(final int a, final int b) {
        return this.cp.getChunkAt(a, b);
    }

    @Override
    public int getLoadedChunks() {
        return this.cp.getLoadedChunks();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List getMobsFor(final EnumCreatureType a, final int b, final int c, final int d) {
        return this.cp.getMobsFor(a, b, c, d);
    }

    @Override
    public String getName() {
        return this.cp.getName();
    }

    @Override
    public Chunk getOrCreateChunk(final int x, final int z) {
        return new BC(this.cp.getOrCreateChunk(x, z), this.selection);
    }

    @Override
    public boolean isChunkLoaded(final int a, final int b) {
        return this.cp.isChunkLoaded(a, b);
    }

    @Override
    public void recreateStructures(final int a, final int b) {
        this.cp.recreateStructures(a, b);
    }

    @Override
    public boolean saveChunks(final boolean a, final IProgressUpdate b) {
        return this.cp.saveChunks(a, b);
    }

    @Override
    public boolean unloadChunks() {
        return this.cp.unloadChunks();
    }

}
