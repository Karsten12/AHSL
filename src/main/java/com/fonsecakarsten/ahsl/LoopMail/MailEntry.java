package com.fonsecakarsten.ahsl.LoopMail;


public class MailEntry {
    private String timestamp;
    private String involvedParties;
    private String subject;
    private String contentUrl;

    public MailEntry() {
        this.timestamp = "";
        this.involvedParties = "";
        this.subject = "";
        this.contentUrl = "";
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getInvolvedParties() {
        return involvedParties;
    }

    public void setInvolvedParties(String involvedParties) {
        this.involvedParties = involvedParties;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContentUrl() {
        return contentUrl;
    }

    public void setContentUrl(String contentUrl) {
        this.contentUrl = contentUrl;
    }
}
