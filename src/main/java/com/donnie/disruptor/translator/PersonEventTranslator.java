package com.donnie.disruptor.translator;

import com.donnie.disruptor.data.PersonEvent;
import com.donnie.disruptor.domain.Person;
import com.lmax.disruptor.EventTranslator;

public class PersonEventTranslator implements EventTranslator<PersonEvent> {
    private int id;

    public PersonEventTranslator(int id) {
        this.id = id;
    }

    @Override
    public void translateTo(PersonEvent event, long sequence) {
        Person p = new Person(id, "Zhang San" + " -- " + id);
        event.setPerson(p);
    }

    public static void eventTranslatorWithTwoArg(PersonEvent event, long sequencem, int id, String name) {
        Person p = new Person(id, name);
        event.setPerson(p);
    }
}
