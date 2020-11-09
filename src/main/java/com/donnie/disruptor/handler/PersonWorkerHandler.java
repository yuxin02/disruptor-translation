package com.donnie.disruptor.handler;

import com.donnie.disruptor.data.PersonEvent;
import com.lmax.disruptor.WorkHandler;

public class PersonWorkerHandler implements WorkHandler<PersonEvent> {
    private String workName;

    public PersonWorkerHandler(String name) {
        this.workName = name;
    }

    @Override
    public void onEvent(PersonEvent event) throws Exception {
        System.out.println("WorkHandler[" + workName + "]处理事件" + event.getPerson());
    }
}
