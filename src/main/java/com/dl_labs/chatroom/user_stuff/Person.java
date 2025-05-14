package com.dl_labs.chatroom.user_stuff;

public class Person {
    
    private String name;
    private boolean isHost;

    public Person(String name, boolean isHost) {
        this.name = name;
        this.isHost = isHost;
        }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public boolean isHost() {
        return isHost;
    }
    public void setHost(boolean isHost) {
        this.isHost = isHost;
    }
    

}
