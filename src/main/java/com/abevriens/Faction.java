package com.abevriens;

import org.bukkit.Chunk;

import java.util.ArrayList;
import java.util.List;

public class Faction {
    public String factionName;

    public List<Chunk> occupiedChunks = new ArrayList<>();

    public List<POJO_Player> players = new ArrayList<>();

    public POJO_Player factionOwner;

    public Faction(POJO_Player _factionOwner, String _factionName, List<POJO_Player> _players, List<Chunk> _occupiedChunks) {
        factionOwner = _factionOwner;
        factionName = _factionName;
        players = _players;
        occupiedChunks = _occupiedChunks;
    }
}
