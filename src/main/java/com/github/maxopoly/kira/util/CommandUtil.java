package com.github.maxopoly.kira.util;

public class CommandUtil {

    public static CommandRoute getRoute(String argument, String[] servers) {
        String[] split = argument.split(" ", 2);
        for (String configServer : servers) {
            if (configServer.equalsIgnoreCase(split[0])) {
                return new CommandRoute(configServer, split[1]);
            }
        }
        return new CommandRoute(servers[0], argument);
    }

    public record CommandRoute(String server, String command) {

    }
}
