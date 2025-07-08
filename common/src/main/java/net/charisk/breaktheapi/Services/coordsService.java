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

package net.charisk.breaktheapi.Services;

import com.google.gson.*;
import net.charisk.breaktheapi.api.Fetch;
import net.charisk.breaktheapi.utils.config;

import java.util.Optional;

public class coordsService {
    private static final String API_URL = config.getInstance().getApiURL() + "/location";
    private final Gson gson = new Gson();
    private final Fetch fetch = Fetch.getInstance();

    public LocationResult get(double x, double z) throws Exception {
        JsonArray coords = new JsonArray();
        coords.add(x);
        coords.add(z);
        JsonArray queryArray = new JsonArray();
        queryArray.add(coords);
        JsonObject payload = new JsonObject();
        payload.add("query", queryArray);

        String resp = fetch.GetRequest(config.getInstance().getApiURL()).body();
        JsonArray data = JsonParser.parseString(resp).getAsJsonArray();
        if (data.size() != 1 || !data.get(0).isJsonObject()) {
            throw new JsonParseException("Unexpected API response format: " + resp);
        }

        JsonObject obj = data.get(0).getAsJsonObject();
        boolean isWilderness = obj.has("isWilderness") && obj.get("isWilderness").getAsBoolean();

        Optional<String> townName = Optional.empty();
        Optional<String> nationName = Optional.empty();
        if (!isWilderness) {
            if (obj.has("town") && obj.get("town").isJsonObject()) {
                JsonObject town = obj.getAsJsonObject("town");
                if (town.has("name")) townName = Optional.of(town.get("name").getAsString());
            }
            if (obj.has("nation") && obj.get("nation").isJsonObject()) {
                JsonObject nation = obj.getAsJsonObject("nation");
                if (nation.has("name")) nationName = Optional.of(nation.get("name").getAsString());
            }
        }

        return new LocationResult(isWilderness, townName, nationName);
    }

    public static class LocationResult {
        private final boolean wilderness;
        private final Optional<String> townName;
        private final Optional<String> nationName;

        public LocationResult(boolean wilderness, Optional<String> townName, Optional<String> nationName) {
            this.wilderness = wilderness;
            this.townName = townName;
            this.nationName = nationName;
        }

        public boolean isWilderness() {
            return wilderness;
        }

        public Optional<String> getTownName() {
            return townName;
        }

        public Optional<String> getNationName() {
            return nationName;
        }

        public String toString() {
            return "LocationResult{" +
                    "wilderness=" + wilderness +
                    ", townName=" + townName +
                    ", nationName=" + nationName +
                    '}';
        }
    }
}
