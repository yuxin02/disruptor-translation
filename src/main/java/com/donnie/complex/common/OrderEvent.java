package com.donnie.complex.common;

public class OrderEvent {

    private Order order;

    public OrderEvent() {
    }

    public OrderEvent(Order order) {
        this.order = order;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }
}
