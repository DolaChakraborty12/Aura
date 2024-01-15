package com.example.admin;

public class uploadinfo {

    public String imageName,imageName1,imageName2;
    public String imageURL;
    public uploadinfo(){}

    public uploadinfo(String name,String name1,String name2, String url) {
        this.imageName = name;
        this.imageName1 = name1;
        this.imageName2 = name2;
        this.imageURL = url;
    }

    public String getImageName() {
        return imageName;
    }
    public String getImageName1() {
        return imageName1;
    }
    public String getImageName2() {
        return imageName2;
    }
    public String getImageURL() {
        return imageURL;
    }
}

