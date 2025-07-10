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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class onlinestaffService {
    private final Fetch fetcher = Fetch.getInstance();

    public List<UUID> get(List<UUID> onlineUsers) throws Exception {
        String jsonResponse = fetcher.GetRequest(config.getInstance().getStaffRepoURL()).body();
        JsonObject staffJson = JsonParser.parseString(jsonResponse).getAsJsonObject();

        List<String> staffUuids = new ArrayList<>();

        for (String role : staffJson.keySet()) {
            JsonArray roleArray = staffJson.getAsJsonArray(role);
            for (JsonElement element : roleArray) {
                if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                    staffUuids.add(element.getAsString());
                }
            }
        }
        return onlineUsers.stream()
                .map(UUID::toString)
                .filter(staffUuids::contains)
                .map(UUID::fromString)
                .toList();
    }
}
