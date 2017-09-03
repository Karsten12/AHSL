package com.fonsecakarsten.ahsl.News;

import java.io.Serializable;

public class NewsArticle implements Serializable {
    private String articleName;
    private String author;
    private String datePosted;
    private String authorType;
    private String articleUrl;
    private String displayAuthor;

    public NewsArticle() {
        this.articleName = "";
        this.author = "";
        this.authorType = "";
        this.datePosted = "";
        this.articleUrl = "";
        this.displayAuthor = null;
    }

    public String getArticleName() {
        return articleName;
    }

    public void setArticleName(String articleName) {
        this.articleName = articleName;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDatePosted() {
        return datePosted;
    }

    public void setDatePosted(String datePosted) {
        this.datePosted = datePosted;
    }

    public String getAuthorType() {
        return authorType;
    }

    public void setAuthorType(String authorType) {
        this.authorType = authorType;
    }

    public String getArticleUrl() {
        return articleUrl;
    }

    public void setArticleUrl(String articleUrl) {
        this.articleUrl = articleUrl;
    }

    public void setDisplayAuthor(String displayAuthor) {
        this.displayAuthor = displayAuthor;
    }

    public String getDisplayAuthor() {
        return displayAuthor;
    }
}
