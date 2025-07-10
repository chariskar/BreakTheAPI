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

package net.chariskar.breaktheapi.Services;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.chariskar.breaktheapi.api.Fetch;
import net.chariskar.breaktheapi.utils.config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GoToService {
    private static final Logger LOGGER = LoggerFactory.getLogger("breaktheapi");
    private static final ExecutorService IO_EXECUTOR = Executors.newFixedThreadPool(4);

    /**
     * Finds the nearest valid town spawns for the given name.
     *
     * @return future completing with either a list of town names or throws on fatal error.
     */
    public CompletableFuture<List<String>> findValidTowns(String townName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Fetch fetch = Fetch.getInstance();

                int radius = 500;
                int attempts = 3;

                while (attempts-- > 0) {
                    JsonObject payload = new JsonObject();
                    JsonArray queryArr = new JsonArray();
                    JsonObject query = new JsonObject();
                    query.addProperty("target_type", "TOWN");
                    query.addProperty("target", townName);
                    query.addProperty("search_type", "TOWN");
                    query.addProperty("radius", radius);
                    queryArr.add(query);
                    payload.add("query", queryArr);

                    String nearbyResp = fetch.PostRequest(
                            config.getInstance().getApiURL() + "/nearby",
                            payload.toString()
                    ).body();
                    JsonArray nearbyArray = JsonParser.parseString(nearbyResp)
                            .getAsJsonArray().get(0).getAsJsonArray();

                    List<String> towns = new ArrayList<>();
                    for (JsonElement el : nearbyArray) {
                        JsonObject obj = el.getAsJsonObject();
                        if (obj.has("name")) towns.add(obj.get("name").getAsString());
                    }

                    if (towns.isEmpty()) {
                        radius += 500;
                        continue;
                    }

                    JsonObject detailsPayload = new JsonObject();
                    JsonArray townQuery = new JsonArray();
                    towns.forEach(townQuery::add);
                    detailsPayload.add("query", townQuery);

                    String detailsResp = fetch.PostRequest(
                            config.getInstance().getApiURL() + "/towns",
                            detailsPayload.toString()
                    ).body();
                    JsonArray detailsArr = JsonParser.parseString(detailsResp).getAsJsonArray();

                    List<String> valid = new ArrayList<>();
                    for (JsonElement el : detailsArr) {
                        JsonObject town = el.getAsJsonObject();
                        JsonObject status = town.get("status").getAsJsonObject();

                        if (status.get("isPublic").getAsBoolean()
                                && status.get("canOutsidersSpawn").getAsBoolean()) {
                            valid.add(town.get("name").getAsString());
                        } else if (status.get("isCapital").getAsBoolean()) {
                            String nationUuid = town.get("nation")
                                    .getAsJsonObject().get("uuid").getAsString();
                            JsonObject natPayload = new JsonObject();
                            JsonArray natQuery = new JsonArray();
                            natQuery.add(nationUuid);
                            natPayload.add("query", natQuery);

                            String natResp = fetch.PostRequest(
                                    config.getInstance().getApiURL() + "/nations",
                                    natPayload.toString()
                            ).body();
                            JsonObject nat = JsonParser.parseString(natResp)
                                    .getAsJsonArray().get(0).getAsJsonObject();
                            JsonObject natStatus = nat.get("status").getAsJsonObject();
                            if (natStatus.get("isPublic").getAsBoolean()) {
                                valid.add(town.get("name").getAsString());
                            }
                        }
                    }

                    if (!valid.isEmpty()) {
                        return valid;
                    }

                    radius += 500;
                }

                return Collections.emptyList();

            } catch (Exception e) {
                LOGGER.error("Error in GoToService", e);
                throw new CompletionException(e);
            }
        }, IO_EXECUTOR);
    }
}