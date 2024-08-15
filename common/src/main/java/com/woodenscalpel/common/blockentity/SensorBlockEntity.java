package com.woodenscalpel.common.blockentity;

import com.woodenscalpel.Minefinifactory;
import com.woodenscalpel.common.blockentity.util.TickableBlockEntity;
import com.woodenscalpel.common.blocks.SensorBlock;
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


public class SensorBlockEntity extends BlockEntity implements TickableBlockEntity {
    public SensorBlockEntity(BlockEntityType<SensorBlockEntity> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public SensorBlockEntity(BlockPos pos, BlockState blockState) {
        super(BlockInit.sensorBlockEntity.get(), pos, blockState);
    }


    @Override
    public void tick() {
        if(level.isClientSide){return;}
        if(MasterTick.isMasterTick(this.level.getServer())){
            boolean powerflag = false;
            BlockPos weldPos = this.getBlockPos().relative(this.getBlockState().getValue(WelderBlock.FACING));
            AABB blockabove = new AABB(weldPos, new BlockPos(weldPos.getX() + 1, weldPos.getY() + 1, weldPos.getZ() + 1));
            List<BaseEntity> ents = level.getEntitiesOfClass(BaseEntity.class, blockabove);
            for (BaseEntity e : ents) {
                Minefinifactory.LOGGER.info("Doing the thing");
                if(e.isBlock(weldPos,e.getBlocks())){
                    Minefinifactory.LOGGER.info("setting block state");
                    level.setBlock(this.getBlockPos(), this.getBlockState().setValue(SensorBlock.POWERED, true),2);
                    level.updateNeighborsAt(this.getBlockPos(),this.getBlockState().getBlock());
                    powerflag = true;
                    break;
                }
            }

            if(!powerflag){
                level.setBlock(this.getBlockPos(), this.getBlockState().setValue(SensorBlock.POWERED, false),2);
                level.updateNeighborsAt(this.getBlockPos(),this.getBlockState().getBlock());
            }
        }
    }


}
