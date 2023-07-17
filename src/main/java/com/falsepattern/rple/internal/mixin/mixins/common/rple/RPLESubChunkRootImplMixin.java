/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.rple.internal.mixin.mixins.common.rple;

import com.falsepattern.lumina.api.chunk.LumiSubChunk;
import com.falsepattern.rple.api.color.ColorChannel;
import com.falsepattern.rple.internal.common.storage.chunk.RPLESubChunk;
import com.falsepattern.rple.internal.common.storage.chunk.RPLESubChunkRoot;
import com.falsepattern.rple.internal.common.storage.chunk.RPLESubChunkWrapper;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.falsepattern.lumina.api.init.LumiSubChunkBaseInit.LUMI_SUB_CHUNK_BASE_INIT_METHOD_REFERENCE;
import static com.falsepattern.lumina.api.init.LumiSubChunkBaseInit.LUMI_SUB_CHUNK_BASE_INIT_MIXIN_VALUE;
import static com.falsepattern.rple.internal.mixin.plugin.MixinPlugin.POST_LUMINA_MIXIN_PRIORITY;

@Unique
@Mixin(value = ExtendedBlockStorage.class, priority = POST_LUMINA_MIXIN_PRIORITY)
public abstract class RPLESubChunkRootImplMixin implements LumiSubChunk, RPLESubChunkRoot {
    private RPLESubChunk rple$redChannel;
    private RPLESubChunk rple$greenChannel;
    private RPLESubChunk rple$blueChannel;

    @Inject(method = LUMI_SUB_CHUNK_BASE_INIT_METHOD_REFERENCE,
            at = @At("RETURN"),
            remap = false,
            require = 1)
    @Dynamic(LUMI_SUB_CHUNK_BASE_INIT_MIXIN_VALUE)
    private void lumiSubChunkInit(CallbackInfo ci) {
        this.rple$redChannel = new RPLESubChunkWrapper();
        this.rple$greenChannel = new RPLESubChunkWrapper();
        this.rple$blueChannel = new RPLESubChunkWrapper();
    }

    @Override
    public RPLESubChunk rple$subChunk(ColorChannel channel) {
        switch (channel) {
            default:
            case RED_CHANNEL:
                return rple$redChannel;
            case GREEN_CHANNEL:
                return rple$greenChannel;
            case BLUE_CHANNEL:
                return rple$blueChannel;
        }
    }
}