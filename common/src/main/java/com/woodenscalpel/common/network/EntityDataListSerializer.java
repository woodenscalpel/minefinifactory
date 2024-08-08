package com.woodenscalpel.common.network;

import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class EntityDataListSerializer {
    //public static final EntityDataSerializer<List> LIST = EntityDataSerializer.simple(FriendlyByteBuf::writeCollection, FriendlyByteBuf::readCollection);

    public static final EntityDataSerializer<List<Tuple<Vec3i, BlockState>>> BLOCKTUPLE;

    static {
        BLOCKTUPLE = new EntityDataSerializer.ForValueType<List<Tuple<Vec3i, BlockState>>>() {
            @Override
            public void write(FriendlyByteBuf buffer, List<Tuple<Vec3i, BlockState>> values) {

                buffer.writeInt(values.size());

                for(Tuple<Vec3i,BlockState> t : values){
                    Vec3i v = t.getA();
                    BlockState b = t.getB();
                    buffer.writeInt(v.getX());
                    buffer.writeInt(v.getY());
                    buffer.writeInt(v.getZ());

                    buffer.writeId(Block.BLOCK_STATE_REGISTRY,b);

                }

            }

            @Override
            public List<Tuple<Vec3i, BlockState>> read(FriendlyByteBuf buffer) {

                List<Tuple<Vec3i,BlockState>> blocks = new ArrayList<>();

                int length = buffer.readInt();
                for(int i = 0; i < length; i ++){
                    Vec3i v = new Vec3i(buffer.readInt(),buffer.readInt(),buffer.readInt());
                    BlockState b = buffer.readById(Block.BLOCK_STATE_REGISTRY);
                    Tuple<Vec3i,BlockState> t = new Tuple<>(v,b);
                    blocks.add(t);
                }
                return blocks;
            }
        };
    }

}
