package com.fonsecakarsten.ahsl.LoopMail;

public class MailDetail {
    private String to;
    private String from;
    private String subject;
    private String date;
    private String content;

    public MailDetail() {
        this.to = "";
        this.from = "";
        this.subject = "";
        this.date = "";
        this.content = "";
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTo() {
        return to;
    }

    public String getFrom() {
        return from;
    }

    public String getSubject() {
        return subject;
    }

    public String getDate() {
        return date;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
