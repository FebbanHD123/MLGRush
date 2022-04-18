package de.febanhd.mlgrush.map.template;

import com.google.common.collect.Lists;
import de.febanhd.mlgrush.MLGRush;
import de.febanhd.mlgrush.map.Map;
import de.febanhd.mlgrush.map.MapPaster;
import de.febanhd.mlgrush.nms.NMSUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class MapCreator {

    public static int DISTANCE = 150;
    public static final int START_X = 50;

    private final String actionbarGenerateInfo = MLGRush.getString("actionbar.loadmap");
    private final int maxCachedMapsAmount = MLGRush.getInstance().getConfig().getInt("map_generation.cached_maps.max", 6);
    private final int minCachedMapsAmount = MLGRush.getInstance().getConfig().getInt("map_generation.cached_maps.min", 3);

    private final CopyOnWriteArrayList<Map> loadedMaps = Lists.newCopyOnWriteArrayList();
    private final List<MapRequest> requests = Lists.newArrayList();
    private final List<MapWorldSlot> worldSlots = Lists.newArrayList();
    private final MapTemplate mapTemplate;
    private final AtomicInteger mapCreates = new AtomicInteger();

    public MapCreator(MapTemplate mapTemplate) {
        this.mapTemplate = mapTemplate;
        Bukkit.getScheduler().scheduleSyncRepeatingTask(MLGRush.getInstance(), this::tick, 20, 10);
    }

    public void requestMap(Player player1, Player player2, Consumer<Map> callback) {
        this.requests.add(new MapRequest(player1, player2, callback));
    }

    private void tick() {
        this.requests.forEach(mapRequest -> {
            Player player1 = mapRequest.getPlayer1(), player2 = mapRequest.getPlayer2();
            Map freeMap = getFreeMap();
            //return cached map
            if(freeMap != null) {
                freeMap.setIngame(player1, player2);
                mapRequest.getConsumer().accept(freeMap);
                return;
            }

            //generate new map
            AtomicInteger taskID = new AtomicInteger();
            final MapPaster paster = this.generateMap(map -> {
                this.loadedMaps.add(map);
                Bukkit.getScheduler().cancelTask(taskID.get());
                if(!player1.isOnline() || !player2.isOnline()) return;
                map.setIngame(player1, player2);
                mapRequest.getConsumer().accept(map);
            });


            //displays action bar and cancel joining if a player leaves during the paste process
            taskID.set(Bukkit.getScheduler().scheduleSyncRepeatingTask(MLGRush.getInstance(), () -> {
                String actionBarString = actionbarGenerateInfo.replaceAll("%percent%", paster.getProgressPercent() + "%");
                if(player1.isOnline())
                    NMSUtil.sendActionbar(player1, actionBarString);
                if(player2.isOnline())
                    NMSUtil.sendActionbar(player2, actionBarString);

                if(!player1.isOnline() || !player2.isOnline()) {
                    Bukkit.getScheduler().cancelTask(taskID.get());
                    if(player1.isOnline()) {
                        player1.sendMessage(MLGRush.getMessage("messages.map_creation.cancel"));
                    }else {
                        player2.sendMessage(MLGRush.getMessage("messages.map_creation.cancel"));
                    }
                }
            }, 1, 20));
        });
        this.requests.clear();

        //delete maps when there are too many free ones
        //and create some when there are not enough
        AtomicInteger freeMapCount = new AtomicInteger();
        this.loadedMaps.stream()
                .filter(map -> map.getState() == Map.State.FREE)
                .forEach(map -> {
                    if(freeMapCount.get() >= this.maxCachedMapsAmount) {
                        this.loadedMaps.remove(map);
                        map.deleteAsync(null);
                        this.worldSlots.removeIf(mapWorldSlot -> mapWorldSlot.getX() == map.getX());
                        return;
                    }
                    freeMapCount.incrementAndGet();
                });
        if((freeMapCount.get() - this.mapCreates.get()) < this.minCachedMapsAmount) {
            this.mapCreates.incrementAndGet();
            this.generateMap(map -> {
                this.loadedMaps.add(map);
                this.mapCreates.decrementAndGet();
            });
        }
    }

    private Map getFreeMap() {
        return this.loadedMaps.stream().filter(map -> map.getState() == Map.State.FREE).findFirst().orElse(null);
    }

    private MapPaster generateMap(Consumer<Map> map) {
        MapWorldSlot slot = this.getFreeWorldSlot();
        slot.setOccupied();
        return paste(this.mapTemplate.getWorld(), slot.getX(), map);
    }

    private MapPaster paste(World world, int x, Consumer<Map> map) {
        MapPaster paster = new MapPaster(this.mapTemplate, world, x);
        paster.paste(map);
        return paster;
    }

    public MapWorldSlot getFreeWorldSlot() {
        return this.worldSlots.stream()
                .filter(MapWorldSlot::isFree)
                .findFirst()
                .orElseGet(() -> {
                    int x;
                    if(this.worldSlots.isEmpty())
                        x = START_X;
                    else
                        x = START_X + this.worldSlots.size() * DISTANCE;
                   MapWorldSlot slot = new MapWorldSlot(x);
                   this.worldSlots.add(slot);
                   return slot;
                });
    }

}
