package com.abevriens.commands;

import com.abevriens.*;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class Factions_Requests {
    private static final int CHAT_SIZE = 8;
    public int page;
    public CommandContext commandContext;

    public Factions_Requests(CommandContext _commandContext, int _page) {
        page = _page;
        commandContext = _commandContext;

        command_Request();
    }

    private void command_Request() {
        List<String> list = commandContext.cc_player.faction.playerJoinRequests;

        if(commandContext.cc_player.faction.isEmptyFaction()) {
           commandContext.player.spigot().sendMessage(TextUtil.GenerateErrorMsg("Je zit niet in een faction, als je join requests" +
                   " wil bekijken kun je een faction maken met /factions create").create());

           return;
        } else if(list.size() < 1) {
            commandContext.player.spigot().sendMessage(TextUtil.GenerateErrorMsg("Geen requests gevonden, wacht totdat" +
                    " iemand je faction joinen!").create());

            return;
        }

        int lastPage = (int)Math.ceil((double) list.size() / CHAT_SIZE);

        if(page > lastPage) {
            page = lastPage;
        } else if(page < 1) {
            page = 1;
        }

        ComponentBuilder header = TextUtil.GenerateHeaderMsg("Requests [" + page + "/" + lastPage + "]");
        ComponentBuilder footer = TextUtil.GenerateFooterButtonMsg("/factions requests " + (page-1),
                "/factions requests " + (page+1),
                "Ga een pagina terug",
                "Ga een pagina verder");

        header.append(TextUtil.newLine);

        ComponentBuilder componentBuilder = new ComponentBuilder().append(TextUtil.newLine);

        componentBuilder.append(header.create());

        for(int j = (page-1) * CHAT_SIZE; j < CHAT_SIZE + (page-1) * CHAT_SIZE; j++) {
            if(list.size()-1 < j) {
                break;
            }

            OfflinePlayer request_player = Bukkit.getOfflinePlayer(UUID.fromString(list.get(j)));
            CC_Player request_cc_player = CrackCityRaids.playerManager.getCCPlayer(request_player);

            TextComponent playerNumber = new TextComponent(j + 1 + ": ");
            playerNumber.setColor(ChatColor.GOLD);
            playerNumber.setBold(true);
            TextComponent playerInfo = new TextComponent(request_cc_player.displayName);
            playerInfo.setBold(false);

            TextComponent acceptButton = new TextComponent(" [Accept]");
            acceptButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Klik hier om deze " +
                    "speler te accepteren")));
            acceptButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/factions accept "
                    + request_cc_player.displayName));
            acceptButton.setColor(ChatColor.DARK_AQUA);

            componentBuilder.append(playerNumber);
            componentBuilder.append(playerInfo);

            if(commandContext.cc_player.faction.factionOwner.uuid.equals(commandContext.cc_player.uuid)) {
                componentBuilder.append(acceptButton);
            }

            if(j == (CHAT_SIZE + (page-1) * CHAT_SIZE) - 1 || list.size()-1 == j) {
                componentBuilder.append("\n").event((ClickEvent) null).event((HoverEvent) null);
            } else {
                componentBuilder.append(TextUtil.newLine).event((ClickEvent) null).event((HoverEvent) null);
            }
        }

        commandContext.player.spigot().sendMessage(componentBuilder.create());
        commandContext.player.spigot().sendMessage(footer.create());
    }
}
