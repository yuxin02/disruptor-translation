package com.donnie.disruptor.translator;

import com.donnie.disruptor.data.PersonEvent;
import com.donnie.disruptor.domain.Person;
import com.lmax.disruptor.EventTranslator;

public class PersonEventTranslatorII implements EventTranslator<PersonEvent> {
    private int id;
    private String name;

    public PersonEventTranslatorII(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public void translateTo(PersonEvent event, long sequence) {
        Person p = new Person(id, name);
        event.setPerson(p);
    }

    public static void eventTranslatorWithTwoArg(PersonEvent event, long sequencem, int id, String name) {
        Person p = new Person(id, name);
        event.setPerson(p);
    }
}
