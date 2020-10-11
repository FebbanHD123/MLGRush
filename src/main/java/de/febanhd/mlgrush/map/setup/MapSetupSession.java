package de.febanhd.mlgrush.map.setup;

import com.google.common.collect.Lists;
import de.febanhd.mlgrush.MLGRush;
import de.febanhd.mlgrush.map.MapTemplate;
import de.febanhd.mlgrush.map.elements.BedObject;
import de.febanhd.mlgrush.util.Cuboid;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;

public class MapSetupSession implements Listener {

    public static final ArrayList<Player> PLAYERS = Lists.newArrayList();

    public static boolean isInSetup(Player player) {
        return MapSetupSession.PLAYERS.contains(player);
    }

    private Player player;
    private int currentStep;
    private boolean running;

    private Location[] regionLocations;
    private String mapName;

    private Location spawnLocation1, spawnLocation2;
    private Location deathLocation, maxBuildLocation;

    private final Location[] bedLocations1 = new Location[2], bedLocations2 = new Location[2];


    public MapSetupSession(Player player) {
        MapSetupSession.PLAYERS.add(player);
        this.player = player;
        this.currentStep = 0;
        this.running = true;
        this.regionLocations = new Location[2];
        Bukkit.getPluginManager().registerEvents(this, MLGRush.getInstance());
        this.nextStep();
    }

    private void nextStep() {
        this.currentStep++;
        switch(this.currentStep) {

            case 1: case 2:
                player.sendMessage(MLGRush.PREFIX + "§aGehe zur " + this.currentStep + "ten Region-Location uns benutze '§cgesetzt§a'.");
                break;
            case 3:
                player.sendMessage(MLGRush.PREFIX + "§aGehe zum Spawn vom Spieler §c1 §aund benutze '§cgesetzt§a'.");
                break;
            case 4:
                player.sendMessage(MLGRush.PREFIX + "§aKlicke auf den unteren Teil des Bettes von Spieler §c1§a.");
                break;
            case 5:
                player.sendMessage(MLGRush.PREFIX + "§aKlicke auf den oberen Teil des Bettes von Spieler §c1§a.");
                break;
            case 6:
                player.sendMessage(MLGRush.PREFIX + "§aGehe zum Spawn vom Spieler §c2 §aund benutze '§cgesetzt§a'.");
                break;
            case 7:
                player.sendMessage(MLGRush.PREFIX + "§aKlicke auf den unteren Teil des Bettes von Spieler §c2§a.");
                break;
            case 8:
                player.sendMessage(MLGRush.PREFIX + "§aKlicke auf den oberen Teil des Bettes von Spieler §c2§a.");
                break;
            case 9:
                player.sendMessage(MLGRush.PREFIX + "§aGehe nun zur maximalen Bauhöhe der Map und benutze '§cgesetzt§a'.");
                break;
            case 10:
                player.sendMessage(MLGRush.PREFIX + "§aGehe nun zu der Todeshöhe der Map und benutze '§cgesetzt§a'.");
                break;
            case 11:
                player.sendMessage(MLGRush.PREFIX + "§aGebe nun den Namen der Map an.");
                break;
            case 12:
                this.finish();
                break;
        }
    }

    private void finish() {
        this.running = false;
        Bukkit.getScheduler().scheduleSyncDelayedTask(MLGRush.getInstance(), () -> {

            HandlerList.unregisterAll(this); // unregister Listener

            Cuboid region = new Cuboid(this.regionLocations[0], this.regionLocations[1]);

            player.sendMessage(MLGRush.PREFIX + "§7Genieriere Welt...");

            MapTemplate template = new MapTemplate(this.mapName, region, spawnLocation1, spawnLocation2,
                    new BedObject(bedLocations1[0], bedLocations1[1]), new BedObject(bedLocations2[0], bedLocations2[1]), this.deathLocation, this.maxBuildLocation);

            player.sendMessage(MLGRush.PREFIX + "§aWelt geladen!");

            MLGRush.getInstance().getMapManager().addMapTemplate(template);

            player.sendMessage(MLGRush.PREFIX + "§aSetup beendet.");
            player.sendMessage(MLGRush.PREFIX + "§4Achtung: §cBitte beachte, dass du dieses Template also die Blöcke, die du soeben als Region angegeben hast, nicht ändern darfst.");

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

                    switch(this.currentStep) {

                        case 1: case 2:
                            this.regionLocations[this.currentStep - 1] = player.getLocation();
                            player.sendMessage(MLGRush.PREFIX + "§2Location gesetzt!");
                            this.nextStep();
                            break;
                        case 3:
                            this.spawnLocation1 = player.getLocation().clone().add(0, 0.25, 0);
                            player.sendMessage(MLGRush.PREFIX + "§2Location gesetzt!");
                            this.nextStep();
                            break;
                        case 6:
                            this.spawnLocation2 = player.getLocation().clone().add(0, 0.25, 0);
                            player.sendMessage(MLGRush.PREFIX + "§2Location gesetzt!");
                            this.nextStep();
                            break;
                        case 9:
                            this.maxBuildLocation = player.getLocation();
                            this.nextStep();
                            break;
                        case 10:
                            this.deathLocation = player.getLocation();
                            this.nextStep();
                            break;
                    }

                }else if(this.currentStep == 11) {
                    this.mapName = event.getMessage();
                    this.nextStep();
                }
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
}
