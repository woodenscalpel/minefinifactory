package com.woodenscalpel.common.misc;

import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;

public class Helpers {

    public static Vec3 Vec3itof(Vec3i vec3i){
        return new Vec3(vec3i.getX(),vec3i.getY(),vec3i.getZ());
    }
}
