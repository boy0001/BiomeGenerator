package com.empcraft.biomes.generators;

import com.empcraft.biomes.ReflectionUtils;

import net.minecraft.server.v1_7_R4.Block;
import net.minecraft.server.v1_7_R4.Blocks;
import net.minecraft.server.v1_7_R4.Chunk;

public class BC extends Chunk {

    private final Chunk chunk;
    private final int[] selection;

    public BC(final Chunk chunk, final int[] selection) {
        super(chunk.world, chunk.locX, chunk.locZ);

        this.chunk = chunk;
        this.selection = selection;

        this.world = this.chunk.world;
        this.heightMap = this.chunk.heightMap;
        this.tileEntities = this.chunk.tileEntities;
        this.entitySlices = this.chunk.entitySlices;
        this.done = this.chunk.done;
        this.lit = this.chunk.lit;
        this.lastSaved = this.chunk.lastSaved;

        this.a(this.chunk.getSections());
        this.a(this.chunk.m());
        this.b = this.chunk.b;
        this.c = this.chunk.c;
        this.d = this.chunk.d;
        this.m = this.chunk.m;
        this.n = this.chunk.n;
        this.o = this.chunk.o;
        this.q = this.chunk.q;
        this.r = this.chunk.r;
        this.s = this.chunk.s;

        try {
            ReflectionUtils.setFieldValue(Chunk.class, "w", this, ReflectionUtils.getFieldValue(Chunk.class, "w", Boolean.class, this.chunk));
            ReflectionUtils.setFieldValue(Chunk.class, "x", this, ReflectionUtils.getFieldValue(Chunk.class, "x", Integer.class, this.chunk));
        } catch (final NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean a(final int x, final int y, final int z, final Block block, final int data) {
        final int wx = (this.chunk.locX * 16) + x;
        final int wz = (this.chunk.locZ * 16) + z;

        if ((wx >= this.selection[0]) && (wz >= this.selection[1]) && (wx <= this.selection[2]) && (wz <= this.selection[3])) {
            return this.chunk.a(x, y, z, block, data);
        }

        return false;
    }

    @Override
    public int b(final int x, final int y, final int z) {
        return this.getType(x, y, z).k();
    }

    @Override
    public int getData(final int x, final int y, final int z) {
        final int wx = (this.chunk.locX * 16) + x;
        final int wz = (this.chunk.locZ * 16) + z;

        if ((wx >= this.selection[0]) && (wz >= this.selection[1]) && (wx <= this.selection[2]) && (wz <= this.selection[3])) {
            return this.chunk.getData(x, y, z);
        }

        return 0;
    }

    @Override
    public Block getType(final int x, final int y, final int z) {
        final int wx = (this.chunk.locX * 16) + x;
        final int wz = (this.chunk.locZ * 16) + z;

        if ((wx >= this.selection[0]) && (wz >= this.selection[1]) && (wx <= this.selection[2]) && (wz <= this.selection[3])) {
            return this.chunk.getType(x, y, z);
        }

        return Blocks.AIR;
    }

}