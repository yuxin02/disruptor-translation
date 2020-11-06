package com.donnie.disruptor.data;

import com.donnie.disruptor.domain.Person;

public class PersonEvent {
    private Person person;

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;

    }
}
