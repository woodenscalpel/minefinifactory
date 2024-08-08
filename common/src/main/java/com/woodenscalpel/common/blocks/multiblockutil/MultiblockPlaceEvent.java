package com.woodenscalpel.common.blocks.multiblockutil;

import com.woodenscalpel.Minefinifactory;
import dev.architectury.event.EventResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class MultiblockPlaceEvent {


    public static EventResult onPlace(Level level, BlockPos pos, BlockState state, @Nullable Entity placer){
        if(level.getBlockEntity(pos) instanceof IMultiblockEntity && state.hasBlockEntity()){
            ((IMultiblockEntity) level.getBlockEntity(pos)).onPlace();
        }
        return EventResult.pass();
    };
}
