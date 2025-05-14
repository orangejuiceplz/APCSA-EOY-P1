package com.dl_labs.chatroom.user_stuff;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message {
    private String content;
    private Person sender;
    private LocalDateTime timestamp;
    private MessageType type;
    
    // an enum is basically a list of constants
    // i use this in c++
    public enum MessageType {
        CHAT,       
        SYSTEM,     
        COMMAND,    
        PRIVATE     
    }
    
    public Message(String content, Person sender) {
        this.content = content;
        this.sender = sender;
        this.timestamp = LocalDateTime.now();
        this.type = MessageType.CHAT;
    }
    
    public Message(String content) {
        this.content = content;
        this.sender = null;
        this.timestamp = LocalDateTime.now();
        this.type = MessageType.SYSTEM;
    }
    
    public Message(String content, Person sender, MessageType type) {
        this.content = content;
        this.sender = sender;
        this.timestamp = LocalDateTime.now();
        this.type = type;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public Person getSender() {
        return sender;
    }
    
    public void setSender(Person sender) {
        this.sender = sender;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public MessageType getType() {
        return type;
    }
    
    public void setType(MessageType type) {
        this.type = type;
    }
    
    public String format() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String time = timestamp.format(formatter);
        
        switch (type) {
            case CHAT:
                return "[" + time + "] " + sender.getName() + ": " + content;
            case SYSTEM:
                return "[" + time + "] " + content;
            case COMMAND:
                return "[" + time + "] " + sender.getName() + " (command): " + content;
            case PRIVATE:
                return "[" + time + "] [Private message from " + sender.getName() + "]: " + content;
            default:
                return "[" + time + "] " + content;
        }
    }
    
    @Override
    public String toString() {
        return format();
    }
}
