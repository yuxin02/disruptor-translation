package com.donnie.complex.scene;

import com.donnie.complex.common.*;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

/**
 * 场景九：多生产者，单消费者模式
 * (P1,P2,P3)--C1
 */
public class SceneIX {
    // 1048576
    private static final int BUFFER_SIZE = 1 << 10 << 10;

    public static void main(String[] args) throws InterruptedException {
        Disruptor<OrderEvent> disruptor = new Disruptor<>(new OrderEventFactory(), BUFFER_SIZE, Executors.defaultThreadFactory(),
                        ProducerType.MULTI,  // 注意这里一定要使用MULTI，否则会出现消息覆盖的情况
                        new YieldingWaitStrategy());

        disruptor.handleEventsWith(new OrderEventHandler("C1"));
        disruptor.start();

        RingBuffer<OrderEvent> ringBuffer = disruptor.getRingBuffer();
        //判断生产者是否已经生产完毕
        final CountDownLatch countDownLatch = new CountDownLatch(3);
        for (int i = 0; i < 3; i++) {
            Thread thread = new Thread() {
                @Override
                public void run() {
                    for (int j = 0; j < 3; j++) {
                        new OrderEventProducer(ringBuffer).onData(new Order(j).setName(Thread.currentThread().getName() + "'s " + j + "th message"));
                    }
                    countDownLatch.countDown();
                }
            };
            thread.setName("producer thread " + i);
            thread.start();
        }
        countDownLatch.await();
        //为了保证消费者线程已经启动，留足足够的时间
        Thread.sleep(1000);
        disruptor.shutdown();

    }
}
