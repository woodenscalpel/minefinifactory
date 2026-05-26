package com.woodenscalpel.common.blockentity;

import com.woodenscalpel.common.blockentity.util.TickableBlockEntity;
import com.woodenscalpel.common.blocks.WelderBlock;
import com.woodenscalpel.common.init.BlockInit;
import com.woodenscalpel.common.mastertick.MasterTick;
import com.woodenscalpel.common.multiblockentity.BaseEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;


public class RotatorBlockEntity extends BlockEntity implements TickableBlockEntity {

    public boolean directionClockwise = true;

    public RotatorBlockEntity(BlockEntityType<RotatorBlockEntity> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public RotatorBlockEntity(BlockPos pos, BlockState blockState) {
        super(BlockInit.rotatorBlockEntity.get(), pos, blockState);
        directionClockwise = true;
    }


    @Override
    public void tick() {
       /*
        if(level.isClientSide){return;}
        if(MasterTick.isMasterTick(this.level.getServer())){
            BlockPos weldPos = this.getBlockPos().relative(this.getBlockState().getValue(WelderBlock.FACING));
            AABB blockabove = new AABB(weldPos);
            List<BaseEntity> ents = level.getEntitiesOfClass(BaseEntity.class, blockabove);
            for (BaseEntity e : ents) {
                if (e.isBlock(weldPos, e.getBlocks())) {
                    e.rotateBlocks();

                }
            }
            }

        */
    }


}
