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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class WelderBlockEntity extends BlockEntity implements TickableBlockEntity, IMultiblockEntity {


    public List<BlockPos> connections;

    //Constructors

    public WelderBlockEntity(BlockEntityType<WelderBlockEntity> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public WelderBlockEntity(BlockPos pos, BlockState blockState) {
        super(BlockInit.welderBlockEntity.get(), pos, blockState);
        this.connections = new ArrayList<>();
    }


    //Multiblock stuff
    @Override
    public void onPlace() {
        for(Direction dir : Direction.values()){
            BlockEntity e = level.getBlockEntity(getBlockPos().relative(dir));
            if(e instanceof WelderBlockEntity){
                if(e.getBlockState().getValue(WelderBlock.FACING) == this.getBlockState().getValue(WelderBlock.FACING)){
                    connections.add(e.getBlockPos());
                }
            }
        }
    }

    @Override
    public void onBreak() {

        for(Direction dir : Direction.values()) {
            BlockEntity e = level.getBlockEntity(getBlockPos().relative(dir));
            if(e instanceof WelderBlockEntity){
              if (((WelderBlockEntity) e).connections.contains(this.getBlockPos())){
                  ((WelderBlockEntity) e).connections.remove(this.getBlockPos());
                }
            }
        }

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
                    for(BlockPos c : connections){
                        if(level.getBlockState(c) != Blocks.AIR.defaultBlockState()) {

                            BlockPos weldPos2 = c.relative(level.getBlockState(c).getValue(WelderBlock.FACING));
                            AABB blockabove2 = new AABB(weldPos2, new BlockPos(weldPos2.getX() + 1, weldPos2.getY() + 1, weldPos2.getZ() + 1));
                            List<BaseEntity> ents2 = level.getEntitiesOfClass(BaseEntity.class, blockabove2);
                            for (BaseEntity e2 : ents2) {
                                if (e2 != e) {
                                    if (e2.isBlock(weldPos2, e2.getBlocks())) {
                                        weld(e, e2);
                                    }
                                }
                            }
                        }
                    }
                };
            }

        }

    }

    public void neighbourChanged(BlockPos neighbourPos){
        if(connections.contains(neighbourPos)){
            connections.remove(neighbourPos);
        }
        this.level.sendBlockUpdated(getBlockPos(),this.getBlockState(),this.getBlockState(), Block.UPDATE_ALL);
    }

    private void weld(BaseEntity e, BaseEntity e2) {



       List<Tuple<Vec3i,BlockState>> newblocks = e.getBlocks();

        Vec3i offset = e2.getBasePos().subtract(e.getBasePos());

        for(Tuple<Vec3i,BlockState> b : e2.getBlocks()){
            newblocks.add(new Tuple<>(b.getA().offset(offset), b.getB()));
        }


        //e.recalculateHitbox();
        e.destroy();
        e2.destroy();
        level.addFreshEntity(new BaseEntity(level,e.position(), newblocks));
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag nbt = super.getUpdateTag();
        saveAdditional(nbt);
        return nbt;
    }




    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        CompoundTag modTag = new CompoundTag();
        List<Integer> connints = new ArrayList<>();
        for(BlockPos pos : connections){
            connints.add(pos.getX());
            connints.add(pos.getY());
            connints.add(pos.getZ());
        }
        modTag.putIntArray("connections",connints);
        tag.put(Minefinifactory.MOD_ID,modTag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        CompoundTag modTag = tag.getCompound(Minefinifactory.MOD_ID);
        int[] connints = modTag.getIntArray("connections");

        List<BlockPos> conn = new ArrayList<>();
        for(int i = 0; i<(connints.length /3); i++){
            conn.add(new BlockPos(connints[i*3],connints[i*3+1],connints[i*3+2]));
        }
        this.connections = conn;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
