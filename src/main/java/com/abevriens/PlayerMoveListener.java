package com.abevriens;

import com.abevriens.jda.DiscordIdEnum;
import com.abevriens.jda.RaidAlertEmbedBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.awt.*;

public class PlayerMoveListener implements Listener {
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        CC_Player cc_player = CrackCityRaids.instance.playerManager.getCCPlayer(player);

        FactionCore closestCore = CrackCityRaids.instance.factionCoreManager.getClosestFactionCore(event.getFrom());

        if(closestCore == null) return;

        Faction closestFaction = CrackCityRaids.instance.factionManager.getFaction(closestCore.factionName);

        if(cc_player.faction == closestFaction) return;

        if(isWithinBounds(event.getFrom(), closestCore)) {
            player.teleport(cc_player.previousLocation);
            ComponentBuilder errorMsg;
            if(closestFaction.raidAlert.started) {
                errorMsg = TextUtil.GenerateErrorMsg("Je probeert een faction te betreden die niet " +
                        "van jou is. Er is al een raid timer gestart en je kunt over " +
                        FactionManager.generateCountdownTimeString(closestFaction.raidAlert.countdown) + " beginnen met raiden.");

                if(!closestFaction.raidAlert.enteredPlayerList.contains(cc_player.displayName)) {
                    closestFaction.raidAlert.enteredPlayerList.add(cc_player.displayName);
                }
                if(!closestFaction.raidAlert.enteredFactionList.contains(cc_player.faction.factionName)) {
                    closestFaction.raidAlert.enteredFactionList.add(cc_player.faction.factionName);
                }
                closestFaction.raidAlert.updateTimerMessage();
            } else {
                closestFaction.raidAlert.started = true;
                errorMsg = TextUtil.GenerateErrorMsg(
                        "Je probeert een faction te betreden die niet van jou is, er is een raid alert verstuurd. " +
                                "Je kunt over 6 uur de faction betreden en beginnen met raiden.");
                TextChannel infoChannel = CrackCityRaids.instance.discordManager.getGuild().getTextChannelById(
                        closestFaction.discordIdMap.get(DiscordIdEnum.INFO_CHANNEL));
                if(!closestFaction.raidAlert.enteredFactionList.contains(cc_player.faction.factionName)) {
                    closestFaction.raidAlert.enteredFactionList.add(cc_player.faction.factionName);
                }

                if(!closestFaction.raidAlert.enteredPlayerList.contains(cc_player.displayName)) {
                    closestFaction.raidAlert.enteredPlayerList.add(cc_player.displayName);
                }

                EmbedBuilder raidAlertEmbedBuilder = new RaidAlertEmbedBuilder(closestFaction.raidAlert);
                if (infoChannel != null) {
                    infoChannel.sendMessage(raidAlertEmbedBuilder.build()).queue(message -> {
                        closestFaction.discordIdMap.put(DiscordIdEnum.TIMER, message.getId());
                        closestFaction.raidAlert.runTimer();
                    });
                }
            }
            player.spigot().sendMessage(errorMsg.create());
        }

        cc_player.previousLocation = event.getFrom();
    }

    public boolean isWithinBounds(Location location, FactionCore factionCore) {
        int xLoc = location.getBlockX();
        int yLoc = location.getBlockY();
        int zLoc = location.getBlockZ();

        Faction faction = CrackCityRaids.instance.factionManager.getFaction(factionCore.factionName);
        int blockMinX = factionCore.blockLocation.getBlockX() - faction.xSize/2;
        int blockMaxX = factionCore.blockLocation.getBlockX() + faction.xSize/2;
        int blockMinY = factionCore.blockLocation.getBlockY() - faction.ySize/2;
        int blockMaxY = factionCore.blockLocation.getBlockY() + faction.ySize/2;
        int blockMinZ = factionCore.blockLocation.getBlockZ() - faction.xSize/2;
        int blockMaxZ = factionCore.blockLocation.getBlockZ() + faction.xSize/2;
        return xLoc > blockMinX && xLoc < blockMaxX &&
                yLoc > blockMinY && yLoc < blockMaxY &&
                zLoc > blockMinZ && zLoc < blockMaxZ;
    }
}
