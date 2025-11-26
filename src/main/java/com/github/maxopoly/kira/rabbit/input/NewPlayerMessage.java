package com.github.maxopoly.kira.rabbit.input;

import com.github.maxopoly.kira.KiraMain;
import com.github.maxopoly.kira.rabbit.RabbitInputSupplier;
import com.github.maxopoly.kira.relay.actions.NewPlayerAction;
import org.json.JSONObject;

import java.util.UUID;

public class NewPlayerMessage extends RabbitMessage {

    public NewPlayerMessage() {
        super("newplayer");
    }

    @Override
    public void handle(JSONObject json, RabbitInputSupplier supplier) {
        String player = json.getString("player");
        UUID playerUUID = UUID.fromString(json.getString("playerUUID"));
        long timestamp = json.optLong("timestamp", System.currentTimeMillis());

        NewPlayerAction action = new NewPlayerAction(timestamp, player, playerUUID);
        KiraMain.getInstance().getAPISessionManager().handleNewPlayerMessage(action);
        KiraMain.getInstance().getGroupChatManager().applyToAll(json.getString("server"), chat -> {
            chat.sendNewPlayer(action);
        });
    }
}
