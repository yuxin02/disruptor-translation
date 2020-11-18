package com.donnie.disruptor.test.demo0;

import com.donnie.disruptor.data.PersonEvent;
import com.donnie.disruptor.domain.Person;
import com.donnie.disruptor.factory.PersonEventFactory;
import com.donnie.disruptor.translator.PersonEventTranslator;
import com.lmax.disruptor.RingBuffer;

import java.util.concurrent.CountDownLatch;

public class MainTest {

    private static RingBuffer<PersonEvent> ringBuffer;

    private static PersonEvent eventFactory() {
        return new PersonEvent();
    }

    private static void eventTranslator(PersonEvent event, long sequence) {
        Person p = new Person(1, "Zhang San");
        event.setPerson(p);
    }

    private static void eventTranslatorWithTwoArg(PersonEvent event, long sequencem, int id, String name) {
        Person p = new Person(id, name);
        event.setPerson(p);
    }

    public static void main(String[] args) {
//        exampleWithTranslator(5);
//        exampleWithTranslatorAndArg();
//        exampleOfRingBufferPublishDirectly();


        exampleWithMultiProducer();

        System.out.println("Remaining capacity: " + ringBuffer.remainingCapacity());
        PersonEvent personEvent = ringBuffer.get(0);
        System.out.println("Event: " + personEvent);
        System.out.println("Data: " + personEvent.getPerson());


        System.out.println();
        System.out.println("Remaining capacity: " + ringBuffer.remainingCapacity());
        personEvent = ringBuffer.get(1);
        System.out.println("Event: " + personEvent);
        System.out.println("Data: " + personEvent.getPerson());
    }


    private static void exampleWithTranslator(int counter) {
        // Using new create object
        ringBuffer = RingBuffer.createSingleProducer(new PersonEventFactory(), 4);
        for (int i = 0; i < counter; i++) {
            ringBuffer.publishEvent(new PersonEventTranslator(i));
        }
    }

    private static void exampleWithTranslatorAndArg() {
        // Using method reference
        ringBuffer = RingBuffer.createSingleProducer(MainTest::eventFactory, 4);
        ringBuffer.publishEvent(MainTest::eventTranslatorWithTwoArg, 1, "Wang Ergou");
    }


    private static void exampleOfRingBufferPublishDirectly() {
        // Using method reference
        ringBuffer = RingBuffer.createSingleProducer(MainTest::eventFactory, 4);

        long sequence = ringBuffer.next();
        try {
            PersonEvent personEvent = ringBuffer.get(sequence);
            Person p = new Person(3, "Li Si");
            personEvent.setPerson(p);
        } finally {
            // TODO: 为什么要放到finally中？
            ringBuffer.publish(sequence);
        }
        System.out.println("RingBuffer: " + ringBuffer);

    }

    private static void exampleWithMultiProducer() {
        ringBuffer = RingBuffer.createMultiProducer(MainTest::eventFactory, 1<<10);
        // TODO: 这里换成SingleProducer会出错，single里面没有同步机制，multi里通过竞争cursor达到同步
//        ringBuffer = RingBuffer.createSingleProducer(MainTest::eventFactory, 1<<3);

        int size = 1024;
        CountDownLatch countDownLatch = new CountDownLatch(size);

        for (int i = 0; i < size; i++) {
            final int index = i;
            new Thread(() -> {
                long seq = ringBuffer.next();
                try {
                    PersonEvent personEvent = ringBuffer.get(seq);
                    personEvent.setPerson(new Person(index, index + "s"));

                } finally {
                    ringBuffer.publish(seq);
                    countDownLatch.countDown();
                }
            }).start();
        }

        try {
            countDownLatch.await();
            for (int i = 0; i < size; i++) {
                PersonEvent personEvent = ringBuffer.get(i);

                System.out.println(personEvent.getPerson());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {

        }
    }
}
