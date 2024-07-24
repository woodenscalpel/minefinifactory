package com.woodenscalpel.common.blocks.pusher;

import com.woodenscalpel.Minefinifactory;
import com.woodenscalpel.common.init.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.PistonType;

public class PusherHeadBlock extends PistonHeadBlock {
    public PusherHeadBlock(Properties properties) {
        super(properties);
    }

    private boolean isFittingBase(BlockState baseState, BlockState extendedState) {
        Block block = BlockInit.pusherHeadBlock.get();
        return extendedState.is(block) && (Boolean)extendedState.getValue(PusherBaseBlock.EXTENDED) && extendedState.getValue(FACING) == baseState.getValue(FACING);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && player.getAbilities().instabuild) {
            BlockPos blockPos = pos.relative(((Direction)state.getValue(FACING)).getOpposite());
            if (this.isFittingBase(state, level.getBlockState(blockPos))) {
                level.destroyBlock(blockPos, false);
            }
        }

        super.playerWillDestroy(level, pos, state, player);
    }
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            super.onRemove(state, level, pos, newState, movedByPiston);
            BlockPos blockPos = pos.relative(((Direction)state.getValue(FACING)).getOpposite());
            if (this.isFittingBase(state, level.getBlockState(blockPos))) {
                level.destroyBlock(blockPos, true);
            }

        }
    }
    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState blockState = level.getBlockState(pos.relative(((Direction)state.getValue(FACING)).getOpposite()));
        Minefinifactory.LOGGER.info(String.valueOf(isFittingBase(state,blockState)));
        return this.isFittingBase(state, blockState) || blockState.is(Blocks.MOVING_PISTON) && blockState.getValue(FACING) == state.getValue(FACING);
    }
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        if (state.canSurvive(level, pos)) {
            level.neighborChanged(pos.relative(((Direction)state.getValue(FACING)).getOpposite()), neighborBlock, neighborPos);
        }

    }


}
