package com.woodenscalpel.common.blockentity;

import com.woodenscalpel.Minefinifactory;
import com.woodenscalpel.common.blockentity.util.TickableBlockEntity;
import com.woodenscalpel.common.init.BlockInit;
import com.woodenscalpel.common.mastertick.MasterTick;
import com.woodenscalpel.common.multiblockentity.BaseEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;


public class LifterBlockEntity extends BlockEntity implements TickableBlockEntity {
    public LifterBlockEntity(BlockEntityType<LifterBlockEntity> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public LifterBlockEntity(BlockPos pos, BlockState blockState) {
        super(BlockInit.lifterBlockEntity.get(), pos, blockState);
    }


    @Override
    public void tick() {
    }


}
