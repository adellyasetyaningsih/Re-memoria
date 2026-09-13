package com.example.finalproject.models;

public class Tape {

    public String id;
    public String title;
    public String date;
    public int color;
    public String audioUrl;
    public long durationMs;

    public Tape() {}

    public Tape(String id, String title, String date, int color) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.color = color;
    }
}
