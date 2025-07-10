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

import net.chariskar.breaktheapi.api.Fetch;
import net.chariskar.breaktheapi.types.Nation;

public class nationPopulationService {

    Fetch fetch = Fetch.getInstance();

    public static int getNationBonus(int residentAmt) {
        if (residentAmt >= 200) return 100;
        else if (residentAmt >= 120) return 80;
        else if (residentAmt >= 80) return 60;
        else if (residentAmt >= 60) return 50;
        else if (residentAmt >= 40) return 30;
        else if (residentAmt >= 20) return 10;
        else return 0;
    }

    public String get(String name) {
        Nation resp = fetch.getNation(name);
        int numResidents = resp.getResidents().get().size();
        return name + " nation has " + numResidents + " residents and a nation bonus of " + getNationBonus(numResidents);
    }
}
