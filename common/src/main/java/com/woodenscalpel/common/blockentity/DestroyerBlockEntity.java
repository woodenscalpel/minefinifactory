package com.woodenscalpel.common.blockentity;

import com.woodenscalpel.Minefinifactory;
import com.woodenscalpel.common.blockentity.util.TickableBlockEntity;
import com.woodenscalpel.common.blocks.WelderBlock;
import com.woodenscalpel.common.blocks.multiblockutil.IMultiblockEntity;
import com.woodenscalpel.common.init.BlockInit;
import com.woodenscalpel.common.mastertick.MasterTick;
import com.woodenscalpel.common.multiblockentity.BaseEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DestroyerBlockEntity extends BlockEntity implements TickableBlockEntity {



    //Constructors

    public DestroyerBlockEntity(BlockEntityType<DestroyerBlockEntity> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public DestroyerBlockEntity(BlockPos pos, BlockState blockState) {
        super(BlockInit.destroyerBlockEntity.get(), pos, blockState);
    }




    //Tick
    @Override
    public void tick() {
        if(level.isClientSide){return;}
        if(MasterTick.isMasterTick()){
            BlockPos weldPos = this.getBlockPos().relative(this.getBlockState().getValue(WelderBlock.FACING));
            AABB blockabove = new AABB(weldPos, new BlockPos(weldPos.getX() + 1, weldPos.getY() + 1, weldPos.getZ() + 1));
            List<BaseEntity> ents = level.getEntitiesOfClass(BaseEntity.class, blockabove);
           for (BaseEntity e : ents) {
               if(e.isBlock(weldPos,e.getBlocks())){
                  e.destroyBlockandReconstitute(weldPos);
               }
            }

        }

    }



}
