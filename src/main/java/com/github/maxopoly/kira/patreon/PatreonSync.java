package com.github.maxopoly.kira.patreon;

import com.github.maxopoly.kira.KiraMain;
import com.github.maxopoly.kira.user.KiraUser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PatreonSync implements Runnable {
    private static final Logger LOGGER = LogManager.getLogger(PatreonSync.class);

    private final HttpClient client = HttpClient.newHttpClient();

    private final String accessToken;
    private final String campaignId;
    private final String server;

    public PatreonSync(String accessToken, String campaignId, String server) {
        this.accessToken = accessToken;
        this.campaignId = campaignId;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            checkPatreon();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (RuntimeException | IOException ex) {
            LOGGER.catching(ex);
        }
    }

    private void checkPatreon() throws IOException, InterruptedException {
        Map<String, Long> userDiscordId = new HashMap<>();
        Map<String, String> userTier = new HashMap<>();

        checkPatreonRecursive("https://www.patreon.com/api/oauth2/v2/campaigns/" + campaignId + "/members" +
            "?include=currently_entitled_tiers,user" +
            "&" + URLEncoder.encode("fields[tier]", StandardCharsets.UTF_8) + "=title,amount_cents" +
            "&" + URLEncoder.encode("fields[user]", StandardCharsets.UTF_8) + "=social_connections" +
            "&" + URLEncoder.encode("page[count]", StandardCharsets.UTF_8) + "=100", userDiscordId, userTier);

        Map<UUID, String> playerTier = new HashMap<>();

        for (Map.Entry<String, Long> entry : userDiscordId.entrySet()) {
            KiraUser user = KiraMain.getInstance().getUserManager().getUserByDiscordID(entry.getValue());
            UUID uuid = user.getIngameUUID();
            if (uuid != null) {
                playerTier.put(uuid, userTier.get(entry.getKey()));
            }
        }

        System.out.println(playerTier);
        playerTier.put(UUID.fromString("2dd2f992-32ab-4425-a0a8-e7209204a750"), "Civ Power Player");
        KiraMain.getInstance().getMCRabbitGateway().sendPatreon(this.server, playerTier);
    }

    private void checkPatreonRecursive(String url, Map<String, Long> userDiscordId, Map<String, String> userTier) throws IOException, InterruptedException {
        HttpResponse<String> response = client.send(HttpRequest.newBuilder(URI.create(url))
            .header("Authorization", "Bearer " + accessToken)
            .build(), HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            LOGGER.warn("Invalid status code handling response: {}", response);
            return;
        }

        Map<String, Integer> tierPrices = new HashMap<>();
        Map<String, String> tierNames = new HashMap<>();

        JSONObject object = new JSONObject(response.body());

        JSONArray included = object.getJSONArray("included");
        for (int i = 0; i < included.length(); i++) {
            JSONObject data = included.getJSONObject(i);
            String type = data.getString("type");
            if (type.equals("tier")) {
                JSONObject attributes = data.getJSONObject("attributes");
                tierNames.put(data.getString("id"), attributes.getString("title"));
                tierPrices.put(attributes.getString("title"), attributes.getInt("amount_cents"));
            } else if (type.equals("user")) {
                JSONObject attributes = data.getJSONObject("attributes");
                if (attributes.has("social_connections")) {
                    JSONObject socialConnections = attributes.getJSONObject("social_connections");
                    if (socialConnections.has("discord") && !socialConnections.isNull("discord")) {
                        userDiscordId.put(data.getString("id"), Long.parseLong(socialConnections.getJSONObject("discord").getString("user_id")));
                    }
                }
            }
        }

        JSONArray data = object.getJSONArray("data");
        for (int i = 0; i < data.length(); i++) {
            JSONObject member = data.getJSONObject(i);
            JSONObject relationships = member.getJSONObject("relationships");
            String id = relationships.getJSONObject("user").getJSONObject("data").getString("id");
            JSONArray tiers = relationships.getJSONObject("currently_entitled_tiers").getJSONArray("data");
            if (tiers.isEmpty()) {
                continue;
            }

            for (int t = 0; t < tiers.length(); t++) {
                userTier.merge(id, tierNames.get(tiers.getJSONObject(t).getString("id")),
                    (oldValue, value) -> tierPrices.get(value) > tierPrices.get(oldValue) ? value : oldValue);
            }
        }

        if (object.has("links")) {
            JSONObject links = object.getJSONObject("links");
            if (links.has("next")) {
                checkPatreonRecursive(links.getString("next"), userDiscordId, userTier);
            }
        }
    }
}
