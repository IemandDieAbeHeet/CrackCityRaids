package com.abevriens.commands;

import com.abevriens.CrackCityRaids;
import com.abevriens.Faction;
import com.abevriens.FactionManager;
import com.abevriens.TextUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

import java.util.ArrayList;

public class Factions_List {
    private static final int CHAT_SIZE = 8;
    public int page;

    CommandContext commandContext;

    public Factions_List(CommandContext _commandContext, int _page) {
        page = _page;
        commandContext = _commandContext;

        command_List();
    }

    private void command_List() {
        ArrayList<Faction> list = (ArrayList<Faction>) CrackCityRaids.factionManager.factionList;

        if(list.size() < 1) {
            commandContext.player.spigot().sendMessage(TextUtil.GenerateErrorMsg("Geen factions gevonden, ben de eerste" +
                    " faction door /factions create te gebruiken!").create());

            return;
        }

        int lastPage = (int)Math.ceil((double) list.size() / CHAT_SIZE);

        if(page > lastPage) {
            page = lastPage;
        } else if(page < 1) {
            page = 1;
        }

        ComponentBuilder header = TextUtil.GenerateHeaderMsg("Factions [" + page + "/" + lastPage + "]");
        ComponentBuilder footer = TextUtil.GenerateFooterButtonMsg("/factions list " + (page-1),
                "/factions list " + (page+1),
                "Ga een pagina terug",
                "Ga een pagina verder");

        header.append(TextUtil.newLine);

        ComponentBuilder componentBuilder = new ComponentBuilder().append(TextUtil.newLine);

        componentBuilder.append(header.create());

        for(int j = (page-1) * CHAT_SIZE; j < CHAT_SIZE + (page-1) * CHAT_SIZE; j++) {
            if(list.size()-1 < j) {
                break;
            }

            TextComponent factionNumber = new TextComponent(j + 1 + ": ");
            factionNumber.setColor(ChatColor.GOLD);
            factionNumber.setBold(true);
            TextComponent factionInfo = new TextComponent(list.get(j).factionName + " - " + list.get(j).players.size());
            factionInfo.setBold(false);

            factionNumber.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Klik voor meer info")));
            factionNumber.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/factions info " + list.get(j).factionName));

            TextComponent joinButton = new TextComponent(" [Join]");
            joinButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Klik hier om de faction te joinen")));
            joinButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/factions join " + list.get(j).factionName));
            joinButton.setColor(ChatColor.DARK_AQUA);

            componentBuilder.append(factionNumber);
            componentBuilder.append(factionInfo);

            if(commandContext.cc_player.faction.isEmptyFaction() && list.get(j).isJoinable()) {
                componentBuilder.append(joinButton);
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
