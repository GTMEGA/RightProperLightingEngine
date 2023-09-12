/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.rple.internal.mixin.mixins.common.rple;

import com.falsepattern.rple.api.common.block.RPLEBlockRoot;
import com.falsepattern.rple.api.common.color.LightValueColor;
import com.falsepattern.rple.api.common.color.RPLEColor;
import lombok.val;
import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import static com.falsepattern.rple.internal.mixin.plugin.MixinPlugin.RPLE_ROOT_MIXIN_PRIORITY;

@Unique
@Mixin(value = Block.class, priority = RPLE_ROOT_MIXIN_PRIORITY)
@SuppressWarnings("unused")
public abstract class RPLEBlockRootImplMixin implements RPLEBlockRoot {
    @Shadow
    public abstract int getLightValue();

    @Shadow(remap = false)
    public abstract int getLightValue(IBlockAccess world, int posX, int posY, int posZ);

    @Shadow
    public abstract int getLightOpacity();

    @Shadow(remap = false)
    public abstract int getLightOpacity(IBlockAccess world, int posX, int posY, int posZ);

    @Dynamic("Initialized in: [com.falsepattern.rple.internal.mixin.mixins.common.rple.RPLEBlockInitImplMixin]")
    private boolean rple$hasBrightness;
    @Dynamic("Initialized in: [com.falsepattern.rple.internal.mixin.mixins.common.rple.RPLEBlockInitImplMixin]")
    private boolean rple$hasTranslucency;

    @Dynamic("Initialized in: [com.falsepattern.rple.internal.mixin.mixins.common.rple.RPLEBlockInitImplMixin]")
    protected ThreadLocal<Boolean> rple$passInternalLightValue;
    @Dynamic("Initialized in: [com.falsepattern.rple.internal.mixin.mixins.common.rple.RPLEBlockInitImplMixin]")
    protected ThreadLocal<Boolean> rple$passInternalLightOpacity;

    @Override
    public RPLEColor rple$getInternalColoredBrightness() {
        if (!rple$hasBrightness)
            return LightValueColor.LIGHT_VALUE_0;

        rple$passInternalLightValue.set(true);
        val lightValue = getLightValue();
        rple$passInternalLightValue.set(false);
        return LightValueColor.fromVanillaLightValue(lightValue);
    }

    @Override
    public RPLEColor rple$getInternalColoredBrightness(IBlockAccess world, int posX, int posY, int posZ) {
        if (!rple$hasBrightness)
            return LightValueColor.LIGHT_VALUE_0;

        rple$passInternalLightValue.set(true);
        val lightValue = getLightValue(world, posX, posY, posZ);
        rple$passInternalLightValue.set(false);
        return LightValueColor.fromVanillaLightValue(lightValue);
    }

    @Override
    public RPLEColor rple$getInternalColoredTranslucency() {
        if (!rple$hasTranslucency)
            return LightValueColor.LIGHT_VALUE_0;

        rple$passInternalLightOpacity.set(true);
        val lightOpacity = getLightOpacity();
        rple$passInternalLightOpacity.set(false);
        return LightValueColor.fromVanillaLightOpacity(lightOpacity);
    }

    @Override
    public RPLEColor rple$getInternalColoredTranslucency(IBlockAccess world, int posX, int posY, int posZ) {
        if (!rple$hasTranslucency)
            return LightValueColor.LIGHT_VALUE_0;

        rple$passInternalLightOpacity.set(true);
        val lightOpacity = getLightOpacity(world, posX, posY, posZ);
        rple$passInternalLightOpacity.set(false);
        return LightValueColor.fromVanillaLightOpacity(lightOpacity);
    }
}
