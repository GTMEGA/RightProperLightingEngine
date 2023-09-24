package com.falsepattern.rple.internal.common.cache;

import com.falsepattern.lumina.api.chunk.LumiChunkRoot;
import com.falsepattern.lumina.api.lighting.LightType;
import com.falsepattern.lumina.api.world.LumiWorld;
import com.falsepattern.rple.api.common.RPLEColorUtil;
import com.falsepattern.rple.api.common.block.RPLEBlock;
import com.falsepattern.rple.api.common.color.ColorChannel;
import com.falsepattern.rple.api.common.color.RPLEColor;
import com.falsepattern.rple.internal.client.render.TessellatorBrightnessHelper;
import com.falsepattern.rple.internal.common.chunk.RPLEChunkRoot;
import com.falsepattern.rple.internal.common.world.RPLEWorld;
import com.falsepattern.rple.internal.common.world.RPLEWorldRoot;
import lombok.Getter;
import lombok.val;
import lombok.var;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraftforge.common.util.ForgeDirection;

import java.util.Arrays;
import java.util.BitSet;

import static com.falsepattern.lumina.api.lighting.LightType.BLOCK_LIGHT_TYPE;
import static com.falsepattern.lumina.api.lighting.LightType.SKY_LIGHT_TYPE;
import static net.minecraftforge.common.util.ForgeDirection.EAST;
import static net.minecraftforge.common.util.ForgeDirection.NORTH;
import static net.minecraftforge.common.util.ForgeDirection.SOUTH;
import static net.minecraftforge.common.util.ForgeDirection.UP;
import static net.minecraftforge.common.util.ForgeDirection.WEST;


//TODO: [CACHE_DONE] When a light color value is requested, it will get it from the requested block
//TODO: [CACHE_DONE] Said value MUST NOT be stored as the provided object, and must be IMMEDIATELY unpacked into the integer RGB values
//TODO: [CACHE_DONE] This is because there are ZERO promises on the value being immutable
//
//TODO: [CACHE_DONE] If a new value is queried and provided by any of the non-root caches, it must be propagated to ALL other caches
//TODO: [CACHE_DONE] This is VERY IMPORTANT! as getting the light/opacity value FROM is expensive!
public final class DynamicBlockCacheRoot implements RPLEBlockCacheRoot {
    static final int CHUNK_XZ_SIZE = 16;
    static final int CHUNK_XZ_BITMASK = 15;
    static final int CHUNK_Y_SIZE = 256;
    static final int CHUNK_Y_BITMASK = 255;
    static final int CACHE_CHUNK_XZ_SIZE = 3;
    static final int CENTER_TO_MIN_DISTANCE = CACHE_CHUNK_XZ_SIZE / 2;
    static final int TOTAL_CACHED_CHUNK_COUNT = CACHE_CHUNK_XZ_SIZE * CACHE_CHUNK_XZ_SIZE;
    static final int ELEMENT_COUNT_PER_CHUNK = CHUNK_XZ_SIZE * CHUNK_XZ_SIZE * CHUNK_Y_SIZE;
    static final int ELEMENT_COUNT_PER_CACHED_THING = TOTAL_CACHED_CHUNK_COUNT * ELEMENT_COUNT_PER_CHUNK;

    static final int BITSIZE_CHUNK_XZ = 4;
    static final int BITSIZE_CHUNK_Y = 8;
    static final int BITSHIFT_CHUNK_Z = BITSIZE_CHUNK_XZ + BITSIZE_CHUNK_Y;
    static final int BITSHIFT_CHUNK_X = BITSIZE_CHUNK_Y;
    static final int BITSHIFT_CHUNK = BITSIZE_CHUNK_XZ + BITSIZE_CHUNK_XZ + BITSIZE_CHUNK_Y;

    static final int COLOR_CHANNEL_COUNT = ColorChannel.values().length;

    private final RPLEWorldRoot worldRoot;

    private final DynamicBlockCache[] blockCaches = new DynamicBlockCache[COLOR_CHANNEL_COUNT];

    // Z/X 3/3
    private final RPLEChunkRoot[] rootChunks = new RPLEChunkRoot[TOTAL_CACHED_CHUNK_COUNT];
    // Used for populating
    private final ChunkCacheCompact helperCache = new ChunkCacheCompact();

    // CZ/CX/Z/X/Y 3/3/16/16/256
    private final Block[] blocks = new Block[ELEMENT_COUNT_PER_CACHED_THING];
    // CZ/CX/Z/X/Y 3/3/16/16/256
    private final int[] blockMetas = new int[ELEMENT_COUNT_PER_CACHED_THING];
    // CZ/CX/Z/X/Y 3/3/16/16/256
    private final BitSet airChecks = new BitSet(ELEMENT_COUNT_PER_CACHED_THING);

    // CZ/CX/Z/X/Y 3/3/16/16/256
    private final BitSet checkedBlocks = new BitSet(ELEMENT_COUNT_PER_CACHED_THING);

    // CZ/CX/Z/X/Y 3/3/16/16/256
    private final short[] brightnesses = new short[ELEMENT_COUNT_PER_CACHED_THING];
    // CZ/CX/Z/X/Y 3/3/16/16/256
    private final short[] translucencies = new short[ELEMENT_COUNT_PER_CACHED_THING];

    private int minChunkPosX;
    private int minChunkPosZ;
    private int maxChunkPosX;
    private int maxChunkPosZ;

    @Getter
    private boolean isReady;

    public DynamicBlockCacheRoot(@NotNull RPLEWorldRoot worldRoot) {
        this.worldRoot = worldRoot;
    }

    @Override
    public @NotNull String lumi$blockCacheRootID() {
        return "rple_dynamic_block_cache_root";
    }

    @Override
    public @NotNull RPLEBlockCache lumi$createBlockCache(LumiWorld world) {
        if (!(world instanceof RPLEWorld))
            throw new IllegalArgumentException("World must be an RPLEWorld");
        val rpleWorld = (RPLEWorld) world;
        val channel = rpleWorld.rple$channel();
        val cacheIndex = channel.ordinal();
        if (blockCaches[cacheIndex] == null)
            blockCaches[cacheIndex] = new DynamicBlockCache(rpleWorld);
        else if (blockCaches[cacheIndex].lumi$world() != world)
            throw new IllegalArgumentException("Block cache already created for a different world");

        return blockCaches[cacheIndex];
    }

    @Override
    public int lumi$minChunkPosX() {
        return minChunkPosX;
    }

    @Override
    public int lumi$minChunkPosZ() {
        return minChunkPosZ;
    }

    @Override
    public int lumi$maxChunkPosX() {
        return maxChunkPosX;
    }

    @Override
    public int lumi$maxChunkPosZ() {
        return maxChunkPosZ;
    }

    @Override
    public void lumi$clearCache() {
        if (!isReady)
            return;

        // We don't need to clear the "blocks" array because blocks are singletons
        Arrays.fill(rootChunks, null);
        checkedBlocks.clear();

        isReady = false;
    }

    @Override
    public @NotNull String lumi$blockStorageRootID() {
        return "rple_dynamic_block_cache_root";
    }

    @Override
    public boolean lumi$isClientSide() {
        return worldRoot.lumi$isClientSide();
    }

    @Override
    public boolean lumi$hasSky() {
        return worldRoot.lumi$hasSky();
    }

    @Override
    public @NotNull Block lumi$getBlock(int posX, int posY, int posZ) {
        return blocks[getIndex(posX, posY, posZ)];
    }

    @Override
    public int lumi$getBlockMeta(int posX, int posY, int posZ) {
        return blockMetas[getIndex(posX, posY, posZ)];
    }

    @Override
    public boolean lumi$isAirBlock(int posX, int posY, int posZ) {
        return airChecks.get(getIndex(posX, posY, posZ));
    }

    @Override
    public @Nullable TileEntity lumi$getTileEntity(int posX, int posY, int posZ) {
        val block = lumi$getBlock(posX, posY, posZ);
        val blockMeta = lumi$getBlockMeta(posX, posY, posZ);
        if (!block.hasTileEntity(blockMeta))
            return null;
        val chunkRoot = chunkFromBlockPos(posX, posZ);
        if (chunkRoot == null)
            return null;

        val chunkBase = (Chunk) chunkRoot;
        val subChunkX = posX & CHUNK_XZ_BITMASK;
        val subChunkZ = posZ & CHUNK_XZ_BITMASK;
        return chunkBase.getTileEntityUnsafe(subChunkX, posY, subChunkZ);
    }

    @Override
    public @NotNull RPLEBlockCache rple$blockCache(@NotNull ColorChannel channel) {
        val cacheIndex = channel.ordinal();
        if (blockCaches[cacheIndex] == null)
            throw new IllegalStateException("Block cache not created for channel " + channel.name());
        return blockCaches[cacheIndex];
    }

    @Override
    public @NotNull RPLEBlockStorage rple$blockStorage(@NotNull ColorChannel channel) {
        return rple$blockCache(channel);
    }

    private int getIndex(int posX, int posY, int posZ) {
        val theChunk = chunkFromBlockPos(posX, posZ);
        val cacheIndex = cacheIndexFromBlockPos(posX, posY, posZ);
        if (checkedBlocks.get(cacheIndex))
            return cacheIndex;

        if (theChunk == null) {
            blocks[cacheIndex] = Blocks.air;
            blockMetas[cacheIndex] = 0;
            airChecks.clear(cacheIndex);
            checkedBlocks.clear(cacheIndex);
            brightnesses[cacheIndex] = 0;
            translucencies[cacheIndex] = 0;
        } else {
            val subChunkX = posX & CHUNK_XZ_BITMASK;
            val subChunkZ = posZ & CHUNK_XZ_BITMASK;

            val block = theChunk.lumi$getBlock(subChunkX, posY, subChunkZ);
            val blockMeta = theChunk.lumi$getBlockMeta(subChunkX, posY, subChunkZ);

            blocks[cacheIndex] = block;
            blockMetas[cacheIndex] = blockMeta;

            airChecks.set(cacheIndex, block.isAir(helperCache, posX, posY, posZ));

            val blockBrightness = ((RPLEBlock)block).rple$getBrightnessColor(helperCache, blockMeta, posX, posY, posZ);
            val blockTranslucency = ((RPLEBlock)block).rple$getTranslucencyColor(helperCache, blockMeta, posX, posY, posZ);

            brightnesses[cacheIndex] = colorToCache(blockBrightness);
            translucencies[cacheIndex] = colorToCache(blockTranslucency);

            checkedBlocks.set(cacheIndex);
        }
        return cacheIndex;
    }

    private void setupCache(int centerChunkPosX, int centerChunkPosZ) {
        val minChunkPosX = centerChunkPosX - CENTER_TO_MIN_DISTANCE;
        val minChunkPosZ = centerChunkPosZ - CENTER_TO_MIN_DISTANCE;

        val maxChunkPosX = minChunkPosX + CACHE_CHUNK_XZ_SIZE;
        val maxChunkPosZ = minChunkPosZ + CACHE_CHUNK_XZ_SIZE;

        for (var chunkPosZ = 0; chunkPosZ < CACHE_CHUNK_XZ_SIZE; chunkPosZ++) {
            val realChunkPosZ = chunkPosZ + minChunkPosZ;
            for (var chunkPosX = 0; chunkPosX < CACHE_CHUNK_XZ_SIZE; chunkPosX++) {
                val rootChunkIndex = (chunkPosZ * CACHE_CHUNK_XZ_SIZE) + chunkPosX;
                val realChunkPosX = chunkPosX + minChunkPosX;

                val chunkProvider = worldRoot.lumi$chunkProvider();
                chunkExistsCheck:
                {
                    if (!chunkProvider.chunkExists(realChunkPosX, realChunkPosZ))
                        break chunkExistsCheck;
                    val chunkBase = chunkProvider.provideChunk(realChunkPosX, realChunkPosZ);
                    if (!(chunkBase instanceof RPLEChunkRoot))
                        break chunkExistsCheck;
                    val chunkRoot = (RPLEChunkRoot) chunkBase;
                    rootChunks[rootChunkIndex] = chunkRoot;
                }
                rootChunks[rootChunkIndex] = null;
            }
        }
        helperCache.init(rootChunks, CACHE_CHUNK_XZ_SIZE, minChunkPosX, minChunkPosZ);
        this.minChunkPosX = minChunkPosX;
        this.minChunkPosZ = minChunkPosZ;
        this.maxChunkPosX = maxChunkPosX;
        this.maxChunkPosZ = maxChunkPosZ;
        checkedBlocks.clear();
        isReady = true;
    }

    int cacheIndexFromBlockPos(int posX, int posY, int posZ) {
        val chunkPosZ = (posZ >> BITSIZE_CHUNK_XZ) - minChunkPosZ;
        val chunkPosX = (posX >> BITSIZE_CHUNK_XZ) - minChunkPosX;

        // val chunkBase = (chunkPosZ * CACHE_CHUNK_XZ_SIZE + chunkPosX) * ELEMENT_COUNT_PER_CHUNK;
        // chunk element count is always 16*16*256, so we optimize away the multiply
        val chunkBase = (chunkPosZ * CACHE_CHUNK_XZ_SIZE + chunkPosX) << BITSHIFT_CHUNK;

        val subChunkZ = posZ & CHUNK_XZ_BITMASK;
        val subChunkX = posX & CHUNK_XZ_BITMASK;
        val subChunkY = posY & CHUNK_Y_BITMASK;

        //val subChunkOffset = (subChunkZ * CHUNK_XZ_SIZE + subChunkX) * CHUNK_Y_SIZE + subChunkY;
        //All these are constants so we can reduce it to bit shuffling
        val subChunkOffset = (subChunkZ << BITSHIFT_CHUNK_Z) | (subChunkX << BITSHIFT_CHUNK_X) | subChunkY;
        int index = chunkBase | subChunkOffset;
        if (index < 0 || index >= blocks.length) {
            chunkFromBlockPos(posX, posZ);
            return cacheIndexFromBlockPos(posX, posY, posZ);
        } else {
            return index;
        }
    }

    private @Nullable LumiChunkRoot chunkFromBlockPos(int posX, int posZ) {
        val baseChunkPosX = posX >> BITSIZE_CHUNK_XZ;
        val baseChunkPosZ = posZ >> BITSIZE_CHUNK_XZ;
        if (!isReady) {
            setupCache(baseChunkPosX, baseChunkPosZ);
        }

        if (baseChunkPosX < minChunkPosX || baseChunkPosX >= maxChunkPosX ||
            baseChunkPosZ < minChunkPosZ || baseChunkPosZ >= maxChunkPosZ) {
            setupCache(baseChunkPosX, baseChunkPosZ);
        }
        val chunkPosX = baseChunkPosX - minChunkPosX;
        val chunkPosZ = baseChunkPosZ - minChunkPosZ;

        val rootChunk = rootChunks[chunkPosZ * CACHE_CHUNK_XZ_SIZE + chunkPosX];
        if (rootChunk != null)
            return rootChunk;

        val chunkProvider = worldRoot.lumi$chunkProvider();
        if (!chunkProvider.chunkExists(baseChunkPosX, baseChunkPosZ))
            return null;
        val chunkBase = chunkProvider.provideChunk(baseChunkPosX, baseChunkPosZ);
        if (!(chunkBase instanceof RPLEChunkRoot))
            return null;

        return rootChunks[chunkPosZ * CACHE_CHUNK_XZ_SIZE + chunkPosX] = (RPLEChunkRoot) chunkBase;
    }


    private static final int CACHE_CHANNEL_BITMASK = 0xf;
    private static final int CACHE_ELEMENT_BITMASK = 0xfff;
    private static final int CACHE_ENTRY_RED_OFFSET = 0;
    private static final int CACHE_ENTRY_GREEN_OFFSET = 4;
    private static final int CACHE_ENTRY_BLUE_OFFSET = 8;

    //Utility methods
    private static short colorToCache(RPLEColor color) {
        int red = color.red();
        int green = color.green();
        int blue = color.blue();
        return (short) ((red & CACHE_CHANNEL_BITMASK) << CACHE_ENTRY_RED_OFFSET |
                       (green & CACHE_CHANNEL_BITMASK) << CACHE_ENTRY_GREEN_OFFSET |
                       (blue & CACHE_CHANNEL_BITMASK) << CACHE_ENTRY_BLUE_OFFSET);
    }

    private static int cacheToChannel(short cacheableS, ColorChannel channel) {
        int cacheable = cacheableS & CACHE_ELEMENT_BITMASK;
        switch (channel) {
            default:
            case RED_CHANNEL:
                return (cacheable >>> CACHE_ENTRY_RED_OFFSET) & CACHE_CHANNEL_BITMASK;
            case GREEN_CHANNEL:
                return (cacheable >>> CACHE_ENTRY_GREEN_OFFSET) & CACHE_CHANNEL_BITMASK;
            case BLUE_CHANNEL:
                return (cacheable >>> CACHE_ENTRY_BLUE_OFFSET) & CACHE_CHANNEL_BITMASK;
        }
    }

    //TODO: [CACHE_DONE] Will store one brightness/opacity per block, per channel
    //TODO: [CACHE_DONE] Unlike LUMINA, it will ask the root for said values instead of getting them on it's own
    public final class DynamicBlockCache implements RPLEBlockCache {
        private final RPLEWorld world;

        public DynamicBlockCache(@NotNull RPLEWorld world) {
            this.world = world;
        }

        @Override
        public @NotNull ColorChannel rple$channel() {
            return world.rple$channel();
        }

        @Override
        public @NotNull RPLEBlockCacheRoot lumi$root() {
            return DynamicBlockCacheRoot.this;
        }

        @Override
        public @NotNull String lumi$BlockCacheID() {
            return "rple_dynamic_block_cache";
        }

        @Override
        public void lumi$clearCache() {
        }

        @Override
        public @NotNull String lumi$blockStorageID() {
            return "rple_dynamic_block_cache";
        }

        @Override
        public @NotNull RPLEWorld lumi$world() {
            return world;
        }

        @Override
        public int rple$getChannelBrightnessForTessellator(int posX, int posY, int posZ, int minBlockLight) {
            // TODO: [CACHE] Equal implementation to one found in RPLEWorldContainer, optionally cached.
            //  FP: Will do once the serverside works
            var blockLightValue = rple$getChannelLightValueForRender(BLOCK_LIGHT_TYPE, posX, posY, posZ);
            blockLightValue = Math.max(blockLightValue, minBlockLight);
            val skyLightValue = rple$getChannelLightValueForRender(SKY_LIGHT_TYPE, posX, posY, posZ);
            return TessellatorBrightnessHelper.lightLevelsToBrightnessForTessellator(blockLightValue, skyLightValue);
        }

        @Override
        public int rple$getChannelLightValueForRender(@NotNull LightType lightType, int posX, int posY, int posZ) {
            // TODO: [CACHE] Equal implementation to one found in RPLEWorldContainer, optionally cached.
            //  FP: Will do once the serverside works
            if (lightType == SKY_LIGHT_TYPE && !DynamicBlockCacheRoot.this.lumi$hasSky())
                return 0;

            if (posY < 0) {
                posY = 0;
            } else if (posY > 255) {
                return lightType.defaultLightValue();
            }
            if (posX < -30000000 || posX >= 30000000)
                return lightType.defaultLightValue();
            if (posZ < -30000000 || posZ >= 30000000)
                return lightType.defaultLightValue();

            val block = DynamicBlockCacheRoot.this.lumi$getBlock(posX, posY, posZ);
            if (block.getUseNeighborBrightness()) {
                var lightValue = 0;
                lightValue = Math.max(lightValue, getNeighborLightValue(lightType, posX, posY, posZ, UP));
                lightValue = Math.max(lightValue, getNeighborLightValue(lightType, posX, posY, posZ, NORTH));
                lightValue = Math.max(lightValue, getNeighborLightValue(lightType, posX, posY, posZ, SOUTH));
                lightValue = Math.max(lightValue, getNeighborLightValue(lightType, posX, posY, posZ, WEST));
                lightValue = Math.max(lightValue, getNeighborLightValue(lightType, posX, posY, posZ, EAST));
                return lightValue;
            }
            return lumi$getBrightness(lightType, posX, posY, posZ);
        }

        private int getNeighborLightValue(LightType lightType, int posX, int posY, int posZ, ForgeDirection direction) {
            posX += direction.offsetX;
            posY += direction.offsetY;
            posZ += direction.offsetZ;
            return lumi$getBrightness(lightType, posX, posY, posZ);
        }

        @Override
        public int lumi$getBrightness(@NotNull LightType lightType, int posX, int posY, int posZ) {
            return world.lumi$getBrightness(lightType, posX, posY, posZ);
        }

        @Override
        public int lumi$getBrightness(int posX, int posY, int posZ) {
            return world.lumi$getBrightness(posX, posY, posZ);
        }

        @Override
        public int lumi$getLightValue(int posX, int posY, int posZ) {
            return world.lumi$getLightValue(posX, posY, posZ);
        }

        @Override
        public int lumi$getLightValue(@NotNull LightType lightType, int posX, int posY, int posZ) {
            return world.lumi$getLightValue(lightType, posX, posY, posZ);
        }

        @Override
        public int lumi$getBlockLightValue(int posX, int posY, int posZ) {
            return world.lumi$getBlockLightValue(posX, posY, posZ);
        }

        @Override
        public int lumi$getSkyLightValue(int posX, int posY, int posZ) {
            return world.lumi$getSkyLightValue(posX, posY, posZ);
        }

        @Override
        public int lumi$getBlockBrightness(int posX, int posY, int posZ) {
            val index = DynamicBlockCacheRoot.this.getIndex(posX, posY, posZ);
            short cachedBrightness = brightnesses[index];
            return cacheToChannel(cachedBrightness, rple$channel());
        }

        @Override
        public int lumi$getBlockOpacity(int posX, int posY, int posZ) {
            val index = DynamicBlockCacheRoot.this.getIndex(posX, posY, posZ);
            short cachedTranslucency = translucencies[index];
            return RPLEColorUtil.invertColorComponent(cacheToChannel(cachedTranslucency, rple$channel()));
        }

        @Override
        public int lumi$getBlockBrightness(@NotNull Block block, int blockMeta, int posX, int posY, int posZ) {
            return lumi$getBlockBrightness(posX, posY, posZ);
        }

        @Override
        public int lumi$getBlockOpacity(@NotNull Block block, int blockMeta, int posX, int posY, int posZ) {
            return lumi$getBlockOpacity(posX, posY, posZ);
        }
    }

}
