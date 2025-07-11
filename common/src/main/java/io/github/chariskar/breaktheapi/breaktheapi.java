package io.github.chariskar.breaktheapi;

import io.github.chariskar.breaktheapi.Services.*;
import io.github.chariskar.breaktheapi.api.Fetch;
import io.github.chariskar.breaktheapi.engine.nearby;
import io.github.chariskar.breaktheapi.types.Nation;
import io.github.chariskar.breaktheapi.types.Resident;
import io.github.chariskar.breaktheapi.types.Town;
import io.github.chariskar.breaktheapi.types.reference;
import io.github.chariskar.breaktheapi.utils.config;


public final class breaktheapi {

    public static config Config;
    public Fetch fetch = Fetch.getInstance();
    public nearby nearby = new nearby();
    public Class<Nation> Nation = Nation.class;
    public Class<Resident> Resident = Resident.class;
    public Class<Town> Town = Town.class;
    public Class<reference> Reference = reference.class;
    public bestdealsService bestdeals = new bestdealsService();
    public coordsService coords = new coordsService();
    public discordLinkedService discordLinked = new discordLinkedService();
    public friendsService friends = new friendsService();
    public GoToService goTo = new GoToService();
    public lastSeenService lastSeen = new lastSeenService();
    public locateService locate = new locateService();
    public nationPopulationService nationPop = new nationPopulationService();
    public onlinestaffService onlineStaff = new onlinestaffService();
    public townlessService townless = new townlessService();
    public findPlayerService findPlayer = new findPlayerService();

    public breaktheapi(String configName) {
        config.setConfigFile(configName);
    }
}
