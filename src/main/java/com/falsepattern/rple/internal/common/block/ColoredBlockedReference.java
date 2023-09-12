/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.rple.internal.common.block;

import com.falsepattern.rple.api.common.color.RPLEColor;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.val;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

@Accessors(fluent = true, chain = false)
@RequiredArgsConstructor
public final class ColoredBlockedReference {
    private final RPLEBlockInit block;

    @Setter
    private @Nullable RPLEColor baseBrightnessColor;
    @Setter
    private @Nullable RPLEColor baseTranslucencyColor;

    private final TIntObjectMap<RPLEColor> metaBrightnessColorsMap = new TIntObjectHashMap<>();
    private final TIntObjectMap<RPLEColor> metaTranslucencyColorsMap = new TIntObjectHashMap<>();

    public void metaBrightnessColorsMap(int blockMeta, RPLEColor color) {
        if (blockMeta >= 0)
            metaBrightnessColorsMap.put(blockMeta, color);
    }

    public void metaTranslucencyColorsMap(int blockMeta, RPLEColor color) {
        if (blockMeta >= 0)
            metaTranslucencyColorsMap.put(blockMeta, color);
    }

    public void apply() {
        block.rple$initBaseBrightnessColor(baseBrightnessColor);
        block.rple$initMetaBrightnessColors(metaColorsArrayFromMap(metaBrightnessColorsMap));
        block.rple$initBaseTranslucencyColor(baseTranslucencyColor);
        block.rple$initMetaTranslucencyColors(metaColorsArrayFromMap(metaTranslucencyColorsMap));
    }

    private @Nullable RPLEColor @Nullable [] metaColorsArrayFromMap(TIntObjectMap<RPLEColor> metaColorsMap) {
        if (metaColorsMap.isEmpty())
            return null;
        val blockMetas = metaColorsMap.keys();
        val maxBlockMeta = Arrays.stream(blockMetas)
                                 .max()
                                 .orElse(-1);
        if (maxBlockMeta < 0)
            return null;

        val metaColorsLength = maxBlockMeta + 1;
        val metaColors = new RPLEColor[metaColorsLength];
        for (val blockMeta : blockMetas)
            metaColors[blockMeta] = metaColorsMap.get(blockMeta);
        return metaColors;
    }
}
