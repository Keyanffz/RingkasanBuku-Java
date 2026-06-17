package com.ringkasanbuku.model;

public class Chapter {
    private int index;
    private String title;
    private String content;

    public Chapter(int index, String title, String content) {
        this.index = index;
        this.title = title;
        this.content = content;
    }

    public int getIndex() { return index; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public void setTitle(String title) { this.title = title; }
}
