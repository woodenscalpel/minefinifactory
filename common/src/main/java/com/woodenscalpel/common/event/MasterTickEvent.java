package com.woodenscalpel.common.event;

import dev.architectury.event.EventResult;

public class MasterTickEvent {


    public EventResult maintick(){
        return EventResult.pass();
    }
}
