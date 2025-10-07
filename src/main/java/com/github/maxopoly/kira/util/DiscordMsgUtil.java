package com.github.maxopoly.kira.util;

public class DiscordMsgUtil {
    public static String escape(String message) {
        return message
                .replace("`", "\\`")
                .replace("*", "\\*")
                .replace("_", "\\_")
                .replace("#", "\\#")
                .replace("-", "\\-")
                .replace("+", "\\+");
    }

}
