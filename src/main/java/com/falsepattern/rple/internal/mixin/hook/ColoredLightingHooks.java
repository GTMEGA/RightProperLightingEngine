/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.rple.internal.mixin.hook;

import com.falsepattern.rple.api.client.RPLEBlockBrightnessUtil;
import com.falsepattern.rple.api.common.RPLEColorUtil;
import com.falsepattern.rple.api.common.block.RPLEBlock;
import com.falsepattern.rple.internal.client.render.CookieMonster;
import com.falsepattern.rple.internal.client.render.CookieMonsterHelper;
import com.falsepattern.rple.internal.client.render.EntityColorHandler;
import com.falsepattern.rple.internal.client.render.TessellatorBrightnessHelper;
import com.falsepattern.rple.internal.common.chunk.RPLESubChunkRoot;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.entity.Entity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import static com.falsepattern.rple.api.common.RPLEColorUtil.lightOpacityFromColor;
import static com.falsepattern.rple.api.common.RPLEColorUtil.lightValueFromColor;
import static com.falsepattern.rple.api.common.color.ColorChannel.*;

@UtilityClass
public final class ColoredLightingHooks {
    @SuppressWarnings("CastToIncompatibleInterface")
    public static int getMaxBlockLightValue(ExtendedBlockStorage subChunkBase,
                                            int subChunkPosX,
                                            int subChunkPosY,
                                            int subChunkPosZ) {
        val subChunkRoot = (RPLESubChunkRoot) subChunkBase;
        val redLightValue = subChunkRoot.rple$subChunk(RED_CHANNEL)
                                        .lumi$getBlockLightValue(subChunkPosX, subChunkPosY, subChunkPosZ);
        val greenLightValue = subChunkRoot.rple$subChunk(GREEN_CHANNEL)
                                          .lumi$getBlockLightValue(subChunkPosX, subChunkPosY, subChunkPosZ);
        val blueLightValue = subChunkRoot.rple$subChunk(BLUE_CHANNEL)
                                         .lumi$getBlockLightValue(subChunkPosX, subChunkPosY, subChunkPosZ);
        return RPLEColorUtil.maxColorComponent(redLightValue, greenLightValue, blueLightValue);
    }

    @SuppressWarnings("CastToIncompatibleInterface")
    public static int getMaxSkyLightValue(ExtendedBlockStorage subChunkBase,
                                          int subChunkPosX,
                                          int subChunkPosY,
                                          int subChunkPosZ) {
        val subChunkRoot = (RPLESubChunkRoot) subChunkBase;
        val redLightValue = subChunkRoot.rple$subChunk(RED_CHANNEL)
                                        .lumi$getSkyLightValue(subChunkPosX, subChunkPosY, subChunkPosZ);
        val greenLightValue = subChunkRoot.rple$subChunk(GREEN_CHANNEL)
                                          .lumi$getSkyLightValue(subChunkPosX, subChunkPosY, subChunkPosZ);
        val blueLightValue = subChunkRoot.rple$subChunk(BLUE_CHANNEL)
                                         .lumi$getSkyLightValue(subChunkPosX, subChunkPosY, subChunkPosZ);
        return RPLEColorUtil.maxColorComponent(redLightValue, greenLightValue, blueLightValue);
    }

    public static int getLightValue(Block block) {
        val rpleBlock = (RPLEBlock) block;
        val color = rpleBlock.rple$getBrightnessColor();
        return lightValueFromColor(color);
    }

    public static int getLightValue(IBlockAccess world,
                                    Block block,
                                    int posX,
                                    int posY,
                                    int posZ) {
        val rpleBlock = (RPLEBlock) block;
        val blockMeta = world.getBlockMetadata(posX, posY, posZ);
        val color = rpleBlock.rple$getBrightnessColor(world, blockMeta, posX, posY, posZ);
        return lightValueFromColor(color);
    }

    public static int getLightOpacity(Block block) {
        if (block.isOpaqueCube())
            return 15;

        val rpleBlock = (RPLEBlock) block;
        val color = rpleBlock.rple$getTranslucencyColor();
        return lightOpacityFromColor(color);
    }

    public static int getLightOpacity(IBlockAccess world,
                                      Block block,
                                      int posX,
                                      int posY,
                                      int posZ) {
        if (block.isOpaqueCube())
            return 15;

        val rpleBlock = (RPLEBlock) block;
        val blockMeta = world.getBlockMetadata(posX, posY, posZ);
        val color = rpleBlock.rple$getTranslucencyColor(world, blockMeta, posX, posY, posZ);
        return lightOpacityFromColor(color);
    }

    @SideOnly(Side.CLIENT)
    public static int getEntityRGBBrightnessForTessellator(Entity entity,
                                                           IBlockAccess world,
                                                           int posX,
                                                           int posY,
                                                           int posZ,
                                                           int minBrightnessCookie) {
        var brightness = getRGBBrightnessForTessellator(world, posX, posY, posZ, minBrightnessCookie);
        if (EntityColorHandler.isOnBlockList(entity.getClass())) {
            val packedBrightness = CookieMonster.cookieToPackedLong(brightness);
            brightness = TessellatorBrightnessHelper.getBrightestChannelFromPacked(packedBrightness);
        }
        return brightness;
    }

    @SideOnly(Side.CLIENT)
    public static int getBlockRGBBrightnessForTessellator(IBlockAccess world,
                                                          int posX,
                                                          int posY,
                                                          int posZ) {
        var block = world.getBlock(posX, posY, posZ);
        var blockMeta = world.getBlockMetadata(posX, posY, posZ);

        var brightnessCookie = RPLEBlockBrightnessUtil.getBlockBrightnessForTessellator(world,
                                                                                        block,
                                                                                        blockMeta,
                                                                                        posX,
                                                                                        posY,
                                                                                        posZ);
        brightnessCookie = getRGBBrightnessForTessellator(world, posX, posY, posZ, brightnessCookie);

        if (brightnessCookie == 0 && block instanceof BlockSlab) {
            --posY;

            block = world.getBlock(posX, posY, posZ);
            blockMeta = world.getBlockMetadata(posX, posY, posZ);

            brightnessCookie = RPLEBlockBrightnessUtil.getBlockBrightnessForTessellator(world,
                                                                                        block,
                                                                                        blockMeta,
                                                                                        posX,
                                                                                        posY,
                                                                                        posZ);
            brightnessCookie = getRGBBrightnessForTessellator(world, posX, posY, posZ, brightnessCookie);
        }

        return brightnessCookie;
    }

    @SideOnly(Side.CLIENT)
    public static int getBlockFluidRGBBrightnessForTessellator(IBlockAccess world,
                                                               int posX,
                                                               int posY,
                                                               int posZ) {
        val brightness = getRGBBrightnessForTessellator(world, posX, posY, posZ);
        val topBrightness = getRGBBrightnessForTessellator(world, posX, posY + 1, posZ);
        return CookieMonsterHelper.packedMax(brightness, topBrightness);
    }

    @SideOnly(Side.CLIENT)
    public static int getRGBBrightnessForTessellator(IBlockAccess world, int posX, int posY, int posZ) {
        return RPLEBlockBrightnessUtil.getRGBBrightnessForTessellator(world, posX, posY, posZ);
    }

    @SideOnly(Side.CLIENT)
    public static int getRGBBrightnessForTessellator(IBlockAccess world,
                                                     int posX,
                                                     int posY,
                                                     int posZ,
                                                     int minBrightnessCookie) {
        val packedMinBrightness = CookieMonster.cookieToPackedLong(minBrightnessCookie);

        val minRedBrightness = TessellatorBrightnessHelper.getBrightnessRed(packedMinBrightness);
        val minGreenBrightness = TessellatorBrightnessHelper.getBrightnessGreen(packedMinBrightness);
        val minBlueBrightness = TessellatorBrightnessHelper.getBrightnessBlue(packedMinBrightness);

        val minRedBlockLight = TessellatorBrightnessHelper.getBlockLightFromBrightness(minRedBrightness);
        val minGreenBlockLight = TessellatorBrightnessHelper.getBlockLightFromBrightness(minGreenBrightness);
        val minBlueBlockLight = TessellatorBrightnessHelper.getBlockLightFromBrightness(minBlueBrightness);

        return RPLEBlockBrightnessUtil.getRGBBrightnessForTessellator(world,
                                                                      posX,
                                                                      posY,
                                                                      posZ,
                                                                      minRedBlockLight,
                                                                      minGreenBlockLight,
                                                                      minBlueBlockLight);
    }
}
