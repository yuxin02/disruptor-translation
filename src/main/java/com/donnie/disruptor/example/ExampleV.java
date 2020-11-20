package com.donnie.disruptor.example;

import com.donnie.disruptor.data.PersonEvent;
import com.donnie.disruptor.domain.Person;
import com.donnie.disruptor.factory.PersonEventFactory;
import com.donnie.disruptor.handler.PersonEventHandler;
import com.donnie.disruptor.handler.PersonWorkerHandler;
import com.donnie.disruptor.translator.PersonEventTranslatorII;
import com.lmax.disruptor.BatchEventProcessor;
import com.lmax.disruptor.IgnoreExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WorkerPool;
import com.lmax.disruptor.dsl.BasicExecutor;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 单生产者多消费者模式（独立模式，两个消费者之间没有关系，独立消费RingBuffer的数据）
 * @author chenweibing
 */
public class ExampleV {

    public static void main(String[] args) throws InterruptedException {
        /**
         * 创建一个RingBuffer，注意容量是4。
         */
        RingBuffer<PersonEvent> ringBuffer = RingBuffer.createSingleProducer(new PersonEventFactory(), 4);

        PersonWorkerHandler worker1 = new PersonWorkerHandler("worker-1");
        PersonWorkerHandler worker2 = new PersonWorkerHandler("worker-2");
        PersonWorkerHandler worker3 = new PersonWorkerHandler("worker-3");

        WorkerPool<PersonEvent> workerPool = new WorkerPool<>(ringBuffer, ringBuffer.newBarrier(),
                new IgnoreExceptionHandler(), worker1, worker2, worker3);

        //将WorkPool的工作序列集设置为ringBuffer的追踪序列
        ringBuffer.addGatingSequences(workerPool.getWorkerSequences());
        //创建一个线程池用于执行Workhandler。
        Executor executor = new BasicExecutor(Executors.defaultThreadFactory());
        //启动WorkPool。
        workerPool.start(executor);

//        workerPool.start(executor);

        TimeUnit.SECONDS.sleep(50);
        //往RingBuffer上发布事件
        for (int i = 1; i <= 10; i++) {
            System.out.println("发布事件[" + i + "]");
            ringBuffer.publishEvent(PersonEventTranslatorII::eventTranslatorWithTwoArg, i,  "No#"+i);
            TimeUnit.SECONDS.sleep(5);
        }

        //为了保证消费者线程已经启动，留足足够的时间
        try {
            TimeUnit.SECONDS.sleep(10);
            workerPool.halt();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
