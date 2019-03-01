package com.example.asus.gxchat;

public class Path {

    public static final int TYPE_HEAD = 0;
    public static final int TYPE_BACKROUND = 1;
    private String path;
    private int type;

    public void setType(int type){
        this.type = type;
    }

    public void setPath(String path){
        this.path = path;
    }

    public Path(String path, int type){
        this.path = path;
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public String getPath() {
        return path;
    }
}
