package com.woodenscalpel.common.blockentity;

import com.woodenscalpel.Minefinifactory;
import com.woodenscalpel.common.blockentity.util.TickableBlockEntity;
import com.woodenscalpel.common.blocks.ConveyorBlock;
import com.woodenscalpel.common.blocks.WelderBlock;
import com.woodenscalpel.common.blocks.pusher.PusherBaseBlock;
import com.woodenscalpel.common.init.BlockInit;
import com.woodenscalpel.common.multiblockentity.BaseEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class ConveyorBlockEntity extends BlockEntity implements TickableBlockEntity {
    public ConveyorBlockEntity(BlockEntityType<ConveyorBlockEntity> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public ConveyorBlockEntity(BlockPos pos, BlockState blockState) {
        super(BlockInit.conveyorBlockEntity.get(), pos, blockState);

    }


    @Override
    public void tick() {
      //  Minefinifactory.LOGGER.info("TICKER");

        //check block above for entity
        Level world = this.getLevel();

        //List list = world.getEntities(Entity.class, AxisAlignedBB.getBoundingBox((float) xCoord, (float) yCoord, (float) zCoord, (float) (xCoord + 1), (float) (yCoord + 2), (float) (zCoord + 1)));
        assert world != null;

        BlockPos posAbove = this.getBlockPos().relative(Direction.UP);
        AABB BoundAbove = new AABB(posAbove, new BlockPos(posAbove.getX() + 1, posAbove.getY() + 1, posAbove.getZ() + 1));
        List<BaseEntity> ents = level.getEntitiesOfClass(BaseEntity.class, BoundAbove);
        for (BaseEntity e : ents) {
            if (e.isBlock(posAbove, e.getBlocks())) {
                //ArrayList<Integer> pushInt = new ArrayList<Integer>();
                //Minefinifactory.LOGGER.info("Found!");
                e.addInfluence(this.getBlockState().getValue(ConveyorBlock.FACING),1, UUID.randomUUID());
            }
        }





        //propogate impulse down entity (use function in entity)
    }

}
