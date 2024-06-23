/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.rple.api.common;

import com.falsepattern.lib.util.MathUtil;
import com.falsepattern.rple.api.common.color.CustomPaletteColor;

@SuppressWarnings("unused")
public final class ServerColorHelper {
    public static final int COLOR_MIN = 0;
    public static final int COLOR_MAX = 15;

    public static final int CHANNEL_4BIT_MASK = 0xf;
    public static final int CHANNEL_4BIT_TO_RGB16_RED = 8;
    public static final int CHANNEL_4BIT_TO_RGB16_GREEN = 4;
    public static final int CHANNEL_4BIT_TO_RGB16_BLUE = 0;

    public static final CustomPaletteColor ERROR_COLOR = new CustomPaletteColor(
            RGB16FromRGBChannel4Bit(COLOR_MAX, COLOR_MIN, COLOR_MIN),
            "invalid_domain",
            "invalid_name"
    );

    private ServerColorHelper() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }


    public static short RGB16OpacityTranslucentSwap(short val) {
        if (val == -1)
            return -1;
        int red = invertColorComponent(red(val));
        int green = invertColorComponent(green(val));
        int blue = invertColorComponent(blue(val));

        return RGB16FromRGBChannel4Bit(red, green, blue);
    }

    public static short RGB16FromRGBChannel4Bit(int red, int green, int blue) {
        return (short) ((red & CHANNEL_4BIT_MASK) << CHANNEL_4BIT_TO_RGB16_RED |
                (green & CHANNEL_4BIT_MASK) << CHANNEL_4BIT_TO_RGB16_GREEN |
                (blue & CHANNEL_4BIT_MASK) << CHANNEL_4BIT_TO_RGB16_BLUE);
    }

    public static int red(short rgb16) {
        return ((rgb16 >> CHANNEL_4BIT_TO_RGB16_RED) & CHANNEL_4BIT_MASK);
    }
    public static int green(short rgb16) {
        return ((rgb16 >> CHANNEL_4BIT_TO_RGB16_GREEN) & CHANNEL_4BIT_MASK);
    }
    public static int blue(short rgb16) {
        return ((rgb16 >> CHANNEL_4BIT_TO_RGB16_BLUE) & CHANNEL_4BIT_MASK);
    }
    
    public static int lightValueFromRGB16(short rgb16) {
        return maxColorComponent(rgb16);
    }

    public static int lightOpacityFromRGB16(short rgb16) {
        return invertColorComponent(maxColorComponent(rgb16));
    }

    public static int clampColorComponent(int component) {
        return MathUtil.clamp(component, COLOR_MIN, COLOR_MAX);
    }

    public static int invertColorComponent(int component) {
        return COLOR_MAX - clampColorComponent(component);
    }

    public static int minColorComponent(short rgb16) {
        return minColorComponent(red(rgb16), green(rgb16), blue(rgb16));
    }

    public static int maxColorComponent(short rgb16) {
        return maxColorComponent(red(rgb16), green(rgb16), blue(rgb16));
    }

    public static int minColorComponent(int red, int green, int blue) {
        return Math.min(red, Math.min(green, blue));
    }

    public static int maxColorComponent(int red, int green, int blue) {
        return Math.max(red, Math.max(green, blue));
    }

    public static int colorHashCode(short rgb16) {
        return rgb16;
    }
}
