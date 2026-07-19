package com.woodenscalpel.common.multiblockentity;

import com.woodenscalpel.Minefinifactory;
import com.woodenscalpel.common.blockentity.RotatorBlockEntity;
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
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.woodenscalpel.common.mastertick.MasterTick.TICKSPERBLOCK;

public class BaseEntity extends Entity {

    public static final EntityDataAccessor<List<Tuple<Vec3i, BlockState>>> BLOCKS = SynchedEntityData.defineId(BaseEntity.class, EntityDataListSerializer.BLOCKTUPLE);

    public static final List<Direction> DirectionPriority = Arrays.asList(Direction.UP, Direction.DOWN, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);
    private static final Integer GRAVITYPRIORITY = 0;
    private static final Integer CONVEYORPRIORITY = 1;
    private static final Integer LIFTERPRIORITY = 2;
    private static final Integer PISTONPRIORITY = 3;
    private static final Integer CANCELPRIORITY = Integer.MAX_VALUE;
    private static final String PUSHNBTTAG = "dir";

    private Set<BaseEntity> pushProvidence;

    public void addInfluence(Direction dir, int i, UUID uuid) { //direction, priority, UUID
        //Minefinifactory.LOGGER.info("Called");
        if (influenceList2 == null) {
            influenceList2 = new ArrayList<>();
        }
        if (influenceList2 != null) {
            influenceList2.add(new ArrayList<>(Arrays.asList(dir, i, uuid)));
            //Minefinifactory.LOGGER.info("ADDED");
        }

        //recurse in dir

        for(Tuple<Vec3i,BlockState> t: this.getBlocks()){
            Vec3i pos = t.getA();
            BlockPos blockCheck = new BlockPos(getBasePos().offset(pos)).relative(dir);

            AABB blockcheckAABB = new AABB(blockCheck);
            List<BaseEntity> ents = level().getEntitiesOfClass(BaseEntity.class, blockcheckAABB);
            for (BaseEntity e : ents) {
                    if (!e.equals(this) && e.isBlock(blockCheck, e.getBlocks())) {

                        e.addInfluence(dir,i,uuid);
                        break;
                    }
            }
        }


    }

    //public boolean isIdle;
    public enum State {
        IDLE,
        MOVING,
        ROTATING
    }
    public State state;

    boolean rotationClockwise; //determines rotation direction for animation, true if clockwise, false if ccw

    public Tuple<Integer,Direction> internalInfluence;
    public Tuple<Integer,Direction> externalInfluence;
    public Tuple<Integer,Direction> strongestInfluence;

    public List<Object> influenceList;
    public List<List<Object>> influenceList2;

    public CompoundTag pushNBT;

    public boolean lifterIgnoreGravity;




    public BaseEntity(EntityType<BaseEntity> entityType, Level level) {
        super(entityType, level);
    }

    public BaseEntity(Level level, Vec3 position, List<Tuple<Vec3i, BlockState>> newblocks) {

        this(EntityInit.baseEntityType.get() , level);

        //isIdle = true;
        state = State.IDLE;

        internalInfluence = new Tuple<>(-999, null);
        externalInfluence = new Tuple<>(-999, null);
        strongestInfluence = new Tuple<>(-999, null);

        pushNBT = new CompoundTag();
        pushNBT.putInt(PUSHNBTTAG, -1);

        this.setBlocks(newblocks);
        setPos(position.x,position.y,position.z);

        //Below is the initialization for maintick2
        //List of all influences acting on block
        influenceList = new ArrayList<>();
        influenceList2 = new ArrayList<>();
    }

    public void destroy() {
        //might need t9o clean up blocks in world or something before killing
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
            level().addFreshEntity(new BaseEntity(level(), this.position(),newblocks)); // TODO all blocks will retain parents origin position when they should probably be recalculated
        }
        this.destroy();
    }

    public Tuple<Vec3i,BlockState> scanfirsttuple(List<Tuple<Vec3i,BlockState>> blocks, Vec3i check){
        //Takes in relpos as input
        for(Tuple<Vec3i,BlockState> t : blocks){
            if(t.getA().equals(check)){return t;}
        }
        return null;
    }

    public boolean isBlock(BlockPos absPos,List<Tuple<Vec3i,BlockState>> blocks) {
        // Vec3i relpos = ((Vec3i) absPos.offset(this.getBasePos().multiply(-1)));
        Vec3i relpos = this.getBasePos().multiply(-1).offset(absPos);
        for (Tuple<Vec3i, BlockState> block : blocks) {
            if (block.getA().equals(relpos)) {
                return true;
            }
        }
        return false;
    }

    public void addneighbours(Stack<Vec3i> queue, Vec3i relpos){
        for(Direction d : Direction.values()){
            queue.add(relpos.relative(d));
        }
    }




    @Override
    public void tick() {
        if(level().isClientSide){return;}

        this.noPhysics = true;// We do our own physics.
        this.horizontalCollision = false;
        this.verticalCollision = false;
        super.tick();

        //Minefinifactory.LOGGER.info("WTRFD");

        if (MasterTick.isPhase1(getServer())) {
            //Phase 1 - no logic here - influence is propogated through entities by influence sources
            // Wrong! Might as well add gravity influence here maybe
            //Double wrong - waste of time if gravity is low priority, just check if no other influence
        }

        if (MasterTick.isPhase2(getServer())){
            //phase 2 - resolve conflicts

            int strongest = 0;
            if(influenceList2 != null) {
                for (List<Object> influence : influenceList2) {
                    Minefinifactory.LOGGER.info(influence.get(0).toString());
                    if ((int) influence.get(1) > strongest) {
                        strongest = (int) influence.get(1);
                        strongestInfluence = new Tuple<>((int) influence.get(1), (Direction) influence.get(0));
                        Minefinifactory.LOGGER.info(strongestInfluence.getB().getName());

                    }

                }
            }
        }

        if (MasterTick.isMasterTick(this.getServer())){
            influenceList2 = new ArrayList<>();
            maintick2();
        }

        else{
            subtick2();
        }
    }

    private void subtick(){
        //Minefinifactory.LOGGER.info(state.name());
        Direction moveDir = strongestInfluence.getB();
        //moveDirection = getMoveDirFromStack();

        if(state == State.MOVING && moveDir != null) {
            boolean move = true;
            if (pushProvidence != null) {
                for (BaseEntity ent : pushProvidence) {
                    if (ent.strongestInfluence.getB() != moveDir) {
                        move = false;
                    }
                }
                if (move) {
                    move(MoverType.SELF, Helpers.Vec3itof(moveDir.getNormal()).scale(1F / TICKSPERBLOCK));
                }
            }
        }
        if(state == State.ROTATING){
            //rotate(new Rotation Rotation.);
        }
        if(level().getServer().getTickCount() % (TICKSPERBLOCK) == (TICKSPERBLOCK - 1)) {
            resetvarsfornexttick();
        }
    }

    private void subtick2(){
        //Minefinifactory.LOGGER.info(state.name());
        Direction moveDir = strongestInfluence.getB();
        //moveDirection = getMoveDirFromStack();

        //if(state == State.MOVING && moveDir != null) {
        if( moveDir != null) {
                move(MoverType.SELF, Helpers.Vec3itof(moveDir.getNormal()).scale(1F / TICKSPERBLOCK));
        }


        if(level().getServer().getTickCount() % (TICKSPERBLOCK) == (TICKSPERBLOCK - 1)) {
            resetvarsfornexttick();
        }
    }

    private void resetvarsfornexttick() {
        /*
        This is done during subtick, as if done at beginning of maintick it will zero out fields entities that tick before it have set
         */
        lifterIgnoreGravity = false;
        pushProvidence = null;
        internalInfluence = new Tuple<>(-999, null);
        externalInfluence = new Tuple<>(-999, null);
        strongestInfluence = new Tuple<>(-999, null);
    }

    private void maintick2(){
        //cleanup steps from previous refactor idk

        snaptoblock();//Failsafe, Should be aligned to block already
        List<Tuple<Vec3i,BlockState>> blocks = getBlocks(); //TODO Big packet every tick is not good
        //TODO do we have to get this list every tick?

        //If idle since last maintick, place blocks in world so they can be operated on by machines.
        if (state == State.IDLE && isNotPlaced(blocks)) {
            placeBlocks(blocks);
        }

        //getMoveDirInitialScan2(blocks);
        //assertivePushCheck(blocks,new Tuple<>(10,Direction.NORTH));

        /*
        if(strongestInfluence.getB() != null) {
            assertivePushCheck(blocks, strongestInfluence);
        }
        */


        //state = State.MOVING;
        //strongestInfluence = compareInfluence();

        //Minefinifactory.LOGGER.info(strongestInfluence.getB().getName());

        //Minefinifactory.LOGGER.info(strongestInfluence.getA().toString());
        if(strongestInfluence.getB() != null) {
            preventCollision(blocks);
        }

        //if idle since last maintick, but starting to move now, remove blocks from world.
        if (strongestInfluence.getB() != null && state == State.IDLE) {
            removeBlocks(blocks);
        }

        if (strongestInfluence.getB() == null) {
            state = State.IDLE;
        } else {
            state = State.MOVING; //This is what is checked to start movement
        }


    }

    private void maintick() {

        snaptoblock();//Failsafe, Should be aligned to block already
        List<Tuple<Vec3i,BlockState>> blocks = getBlocks(); //TODO Big packet every tick is not good

        //If idle since last maintick, place blocks in world so they can be operated on by machines.
        if (state == State.IDLE && isNotPlaced(blocks)) {
            placeBlocks(blocks);
        }

        internalInfluence = getMoveDirInitialScan(blocks);
        strongestInfluence = compareInfluence();

        //Check for potential collision with other entity, and prevent the movement of one of them based on priority.
        //might offload to subtick, but would cause jerky movement
        if(strongestInfluence.getB() != null) {
            preventCollision(blocks);
        }

        //if idle since last maintick, but starting to move now, remove blocks from world.
        if (strongestInfluence.getB() != null && state == State.IDLE) {
            removeBlocks(blocks);
        }

        if (strongestInfluence.getB() == null) {
            state = State.IDLE;
        } else {
            state = State.MOVING; //This is what is checked to start movement
        }

    }

    private Tuple<Integer, Direction> compareInfluence() {
        if(externalInfluence.getA() > internalInfluence.getA()){
            return externalInfluence;
        }
        return internalInfluence;
    }

    private void preventCollision(List<Tuple<Vec3i, BlockState>> blocks) {
        for(Tuple<Vec3i,BlockState> t: blocks) {
            Vec3i pos = t.getA();
            BlockPos blockCheck = new BlockPos(getBasePos().offset(pos)).offset(strongestInfluence.getB().getNormal().multiply(2));

            //AABB blockcheckAABB = new AABB(blockCheck, new BlockPos(blockCheck.getX() + 1, blockCheck.getY() + 1, blockCheck.getZ() + 1));
            AABB blockcheckAABB = new AABB(blockCheck);
            List<BaseEntity> ents = level().getEntitiesOfClass(BaseEntity.class, blockcheckAABB);
            for (BaseEntity e : ents) {

                if(e.strongestInfluence.getB() == this.strongestInfluence.getB().getOpposite()){
                    if (!e.equals(this) && e.isBlock(blockCheck, e.getBlocks())) {
                        Minefinifactory.LOGGER.info("PREVENT");
                        //TODO implement push type priority
                        //goes through direction priority. The first entity that matches the direction gets to keep its direction
                        for(Direction dir: DirectionPriority){
                            if(this.strongestInfluence.getB() == dir){
                                e.cancelmove();
                               return;
                            }
                            if(e.strongestInfluence.getB() == dir){
                                this.cancelmove();
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    private void cancelmove() {
        this.strongestInfluence = new Tuple<>(CANCELPRIORITY,null);

        //todo entity wont know it was canceled and may try to repush when it is polled in minecrafts update order and redo calculations. Should add a flag for if entity has calculated this tick
        if(pushProvidence != null) {
            for (BaseEntity e : this.pushProvidence) {
                Minefinifactory.LOGGER.info("CANCEL MOVE OF " + e.toString());
                e.strongestInfluence = new Tuple<>(CANCELPRIORITY,null);
            }
        }
    }


    private Tuple<Integer,Direction> getMoveDirInitialScan(List<Tuple<Vec3i,BlockState>> blocks) {


        int LIFTERHEIGHT = 5;
        Queue<Direction> conveyorQueue = new ArrayDeque<Direction>();
        //scan under
        boolean gravity = true;
        for (int i = 0; i < blocks.size(); i++) {
            for (int h = -1; h > -LIFTERHEIGHT; h--) {
                BlockPos checkPos = new BlockPos(getBasePos().offset(blocks.get(i).getA())).offset(0, h, 0);
                BlockState checkedBlock = level().getBlockState(checkPos);

                //check block directly underneath
                if (h == -1) {
                    if (checkedBlock.getBlock() == BlockInit.conveyorBlock.get()) {

                        gravity = false;
                        conveyorQueue.add(checkedBlock.getValue(ConveyorBlock.FACING));

                    }

                }

                //Rotator Blocks
                if (checkedBlock.getBlock() == BlockInit.rotatorBlock.get()) {
                    if (canRotate()) {
                        state = State.ROTATING;
                        rotationClockwise = ((RotatorBlockEntity) level().getBlockEntity(checkPos)).directionClockwise;
                        return new Tuple<>(-1, null);
                    }
                }

                //Check lifter blocks
                if (checkedBlock.getBlock() == BlockInit.lifterBlock.get()) {
                    if (assertivePushCheck(blocks, new Tuple<>(LIFTERPRIORITY, Direction.UP))) {
                        return new Tuple<>(LIFTERPRIORITY, Direction.UP);
                    }
                }

                if (checkedBlock.getBlock() != Blocks.AIR) {
                    break;
                }
            }
        }

        //commmit to moving down if no blocks were found underneath
        if (gravity) {

            //TODO assertivePushCheck doublechecks blocks that were already checked above. Entity checking should be incoporated above to reduce computation
            if (assertivePushCheck(blocks, new Tuple<>(GRAVITYPRIORITY, Direction.DOWN))) {
                return new Tuple<>(GRAVITYPRIORITY, Direction.DOWN);
            }
        }
        //commit to influenced movement
        Queue<Direction> conveyorInfluence = processConveyorInfluence(conveyorQueue);

        while (!conveyorInfluence.isEmpty()) {
            Direction dir = conveyorInfluence.poll();
            if (assertivePushCheck(blocks, new Tuple<>(CONVEYORPRIORITY, dir))) {
                return new Tuple<>(CONVEYORPRIORITY, dir);
            }

        }


        //Dont Move
        return new Tuple<>(-1, null);
    }



    private Tuple<Integer,Direction> getMoveDirInitialScan2(List<Tuple<Vec3i,BlockState>> blocks) {


        int LIFTERHEIGHT = 5;
        Queue<Direction> conveyorQueue = new ArrayDeque<Direction>();
        //scan under
        boolean gravity = true;
        for (int i = 0; i < blocks.size(); i++) {

                BlockPos checkPos = new BlockPos(getBasePos().offset(blocks.get(i).getA())).offset(0, -1, 0);
                BlockState checkedBlock = level().getBlockState(checkPos);

                //check block directly underneath

                    if (checkedBlock.getBlock() == BlockInit.conveyorBlock.get()) {

                        gravity = false;
                        conveyorQueue.add(checkedBlock.getValue(ConveyorBlock.FACING));

                    }

        }

        //commmit to moving down if no blocks were found underneath

        if (gravity) {

            //TODO assertivePushCheck doublechecks blocks that were already checked above. Entity checking should be incoporated above to reduce computation
            if (assertivePushCheck(blocks, new Tuple<>(GRAVITYPRIORITY, Direction.DOWN))) {
                return new Tuple<>(GRAVITYPRIORITY, Direction.DOWN);
            }
        }

        //commit to influenced movement
        Queue<Direction> conveyorInfluence = processConveyorInfluence(conveyorQueue);

        while (!conveyorInfluence.isEmpty()) {
            Direction dir = conveyorInfluence.poll();
            if (assertivePushCheck(blocks, new Tuple<>(CONVEYORPRIORITY, dir))) {
                return new Tuple<>(CONVEYORPRIORITY, dir);
            }

        }


        //Dont Move
        return new Tuple<>(-1, null);
    }

    private boolean canRotate() {
        //TODO implement rotation collision check
        return true;
    }

    public boolean assertivePushCheck(List<Tuple<Vec3i, BlockState>> blocks, Tuple<Integer,Direction> push) {
        Tuple<Integer,Set<BaseEntity>> resulttuple = exploratoryPushCheck(blocks,push);
        if(resulttuple != null && resulttuple.getB() != null){

            for(BaseEntity e : resulttuple.getB()){
                e.strongestInfluence = push;
                e.pushProvidence = resulttuple.getB();



                if (e.state == State.IDLE) {
                    e.removeBlocks(e.getBlocks());
                }
                e.state = State.MOVING;
            }
            return true;
        }
        return false;
    }

    private Tuple<Integer,Set<BaseEntity>> exploratoryPushCheck(List<Tuple<Vec3i, BlockState>> blocks, Tuple<Integer,Direction> push) {
        Set<BaseEntity> potentialNewPushedEnts = new HashSet<>();

        Integer attemptedPriority = push.getA();
        Direction attemptedDir =  push.getB();

        for(Tuple<Vec3i,BlockState> t: blocks){
            Vec3i pos = t.getA();
            BlockPos blockCheck = new BlockPos(getBasePos().offset(pos)).relative(attemptedDir);

            AABB blockcheckAABB = new AABB(blockCheck);
            List<BaseEntity> ents = level().getEntitiesOfClass(BaseEntity.class, blockcheckAABB);
            for (BaseEntity e : ents) {
                if(canPushEntity(e,push)) {
                    if (!e.equals(this) && e.isBlock(blockCheck, e.getBlocks())) {
                        potentialNewPushedEnts.add(e);
                    }
                }else{
                    return new Tuple<>(-1,null);
                }
            }

            if(scanfirsttuple(blocks,pos.relative(push.getB())) == null) { //TODO very inefficient. if the checked block is also in the structure it shouldnt be in the block list to check in the first place

                if (ents.isEmpty() && level().getBlockState(blockCheck) != Blocks.AIR.defaultBlockState()) {
                    return new Tuple<>(-1,null);
                }
            }
        }

        //No blocks stopping. Attempt to push all downstreamblocks
        //TODO actually this cant be assertive as what if the entity is pushing two entities but one returns true and the next one returns false.
        //TODO actually it can be assertive as we can do the logic in here, but we have to save the list of entities as part of the state as the pushing will be
        //done in a seperate call. Unless we pass around the object across the whole tree and then assert the push at the root.

        boolean shouldPush = true;
        Set<BaseEntity> tempChildren = new HashSet<>();
        if(!potentialNewPushedEnts.isEmpty()) {
            for (BaseEntity e : potentialNewPushedEnts) {
                //Dont push down lifter stack
                //if(dir == Direction.DOWN && e.lifterIgnoreGravity){return null;}
                Set<BaseEntity> tempChildren1 = e.exploratoryPushCheck(e.getBlocks(),push).getB();
                if(tempChildren1 == null){
                    shouldPush = false;
                    break;
                }else{
                    tempChildren.addAll(tempChildren1);
                }

            }
        }else{
        }
        if(shouldPush){
            if(!tempChildren.isEmpty()){potentialNewPushedEnts.addAll(tempChildren);}
            potentialNewPushedEnts.add(this);
           return new Tuple<>(push.getA(),potentialNewPushedEnts);
        }

        return new Tuple<>(-1,null);
    }

    private boolean canPushEntity(BaseEntity e, Tuple<Integer, Direction> push) {
        return (push.getA() >= e.strongestInfluence.getA());
    }

    private Queue<Direction> processConveyorInfluence(Queue<Direction> conveyorQueue) {
        int ew_conveyor_influence = 0;
        int ns_conveyor_influence = 0;
        while(!conveyorQueue.isEmpty()){
            Direction dir = conveyorQueue.poll();
            switch (dir) {
                case NORTH -> ns_conveyor_influence++;
                case EAST -> ew_conveyor_influence++;
                case SOUTH -> ns_conveyor_influence--;
                case WEST -> ew_conveyor_influence--;
            }
        }

        Queue<Direction> infQueue = new ArrayDeque<>();

        if(ew_conveyor_influence >0){infQueue.add(Direction.EAST);}
        if(ew_conveyor_influence <0){infQueue.add(Direction.WEST);}

        if(ns_conveyor_influence >0){infQueue.add(Direction.NORTH);}
        if(ns_conveyor_influence <0){infQueue.add(Direction.SOUTH);}

        return infQueue;

    }


    private boolean getLifterInfluence(List<Tuple<Vec3i,BlockState>> blocks) {
        // rolled into initial conveyor check to not repeat work.
        int LIFTERHEIGHT = 5;

        for(int i=0;i<blocks.size();i++) {
           for(int h=-1;h>-LIFTERHEIGHT;h--){
               BlockPos liftPos = new BlockPos(getBasePos().offset(blocks.get(i).getA())).offset(0,h,0);
               if (level().getBlockState(liftPos).getBlock() == BlockInit.lifterBlock.get()) {
                  return true ;
               }

               if (level().getBlockState(liftPos).getBlock() != Blocks.AIR) {
                   break;
               }
           }
        }



        return false;
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
        //Cant use getBlockX as it is out of sync sometimes. Rounds absolute pos to catch this even if it is a tick off
        //return new Vec3i(getBlockX(),getBlockY(),getBlockZ());
        return new Vec3i((int) Math.round(position().x), (int) Math.round(position().y), (int) Math.round(position().z));
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

        internalInfluence = getInfluenceTuple(compound,"internal");
        externalInfluence = getInfluenceTuple(compound,"external");
        strongestInfluence = getInfluenceTuple(compound,"strongest");

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

        putInfluenceTuple(compound,internalInfluence,"internal");
        putInfluenceTuple(compound,externalInfluence,"external");
        putInfluenceTuple(compound,strongestInfluence,"strongest");

    }

    private void putInfluenceTuple(CompoundTag compound, Tuple<Integer, Direction> internalInfluence, String tagprefix) {
        compound.putInt(tagprefix+"priority",internalInfluence.getA());
        if(internalInfluence.getB() != null) {
            compound.putInt(tagprefix + "dir", internalInfluence.getB().get3DDataValue());
        }
        else{
            compound.putInt(tagprefix + "dir",-1);
        }
    }

    private Tuple<Integer, Direction> getInfluenceTuple(CompoundTag compound, String tagprefix) {
        Integer priority = compound.getInt(tagprefix+"priority");
        int dirint = compound.getInt(tagprefix+"dir");
        Direction dir = null;
        if(dirint != -1) {
            dir = Direction.from3DDataValue(compound.getInt(tagprefix + "dir"));
        }
        return new Tuple<Integer,Direction>(priority,dir);
    }

    @Override
    public @NotNull PushReaction getPistonPushReaction() {
        Minefinifactory.LOGGER.info("PISTON PUSH");
        strongestInfluence = new Tuple<>(-999,null);
        return PushReaction.BLOCK;
    }

    public void rotateBlocks() {
    }
}
