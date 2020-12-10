package de.febanhd.mlgrush.updatechecker;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class UpdateChecker {

    private final Executor executor;
    private final int resourceID;
    private Plugin plugin;

    private String version;

    public UpdateChecker(JavaPlugin plugin, Executor executor, int resourceID) {
        this.executor = executor;
        this.plugin = plugin;
        this.resourceID = resourceID;
    }

    public void getVersion(final Consumer<String> consumer) {
        executor.execute(() -> {
            try {
                InputStream stream = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + this.resourceID).openStream();
                Scanner scanner = new Scanner(stream);
                if(scanner.hasNext()) {
                    this.version = scanner.next();
                    consumer.accept(this.version);
                }
            }catch (IOException e) {
                this.plugin.getLogger().info("Cannot look for updates: " + e.getMessage());
            }
        });
    }

    public String getCachedVersion() {
        return this.version;
    }
}
