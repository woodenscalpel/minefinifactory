package com.woodenscalpel.common.blocks.multiblockutil;

import com.woodenscalpel.common.multiblockentity.BaseEntity;
import dev.architectury.event.EventResult;
import dev.architectury.utils.value.IntValue;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MultiblockBreakEvent {

    public static EventResult onBreak(Level level, BlockPos pos, BlockState state, ServerPlayer player, @Nullable IntValue xp) {
        if(state.getBlock() instanceof IMultiblockEntity && state.hasBlockEntity()){
            ((IMultiblockEntity) level.getBlockEntity(pos)).onBreak();
        }
        AABB blockabove = new AABB(pos, new BlockPos(pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1));
        List<BaseEntity> ents = level.getEntitiesOfClass(BaseEntity.class, blockabove);
        for (BaseEntity e : ents) {
            if(e.isBlock(pos,e.getBlocks())){
                e.destroyBlockandReconstitute(pos);
            }
        }
        return EventResult.pass();
    }
}
