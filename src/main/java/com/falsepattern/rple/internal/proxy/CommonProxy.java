/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.rple.internal.proxy;

import com.falsepattern.rple.internal.Tags;
import com.falsepattern.rple.internal.common.block.LampBlock;
import com.falsepattern.rple.internal.common.block.LampItemBlock;
import com.falsepattern.rple.internal.common.block.RPLEBlockInit;
import com.falsepattern.rple.internal.common.config.ColorConfigLoader;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.registry.GameRegistry;
import lombok.NoArgsConstructor;
import lombok.val;
import net.minecraft.block.Block;

import static com.falsepattern.rple.internal.common.block.BlockColorManager.blockColorManager;
import static com.falsepattern.rple.internal.common.event.LumiEventHandler.lumiEventHandler;

@NoArgsConstructor
public abstract class CommonProxy {
    public void preInit(FMLPreInitializationEvent evt) {
        lumiEventHandler().registerEventHandler();
        val lamp = new LampBlock();
        lamp.setBlockName(Tags.MOD_ID + ".lamp." + "blue").setBlockTextureName("blue");
        GameRegistry.registerBlock(lamp, LampItemBlock.class, "lamp." + "blue");

        //TODO: [PRE-RELEASE] Fix these values
//        if (RPLEConfig.ENABLE_LAMPS) {
//            for (val lampData : Lamps.values()) {
//                val name = lampData.name().toLowerCase();
//                val r = lampData.r;
//                val g = lampData.g;
//                val b = lampData.b;
//                val lamp = new LampBlock();
//                lamp.setBlockName(Tags.MODID + ".lamp." + name).setBlockTextureName(name);
//                lamp.setColoredLightValue(0, 0, 0, 0);
//                lamp.setColoredLightValue(LampBlock.INVERTED_BIT, r, g, b);
//                lamp.setColoredLightValue(LampBlock.POWERED_BIT, r, g, b);
//                lamp.setColoredLightValue(LampBlock.INVERTED_BIT | LampBlock.POWERED_BIT, 0, 0, 0);
//                GameRegistry.registerBlock(lamp, LampItemBlock.class, "lamp." + name);
//            }
//        }
    }

    public void init(FMLInitializationEvent evt) {
        ColorConfigLoader.generateReadmeFile();
        blockColorManager().registerBlockColors();
    }

    public void postInit(FMLPostInitializationEvent evt) {
        val blocks = (Iterable<Block>) Block.blockRegistry;
        for (val blockBase : blocks) {
            if (!(blockBase instanceof RPLEBlockInit))
                continue;
            val blockInit = (RPLEBlockInit) blockBase;
            blockInit.rple$hasBrightness(blockBase.getLightValue() > 0);
            blockInit.rple$hasTranslucency(!blockBase.isOpaqueCube() || blockBase.getLightOpacity() < 15);
        }
    }

    public void serverAboutToStart(FMLServerAboutToStartEvent evt) {
    }

    public void serverStarting(FMLServerStartingEvent evt) {
    }

    public void serverStarted(FMLServerStartedEvent evt) {
    }

    public void serverStopping(FMLServerStoppingEvent evt) {
    }

    public void serverStopped(FMLServerStoppedEvent evt) {
    }
}
