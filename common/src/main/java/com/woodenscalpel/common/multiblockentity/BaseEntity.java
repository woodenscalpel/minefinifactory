package com.woodenscalpel.common.multiblockentity;

import com.woodenscalpel.Minefinifactory;
import com.woodenscalpel.common.blocks.ConveyorBlock;
import com.woodenscalpel.common.init.BlockInit;
import com.woodenscalpel.common.init.EntityInit;
import com.woodenscalpel.common.misc.Helpers;
import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;
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
import java.util.Stack;

public class BaseEntity extends Entity {

    public final int TICKSPERBLOCK = 5;
    public enum InfluenceTypes{
        CONVEYOR,
        PUSHER,
        GRAVITY
    }
    public Stack<Tuple<InfluenceTypes, Direction>> influenceStack;

    public boolean isIdle;
    public Direction moveDirection;
    public List<Vec3i> relPositions;
    public List<BlockState> states;


    public BaseEntity(EntityType<BaseEntity> entityType, Level level) {
        super(entityType, level);
        relPositions = new ArrayList<>();
        states= new ArrayList<>();
        influenceStack = new Stack<>();


        relPositions.add(new Vec3i(0,0,0));
        relPositions.add(new Vec3i(1,0,0));
        relPositions.add(new Vec3i(1,1,1));
        states.add(Blocks.OAK_FENCE.defaultBlockState());
        states.add(Blocks.ANDESITE.defaultBlockState());
        states.add(Blocks.STONE.defaultBlockState());
    }

    public BaseEntity(Level level, double x, double y, double z){
        this(EntityInit.baseEntityType.get() , level);
        isIdle = true;
        moveDirection = Direction.DOWN;

        //debug

        setPos(x,y,z);
    }


    @Override
    public void tick() {
        if(level().isClientSide){return;}

        this.noPhysics = true;// We do our own physics.
        this.horizontalCollision = false;
        this.verticalCollision = false;
        super.tick();

        if (Minefinifactory.mastertickcount % TICKSPERBLOCK == 0){
            maintick();
        }
        else{
            subtick();
        }
    }

    private void subtick(){
        if(!isIdle && moveDirection != null){
            move(MoverType.SELF,Helpers.Vec3itof(moveDirection.getNormal()).scale(1F/TICKSPERBLOCK));
        }
        this.influenceStack = new Stack<>(); //Only count influence on mainframes
    }
    private void maintick() {

        snaptoblock();//Failsafe, Should be aligned to block already

        //If idle since last maintick, place blocks in world so they can be operated on by machines.
        if (isIdle && isNotPlaced()) {
            placeBlocks();
        }

        moveDirection = getMovementDirection(); // returns whatever movement influence is highest priority / amount (movement influence is added by other blocks). returns null if doesn't want to move

        //if idle since last maintick, but starting to move now, remove blocks from world.
        if (moveDirection != null && isIdle) {
            removeBlocks();
        }

        if (moveDirection == null || !canMove(moveDirection)) {
            isIdle = true;
        } else {

            if(canMove(moveDirection)) {
                isIdle = false; //This is what is checked to start movement
            }
        }

    }

    private boolean canMove(Direction moveDirection) {
        /*
        Collision check. Iterates through all block positions, but can maybe be optimized to only check relevant positions
         */
        for(Vec3i pos : relPositions){
            BlockPos groundcheck = new BlockPos(getBasePos().offset(pos)).relative(moveDirection);
            if(level().getBlockState(groundcheck) != Blocks.AIR.defaultBlockState()) {
                return false;
            }
        }
        return true;
    }

    private Direction getMovementDirection() {



        /*
        MOVEMENT INFLUENCE PRIORITY
         - Gravity

         -Push EAST/WEST
         -PUSH NORTH/SOUTH
         -PUSH UP/DOWN

         -CONVEYOR (additive)
         */
        if(canMove(Direction.DOWN)){
            return Direction.DOWN;
        }

        //get Conveyor influence
        getConveyorInfluence();

        //TODO Very ugly switch statement ahead. Seems like something that could be condensed into a one-liner

        int n_push_influence = 0;
        int e_push_influence = 0;
        int s_push_influence = 0;
        int w_push_influence = 0;
        int u_push_influence = 0;
        int d_push_influence = 0;

        int ew_conveyor_influence = 0;
        int ns_conveyor_influence = 0;

        if(influenceStack != null){
        while(!influenceStack.empty()){
            Tuple<InfluenceTypes,Direction> influencetuple = influenceStack.pop();
            InfluenceTypes type = influencetuple.getA();
            Direction dir = influencetuple.getB();
            switch (type){
                case PUSHER:
                    switch (dir){
                        case UP -> u_push_influence++;
                        case DOWN -> d_push_influence++;
                        case NORTH -> n_push_influence++;
                        case EAST -> e_push_influence++;
                        case SOUTH -> s_push_influence++;
                        case WEST -> w_push_influence++;
                    }
                    break;
                case CONVEYOR:

                    switch (dir) {
                        case NORTH -> ns_conveyor_influence++;
                        case EAST -> ew_conveyor_influence++;
                        case SOUTH -> ns_conveyor_influence--;
                        case WEST -> ew_conveyor_influence--;
                    }
                    break;
                    }
            }}

        if(e_push_influence>0){return Direction.EAST;}
        if(w_push_influence>0){return Direction.WEST;}
        if(n_push_influence>0){return Direction.NORTH;}
        if(s_push_influence>0){return Direction.SOUTH;}
        if(u_push_influence>0){return Direction.UP;}
        if(d_push_influence>0){return Direction.DOWN;}

        if(ew_conveyor_influence >0){return Direction.EAST;}
        if(ew_conveyor_influence <0){return Direction.WEST;}
        if(ns_conveyor_influence >0){return Direction.NORTH;}
        if(ns_conveyor_influence <0){return Direction.SOUTH;}

        return null;
    }

    private void getConveyorInfluence() {

        for(int i=0;i<relPositions.size();i++) {
            BlockPos belowPos = new BlockPos(getBasePos().offset(relPositions.get(i))).relative(Direction.DOWN);
           if (level().getBlockState(belowPos).getBlock() == BlockInit.conveyorBlock.get()) {
               influenceStack.push(new Tuple<>(InfluenceTypes.CONVEYOR, level().getBlockState(belowPos).getValue(ConveyorBlock.FACING)));
           }
        }

    }

    private void removeBlocks() {

        for(int i=0;i<relPositions.size();i++) {
            BlockPos absPos = new BlockPos(getBasePos().offset(relPositions.get(i)));
            level().setBlock(absPos,Blocks.AIR.defaultBlockState(),3); //TODO investigate flags. 3 is what piston uses
        }
    }

    private void placeBlocks() {
        for(int i=0;i<relPositions.size();i++){
            BlockPos absPos = new BlockPos(getBasePos().offset(relPositions.get(i)));
            BlockState state = states.get(i);
            level().setBlock(absPos,state,3); //TODO investigate flags. 3 is what piston uses
        }
    }

    public boolean isNotPlaced() {
        //Checks if first block is air
        return level().getBlockState(new BlockPos(this.getBasePos().offset(relPositions.get(0)))) == Blocks.AIR.defaultBlockState() ;
    }

    public Vec3i getBasePos() {
        return new Vec3i(getBlockX(),getBlockY(),getBlockZ());
    }

    private void snaptoblock() {
        this.setPos(Math.round(this.getX()), Math.round(this.getY()), Math.round(this.getZ()));
    }

    @Override
    public void setPos(double x, double y, double z) {
        this.setPosRaw(x, y, z);
        this.setBoundingBox(this.makeBoundingBox(x,y,z));
    }


    protected AABB makeBoundingBox(double x, double y, double z) {
        return new AABB(x,y,z,x+3,y+2,z+3);
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

}
