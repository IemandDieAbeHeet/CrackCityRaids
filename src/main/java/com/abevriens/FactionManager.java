package com.abevriens;

import org.bson.codecs.configuration.CodecConfigurationException;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FactionManager {
    public List<Faction> factionList = new ArrayList<>();
    public List<String> factionNameList = new ArrayList<>();

    public void LoadFactions() {
        try {
            Iterable<POJO_Faction> factions = CrackCityRaids.instance.dbHandler.factionCollection.find();

            for (POJO_Faction faction : factions) {
                factionList.add(POJOToFaction(faction));
                factionNameList.add(faction.factionName);
            }
        } catch(CodecConfigurationException e) {
            CrackCityRaids.instance.getLogger().info(e.getMessage());
            CrackCityRaids.instance.getLogger().info(ChatColor.RED + "Couldn't load factions from database, a database entry might be corrupted.");
        }
    }

    public Faction getFaction(String factionName) {
        for(Faction faction : factionList) {
            if(faction.factionName.equals(factionName)) {
                return faction;
            }
        }
        return emptyFaction;
    }

    public static Faction emptyFaction = new Faction(null, "None", null, null, JoinStatus.OPEN);

    public static Faction POJOToFaction(@NotNull POJO_Faction pojo_faction) {
        Faction faction;

        faction = new Faction(
                pojo_faction.factionOwner,
                pojo_faction.factionName,
                pojo_faction.players,
                pojo_faction.occupiedChunks,
                pojo_faction.joinStatus
        );

        return faction;
    }

    public static POJO_Faction FactionToPOJO(Faction faction) {
        POJO_Faction pojo_faction = new POJO_Faction();

        pojo_faction.factionName = faction.factionName;
        pojo_faction.factionOwner = faction.factionOwner;
        pojo_faction.occupiedChunks = faction.occupiedChunks;
        pojo_faction.players = faction.players;
        pojo_faction.joinStatus = faction.joinStatus;

        return pojo_faction;
    }
}
