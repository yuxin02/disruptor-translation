package com.donnie.complex.common;

public class Order {
    private int id;

    public Order setName(String name) {
        this.name = name;
        return this;
    }

    private String name;

    public Order(int id) {
        this.id = id;

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Order{");
        sb.append("id=").append(id);
        sb.append(", name='").append(name).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
