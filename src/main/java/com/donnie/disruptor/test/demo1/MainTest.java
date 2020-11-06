package com.donnie.disruptor.test.demo1;

import com.donnie.disruptor.data.PersonEvent;
import com.donnie.disruptor.domain.Person;
import com.donnie.disruptor.factory.PersonEventFactory;
import com.donnie.disruptor.handler.PersonEventHandler;
import com.lmax.disruptor.BatchEventProcessor;
import com.lmax.disruptor.RingBuffer;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class MainTest {
    private static void eventTranslatorWithTwoArg(PersonEvent event, long sequencem, int id, String name) {
        Person p = new Person(id, name);
        event.setPerson(p);
    }

    public static void main(String[] args) throws InterruptedException {
        //创建一个RingBuffer，注意容量是4。
        RingBuffer<PersonEvent> ringBuffer = RingBuffer.createSingleProducer(new PersonEventFactory(), 4);
        /** 创建一个事件处理器。
         * 注意参数：数据提供者就是RingBuffer、序列栅栏也来自RingBuffer
         * EventHandler使用自定义的。
         */
        BatchEventProcessor<PersonEvent> batchEventProcessor =
                new BatchEventProcessor<>(ringBuffer, ringBuffer.newBarrier(), new PersonEventHandler());

        BatchEventProcessor<PersonEvent> batchEventProcessor2 = new BatchEventProcessor<>(ringBuffer, ringBuffer.newBarrier(), (event, sequence, endOfBatch) -> {
            System.out.println("kick your ass " + sequence + " times!!!!");
        });

        //将事件处理器本身的序列设置为ringBuffer的追踪序列。
        ringBuffer.addGatingSequences(batchEventProcessor.getSequence());
//        ringBuffer.addGatingSequences(batchEventProcessor2.getSequence());
        //启动事件处理器。
        new Thread(batchEventProcessor).start();
//        new Thread(batchEventProcessor2).start();

        //往RingBuffer上发布事件
        for (int i = 0; i < 10; i++) {
            ringBuffer.publishEvent(MainTest::eventTranslatorWithTwoArg, i, i + "s");
            System.out.println("发布事件[" + i + "]");
            TimeUnit.SECONDS.sleep(3);
//            TimeUnit.MILLISECONDS.sleep(1);
        }

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}