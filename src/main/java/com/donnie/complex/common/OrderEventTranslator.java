package com.donnie.complex.common;

import com.lmax.disruptor.EventTranslatorOneArg;

public class OrderEventTranslator  implements EventTranslatorOneArg<OrderEvent, Order>{
    @Override
    public void translateTo(OrderEvent event, long sequence, Order order) {
        event.setOrder(order);
    }
}
