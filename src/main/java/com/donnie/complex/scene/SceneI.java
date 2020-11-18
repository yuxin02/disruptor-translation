package com.donnie.complex.scene;

import com.donnie.complex.common.*;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.concurrent.Executors;

/**
 * 场景一：单生产者单消费者
 * P--C
 * 说明：
 * (C1|C2) -- 表示消费者对于同一条消息m不重复消费
 * (C1&C2) -- 表示每个消费者都会对m进行消费，各个消费者之间不存在竞争
 */
public class SceneI {
    // 1048576
    private static final int BUFFER_SIZE = 1 << 10 << 10;

    public static void main(String[] args) throws InterruptedException {
        System.out.println(BUFFER_SIZE);
        Disruptor<OrderEvent> disruptor = new Disruptor<>(new OrderEventFactory(),
                BUFFER_SIZE,
                Executors.defaultThreadFactory(),
                ProducerType.SINGLE,
                new YieldingWaitStrategy());

        disruptor.handleEventsWith(new OrderEventHandler("C"));
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
