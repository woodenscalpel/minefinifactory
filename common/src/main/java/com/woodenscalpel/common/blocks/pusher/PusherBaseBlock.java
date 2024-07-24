package com.woodenscalpel.common.blocks.pusher;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.woodenscalpel.Minefinifactory;
import com.woodenscalpel.common.init.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.PushReaction;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PusherBaseBlock extends PistonBaseBlock {
    public PusherBaseBlock(boolean isSticky, Properties properties) {
        super(isSticky, properties);
    }

    private boolean getNeighborSignal(SignalGetter signalGetter, BlockPos pos, Direction direction) {
        Direction[] var4 = Direction.values();
        int var5 = var4.length;

        int var6;
        for(var6 = 0; var6 < var5; ++var6) {
            Direction direction2 = var4[var6];
            if (direction2 != direction && signalGetter.hasSignal(pos.relative(direction2), direction2)) {
                return true;
            }
        }

        if (signalGetter.hasSignal(pos, Direction.DOWN)) {
            return true;
        } else {
            BlockPos blockPos = pos.above();
            Direction[] var10 = Direction.values();
            var6 = var10.length;

            for(int var11 = 0; var11 < var6; ++var11) {
                Direction direction3 = var10[var11];
                if (direction3 != Direction.DOWN && signalGetter.hasSignal(blockPos.relative(direction3), direction3)) {
                    return true;
                }
            }

            return false;
        }
    }


    public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        Direction direction = (Direction)state.getValue(FACING);
        BlockState blockState = (BlockState)state.setValue(EXTENDED, true);
        if (!level.isClientSide) {
            boolean bl = this.getNeighborSignal(level, pos, direction);
            if (bl && (id == 1 || id == 2)) {
                level.setBlock(pos, blockState, 2);
                return false;
            }

            if (!bl && id == 0) {
                return false;
            }
        }

        if (id == 0) {
            if (!this.moveBlocks(level, pos, direction, true)) {
                return false;
            }

            level.setBlock(pos, blockState, 67);
            level.playSound((Player)null, pos, SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.25F + 0.6F);
            level.gameEvent(GameEvent.BLOCK_ACTIVATE, pos, GameEvent.Context.of(blockState));
        } else if (id == 1 || id == 2) {
            BlockEntity blockEntity = level.getBlockEntity(pos.relative(direction));
            if (blockEntity instanceof PistonMovingBlockEntity) {
                ((PistonMovingBlockEntity)blockEntity).finalTick();
            }

            BlockState blockState2 = (BlockState)((BlockState)Blocks.MOVING_PISTON.defaultBlockState().setValue(MovingPistonBlock.FACING, direction)).setValue(MovingPistonBlock.TYPE, PistonType.DEFAULT);
            level.setBlock(pos, blockState2, 20);
            level.setBlockEntity(MovingPistonBlock.newMovingBlockEntity(pos, blockState2, (BlockState)this.defaultBlockState().setValue(FACING, Direction.from3DDataValue(param & 7)), direction, false, true));
            level.blockUpdated(pos, blockState2.getBlock());
            blockState2.updateNeighbourShapes(level, pos, 2);

            level.removeBlock(pos.relative(direction), false);

            level.playSound((Player)null, pos, SoundEvents.PISTON_CONTRACT, SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.15F + 0.6F);
            level.gameEvent(GameEvent.BLOCK_DEACTIVATE, pos, GameEvent.Context.of(blockState2));
        }

        return true;
    }


    private boolean moveBlocks(Level level, BlockPos pos, Direction facing, boolean extending) {
        BlockPos blockPos = pos.relative(facing);
        if (!extending && level.getBlockState(blockPos).is(Blocks.PISTON_HEAD)) {
            level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 20);
        }

        PistonStructureResolver pistonStructureResolver = new PistonStructureResolver(level, pos, facing, extending);
        if (!pistonStructureResolver.resolve()) {
            return false;
        } else {
            Map<BlockPos, BlockState> map = Maps.newHashMap();
            List<BlockPos> list = pistonStructureResolver.getToPush();
            List<BlockState> list2 = Lists.newArrayList();

            for(int i = 0; i < list.size(); ++i) {
                BlockPos blockPos2 = (BlockPos)list.get(i);
                BlockState blockState = level.getBlockState(blockPos2);
                list2.add(blockState);
                map.put(blockPos2, blockState);
            }

            List<BlockPos> list3 = pistonStructureResolver.getToDestroy();
            BlockState[] blockStates = new BlockState[list.size() + list3.size()];
            Direction direction = extending ? facing : facing.getOpposite();
            int j = 0;

            int k;
            BlockPos blockPos3;
            BlockState blockState2;
            for(k = list3.size() - 1; k >= 0; --k) {
                blockPos3 = (BlockPos)list3.get(k);
                blockState2 = level.getBlockState(blockPos3);
                BlockEntity blockEntity = blockState2.hasBlockEntity() ? level.getBlockEntity(blockPos3) : null;
                dropResources(blockState2, level, blockPos3, blockEntity);
                level.setBlock(blockPos3, Blocks.AIR.defaultBlockState(), 18);
                level.gameEvent(GameEvent.BLOCK_DESTROY, blockPos3, GameEvent.Context.of(blockState2));
                if (!blockState2.is(BlockTags.FIRE)) {
                    level.addDestroyBlockEffect(blockPos3, blockState2);
                }

                blockStates[j++] = blockState2;
            }

            for(k = list.size() - 1; k >= 0; --k) {
                blockPos3 = (BlockPos)list.get(k);
                blockState2 = level.getBlockState(blockPos3);
                blockPos3 = blockPos3.relative(direction);
                map.remove(blockPos3);
                BlockState blockState3 = (BlockState)Blocks.MOVING_PISTON.defaultBlockState().setValue(FACING, facing);
                level.setBlock(blockPos3, blockState3, 68);
                level.setBlockEntity(MovingPistonBlock.newMovingBlockEntity(blockPos3, blockState3, (BlockState)list2.get(k), facing, extending, false));
                blockStates[j++] = blockState2;
            }

            if (extending) {
                PistonType pistonType =  PistonType.DEFAULT;
                BlockState blockState4 = (BlockState)((BlockState) BlockInit.pusherHeadBlock.get().defaultBlockState().setValue(PusherBaseBlock.FACING, facing)).setValue(PusherHeadBlock.TYPE, pistonType);
                blockState2 = (BlockState)((BlockState)Blocks.MOVING_PISTON.defaultBlockState().setValue(MovingPistonBlock.FACING, facing)).setValue(MovingPistonBlock.TYPE, PistonType.DEFAULT);
                map.remove(blockPos);
                level.setBlock(blockPos, blockState2, 68);
                level.setBlockEntity(MovingPistonBlock.newMovingBlockEntity(blockPos, blockState2, blockState4, facing, true, true));
            }

            BlockState blockState5 = Blocks.AIR.defaultBlockState();
            Iterator var25 = map.keySet().iterator();

            while(var25.hasNext()) {
                BlockPos blockPos4 = (BlockPos)var25.next();
                level.setBlock(blockPos4, blockState5, 82);
            }

            var25 = map.entrySet().iterator();

            BlockPos blockPos5;
            while(var25.hasNext()) {
                Map.Entry<BlockPos, BlockState> entry = (Map.Entry)var25.next();
                blockPos5 = (BlockPos)entry.getKey();
                BlockState blockState6 = (BlockState)entry.getValue();
                blockState6.updateIndirectNeighbourShapes(level, blockPos5, 2);
                blockState5.updateNeighbourShapes(level, blockPos5, 2);
                blockState5.updateIndirectNeighbourShapes(level, blockPos5, 2);
            }

            j = 0;

            int l;
            for(l = list3.size() - 1; l >= 0; --l) {
                blockState2 = blockStates[j++];
                blockPos5 = (BlockPos)list3.get(l);
                blockState2.updateIndirectNeighbourShapes(level, blockPos5, 2);
                level.updateNeighborsAt(blockPos5, blockState2.getBlock());
            }

            for(l = list.size() - 1; l >= 0; --l) {
                level.updateNeighborsAt((BlockPos)list.get(l), blockStates[j++].getBlock());
            }

            if (extending) {
                level.updateNeighborsAt(blockPos, Blocks.PISTON_HEAD);
            }

            return true;
        }
    }
}
