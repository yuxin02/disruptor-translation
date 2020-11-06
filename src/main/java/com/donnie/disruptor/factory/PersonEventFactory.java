package com.donnie.disruptor.factory;

import com.donnie.disruptor.data.PersonEvent;
import com.lmax.disruptor.EventFactory;

public class PersonEventFactory implements EventFactory<PersonEvent> {
    @Override
    public PersonEvent newInstance() {
        return new PersonEvent();
    }
}
