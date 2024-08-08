package com.woodenscalpel.common.blocks.multiblockutil;

public interface IMultiblockEntity {
    /***
     Interface for multiblock block entities that helps take care of multiblock formation and destruction
     ***/

    //Bridges the break and place events to these functions in the block entity
    public void onPlace();
    public void onBreak();

}
