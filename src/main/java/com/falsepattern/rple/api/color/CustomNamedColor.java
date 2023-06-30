/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.rple.api.color;

import com.falsepattern.rple.api.RPLEColorAPI;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true, chain = false)
@AllArgsConstructor
public class CustomNamedColor implements RPLENamedColor {
    protected final int red;
    protected final int green;
    protected final int blue;

    protected final String colorDomain;
    protected final String colorName;

    @Override
    public int hashCode() {
        return RPLEColorAPI.colorHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return RPLEColorAPI.namedColorEquals(this, obj);
    }
}