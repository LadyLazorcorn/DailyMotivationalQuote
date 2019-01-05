package com.lazorcorn.simplefirestoreproject;

import com.google.firebase.firestore.Exclude;

public class Entry {
    private String id;
    private String timestamp;
    private String quote = "";
    private String author ="";
    private String category = "";


    public Entry(){
        //public no-arg constructor needed for firestore
    }

    public Entry(String timestamp, String quote, String author, String category){
        this.timestamp = timestamp;
        this.quote = quote;
        this.author = author;
        this.category = category;
    }

    @Exclude //we don't want to let the id appear in the db -> is already the name of the document
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTimestamp(){
        return timestamp;
    }

    public String getQuote(){
        return quote;
    }

    public String getAuthor() {
        return author;
    }

    public String getCategory() {
        return category;
    }
}
