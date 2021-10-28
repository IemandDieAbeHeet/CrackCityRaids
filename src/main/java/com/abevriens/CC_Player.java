package com.abevriens;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.List;

public class CC_Player {
    public String displayName;
    public String uuid;
    public Faction faction;
    public List<String> pendingRequests;
    public Location previousLocation = new Vector(0, 0, 0).toLocation(Bukkit.getWorld("world"));
    public String discordName;

    public CC_Player(String _displayName, String _uuid, Faction _faction, List<String> _pendingRequests) {
        displayName = _displayName;
        uuid = _uuid;
        faction = _faction;
        pendingRequests = _pendingRequests;
    }

    /**
     * Update database after using!
     */
    public void deleteRequests() {
        pendingRequests.clear();
        for(String request : pendingRequests) {
            Faction requestFaction = CrackCityRaids.instance.factionManager.getFaction(request);
            requestFaction.playerJoinRequests.remove(uuid);
        }
    }
}
