package com.donnie.complex.scene;

import com.donnie.complex.common.*;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.concurrent.Executors;

/**
 * 场景三：单生产者，多消费者模式。多消费者对于消息不重复消费。
 * <p>
 * P--(C1|C2)
 * </p>
 */
public class SceneIII {
    // 1024
    public static final int BUFFER_SIZE = 1 << 10 << 10;

    public static void main(String[] args) throws InterruptedException {
        Disruptor<OrderEvent> disruptor =
                new Disruptor<>(new OrderEventFactory(), BUFFER_SIZE, Executors.defaultThreadFactory(), ProducerType.SINGLE, new YieldingWaitStrategy());
        /**
         * 该方法传入的消费者需要实现WorkHandler接口，方法的内部实现是：先创建WorkPool，然后封装WorkPool为EventHandlerPool返回。
         * 消费者1、2对于消息的消费有时有竞争，保证同一消息只能有一个消费者消费
         */
        disruptor.handleEventsWithWorkerPool(new OrderEventHandler("C1"), new OrderEventHandler("C2"));
        disruptor.start();

        OrderEventProducer producer = new OrderEventProducer(disruptor.getRingBuffer());

        for (int i = 0; i < 10; i++) {
            producer.onData(new Order(i));
        }
        //为了保证消费者线程已经启动，留足足够的时间
        Thread.sleep(1000);
        disruptor.shutdown();

    }
}
