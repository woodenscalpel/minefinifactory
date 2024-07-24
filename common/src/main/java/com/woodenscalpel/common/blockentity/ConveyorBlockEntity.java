package com.woodenscalpel.common.blockentity;

import com.woodenscalpel.Minefinifactory;
import com.woodenscalpel.common.blockentity.util.TickableBlockEntity;
import com.woodenscalpel.common.init.BlockInit;
import com.woodenscalpel.common.multiblockentity.BaseEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ConveyorBlockEntity extends BlockEntity implements TickableBlockEntity {
    public ConveyorBlockEntity(BlockEntityType<ConveyorBlockEntity> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public ConveyorBlockEntity(BlockPos pos, BlockState blockState) {
        super(BlockInit.conveyorBlockEntity.get(), pos, blockState);
    }

    @Override
    public void tick() {
        BlockPos pos = this.getBlockPos();
        AABB blockabove = new AABB(new BlockPos(pos.getX(),pos.getY()+1,pos.getZ()),new BlockPos(pos.getX()+1,pos.getY()+2,pos.getZ()+1));
        List<BaseEntity> ents = level.getEntitiesOfClass(BaseEntity.class,blockabove);
        for(BaseEntity e : ents){
            //e.move(MoverType.PISTON,e.position().add(new Vec3(1,0,0)));
            e.push(Direction.EAST);

        }
    }
}
