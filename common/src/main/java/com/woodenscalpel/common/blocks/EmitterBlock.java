package com.woodenscalpel.common.blocks;

import com.woodenscalpel.Minefinifactory;
import com.woodenscalpel.common.multiblockentity.BaseEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class EmitterBlock extends Block {

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public EmitterBlock(Properties properties) {
        super(properties);
        //this.registerDefaultState((BlockState) this.stateDefinition.any().setValue(BlockStateProperties.POWERED, false));
        this.registerDefaultState(this.defaultBlockState().setValue(POWERED, false));//How many properties as you want
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        boolean flag = level.hasNeighborSignal(pos);
        if (flag != (Boolean)state.getValue(POWERED)) {
            if (flag) {
                Minefinifactory.LOGGER.info("POWER");
                List<Tuple<Vec3i,BlockState>> blocks = new ArrayList<>();

                blocks.add(new Tuple<>(new Vec3i(0,0,0), Blocks.BAMBOO_MOSAIC.defaultBlockState()));
                blocks.add(new Tuple<>(new Vec3i(1,0,0), Blocks.ACACIA_FENCE.defaultBlockState()));
                level.addFreshEntity(new BaseEntity(level,new Vec3(pos.getX(),pos.getY()+1,pos.getZ()), blocks ));
            }

            level.setBlock(pos, (BlockState)state.setValue(POWERED, flag), 3);
        }
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
    }

}
