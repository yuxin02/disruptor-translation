package com.donnie.disruptor.example;

import com.donnie.disruptor.data.PersonEvent;
import com.donnie.disruptor.domain.Person;
import com.donnie.disruptor.factory.PersonEventFactory;
import com.donnie.disruptor.handler.PersonEventHandler;
import com.lmax.disruptor.BatchEventProcessor;
import com.lmax.disruptor.RingBuffer;

import java.util.concurrent.TimeUnit;

/**
 * 单生产者多消费者模式（独立模式，两个消费者之间没有关系，独立消费RingBuffer的数据）
 * @author chenweibing
 */
public class ExampleIII {

    public static void main(String[] args) {
        /**
         * 创建一个RingBuffer，注意容量是4。
         */
        RingBuffer<PersonEvent> ringBuffer = RingBuffer.createSingleProducer(new PersonEventFactory(), 4);
        /**
         * 创建一个事件处理器。
         * 注意参数：数据提供者就是RingBuffer、序列栅栏也来自RingBuffer
         */
        BatchEventProcessor<PersonEvent> batchEventProcessor =
                new BatchEventProcessor<>(ringBuffer, ringBuffer.newBarrier(), new PersonEventHandler());

        BatchEventProcessor<PersonEvent> batchEventProcessor2 =
                new BatchEventProcessor<>(ringBuffer, ringBuffer.newBarrier(), (event, sequence, endOfBatch)->{
                    System.out.println("Another processor, events' data: " + event.getPerson() + ", isEndOfBatch:" + endOfBatch);
                });
        /**
         * 将事件处理器本身的序列设置为ringBuffer的追踪序列。
         */
        ringBuffer.addGatingSequences(batchEventProcessor.getSequence());
        ringBuffer.addGatingSequences(batchEventProcessor2.getSequence());
        /**
         * 启动事件处理器。
         */
        new Thread(batchEventProcessor).start();
        new Thread(batchEventProcessor2).start();


        for (int i = 1; i < 8; i++) {
            long sequence = ringBuffer.next();
            try {
                PersonEvent personEvent = ringBuffer.get(sequence);
                Person p = new Person(i, "No#" + i);
                personEvent.setPerson(p);
                System.out.println("Publish event#" + i);
            } finally {
                ringBuffer.publish(sequence);
            }
        }

        //为了保证消费者线程已经启动，留足足够的时间
        try {
            TimeUnit.SECONDS.sleep(10);
            batchEventProcessor.halt();
            batchEventProcessor2.halt();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
