package com.donnie.complex.common;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.WorkHandler;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class OrderEventHandler implements EventHandler<OrderEvent>, WorkHandler<OrderEvent> {
    private String name;
    private static AtomicInteger count = new AtomicInteger(0);
    public OrderEventHandler(String name) {
        this.name = name;
    }
    public static int getCount(){
        return count.get();
    }
    @Override
    public void onEvent(OrderEvent event, long sequence, boolean endOfBatch) throws Exception {
        System.out.println("OrderHandler--" + this.name + "，消费信息：" + event.getOrder());
        TimeUnit.MILLISECONDS.sleep(1000);
        count.incrementAndGet();
    }

    @Override
    public void onEvent(OrderEvent event) throws Exception {
        System.out.println("OrderHandler--" + this.name + "，消费信息：" + event.getOrder());
        count.incrementAndGet();
    }
}
