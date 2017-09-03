package com.fonsecakarsten.ahsl.News;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NewsDetail implements Serializable {
    private String title;
    private final List<String> details;
    private String content;

    public NewsDetail() {
        this.title = "";
        this.details = new ArrayList<>();
        this.content = "";
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getDetails() {
        return details;
    }

    public void addDetail(String detail) {
        this.details.add(detail);
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

