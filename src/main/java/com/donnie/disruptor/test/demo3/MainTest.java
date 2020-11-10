package com.donnie.disruptor.test.demo3;

import com.donnie.disruptor.data.PersonEvent;
import com.donnie.disruptor.factory.PersonEventFactory;
import com.donnie.disruptor.translator.PersonEventTranslator;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.dsl.Disruptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainTest {
    private static final Logger logger = LoggerFactory.getLogger(MainTest.class);

    public static void main(String[] args) {
        logger.info("Start testing ...");
        Disruptor<PersonEvent> disruptor = new Disruptor<>(new PersonEventFactory(), 4, Executors.defaultThreadFactory());

        EventHandler<PersonEvent> handler1 = (event, sequence, endOfBatch) -> {
            System.out.println("handle event's data:" + event.getPerson() + "isEndOfBatch:" + endOfBatch);
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        EventHandler<PersonEvent> handler2 = (event, sequence, endOfBatch) -> {
            System.out.println("kick your ass " + sequence + " times!!!!");
        };

        disruptor.handleEventsWith(handler1).then(handler2);
        //启动Disruptor。
        disruptor.start();
        //发布10个事件。
        for (int i = 0; i < 10; i++) {
            disruptor.publishEvent(PersonEventTranslator::eventTranslatorWithTwoArg, i, i + "s");
            System.out.println("发布事件[" + i + "]");
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
