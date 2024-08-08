package com.woodenscalpel.common.init;

import com.woodenscalpel.client.render.blockentity.DestroyerBER;
import com.woodenscalpel.client.render.blockentity.WelderBER;
import dev.architectury.platform.Platform;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import net.fabricmc.api.EnvType;
import net.minecraft.client.Minecraft;

public class ClientInit {


    public static void register(Minecraft minecraft) {
        if(Platform.getEnv() == EnvType.CLIENT) {
            BlockEntityRendererRegistry.register(BlockInit.welderBlockEntity.get(), WelderBER::new);
            BlockEntityRendererRegistry.register(BlockInit.destroyerBlockEntity.get(), DestroyerBER::new);
        }
    }
}
