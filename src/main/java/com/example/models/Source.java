package com.example.models;

public class Source {
    private int id;
    private String name;
    private Status status;

    public Source(int id, String name, Status status) {
        this.id = id;
        this.name = name;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Status getStatus() {
        return status;
    }
}
