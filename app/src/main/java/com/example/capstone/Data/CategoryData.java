package com.example.capstone.Data;

public class CategoryData {
    String Cimage;
    String Cname;

    public CategoryData(String Cimage, String Cname){
        this.Cimage = Cimage;
        this.Cname = Cname;
    }

    public void setCimage(String cimage) {
        Cimage = cimage;
    }

    public void setCname(String cname) {
        Cname = cname;
    }

    public String getCimage() {
        return Cimage;
    }

    public String getCname() {
        return Cname;
    }
}
