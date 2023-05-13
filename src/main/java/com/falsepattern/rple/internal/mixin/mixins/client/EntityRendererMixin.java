/*
 * Copyright (c) 2023 FalsePattern
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 *
 */

package com.falsepattern.rple.internal.mixin.mixins.client;

import com.falsepattern.rple.internal.Common;
import com.falsepattern.rple.internal.LightMap;
import com.falsepattern.rple.internal.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import shadersmod.client.Shaders;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    @Shadow private Minecraft mc;

    @Shadow private float torchFlickerX;

    @Shadow private boolean lightmapUpdateNeeded;

    @Shadow @Final private DynamicTexture lightmapTexture;

    @Shadow @Final private ResourceLocation locationLightMap;

    @Shadow @Final private int[] lightmapColors;

    @Inject(method = "<init>",
            at = @At(value = "RETURN"),
            require = 1)
    private void setupColorLightMaps(Minecraft minecraft, IResourceManager p_i45076_2_, CallbackInfo ci) {
        Common.RED_LIGHT_MAP_TEXTURE_UNIT = OpenGlHelper.lightmapTexUnit;
        LightMap.init(new LightMap(lightmapTexture,
                                   locationLightMap,
                                   lightmapColors,
                                   Common.RED_LIGHT_MAP_TEXTURE_UNIT,
                                   Common.RED_LIGHT_MAP_SHADER_TEXTURE_UNIT));
    }

    @Inject(method = "enableLightmap",
            at = @At(value = "HEAD"),
            cancellable = true,
            require = 1)
    private void enableLightMaps(double p_78463_1_, CallbackInfo ci) {
        LightMap.enableReconfigureAll();

        if (Utils.shadersEnabled())
            Shaders.enableLightmap();

        ci.cancel();
    }

    @Inject(method = "disableLightmap",
            at = @At(value = "HEAD"),
            cancellable = true,
            require = 1)
    private void disableLightMaps(double p_78483_1_, CallbackInfo ci) {
        LightMap.disableAll();

        if (Utils.shadersEnabled())
            Shaders.disableLightmap();

        ci.cancel();
    }

    @Inject(method = "updateLightmap",
            at = @At(value = "HEAD"),
            cancellable = true,
            require = 1)
    private void updateLightMap(float p_78472_1_, CallbackInfo ci) {
        LightMap.updateLightMap(mc, torchFlickerX, p_78472_1_);
        lightmapUpdateNeeded = false;
        // TODO: This is not compatible with the OptiFine `CustomColorizer`
        ci.cancel();
    }
}
