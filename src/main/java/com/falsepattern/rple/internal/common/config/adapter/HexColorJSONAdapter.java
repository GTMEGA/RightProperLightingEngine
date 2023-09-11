/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.rple.internal.common.config.adapter;

import com.falsepattern.rple.internal.common.config.container.HexColor;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.NoArgsConstructor;
import lombok.val;

import static com.falsepattern.rple.internal.common.config.ColorConfigLoader.colorConfigGSON;
import static com.falsepattern.rple.internal.common.config.ColorConfigLoader.logParsingError;
import static com.falsepattern.rple.internal.common.config.container.HexColor.INVALID_HEX_COLOR;

@NoArgsConstructor
public final class HexColorJSONAdapter extends TypeAdapter<HexColor> {
    @Override
    public void write(JsonWriter out, HexColor value) {
        colorConfigGSON().toJson(value.asColorHex(), String.class, out);
    }

    @Override
    public HexColor read(JsonReader in) {
        final String colorHex;
        try {
            colorHex = colorConfigGSON().fromJson(in, String.class);
        } catch (JsonSyntaxException e) {
            logParsingError("Failed parsing hex color: {}", e.getMessage());
            return INVALID_HEX_COLOR;
        }

        val hexColor = new HexColor(colorHex);
        if (!hexColor.isValid())
            logParsingError("Invalid hex color: {}", hexColor);
        return hexColor;
    }
}
