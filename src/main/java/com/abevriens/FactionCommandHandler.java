package com.abevriens;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

public class FactionCommandHandler implements CommandExecutor {
    Player player;
    POJO_Player pojo_player;
    CC_Player cc_player;
    @NotNull PlayerManager playerManager = CockCityRaids.instance.playerManager;
    @NotNull FactionManager factionManager = CockCityRaids.instance.factionManager;
    int LIST_CHAT_SIZE = 8;

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if(sender instanceof Player) {
            player = (Player) sender;
            cc_player = playerManager.getCCPlayer(player);
            pojo_player = playerManager.getPOJOPlayer(player);
            if(args.length > 0) {
                switch (args[0].toLowerCase()) {
                    case "create":
                        if(args.length > 1) {
                            command_Create(args[1]);
                        } else {
                            ComponentBuilder components = TextUtil.GenerateErrorMsg(
                                    "Geen faction naam opgegeven, gebruik het commando als volgt:\n");

                            TextComponent commandText = new TextComponent("/factions create [naam]");
                            commandText.setBold(true);

                            player.spigot().sendMessage(components.append(commandText).create());
                        }
                        break;
                    case "info":
                        if(args.length > 1) {
                            command_Info(args[1]);
                        } else {
                            command_Info(cc_player.faction.factionName);
                        }
                        break;
                    case "help":
                        if(args.length > 1) {
                            command_Help(Integer.parseInt(args[1]));
                        } else {
                            command_Help(1);
                        }
                        break;
                    case "join":
                        command_Join(args[1]);
                        break;
                    case "leave":
                        command_Leave();
                        break;
                    case "list":
                        if(args.length > 1) {
                            command_List(Integer.parseInt(args[1]));
                        } else {
                            command_List(1);
                        }
                        break;
                    case "delete":
                        command_Delete();
                        break;
                    default:
                        command_Help("Commando argument niet gevonden, probeer iets anders.");
                }
            } else {
                command_Help(1);
            }
            return  true;
        } else {
            return false;
        }
    }

    private void command_Info(String factionName) {
        Faction faction = CockCityRaids.instance.factionManager.getFaction(factionName);

        if(Objects.equals(faction.factionName, FactionManager.emptyFaction.factionName)) {
            ComponentBuilder errorMsg = TextUtil.GenerateErrorMsg(
                    "Je zit nog niet in een faction, gebruik /factions join om er een te joinen of /factions create om een" +
                            "faction aan te maken.");

            player.spigot().sendMessage(errorMsg.create());
        } else {
            ComponentBuilder header = TextUtil.GenerateHeaderMsg("Info");
            ComponentBuilder footer = TextUtil.GenerateFooterMsg();

            BaseComponent[] nameInfo = new ComponentBuilder("Naam: ")
                    .color(ChatColor.GOLD).bold(true)
                    .append(new TextComponent(faction.factionName))
                    .color(ChatColor.WHITE).bold(false).create();

            BaseComponent[] ownerInfo = new ComponentBuilder("Owner: ")
                        .color(ChatColor.GOLD).bold(true)
                    .append(new TextComponent(faction.factionOwner.displayName))
                        .color(ChatColor.WHITE).bold(false).create();

            BaseComponent[] components = new ComponentBuilder()
                    .append(header.create())
                    .append(TextUtil.newLine)
                    .append(nameInfo)
                    .append(TextUtil.newLine)
                    .append(ownerInfo)
                    .append(TextUtil.newLine)
                    .append(footer.create()).create();

            player.spigot().sendMessage(components);
        }
    }

    private void command_Create(String name) {
        if(factionManager.factionNameList.contains(name)) {
            TextComponent errorMessage = new TextComponent("Een faction met deze naam bestaat al, kies een andere naam.");
            errorMessage.setColor(ChatColor.RED);
            player.spigot().sendMessage(errorMessage);
        } else {
            Faction faction = new Faction(
                    pojo_player,
                    name,
                    new ArrayList<POJO_Player>() {
                        {
                            add(pojo_player);
                        }
                    },
                    new ArrayList<Chunk>());

            POJO_Faction pojo_faction = FactionManager.FactionToPOJO(faction);
            pojo_player.factionName = faction.factionName;
            cc_player.faction = faction;
            CockCityRaids.instance.dbHandler.insertFaction(pojo_faction);
            CockCityRaids.instance.dbHandler.updatePlayer(pojo_player);
            factionManager.factionNameList.add(faction.factionName);

            TextComponent successMessage = new TextComponent("Faction is succesvol aangemaakt!");
            successMessage.setColor(ChatColor.GREEN);
            player.spigot().sendMessage(successMessage);
        }
    }

    public void command_Delete() {
        if(!cc_player.faction.factionOwner.uuid.equals(cc_player.uuid)) {
            ComponentBuilder errorMessage = TextUtil.GenerateErrorMsg("Je bent niet de owner van de faction. Als je het echt" +
                    " graag wilt moet je aan " + cc_player.faction.factionOwner.displayName + " vragen of hij jouw owner geeft.");
            player.spigot().sendMessage(errorMessage.create());
        } else {
            factionManager.factionList.remove(cc_player.faction);
            factionManager.factionNameList.remove(cc_player.faction.factionName);
            CockCityRaids.instance.dbHandler.deleteFaction(cc_player.faction.factionName);

            pojo_player.factionName = FactionManager.emptyFaction.factionName;
            CockCityRaids.instance.dbHandler.updatePlayer(pojo_player);
            cc_player.faction = FactionManager.emptyFaction;

            ComponentBuilder successMessage = TextUtil.GenerateSuccessMsg("Faction is succesvol verwijderd!");
            player.spigot().sendMessage(successMessage.create());
        }
    }

    private void command_Leave() {
        if(Objects.equals(cc_player.faction.factionName, FactionManager.emptyFaction.factionName)) {
            TextComponent errorMessage = new TextComponent("Je ziet niet in een faction, gebruik /faction" +
                    " join om een faction te joinen.");
            errorMessage.setColor(ChatColor.RED);
            player.spigot().sendMessage(errorMessage);
        } else if(Objects.equals(cc_player.faction.factionOwner.uuid, cc_player.uuid)) {
            TextComponent errorMessage = new TextComponent("Je bent de owner van deze faction, gebruik /faction delete om" +
                    " de faction te verwijderen of /faction setOwner" +
                    " om iemand anders owner te maken.");
            errorMessage.setColor(ChatColor.RED);
            player.spigot().sendMessage(errorMessage);
        } else {
            playerManager.setPlayerFaction(player, FactionManager.emptyFaction);
            TextComponent leaveMessage = new TextComponent("Faction succesvol verlaten.");
            leaveMessage.setColor(ChatColor.GREEN);
            player.spigot().sendMessage(leaveMessage);
        }
    }

    private void command_Join(String factionName) {
        TextComponent errorMessage = new TextComponent();
        errorMessage.setColor(ChatColor.RED);
        if(!cc_player.faction.factionName.equals(FactionManager.emptyFaction.factionName)) {
            errorMessage.setText("Je zit al in een faction, gebruik eerst /factions leave om je faction te verlaten.");
            player.spigot().sendMessage(errorMessage);
        } else if(!factionManager.factionNameList.contains(factionName)) {
            errorMessage.setText("De opgegeven faction bestaat niet!");
            player.spigot().sendMessage(errorMessage);
        } else {
            Faction newFaction = CockCityRaids.instance.factionManager.getFaction(factionName);
            playerManager.setPlayerFaction(player, newFaction);
        }
    }

    private void command_List(int page) {
        ArrayList<Faction> list = (ArrayList<Faction>) CockCityRaids.instance.factionManager.factionList;

        int lastPage = (int)Math.ceil((double) list.size() / LIST_CHAT_SIZE);

        if(page > lastPage) {
            page = lastPage;
        } else if(page < 1) {
            page = 1;
        }

        ComponentBuilder header = TextUtil.GenerateHeaderMsg("List [" + page + "/" + lastPage + "]");
        ComponentBuilder footer = TextUtil.GenerateFooterButtonMsg("/factions list " + (page-1),
                "/factions list " + (page+1),
                "Ga een pagina terug",
                "Ga een pagina verder");

        header.append(TextUtil.newLine);

        ComponentBuilder componentBuilder = new ComponentBuilder().append(TextUtil.newLine);

        componentBuilder.append(header.create());

        for(int j = (page-1) * LIST_CHAT_SIZE; j < LIST_CHAT_SIZE + (page-1) * LIST_CHAT_SIZE; j++) {
            if(list.size()-1 < j) {
                break;
            }

            TextComponent factionNumber = new TextComponent(j + 1 + ": ");
            factionNumber.setColor(ChatColor.GOLD);
            factionNumber.setBold(true);
            TextComponent factionInfo = new TextComponent(list.get(j).factionName + " - " + list.get(j).players.size());
            factionInfo.setBold(false);

            componentBuilder.append(factionNumber);
            componentBuilder.append(factionInfo);
            componentBuilder.append(TextUtil.newLine);
        }

        player.spigot().sendMessage(componentBuilder.create());
        player.spigot().sendMessage(footer.create());
    }

    private void command_Help(String error) {
        TextComponent errorMessage = new TextComponent(error);
        errorMessage.setColor(ChatColor.RED);
        TextComponent helpText1 = new TextComponent("\n\nKlik ");
        TextComponent helpClick = new TextComponent("hier");
        helpClick.setBold(true);
        helpClick.setUnderlined(true);
        TextComponent helpText2 = new TextComponent(" voor alle commands.");
        helpText2.setBold(false);
        helpText2.setUnderlined(false);

        helpClick.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/factions help 1"));
        helpClick.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Alle commands")));

        BaseComponent[] components = new ComponentBuilder()
                .append(errorMessage)
                .append(helpText1)
                .append(helpClick)
                .append(helpText2)
                .event((ClickEvent) null)
                .event((HoverEvent) null)
                .create();

        player.spigot().sendMessage(components);
    }

    private void command_Help(int page) {
        TextComponent helpText = new TextComponent("Deze command moet nog gemaakt worden :O");
        helpText.setBold(false);
        helpText.setColor(ChatColor.GREEN);

        BaseComponent[] components = new ComponentBuilder()
                .append(TextUtil.GenerateHeaderMsg("Help").create())
                .append(TextUtil.newLine)
                .append(helpText)
                .append(TextUtil.newLine)
                .append(TextUtil.GenerateFooterMsg().create()).create();

        player.spigot().sendMessage(components);
    }
}
