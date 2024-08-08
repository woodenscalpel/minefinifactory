package com.woodenscalpel.common.multiblockentity;

import com.woodenscalpel.Minefinifactory;
import com.woodenscalpel.common.blocks.ConveyorBlock;
import com.woodenscalpel.common.init.BlockInit;
import com.woodenscalpel.common.init.EntityInit;
import com.woodenscalpel.common.mastertick.MasterTick;
import com.woodenscalpel.common.misc.Helpers;
import com.woodenscalpel.common.network.EntityDataListSerializer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.woodenscalpel.common.mastertick.MasterTick.TICKSPERBLOCK;

public class BaseEntity extends Entity {

    public static final EntityDataAccessor<List<Tuple<Vec3i, BlockState>>> BLOCKS = SynchedEntityData.defineId(BaseEntity.class, EntityDataListSerializer.BLOCKTUPLE);

    public boolean isBlock(BlockPos absPos,List<Tuple<Vec3i,BlockState>> blocks) {
       // Vec3i relpos = ((Vec3i) absPos.offset(this.getBasePos().multiply(-1)));
        Vec3i relpos = this.getBasePos().multiply(-1).offset(absPos);
        for(int i=0; i<blocks.size();i++){
           if(blocks.get(i).getA().equals(relpos)){
               return true;}
        }
        return false;
    }

    /*
    public void recalculateHitbox() {
        this.setBoundingBox(this.makeBoundingBox(this.position().x,this.position().y,this.position().z));
    }
     */

    public void destroy() {
        //might need to clean up blocks in world or something before killing
        this.removeBlocks(getBlocks());
        this.kill();
    }

    public void destroyBlockandReconstitute(BlockPos pos) {
        removeBlocks(getBlocks()); //from world
        List<Tuple<Vec3i,BlockState>> blocks = getBlocks();
        Vec3i relpos = pos.offset(getBasePos().multiply(-1));

        Tuple<Vec3i,BlockState> blocktoremove = scanfirsttuple(blocks, relpos);
        blocks.remove(blocktoremove);
        reconstituteBlocks(blocks);
    }

    private void reconstituteBlocks(List<Tuple<Vec3i, BlockState>> blocks) {
        //Lag ahead! changing the block structure to a linked list type thing would be better, but it would make the data packets bigger and I think right now I am sending the data
        //packets every tick, I am going to change this anyway though
        while(!blocks.isEmpty()){
            /*
            Recursively add blocks neighbours to queue. Pop a neighbour from queue, check if it exists in the list, if
            it does add it to the new list and add its neighbours to the queue
             */
            List<Tuple<Vec3i,BlockState>> newblocks = new ArrayList<>();
            Stack<Vec3i> queue = new Stack<>();

            //seed with first block
            Tuple<Vec3i,BlockState> firstblock = blocks.get(0);
            newblocks.add(firstblock);
            blocks.remove(firstblock);
            Minefinifactory.LOGGER.info(""+blocks.size());
            Minefinifactory.LOGGER.info(newblocks.toString());
            addneighbours(queue,firstblock.getA());
            //start algo
            while(!queue.isEmpty()){
                Vec3i check = queue.pop();
                Tuple<Vec3i,BlockState> tuple = scanfirsttuple(blocks,check);
                if(tuple != null){
                    newblocks.add(tuple);
                    blocks.remove(tuple);
                    addneighbours(queue,check);
                }
            }
            Minefinifactory.LOGGER.info("SPAWINGING" +newblocks.size());
           level().addFreshEntity(new BaseEntity(level(), this.position(),newblocks)); // TODO all blocks will retain parents origin position when they should probably be recalculated
        }
        Minefinifactory.LOGGER.info("DESTROYING!");
        this.destroy();
    }

    public Tuple<Vec3i,BlockState> scanfirsttuple(List<Tuple<Vec3i,BlockState>> blocks, Vec3i check){

        for(Tuple<Vec3i,BlockState> t : blocks){
            if(t.getA().equals(check)){return t;}
        }
        return null;
    }

    public void addneighbours(Stack<Vec3i> queue, Vec3i relpos){
        for(Direction d : Direction.values()){
            queue.add(relpos.relative(d));
        }
    }


    public enum InfluenceTypes{
        CONVEYOR,
        PUSHER,
        GRAVITY
    }
    public Stack<Tuple<InfluenceTypes, Direction>> influenceStack;

    public boolean isIdle;
    public Direction moveDirection;

    //public List<Tuple<Vec3i,BlockState>> blocks;

    //public List<Vec3i> relPositions;
    //public List<BlockState> states;


    public BaseEntity(EntityType<BaseEntity> entityType, Level level) {
        super(entityType, level);
        //relPositions = new ArrayList<>();
        //states= new ArrayList<>();
        influenceStack = new Stack<>();

       // blocks = new ArrayList<>();
/*
        blocks.add(new Tuple<>(new Vec3i(0,0,0), Blocks.BAMBOO_MOSAIC.defaultBlockState()));
        blocks.add(new Tuple<>(new Vec3i(1,0,0), Blocks.ACACIA_FENCE.defaultBlockState()));
        blocks.add(new Tuple<>(new Vec3i(1,1,1), Blocks.BROWN_STAINED_GLASS.defaultBlockState()));

 */
        //recalculateHitbox();

    }
/*
    public BaseEntity(Level level, double x, double y, double z){
        this(EntityInit.baseEntityType.get() , level);
        isIdle = true;
        moveDirection = Direction.DOWN;

        blocks = new ArrayList<>();
        blocks.add(new Tuple<>(new Vec3i(0,0,0), Blocks.BAMBOO_MOSAIC.defaultBlockState()));
        blocks.add(new Tuple<>(new Vec3i(1,0,0), Blocks.ACACIA_FENCE.defaultBlockState()));
        blocks.add(new Tuple<>(new Vec3i(1,1,1), Blocks.BROWN_STAINED_GLASS.defaultBlockState()));

        //debug

        setPos(x,y,z);
    }

 */

    public BaseEntity(Level level, Vec3 position, List<Tuple<Vec3i, BlockState>> newblocks) {

        this(EntityInit.baseEntityType.get() , level);
        isIdle = true;
        moveDirection = Direction.DOWN;
        this.setBlocks(newblocks);
        setPos(position.x,position.y,position.z);
    }


    @Override
    public void tick() {
        if(level().isClientSide){return;}

        this.noPhysics = true;// We do our own physics.
        this.horizontalCollision = false;
        this.verticalCollision = false;
        super.tick();

        if (MasterTick.isMasterTick()){
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
        List<Tuple<Vec3i,BlockState>> blocks = getBlocks(); //TODO Big packet every tick is not good

        //If idle since last maintick, place blocks in world so they can be operated on by machines.
        if (isIdle && isNotPlaced(blocks)) {
            placeBlocks(blocks);
        }

        moveDirection = getMovementDirection(blocks); // returns whatever movement influence is highest priority / amount (movement influence is added by other blocks). returns null if doesn't want to move

        //if idle since last maintick, but starting to move now, remove blocks from world.
        if (moveDirection != null && isIdle) {
            removeBlocks(blocks);
        }

        if (moveDirection == null || !canMove(moveDirection,blocks)) {
            isIdle = true;
        } else {

            if(canMove(moveDirection,blocks)) {
                isIdle = false; //This is what is checked to start movement
            }
        }

    }

    private boolean canMove(Direction moveDirection,List<Tuple<Vec3i,BlockState>> blocks) {
        /*
        Collision check. Iterates through all block positions, but can maybe be optimized to only check relevant positions
         */
        for(Tuple<Vec3i,BlockState> t: blocks){
            Vec3i pos = t.getA();
            BlockPos groundcheck = new BlockPos(getBasePos().offset(pos)).relative(moveDirection);
            if(level().getBlockState(groundcheck) != Blocks.AIR.defaultBlockState()) {
                return false;
            }
        }
        return true;
    }

    private Direction getMovementDirection(List<Tuple<Vec3i,BlockState>> blocks) {



        /*
        MOVEMENT INFLUENCE PRIORITY
         - Gravity

         -Push EAST/WEST
         -PUSH NORTH/SOUTH
         -PUSH UP/DOWN

         -CONVEYOR (additive)
         */
        if(canMove(Direction.DOWN,blocks)){
            return Direction.DOWN;
        }

        //get Conveyor influence
        getConveyorInfluence(blocks);

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

    private void getConveyorInfluence(List<Tuple<Vec3i,BlockState>> blocks) {

        for(int i=0;i<blocks.size();i++) {
            BlockPos belowPos = new BlockPos(getBasePos().offset(blocks.get(i).getA())).relative(Direction.DOWN);
           if (level().getBlockState(belowPos).getBlock() == BlockInit.conveyorBlock.get()) {
               influenceStack.push(new Tuple<>(InfluenceTypes.CONVEYOR, level().getBlockState(belowPos).getValue(ConveyorBlock.FACING)));
           }
        }

    }

    private void removeBlocks(List<Tuple<Vec3i,BlockState>> blocks) {
        //from the world
        for(int i=0;i<blocks.size();i++) {
            BlockPos absPos = new BlockPos(getBasePos().offset(blocks.get(i).getA()));
            removeBlock(absPos);
        }
    }

    private void removeBlock(BlockPos pos) {
        level().setBlock(pos,Blocks.AIR.defaultBlockState(),3); //TODO investigate flags. 3 is what piston uses
    }

    private void placeBlocks(List<Tuple<Vec3i,BlockState>> blocks) {
        for(int i=0;i<blocks.size();i++){
            BlockPos absPos = new BlockPos(getBasePos().offset(blocks.get(i).getA()));
            BlockState state = blocks.get(i).getB();
            level().setBlock(absPos,state,3); //TODO investigate flags. 3 is what piston uses
        }
    }

    public boolean isNotPlaced(List<Tuple<Vec3i,BlockState>> blocks) {
        //Checks if first block is air
        return level().getBlockState(new BlockPos(this.getBasePos().offset(blocks.get(0).getA()))) == Blocks.AIR.defaultBlockState() ;
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
        this.setBoundingBox(this.makeBoundingBox(x,y,z, getBlocks()));
    }


    protected AABB makeBoundingBox(double x, double y, double z, List<Tuple<Vec3i,BlockState>> blocks) {

        //super constructor calls this before we populate blocks. this placeholder stops null error, then we call again in our constructor
        if(blocks == null){
            return new AABB(x,y,z,x+1,y+1,z+1);
        }

        int minx = Integer.MAX_VALUE;
        int miny = Integer.MAX_VALUE;
        int minz = Integer.MAX_VALUE;
        int maxx = Integer.MIN_VALUE;
        int maxy = Integer.MIN_VALUE;
        int maxz = Integer.MIN_VALUE;
        for(Tuple<Vec3i,BlockState> b : blocks){
            Vec3i rel = b.getA();
            if(rel.getX() < minx){minx = rel.getX();}
            if(rel.getY() < miny){miny = rel.getY();}
            if(rel.getZ() < minz){minz = rel.getZ();}
            if(rel.getX() > maxx){maxx = rel.getX();}
            if(rel.getY() > maxy){maxy = rel.getY();}
            if(rel.getZ() > maxz){maxz = rel.getZ();}
        }

        //return new AABB(x,y,z,x+3,y+2,z+3);
        return new AABB(x+minx,y+miny,z+minz,x+maxx+1,y+maxy+1,z+maxz+1);
    }


    @Override
    protected void defineSynchedData() {
       this.entityData.define(BLOCKS, new ArrayList<>());
    }

    public List<Tuple<Vec3i,BlockState>> getBlocks(){
        return this.entityData.get(BLOCKS);
    }

    protected void setBlocks(List<Tuple<Vec3i,BlockState>> blocks){
        this.entityData.set(BLOCKS,blocks);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {

        int[] rawints = compound.getIntArray("blocktuple");
        int len = rawints[0];

        List<Tuple<Vec3i,BlockState>> blocks = new ArrayList<>();

        for(int i = 0; i<len;i++){
            blocks.add( new Tuple<>(new Vec3i(rawints[i*4+1],rawints[i*4+2],rawints[i*4+3]), Block.BLOCK_STATE_REGISTRY.byId(rawints[i*4+4])));
        }

        setBlocks(blocks);

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {

        List<Integer> serializedints = new ArrayList<>();
        List<Tuple<Vec3i,BlockState>> blocks = getBlocks();
        serializedints.add(blocks.size());

        for(Tuple<Vec3i,BlockState> t : blocks){
            serializedints.add(t.getA().getX());
            serializedints.add(t.getA().getY());
            serializedints.add(t.getA().getZ());
            serializedints.add(Block.BLOCK_STATE_REGISTRY.getId(t.getB()));
        }

        compound.putIntArray("blocktuple",serializedints);

    }

}
