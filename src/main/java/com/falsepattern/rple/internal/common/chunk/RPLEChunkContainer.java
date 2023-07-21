/*
 * Copyright (c) 2023 FalsePattern, Ven
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.rple.internal.common.chunk;

import com.falsepattern.lumina.api.chunk.LumiChunk;
import com.falsepattern.lumina.api.lighting.LightType;
import com.falsepattern.rple.api.color.ColorChannel;
import com.falsepattern.rple.internal.Tags;
import com.falsepattern.rple.internal.common.world.RPLEWorld;
import com.falsepattern.rple.internal.common.world.RPLEWorldRoot;
import lombok.val;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static com.falsepattern.lumina.api.lighting.LightType.BLOCK_LIGHT_TYPE;
import static com.falsepattern.lumina.api.lighting.LightType.SKY_LIGHT_TYPE;

public final class RPLEChunkContainer implements RPLEChunk {
//    private static final String VERSION_NBT_TAG_NAME = MOD_ID + "_version";
//    private static final String VERSION_NBT_TAG_VALUE = VERSION;
//
//    private static final String BLOCK_COLOR_CONFIG_HASH_NBT_TAG_NAME = "block_color_config_hash";

    private final ColorChannel channel;
    private final String chunkID;
    private final RPLEWorld world;
    private final RPLEChunkRoot root;
    private final LumiChunk lumiChunk;

    private final int chunkPosX;
    private final int chunkPosZ;
    private final int[] skyLightHeightMap;
    private final boolean[] outdatedSkylightColumns;

    private int minSkyLightHeight;
    private int queuedRandomLightUpdates;
    private boolean isLightingInitialized;

    public RPLEChunkContainer(ColorChannel channel, RPLEWorldRoot worldRoot, RPLEChunkRoot root, LumiChunk lumiChunk) {
        this.channel = channel;
        this.chunkID = Tags.MOD_ID + "_" + channel + "_chunk";
        this.world = worldRoot.rple$world(channel);
        this.lumiChunk = lumiChunk;
        this.root = root;

        this.chunkPosX = lumiChunk.lumi$chunkPosX();
        this.chunkPosZ = lumiChunk.lumi$chunkPosZ();
        this.skyLightHeightMap = new int[HEIGHT_MAP_ARRAY_SIZE];
        this.outdatedSkylightColumns = new boolean[HEIGHT_MAP_ARRAY_SIZE];

        this.minSkyLightHeight = Integer.MAX_VALUE;
        this.queuedRandomLightUpdates = 0;
        this.isLightingInitialized = false;
    }

    public RPLEChunkContainer(ColorChannel channel,
                              RPLEWorldRoot worldRoot,
                              RPLEChunkRoot root,
                              LumiChunk lumiChunk,
                              int[] skyLightHeightMap,
                              boolean[] outdatedSkylightColumns) {
        this.channel = channel;
        this.chunkID = Tags.MOD_ID + "_" + channel + "_chunk";
        this.world = worldRoot.rple$world(channel);
        this.lumiChunk = lumiChunk;
        this.root = root;

        this.chunkPosX = lumiChunk.lumi$chunkPosX();
        this.chunkPosZ = lumiChunk.lumi$chunkPosZ();
        this.skyLightHeightMap = skyLightHeightMap;
        this.outdatedSkylightColumns = outdatedSkylightColumns;

        this.minSkyLightHeight = Integer.MAX_VALUE;
        this.queuedRandomLightUpdates = 0;
        this.isLightingInitialized = false;
    }

    @Override
    public @NotNull RPLEChunkRoot lumi$root() {
        return root;
    }

    @Override
    public @NotNull RPLEWorld lumi$world() {
        return world;
    }

    @Override
    public @NotNull String lumi$chunkID() {
        return chunkID;
    }

    @Override
    public void lumi$writeToNBT(@NotNull NBTTagCompound output) {
//        output.setString(VERSION_NBT_TAG_NAME, VERSION_NBT_TAG_VALUE);
//        output.setString(BLOCK_COLOR_CONFIG_HASH_NBT_TAG_NAME, blockColorManager().configHashCode());
        output.setIntArray(SKY_LIGHT_HEIGHT_MAP_NBT_TAG_NAME, skyLightHeightMap);
        output.setBoolean(IS_LIGHT_INITIALIZED_NBT_TAG_NAME, isLightingInitialized);
    }

    @Override
    public void lumi$readFromNBT(@NotNull NBTTagCompound input) {
        isLightingInitialized = false;
        skyLightHeightMapValidCheck:
        {
//            val version = input.getString(VERSION_NBT_TAG_NAME);
//            if (!VERSION_NBT_TAG_VALUE.equals(version))
//                break skyLightHeightMapValidCheck;
//            val configHashCode = input.getString(BLOCK_COLOR_CONFIG_HASH_NBT_TAG_NAME);
//            if (!blockColorManager().configHashCode().equals(configHashCode))
//                break skyLightHeightMapValidCheck;
            if (!input.hasKey(IS_LIGHT_INITIALIZED_NBT_TAG_NAME, 1))
                break skyLightHeightMapValidCheck;
            val isLightInitializedInput = input.getBoolean(IS_LIGHT_INITIALIZED_NBT_TAG_NAME);
            if (!isLightInitializedInput)
                break skyLightHeightMapValidCheck;

            if (!input.hasKey(SKY_LIGHT_HEIGHT_MAP_NBT_TAG_NAME, 11))
                break skyLightHeightMapValidCheck;
            val skyLightHeightMapInput = input.getIntArray(SKY_LIGHT_HEIGHT_MAP_NBT_TAG_NAME);
            if (skyLightHeightMapInput.length != HEIGHT_MAP_ARRAY_SIZE)
                break skyLightHeightMapValidCheck;

            System.arraycopy(skyLightHeightMapInput, 0, skyLightHeightMap, 0, HEIGHT_MAP_ARRAY_SIZE);
            isLightingInitialized = true;
        }
        if (!isLightingInitialized)
            world.lumi$lightingEngine().handleChunkInit(this);
    }

    @Override
    public void lumi$writeToPacket(@NotNull ByteBuffer output) {
    }

    @Override
    public void lumi$readFromPacket(@NotNull ByteBuffer input) {
        isLightingInitialized = true;
    }

    @Override
    public @Nullable RPLESubChunk lumi$getSubChunkIfPrepared(int chunkPosY) {
        val lumiSubChunk = lumiChunk.lumi$getSubChunkIfPrepared(chunkPosY);
        if (!(lumiSubChunk instanceof RPLESubChunkRoot))
            return null;
        val subChunkRoot = (RPLESubChunkRoot) lumiSubChunk;
        return subChunkRoot.rple$subChunk(channel);
    }

    @Override
    public @NotNull RPLESubChunk lumi$getSubChunk(int chunkPosY) {
        val lumiSubChunk = lumiChunk.lumi$getSubChunk(chunkPosY);
        val subChunkRoot = (RPLESubChunkRoot) lumiSubChunk;
        return subChunkRoot.rple$subChunk(channel);
    }

    @Override
    public int lumi$chunkPosX() {
        return chunkPosX;
    }

    @Override
    public int lumi$chunkPosZ() {
        return chunkPosZ;
    }

    @Override
    public void lumi$queuedRandomLightUpdates(int queuedRandomLightUpdates) {
        this.queuedRandomLightUpdates = queuedRandomLightUpdates;
    }

    @Override
    public int lumi$queuedRandomLightUpdates() {
        return queuedRandomLightUpdates;
    }

    @Override
    public void lumi$resetQueuedRandomLightUpdates() {
        queuedRandomLightUpdates = 0;
    }

    @Override
    public int lumi$getBrightness(@NotNull LightType lightType,
                                  int subChunkPosX,
                                  int posY,
                                  int subChunkPosZ) {
        switch (lightType) {
            case BLOCK_LIGHT_TYPE:
                return lumi$getBrightness(subChunkPosX, posY, subChunkPosZ);
            case SKY_LIGHT_TYPE:
                return lumi$getSkyLightValue(subChunkPosX, posY, subChunkPosZ);
            default:
                return 0;
        }
    }

    @Override
    public int lumi$getBrightness(int subChunkPosX, int posY, int subChunkPosZ) {
        val blockBrightness = lumi$getBlockBrightness(subChunkPosX, posY, subChunkPosZ);
        val blockLightValue = lumi$getBlockLightValue(subChunkPosX, posY, subChunkPosZ);
        return Math.max(blockBrightness, blockLightValue);
    }

    @Override
    public int lumi$getLightValue(int subChunkPosX, int posY, int subChunkPosZ) {
        val blockLightValue = lumi$getBlockLightValue(subChunkPosX, posY, subChunkPosZ);
        val skyLightValue = lumi$getSkyLightValue(subChunkPosX, posY, subChunkPosZ);
        return Math.max(blockLightValue, skyLightValue);
    }

    @Override
    public void lumi$setLightValue(@NotNull LightType lightType,
                                   int subChunkPosX,
                                   int posY,
                                   int subChunkPosZ,
                                   int lightValue) {
        switch (lightType) {
            case BLOCK_LIGHT_TYPE:
                lumi$setBlockLightValue(subChunkPosX, posY, subChunkPosZ, lightValue);
                break;
            case SKY_LIGHT_TYPE:
                lumi$setSkyLightValue(subChunkPosX, posY, subChunkPosZ, lightValue);
                break;
            default:
                break;
        }
    }

    @Override
    public int lumi$getLightValue(@NotNull LightType lightType, int subChunkPosX, int posY, int subChunkPosZ) {
        switch (lightType) {
            case BLOCK_LIGHT_TYPE:
                return lumi$getBlockLightValue(subChunkPosX, posY, subChunkPosZ);
            case SKY_LIGHT_TYPE:
                return lumi$getSkyLightValue(subChunkPosX, posY, subChunkPosZ);
            default:
                return 0;
        }
    }

    @Override
    public void lumi$setBlockLightValue(int subChunkPosX, int posY, int subChunkPosZ, int lightValue) {
        val chunkPosY = (posY & 255) / 16;

        subChunkPosX &= 15;
        val subChunkPosY = posY & 15;
        subChunkPosZ &= 15;

        val subChunk = lumi$getSubChunk(chunkPosY);
        subChunk.lumi$setBlockLightValue(subChunkPosX, subChunkPosY, subChunkPosZ, lightValue);

        root.lumi$markDirty();
    }

    @Override
    public int lumi$getBlockLightValue(int subChunkPosX, int posY, int subChunkPosZ) {
        val chunkPosY = (posY & 255) / 16;

        val subChunk = lumi$getSubChunkIfPrepared(chunkPosY);
        if (subChunk == null)
            return BLOCK_LIGHT_TYPE.defaultLightValue();

        subChunkPosX &= 15;
        val subChunkPosY = posY & 15;
        subChunkPosZ &= 15;

        return subChunk.lumi$getBlockLightValue(subChunkPosX, subChunkPosY, subChunkPosZ);
    }

    @Override
    public void lumi$setSkyLightValue(int subChunkPosX, int posY, int subChunkPosZ, int lightValue) {
        if (!world.lumi$root().lumi$hasSky())
            return;

        val chunkPosY = (posY & 255) / 16;

        subChunkPosX &= 15;
        val subChunkPosY = posY & 15;
        subChunkPosZ &= 15;

        val subChunk = lumi$getSubChunk(chunkPosY);
        subChunk.lumi$setSkyLightValue(subChunkPosX, subChunkPosY, subChunkPosZ, lightValue);

        root.lumi$markDirty();
    }

    @Override
    public int lumi$getSkyLightValue(int subChunkPosX, int posY, int subChunkPosZ) {
        if (!world.lumi$root().lumi$hasSky())
            return 0;

        val chunkPosY = (posY & 255) >> 4;

        subChunkPosX &= 15;
        subChunkPosZ &= 15;

        val subChunk = lumi$getSubChunkIfPrepared(chunkPosY);
        if (subChunk == null) {
            if (lumi$canBlockSeeSky(subChunkPosX, posY, subChunkPosZ))
                return SKY_LIGHT_TYPE.defaultLightValue();
            return 0;
        }

        val subChunkPosY = posY & 15;
        return subChunk.lumi$getSkyLightValue(subChunkPosX, subChunkPosY, subChunkPosZ);
    }

    @Override
    public int lumi$getBlockBrightness(int subChunkPosX, int posY, int subChunkPosZ) {
        val block = root.lumi$getBlock(subChunkPosX, posY, subChunkPosZ);
        val blockMeta = root.lumi$getBlockMeta(subChunkPosX, posY, subChunkPosZ);
        return lumi$getBlockBrightness(block, blockMeta, subChunkPosX, posY, subChunkPosZ);
    }

    @Override
    public int lumi$getBlockOpacity(int subChunkPosX, int posY, int subChunkPosZ) {
        val block = root.lumi$getBlock(subChunkPosX, posY, subChunkPosZ);
        val blockMeta = root.lumi$getBlockMeta(subChunkPosX, posY, subChunkPosZ);
        return lumi$getBlockOpacity(block, blockMeta, subChunkPosX, posY, subChunkPosZ);
    }

    @Override
    public int lumi$getBlockBrightness(@NotNull Block block,
                                       int blockMeta,
                                       int subChunkPosX,
                                       int posY,
                                       int subChunkPosZ) {
        val posX = (chunkPosX << 4) + subChunkPosX;
        val posZ = (chunkPosZ << 4) + subChunkPosZ;
        return world.lumi$getBlockBrightness(block, blockMeta, posX, posY, posZ);
    }

    @Override
    public int lumi$getBlockOpacity(@NotNull Block block,
                                    int blockMeta,
                                    int subChunkPosX,
                                    int posY,
                                    int subChunkPosZ) {
        val posX = (chunkPosX << 4) + subChunkPosX;
        val posZ = (chunkPosZ << 4) + subChunkPosZ;
        return world.lumi$getBlockOpacity(block, blockMeta, posX, posY, posZ);
    }

    @Override
    public boolean lumi$canBlockSeeSky(int subChunkPosX, int posY, int subChunkPosZ) {
        subChunkPosX &= 15;
        subChunkPosZ &= 15;
        val index = subChunkPosX + (subChunkPosZ << 4);
        val maxPosY = skyLightHeightMap[index];
        return maxPosY <= posY;
    }

    @Override
    public void lumi$skyLightHeight(int subChunkPosX, int subChunkPosZ, int skyLightHeight) {
        subChunkPosX &= 15;
        subChunkPosZ &= 15;
        val index = subChunkPosX + (subChunkPosZ << 4);
        skyLightHeightMap[index] = skyLightHeight;
    }

    @Override
    public int lumi$skyLightHeight(int subChunkPosX, int subChunkPosZ) {
        subChunkPosX &= 15;
        subChunkPosZ &= 15;
        val index = subChunkPosX + (subChunkPosZ << 4);
        return skyLightHeightMap[index];
    }

    @Override
    public void lumi$minSkyLightHeight(int minSkyLightHeight) {
        this.minSkyLightHeight = minSkyLightHeight;
    }

    @Override
    public int lumi$minSkyLightHeight() {
        return minSkyLightHeight;
    }

    @Override
    public void lumi$resetSkyLightHeightMap() {
        Arrays.fill(skyLightHeightMap, Integer.MAX_VALUE);
        minSkyLightHeight = Integer.MAX_VALUE;
    }

    @Override
    public void lumi$isHeightOutdated(int subChunkPosX, int subChunkPosZ, boolean isHeightOutdated) {
        subChunkPosX &= 15;
        subChunkPosZ &= 15;
        val index = subChunkPosX + (subChunkPosZ << 4);
        outdatedSkylightColumns[index] = isHeightOutdated;
    }

    @Override
    public boolean lumi$isHeightOutdated(int subChunkPosX, int subChunkPosZ) {
        subChunkPosX &= 15;
        subChunkPosZ &= 15;
        val index = subChunkPosX + (subChunkPosZ << 4);
        return outdatedSkylightColumns[index];
    }

    @Override
    public void lumi$resetOutdatedHeightFlags() {
        Arrays.fill(outdatedSkylightColumns, true);
    }

    @Override
    public void lumi$isLightingInitialized(boolean isLightingInitialized) {
        this.isLightingInitialized = isLightingInitialized;
    }

    @Override
    public boolean lumi$isLightingInitialized() {
        return isLightingInitialized;
    }

    @Override
    public void lumi$resetLighting() {
        isLightingInitialized = false;
        world.lumi$lightingEngine().handleChunkInit(this);
    }
}