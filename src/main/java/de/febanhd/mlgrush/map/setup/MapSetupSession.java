package de.febanhd.mlgrush.map.setup;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.febanhd.mlgrush.MLGRush;
import de.febanhd.mlgrush.map.MapTemplate;
import de.febanhd.mlgrush.map.elements.BedObject;
import de.febanhd.mlgrush.util.Cuboid;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.util.BlockIterator;

import java.util.ArrayList;
import java.util.HashMap;

public class MapSetupSession  {

    public static final HashMap<Player, MapSetupSession> PLAYERS = Maps.newHashMap();

    public static boolean isInSetup(Player player) {
        return MapSetupSession.PLAYERS.containsKey(player);
    }

    private Player player;
    private int currentStep;
    private boolean running;

    private Location[] regionLocations;
    private String mapName;

    private Location spawnLocation1, spawnLocation2;
    private Location deathLocation, maxBuildLocation;

    private final boolean english;

    private final Location[] bedLocations1 = new Location[2], bedLocations2 = new Location[2];


    public MapSetupSession(Player player, boolean english) {
        MapSetupSession.PLAYERS.put(player, this);
        this.player = player;
        this.currentStep = 0;
        this.running = true;
        this.regionLocations = new Location[2];
        this.english = english;
        this.nextStep();
    }

    private void nextStep() {
        if(currentStep != 0) {
            if(!english) {
                player.sendMessage(MLGRush.PREFIX + "§fNächster schritt");
            }else
                player.sendMessage(MLGRush.PREFIX + "§fNext step");
        }
        this.currentStep++;
        switch(this.currentStep) {

            case 1: case 2:
                if(!english)
                    player.sendMessage(MLGRush.PREFIX + "§7Gehe zur Region-Location: " + this.currentStep + ", und benutze §e'/mlgrush setupmap set'");
                else
                    player.sendMessage(MLGRush.PREFIX + "§7Go to region location: " + this.currentStep + ", and use §e'/mlgrush setupmap set'.");
                break;
            case 3:
                if(!english)
                    player.sendMessage(MLGRush.PREFIX + "§7Gehe zum Spawn vom Spieler §e1 §7und benutze §e'/mlgrush setupmap set'.");
                else
                    player.sendMessage(MLGRush.PREFIX + "§7Go to spawn from player §e1 §7and use §e'/mlgrush setupmap set'.");
                break;
            case 4:
                if(!english)
                    player.sendMessage(MLGRush.PREFIX + "§7Stelle dich auf den vorderen Teil des Bettes von Spieler §e1 §7und schaue den hinteren an. Benutzte dann §e'/mlgrush setupmap set'.");
                else
                    player.sendMessage(MLGRush.PREFIX + "§7Stand on the front part of the bed of player §e1 §7and look at the back part. Then use §e'/mlgrush setupmap set'.");
                break;
            case 5:
                if(!english)
                    player.sendMessage(MLGRush.PREFIX + "§7Gehe zum Spawn vom Spieler §e2 §7und benutze §e'/mlgrush setupmap set'.");
                else
                    player.sendMessage(MLGRush.PREFIX + "§7Go to spawn from player §e2 §7and use §e'/mlgrush setupmap set'.");
                break;
            case 6:
                if(!english)
                    player.sendMessage(MLGRush.PREFIX + "§7Stelle dich auf den vorderen Teil des Bettes von Spieler §e2 §7und schaue den hinteren an. Benutzte dann §e'/mlgrush setupmap set'.");
                else
                    player.sendMessage(MLGRush.PREFIX + "§7Stand on the front part of the bed of player §e2 §7and look at the back part. Then use §e'/mlgrush setupmap set'.");
                break;
            case 7:
                if(!english)
                    player.sendMessage(MLGRush.PREFIX + "§7Gehe zur maximalen Bauhöhe der Map und benutze §e'/mlgrush setupmap set'.");
                else
                    player.sendMessage(MLGRush.PREFIX + "§7Go to the maximum build height of the map and use §e'/mlgrush setupmap set'.");
                break;
            case 8:
                if(!english)
                    player.sendMessage(MLGRush.PREFIX + "§7Gehe nun zu der Todeshöhe der Map und benutze §e'/mlgrush setupmap set'.");
                else
                    player.sendMessage(MLGRush.PREFIX + "§7Now go to the death height of the map and use §e'/mlgrush setupmap set'.");
                break;
            case 9:
                if(!english)
                    player.sendMessage(MLGRush.PREFIX + "§7Gebe nun den Namen der Map an, indem du folgenden Befehl benutzt: §e/mlgrush setupmap setname <NAME>§7. Anstatt \"<NAME>\" gibst du den Namen an.");
                else
                    player.sendMessage(MLGRush.PREFIX + "§7Now specify the name of the map using the following command: §e/mlgrush setupmap setname <NAME>§7. Instead of \"<NAME>\" specify the name.");
                break;
            case 10:
                this.finish();
                break;
        }
    }

    private void finish() {
        this.running = false;
        Bukkit.getScheduler().scheduleSyncDelayedTask(MLGRush.getInstance(), () -> {

            if(regionLocations[0].getY() > regionLocations[1].getY()) {
                regionLocations[0].add(0, 255, 0);
            }else {
                regionLocations[1].add(0, 255, 0);
            }

            Cuboid region = new Cuboid(this.regionLocations[0], this.regionLocations[1]);

            player.sendMessage(MLGRush.PREFIX + "§7Generate World...");

            MapTemplate template = new MapTemplate(this.mapName, region, spawnLocation1, spawnLocation2,
                    new BedObject(bedLocations1[0], bedLocations1[1]), new BedObject(bedLocations2[0], bedLocations2[1]), this.deathLocation, this.maxBuildLocation);

            player.sendMessage(MLGRush.PREFIX + "§aWorld generated");

            MLGRush.getInstance().getMapManager().addMapTemplate(template);

            if(!english) {
                player.sendMessage(MLGRush.PREFIX + "§aSetup beendet.");
                player.sendMessage(MLGRush.PREFIX + "§4Achtung: §cBitte beachte, dass du dieses Template also die Blöcke, die du soeben als Region angegeben hast, nicht ändern darfst.");
            }else {
                player.sendMessage(MLGRush.PREFIX + "§aSetup finished.");
                player.sendMessage(MLGRush.PREFIX + "§4Attention: §cPlease note that you are not allowed to change this template, i.e. the blocks you just specified as region.");
            }

            MapSetupSession.PLAYERS.remove(player);
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAsyncChat(AsyncPlayerChatEvent event) {
            if(!running) return;
            Player player = event.getPlayer();
            if(player.getUniqueId().equals(this.player.getUniqueId())) {
                event.setCancelled(true);
                if(event.getMessage().equals("gesetzt")) {



                }else if(this.currentStep == 11) {
                    this.mapName = event.getMessage();
                    this.nextStep();
                }
            }
    }

    public void setName(String name) {
        if(this.currentStep == 9) {
            this.mapName = name;
            this.nextStep();
        }else {
            if(!english)
                player.sendMessage(MLGRush.PREFIX + "§cNicht verfügbar");
            else
                player.sendMessage(MLGRush.PREFIX + "§cNot available");
        }
    }

    public void doSet() {
        switch(this.currentStep) {

            case 1: case 2:
                this.regionLocations[this.currentStep - 1] = player.getLocation();
                this.nextStep();
                break;
            case 3:
                this.spawnLocation1 = player.getLocation().clone().add(0, 0.25, 0);
                this.nextStep();
                break;
            case 4:
                this.bedLocations1[0] = player.getLocation().getBlock().getLocation();
                this.bedLocations1[1] = this.getTargetBlock(player, 5).getLocation();
                this.nextStep();
                break;
            case 5:
                this.spawnLocation2 = player.getLocation().clone().add(0, 0.25, 0);
                this.nextStep();
                break;
            case 6:
                this.bedLocations2[0] = player.getLocation().getBlock().getLocation();
                this.bedLocations2[1] = this.getTargetBlock(player, 5).getLocation();
                this.nextStep();
                break;
            case 7:
                this.maxBuildLocation = player.getLocation();
                this.nextStep();
                break;
            case 8:
                this.deathLocation = player.getLocation();
                this.nextStep();
                break;
            default:
                if(!english)
                    player.sendMessage(MLGRush.PREFIX + "§cNicht verfügbar");
                else
                    player.sendMessage(MLGRush.PREFIX + "§cNot available");
                break;
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if(player.getUniqueId().equals(this.player.getUniqueId())) {
            if(this.running) {
                event.setCancelled(true);
                switch (this.currentStep) {
                    case 4:
                        this.bedLocations1[0] = event.getBlock().getLocation();
                        player.sendMessage(MLGRush.PREFIX + "§2Bed-Location gesetzt!");
                        this.nextStep();
                        break;
                    case 5:
                        this.bedLocations1[1] = event.getBlock().getLocation();
                        player.sendMessage(MLGRush.PREFIX + "§2Bed-Location gesetzt!");
                        this.nextStep();
                        break;
                    case 7:
                        this.bedLocations2[0] = event.getBlock().getLocation();
                        player.sendMessage(MLGRush.PREFIX + "§2Bed-Location gesetzt!");
                        this.nextStep();
                        break;
                    case 8:
                        this.bedLocations2[1] = event.getBlock().getLocation();
                        player.sendMessage(MLGRush.PREFIX + "§2Bed-Location gesetzt!");
                        this.nextStep();
                        break;
                }
            }
        }
    }

    private Block getTargetBlock(Player player, int range) {
        BlockIterator iter = new BlockIterator(player, range);
        Block lastBlock = iter.next();
        while (iter.hasNext()) {
            lastBlock = iter.next();
            if (lastBlock.getType() == Material.AIR) {
                continue;
            }
            break;
        }
        return lastBlock;
    }
}
