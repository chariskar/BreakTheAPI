/*
 * This file is part of breaktheapi.
 *
 * breaktheapi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * breaktheapi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with breaktheapi. If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.chariskar.breaktheapi.Services;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.chariskar.breaktheapi.api.Fetch;
import io.github.chariskar.breaktheapi.utils.config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class findPlayerService {
    private static final Logger LOGGER = LoggerFactory.getLogger("breaktheapi");
    private final Fetch fetch = Fetch.getInstance();

    public Optional<PlayerLocationInfo> get(String username) {
        try {
            String playersJson = fetch.GetRequest(config.getInstance().getApiURL() + "/tiles/players.json").body();
            JsonObject playersObject = JsonParser.parseString(playersJson).getAsJsonObject();

            JsonArray playersArray = playersObject.getAsJsonArray("players");
            for (JsonElement playerElement : playersArray) {
                JsonObject user = playerElement.getAsJsonObject();
                String name = user.get("name").getAsString();

                if (name.equalsIgnoreCase(username)) {
                    double x = user.get("x").getAsDouble();
                    double z = user.get("z").getAsDouble();

                    JsonObject payload = new JsonObject();
                    JsonArray queryArray = new JsonArray();
                    JsonArray coords = new JsonArray();
                    coords.add(x);
                    coords.add(z);
                    queryArray.add(coords);
                    payload.add("query", queryArray);

                    String locationJson = fetch.PostRequest(config.getInstance().getApiURL() + "/location", payload.toString()).body();
                    JsonArray locationData = JsonParser.parseString(locationJson).getAsJsonArray();

                    if (!locationData.isEmpty() && locationData.get(0).isJsonObject()) {
                        JsonObject data = locationData.get(0).getAsJsonObject();
                        boolean isWilderness = data.get("isWilderness").getAsBoolean();
                        String townName = "Unknown";

                        if (!isWilderness && data.has("town") && data.get("town").isJsonObject()) {
                            JsonObject town = data.getAsJsonObject("town");
                            townName = town.has("name") ? town.get("name").getAsString() : "Unknown";
                        }

                        return Optional.of(new PlayerLocationInfo(username, x, z, isWilderness, townName, true));
                    }
                }
            }

            return Optional.of(new PlayerLocationInfo(username, 0, 0, true, "N/A", false));

        } catch (Exception e) {
            System.err.println("Error fetching location for " + username + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    public static class PlayerLocationInfo {
        public final String username;
        public final double x;
        public final double z;
        public final boolean isWilderness;
        public final String townName;
        public boolean found = false;

        public PlayerLocationInfo(String username, double x, double z, boolean isWilderness, String townName, boolean found) {
            this.username = username;
            this.x = x;
            this.z = z;
            this.isWilderness = isWilderness;
            this.townName = townName;
            this.found = found;
        }

        @Override
        public String toString() {
            if (!found) {
                return username + " is either offline or not showing up on the map.";
            } else if (isWilderness) {
                return username + " at x: " + x + ", z: " + z + " is in wilderness.";
            } else {
                return username + " at x: " + x + ", z: " + z + " is in town: " + townName + ".";
            }
        }
    }
}
