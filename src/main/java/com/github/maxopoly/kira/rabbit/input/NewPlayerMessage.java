package com.github.maxopoly.kira.rabbit.input;

import org.json.JSONObject;

import net.civmc.kira.Kira;
import com.github.maxopoly.kira.rabbit.RabbitInputSupplier;
import com.github.maxopoly.kira.relay.actions.NewPlayerAction;
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
        Kira.Companion.getInstance().getApiSessionManager().handleNewPlayerMessage(action);
        Kira.Companion.getInstance().getGroupChatManager().applyToAll(chat -> {
            chat.sendNewPlayer(action);
        });
    }
}
