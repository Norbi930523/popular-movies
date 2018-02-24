package com.udacity.popularmovies.model;

/**
 * Created by Norbert Boros on 2018.02.24..
 */

public class MovieReview {

    private String id;

    private String author;

    private String content;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
