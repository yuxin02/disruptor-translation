package com.donnie.disruptor.handler;

import com.donnie.disruptor.data.PersonEvent;
import com.lmax.disruptor.EventHandler;

import java.util.concurrent.TimeUnit;

public class PersonEventHandler implements EventHandler<PersonEvent> {
    @Override
    public void onEvent(PersonEvent event, long sequence, boolean endOfBatch) throws Exception {
        System.out.println("handle event's data:" + event.getPerson() + ", isEndOfBatch:" + endOfBatch);
        TimeUnit.SECONDS.sleep(1);
    }
}
