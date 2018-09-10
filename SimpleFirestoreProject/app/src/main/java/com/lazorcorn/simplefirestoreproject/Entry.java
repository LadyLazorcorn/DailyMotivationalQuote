package com.lazorcorn.simplefirestoreproject;

import com.google.firebase.firestore.Exclude;

public class Entry {
    private String id;
    private String quote = "";
    private String author ="";
    private int category;


    public Entry(){
        //public no-arg constructor needed for firestore
    }

    public Entry(String quote, String author, int category){
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


    public String getQuote(){
        return quote;
    }

    public String getAuthor() {
        return author;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }
}
