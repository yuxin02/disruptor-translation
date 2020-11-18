package com.donnie.complex.scene;

import com.donnie.complex.common.*;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.concurrent.Executors;

/**
 * 场景八：单生产者，多消费者模式。消费者1、2独立消费每一条消息，
 * 消费者3、4不重复消费消费者1、2均处理过的消息，
 * 消费者5消费消费者3或4消费过的消息
 * P--(C1&C2)--(C3|C4)--C5
 */
public class SceneVIII {
    // 1048576
    private static final int BUFFER_SIZE = 1 << 10 << 10;

    public static void main(String[] args) throws InterruptedException {
        Disruptor<OrderEvent> disruptor =
                new Disruptor<>(new OrderEventFactory(), BUFFER_SIZE, Executors.defaultThreadFactory(), ProducerType.SINGLE, new YieldingWaitStrategy());

        disruptor.handleEventsWith(new OrderEventHandler("C1"), new OrderEventHandler("C2"))
                .handleEventsWithWorkerPool(new OrderEventHandler("C3"), new OrderEventHandler("C4"))
                .then(new OrderEventHandler("C5"));
        disruptor.start();

        OrderEventProducer producer = new OrderEventProducer(disruptor.getRingBuffer());

        for (int i = 0; i < 3; i++) {
            producer.onData(new Order(i));
        }
        //为了保证消费者线程已经启动，留足足够的时间
        Thread.sleep(1000);
        disruptor.shutdown();

    }
}
