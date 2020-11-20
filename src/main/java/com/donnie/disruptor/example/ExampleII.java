package com.donnie.disruptor.example;

import com.donnie.disruptor.data.PersonEvent;
import com.donnie.disruptor.domain.Person;
import com.donnie.disruptor.factory.PersonEventFactory;
import com.donnie.disruptor.handler.PersonEventHandler;
import com.lmax.disruptor.BatchEventProcessor;
import com.lmax.disruptor.RingBuffer;

import java.util.concurrent.TimeUnit;

/**
 *  多生产者但消费之模式
 */
public class ExampleII {

    public static void main(String[] args) {
        /**
         * 创建一个RingBuffer，注意容量是4。
         */
        RingBuffer<PersonEvent> ringBuffer = RingBuffer.createMultiProducer(new PersonEventFactory(), 4);
//        RingBuffer<PersonEvent> ringBuffer = RingBuffer.createSingleProducer(new PersonEventFactory(), 4);
        /**
         * 创建一个事件处理器。
         * 注意参数：数据提供者就是RingBuffer、序列栅栏也来自RingBuffer
         */
        BatchEventProcessor<PersonEvent> batchEventProcessor =
                new BatchEventProcessor<>(ringBuffer, ringBuffer.newBarrier(), new PersonEventHandler());
        /**
         * 将事件处理器本身的序列设置为ringBuffer的追踪序列。
         */
        ringBuffer.addGatingSequences(batchEventProcessor.getSequence());
        /**
         * 启动事件处理器。
         */
        new Thread(batchEventProcessor).start();


        for (int i = 1; i <= 5; i++) {
            new Thread(() -> {
                for (int j = 1; j <= 4; j++) {
                    long sequence = ringBuffer.next();
                    try {
                        PersonEvent personEvent = ringBuffer.get(sequence);
                        Person p = new Person(j, "No#" + Thread.currentThread().getName() + "*" + j);
                        personEvent.setPerson(p);
                        System.out.println(Thread.currentThread().getName() + ">>Publish event#" + j);
                    } finally {
                        ringBuffer.publish(sequence);
                    }
                }
            }, "" + i).start();
        }

        //为了保证消费者线程已经启动，留足足够的时间
        try {
            TimeUnit.SECONDS.sleep(25);
            batchEventProcessor.halt();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
