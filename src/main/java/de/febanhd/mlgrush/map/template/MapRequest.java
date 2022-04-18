package de.febanhd.mlgrush.map.template;

import de.febanhd.mlgrush.map.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

@AllArgsConstructor
@Getter
public class MapRequest {

    private final Player player1, player2;
    private final Consumer<Map> consumer;

}
