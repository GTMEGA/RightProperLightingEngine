/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.rple.api.common.block;

import com.falsepattern.rple.api.common.color.RPLEColor;
import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This interface is implemented onto the {@link Block Block Class} by RPLE.
 * <p>
 * It is used as the base of the
 * <p>
 * You <b>SHOULD NOT</b> implement it yourself, but you can cast any instance of {@link Block} into it.
 *
 * @see RPLEBlockBrightnessColorProvider
 * @see RPLEBlockTranslucencyColorProvider
 */
public interface RPLEBlock {
    boolean rple$hasBrightness();

    boolean rple$hasTranslucency();

    @NotNull RPLEColor rple$getBrightnessColor();

    @NotNull RPLEColor rple$getBrightnessColor(int blockMeta);

    @NotNull RPLEColor rple$getBrightnessColor(@NotNull IBlockAccess world,
                                               int blockMeta,
                                               int posX,
                                               int posY,
                                               int posZ);

    @NotNull RPLEColor rple$getTranslucencyColor();

    @NotNull RPLEColor rple$getTranslucencyColor(int blockMeta);

    @NotNull RPLEColor rple$getTranslucencyColor(@NotNull IBlockAccess world,
                                                 int blockMeta,
                                                 int posX,
                                                 int posY,
                                                 int posZ);

    @NotNull RPLEColor rple$getFallbackBrightnessColor();

    @NotNull RPLEColor rple$getFallbackBrightnessColor(int blockMeta);

    @NotNull RPLEColor rple$getFallbackBrightnessColor(@NotNull IBlockAccess world,
                                                       int blockMeta,
                                                       int posX,
                                                       int posY,
                                                       int posZ);

    @NotNull RPLEColor rple$getFallbackTranslucencyColor();

    @NotNull RPLEColor rple$getFallbackTranslucencyColor(int blockMeta);

    @NotNull RPLEColor rple$getFallbackTranslucencyColor(@NotNull IBlockAccess world,
                                                         int blockMeta,
                                                         int posX,
                                                         int posY,
                                                         int posZ);

    @Nullable RPLEColor rple$getConfiguredBrightnessColor();

    @Nullable RPLEColor rple$getConfiguredBrightnessColor(int blockMeta);

    @Nullable RPLEColor rple$getConfiguredTranslucencyColor();

    @Nullable RPLEColor rple$getConfiguredTranslucencyColor(int blockMeta);
}
