package com.donnie.disruptor.handler;

import com.donnie.disruptor.data.PersonEvent;
import com.lmax.disruptor.EventHandler;

import java.util.concurrent.TimeUnit;

public class PersonEventHandler implements EventHandler<PersonEvent> {
    @Override
    public void onEvent(PersonEvent event, long sequence, boolean endOfBatch) throws Exception {
        // 注意这里小睡眠了一下!!
//        TimeUnit.SECONDS.sleep(3);
        System.out.println("handle event's data:" + event.getPerson() + "isEndOfBatch:" + endOfBatch);
    }
}
