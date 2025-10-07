package com.github.maxopoly.kira.relay;

public record GroupId(String server, String name) {

    public static GroupId fromGroupChat(GroupChat chat) {
        return new GroupId(chat.getServer(), chat.getName().toLowerCase());
    }
}
