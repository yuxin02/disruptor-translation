package com.donnie.disruptor.test.demo2;

import com.donnie.disruptor.data.PersonEvent;
import com.donnie.disruptor.domain.Person;
import com.donnie.disruptor.factory.PersonEventFactory;
import com.donnie.disruptor.handler.PersonWorkerHandler;
import com.lmax.disruptor.IgnoreExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WorkerPool;
import com.lmax.disruptor.dsl.BasicExecutor;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainTest {
    private static void eventTranslatorWithTwoArg(PersonEvent event, long sequencem, int id, String name) {
        Person p = new Person(id, name);
        event.setPerson(p);
    }

    /**
     * 说明一下WorkProcessor的主逻辑中几个重点：
     * 1.首先，由于是Work模式，必然是多个事件处理者(WorkProcessor)处理同一批事件，那么肯定会存在多个处理者对同一个要处理事件的竞争，
     * 所以出现了一个workSequence，所有的处理者都使用这一个workSequence，大家通过对workSequence的原子操作来保证不会处理相同的事件。
     * 2.其次，多个事件处理者和事件发布者之间也需要协调，需要等待事件发布者发布完事件之后才能对其进行处理，
     * 这里还是使用序列栅栏来协调(sequenceBarrier.waitFor)。
     */
    public static void main(String[] args) {
        //创建一个RingBuffer，注意容量是4。
        RingBuffer<PersonEvent> ringBuffer = RingBuffer.createSingleProducer(new PersonEventFactory(), 4);

        PersonWorkerHandler worker1 = new PersonWorkerHandler("worker-1");
        PersonWorkerHandler worker2 = new PersonWorkerHandler("worker-2");
        PersonWorkerHandler worker3 = new PersonWorkerHandler("worker-3");

        WorkerPool<PersonEvent> workerPool = new WorkerPool<>(ringBuffer, ringBuffer.newBarrier(),
                new IgnoreExceptionHandler(), worker1, worker2, worker3);

        //将WorkPool的工作序列集设置为ringBuffer的追踪序列。
        ringBuffer.addGatingSequences(workerPool.getWorkerSequences());
        //创建一个线程池用于执行Workhandler。
        Executor executor = new BasicExecutor(Executors.defaultThreadFactory());
        //启动WorkPool。
        workerPool.start(executor);

        //往RingBuffer上发布事件
        for (int i = 0; i < 10; i++) {
            ringBuffer.publishEvent(MainTest::eventTranslatorWithTwoArg, i, i + "s");
            System.out.println("发布事件[" + i + "]");
        }
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

