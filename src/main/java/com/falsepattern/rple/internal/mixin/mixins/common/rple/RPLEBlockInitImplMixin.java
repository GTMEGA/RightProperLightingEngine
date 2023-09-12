/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.rple.internal.mixin.mixins.common.rple;

import com.falsepattern.rple.api.common.color.RPLEColor;
import com.falsepattern.rple.internal.common.block.RPLEBlockInit;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.falsepattern.rple.internal.mixin.plugin.MixinPlugin.RPLE_INIT_MIXIN_PRIORITY;

@Unique
@Mixin(value = Block.class, priority = RPLE_INIT_MIXIN_PRIORITY)
@SuppressWarnings("unused")
public abstract class RPLEBlockInitImplMixin implements RPLEBlockInit {
    @Nullable
    private RPLEColor rple$baseBrightnessColor;
    @Nullable
    private RPLEColor rple$baseTranslucencyColor;
    @Nullable
    private RPLEColor @Nullable [] rple$metaBrightnessColors;
    @Nullable
    private RPLEColor @Nullable [] rple$metaTranslucencyColors;

    private boolean rple$hasBrightness;
    private boolean rple$hasTranslucency;

    protected ThreadLocal<Boolean> rple$passInternalLightValue;
    protected ThreadLocal<Boolean> rple$passInternalLightOpacity;

    @Inject(method = "<init>",
            at = @At("RETURN"),
            require = 1)
    private void rpleBlockInit(Material material, CallbackInfo ci) {
        this.rple$baseBrightnessColor = null;
        this.rple$baseTranslucencyColor = null;
        this.rple$metaBrightnessColors = null;
        this.rple$metaTranslucencyColors = null;

        this.rple$hasBrightness = true;
        this.rple$hasTranslucency = true;

        this.rple$passInternalLightValue = ThreadLocal.withInitial(() -> false);
        this.rple$passInternalLightOpacity = ThreadLocal.withInitial(() -> false);
    }

    @Override
    public void rple$hasBrightness(boolean hasBrightness) {
        rple$hasBrightness = hasBrightness;
    }

    @Override
    public void rple$hasTranslucency(boolean hasTranslucency) {
        rple$hasTranslucency = hasTranslucency;
    }

    @Override
    public void rple$initBaseBrightnessColor(@Nullable RPLEColor baseColoredBrightness) {
        rple$baseBrightnessColor = baseColoredBrightness;
    }

    @Override
    public void rple$initMetaBrightnessColors(@Nullable RPLEColor @Nullable [] metaColoredBrightness) {
        rple$metaBrightnessColors = metaColoredBrightness;
    }

    @Override
    public void rple$initBaseTranslucencyColor(@Nullable RPLEColor baseColoredTranslucency) {
        rple$baseTranslucencyColor = baseColoredTranslucency;
    }

    @Override
    public void rple$initMetaTranslucencyColors(@Nullable RPLEColor @Nullable [] metaColoredTranslucency) {
        rple$metaTranslucencyColors = metaColoredTranslucency;
    }
}
