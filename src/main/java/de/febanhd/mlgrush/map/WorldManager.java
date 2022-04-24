package de.febanhd.mlgrush.map;

import com.google.common.collect.Lists;
import de.febanhd.mlgrush.MLGRush;
import de.febanhd.mlgrush.map.template.MapTemplate;
import de.febanhd.mlgrush.map.template.MapWorldSlot;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class WorldManager {

    public static int DISTANCE = 250;
    public static final int START_X = 50;

    private final int maxPastes = MLGRush.getInstance().getConfig().getInt("map_generation.max_pastes");
    private final AtomicInteger creationAmount = new AtomicInteger(0);
    private final List<Runnable> QUEUE = Lists.newCopyOnWriteArrayList();
    @Getter
    private final World world;
    private final List<MapWorldSlot> worldSlots = Lists.newArrayList();

    public WorldManager(World world) {
        this.world = world;
        Bukkit.getScheduler().scheduleSyncRepeatingTask(MLGRush.getInstance(), this::tick, 20, 15);
    }

    private void tick() {
        if(canCreateMap() && !QUEUE.isEmpty()) {
            this.QUEUE.get(0).run();
            this.QUEUE.remove(0);
        }
    }

    public void createMap(MapTemplate mapTemplate, Consumer<MapPaster> pasterConsumer, Consumer<Map> mapConsumer) {
        QUEUE.add(() -> {
            this.creationAmount.incrementAndGet();
            pasterConsumer.accept(generateMap(mapTemplate, map -> {
                this.creationAmount.decrementAndGet();
                mapConsumer.accept(map);
            }));
        });
    }

    private MapPaster generateMap(MapTemplate mapTemplate, Consumer<Map> map) {
        MapWorldSlot slot = this.getFreeWorldSlot();
        slot.setOccupied();
        MapPaster paster = new MapPaster(mapTemplate, this.world, slot.getX());
        paster.paste(map);
        return paster;
    }

    private boolean canCreateMap() {
        return creationAmount.get() < this.maxPastes;
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

    public void deleteMap(Map map) {
        map.deleteAsync(() -> this.worldSlots.removeIf(mapWorldSlot -> mapWorldSlot.getX() == map.getX()));
    }

}
