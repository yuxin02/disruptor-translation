package com.donnie.disruptor.example;

import com.donnie.disruptor.data.PersonEvent;
import com.donnie.disruptor.domain.Person;
import com.donnie.disruptor.factory.PersonEventFactory;
import com.donnie.disruptor.handler.PersonEventHandler;
import com.donnie.disruptor.translator.PersonEventTranslator;
import com.donnie.disruptor.translator.PersonEventTranslatorII;
import com.lmax.disruptor.BatchEventProcessor;
import com.lmax.disruptor.RingBuffer;

import java.util.concurrent.TimeUnit;

/**
 * 单生产者单消费者模式
 * 这里展示一下直接使用ringBuffer的publishEvent方法系列发布事件
 * @author chenweibing
 */
public class ExampleIV {

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
        /**
         * 将事件处理器本身的序列设置为ringBuffer的追踪序列。
         */
        ringBuffer.addGatingSequences(batchEventProcessor.getSequence());
        /**
         * 启动事件处理器。
         */
        new Thread(batchEventProcessor).start();

        // 发布事件
        // 方式一
        ringBuffer.publishEvent(new PersonEventTranslatorII(0, "No#0"));
        // 方式二
        ringBuffer.publishEvent(PersonEventTranslatorII::eventTranslatorWithTwoArg, 1, "No#1");


        //为了保证消费者线程已经启动，留足足够的时间
        try {
            TimeUnit.SECONDS.sleep(10);
            batchEventProcessor.halt();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
