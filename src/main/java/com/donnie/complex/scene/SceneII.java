package com.donnie.complex.scene;

import com.donnie.complex.common.*;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.concurrent.Executors;

/**
 * 场景二：单生产者多消费者，多消费者间形成依赖关系，每个依赖节点只有一个消费者。
 * <p>
 * P-C1--(C2&C3)--C4
 * </p>
 */
public class SceneII {
    // 1024
    public static final int BUFFER_SIZE = 1 << 10 << 10;

    public static void main(String[] args) throws InterruptedException {
        Disruptor<OrderEvent> disruptor =
                new Disruptor<>(new OrderEventFactory(), BUFFER_SIZE, Executors.defaultThreadFactory(), ProducerType.SINGLE, new YieldingWaitStrategy());

        // 消费者C2、C3只有在C1消费完消息m后，才能消费m。消费者C4只有在C2、C3消费完m后，才能消费该消息。
        disruptor.handleEventsWith(new OrderEventHandler("C1"))
                .then(new OrderEventHandler("C2"), new OrderEventHandler("C3"))
                .then(new OrderEventHandler("C4"));
        disruptor.start();

//        OrderEventProducer producer = new OrderEventProducer(disruptor.getRingBuffer());
//
////        for (int i = 0; i < 3; i++) {
////            producer.onData(new Order(i));
////        }

        // 这里演示一下直接用disruptor发布事件
        for (int i = 0; i < 3; i++) {
            disruptor.publishEvent(new OrderEventTranslator(), new Order(i));
        }
        //为了保证消费者线程已经启动，留足足够的时间
        Thread.sleep(1000);
        disruptor.shutdown();

    }
}
