package com.woodenscalpel.common.blockentity.util;

import com.woodenscalpel.common.blockentity.ConveyorBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;

public interface TickableBlockEntity {
    void tick();

    public static <T extends BlockEntity> BlockEntityTicker<T> getTickerHelper(Level level){
        //return level.isClientSide() ? null : (level0,pos0,state0,blockEntity) -> ((ConveyorBlockEntity) blockEntity).tick();
        return level.isClientSide() ? null : (level0,pos0,state0,blockEntity) -> ((TickableBlockEntity) blockEntity).tick();
    }
}
