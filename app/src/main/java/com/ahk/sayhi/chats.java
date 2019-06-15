package com.ahk.sayhi;

public class chats {
    private boolean seen;
    private long time;
    private static int chatsCount=0;


    public chats() {
        chatsCount++;
    }

    public chats(boolean seen, long time) {
        this.seen = seen;
        this.time = time;
        chatsCount++;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getChatsCount() {
        return chatsCount;

    }
}
