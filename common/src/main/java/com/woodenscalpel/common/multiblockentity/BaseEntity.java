package com.woodenscalpel.common.multiblockentity;

import com.woodenscalpel.Minefinifactory;
import com.woodenscalpel.common.init.BlockInit;
import com.woodenscalpel.common.init.EntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class BaseEntity extends Entity {
    public int tickcount;

    public BaseEntity(EntityType<BaseEntity> entityType, Level level) {
        super(entityType, level);
    }

    public BaseEntity(Level level, double x, double y, double z){
        tickcount = 0;
        this(EntityInit.baseEntityType.get() , level);
        setPos(x,y,z);
    }

    @Override
    public void setPos(double x, double y, double z) {
        this.setPosRaw(x, y, z);
        this.setBoundingBox(this.makeBoundingBox(x,y,z));
    }

    protected AABB makeBoundingBox(double x, double y, double z) {
        //return new AABB(x-0.5,y,z-0.5,x+.4,y+1,z+.4);
        return new AABB(x,y,z,x+3,y+2,z+3);

        //return super.makeBoundingBox();
    }


    public void push(Direction dir){
        List<BlockPos> blocks = getBlocks();
        List<BlockState> blockStates = getBlockStates();


        for(BlockPos b: blocks){
            level().setBlock(b, Blocks.AIR.defaultBlockState(),3); //TODO set proper flags
        }

        for(int i = 0; i<blocks.size(); i++){
            level().setBlock(blocks.get(i).relative(dir),blockStates.get(i),3);
        }

        BlockPos newpos = blockPosition().relative(dir);
        setPos(new Vec3(newpos.getX(),newpos.getY(),newpos.getZ()));

    }

    public List<BlockPos> getBlocks() {
        AABB bounds = getBoundingBox();
        List<BlockPos> blocks = new ArrayList<>();
        for (int x = (int) bounds.minX; x < bounds.maxX; x++) {
            for (int y = (int) bounds.minY; y < bounds.maxY; y++) {
                for (int z = (int) bounds.minZ; z < bounds.maxZ; z++) {
                    blocks.add(new BlockPos(x, y, z));
                }

            }
        }
        Minefinifactory.LOGGER.info(String.valueOf(blocks.size()));
        Minefinifactory.LOGGER.info(String.valueOf(blocks));
        return blocks;
    }

    public List<BlockState> getBlockStates() {
        List<BlockPos> blockPosList = getBlocks();
        List<BlockState> blockStateList= new ArrayList<>();
        for(BlockPos b: blockPosList){
            blockStateList.add(level().getBlockState(b));
        }
        return blockStateList;
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {

    }

    @Override
    public void tick() {

        super.tick();
    }
}
