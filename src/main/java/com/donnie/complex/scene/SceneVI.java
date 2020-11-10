package com.donnie.complex.scene;

import com.donnie.complex.common.*;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.concurrent.Executors;

/**
 * 场景六：单生产者，多消费者。多消费者之间不重复消费，且不同的消费者WorkPool之间存在依赖关系。
 * P--(C1|C2)--(C3|C4)--C5
 */
public class SceneVI {
    // 1024
    public static final int BUFFER_SIZE = 1 << 10 << 10;

    public static void main(String[] args) throws InterruptedException {
        Disruptor<OrderEvent> disruptor =
                new Disruptor<>(new OrderEventFactory(), BUFFER_SIZE, Executors.defaultThreadFactory(), ProducerType.SINGLE, new YieldingWaitStrategy());

        disruptor.handleEventsWithWorkerPool(new OrderEventHandler("C1"), new OrderEventHandler("C2"))
                .thenHandleEventsWithWorkerPool(new OrderEventHandler("C3"), new OrderEventHandler("C4"))
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
