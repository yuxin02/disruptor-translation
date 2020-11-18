package com.donnie.complex.scene;

import com.donnie.complex.common.*;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.concurrent.Executors;

/**
 * 场景三：单生产者多消费者，多消费者对于消息m独立消费。
 * P--(C1&C2)
 * 注：对比一下和场景二的区别
 */
public class SceneIII {
    // 1048576
    private static final int BUFFER_SIZE = 1 << 10 << 10;

    public static void main(String[] args) throws InterruptedException {
        Disruptor<OrderEvent> disruptor =
                new Disruptor<>(new OrderEventFactory(), BUFFER_SIZE, Executors.defaultThreadFactory(), ProducerType.SINGLE, new YieldingWaitStrategy());

        disruptor.handleEventsWith(new OrderEventHandler("C1"), new OrderEventHandler("C2"));
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
