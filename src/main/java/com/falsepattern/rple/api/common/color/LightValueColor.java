/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.rple.api.common.color;

import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum LightValueColor implements RPLENamedColor {
    LIGHT_VALUE_0,
    LIGHT_VALUE_1,
    LIGHT_VALUE_2,
    LIGHT_VALUE_3,
    LIGHT_VALUE_4,
    LIGHT_VALUE_5,
    LIGHT_VALUE_6,
    LIGHT_VALUE_7,
    LIGHT_VALUE_8,
    LIGHT_VALUE_9,
    LIGHT_VALUE_10,
    LIGHT_VALUE_11,
    LIGHT_VALUE_12,
    LIGHT_VALUE_13,
    LIGHT_VALUE_14,
    LIGHT_VALUE_15,
    ;

    private static final LightValueColor[] VALUES = values();
    public static final String LIGHT_LEVEL_COLOR_DOMAIN = "light_value";

    private final int red;
    private final int green;
    private final int blue;

    private final String colorName;

    LightValueColor() {
        final int lightValue = ordinal();

        this.red = lightValue;
        this.green = lightValue;
        this.blue = lightValue;

        this.colorName = String.valueOf(lightValue);
    }

    public static @NotNull LightValueColor fromVanillaLightValue(int vanillaLightValue) {
        final int index = vanillaLightValue & 15;
        return VALUES[index];
    }

    public static @NotNull LightValueColor fromVanillaLightOpacity(int vanillaLightOpacity) {
        final int index = 15 - (vanillaLightOpacity & 15);
        return VALUES[index];
    }

    public static @Nullable RPLEColor attemptMapToEnum(@Nullable RPLEColor color) {
        if (color == null)
            return null;
        for (val otherColor : VALUES) {
            if (color.red() == otherColor.red() &&
                color.green() == otherColor.green() &&
                color.blue() == otherColor.blue())
                return otherColor;
        }
        return null;
    }

    public @NotNull String colorName() {
        return colorName;
    }

    @Override
    public @NotNull String colorDomain() {
        return LIGHT_LEVEL_COLOR_DOMAIN;
    }

    public int red() {
        return red;
    }

    public int green() {
        return green;
    }

    public int blue() {
        return blue;
    }
}
