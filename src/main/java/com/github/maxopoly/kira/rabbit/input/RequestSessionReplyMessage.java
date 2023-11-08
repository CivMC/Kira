package com.github.maxopoly.kira.rabbit.input;

import org.json.JSONObject;

import net.civmc.kira.Kira;
import com.github.maxopoly.kira.rabbit.RabbitInputSupplier;

public class RequestSessionReplyMessage extends RabbitMessage {

	public RequestSessionReplyMessage() {
		super("requestsession");
	}

	@Override
	public void handle(JSONObject json, RabbitInputSupplier supplier) {
		Kira.Companion.getInstance().getRequestSessionManager().handleReply(json);
	}
}
