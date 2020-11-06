package com.donnie.disruptor.handler;

import com.donnie.disruptor.data.PersonEvent;
import com.lmax.disruptor.EventHandler;

public class PersonEventHandler implements EventHandler<PersonEvent> {
    @Override
    public void onEvent(PersonEvent event, long sequence, boolean endOfBatch) throws Exception {
        System.out.println("handle event's data:" + event.getPerson() + "isEndOfBatch:" + endOfBatch);
//        注意这里小睡眠了一下!!
//        TimeUnit.SECONDS.sleep(3);
    }
}
