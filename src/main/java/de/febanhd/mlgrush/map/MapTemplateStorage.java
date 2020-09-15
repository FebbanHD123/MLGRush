package de.febanhd.mlgrush.map;

import com.google.common.collect.Lists;
import de.febanhd.mlgrush.MLGRush;
import de.febanhd.mlgrush.map.elements.BedObject;
import de.febanhd.mlgrush.util.Cuboid;
import de.febanhd.mlgrush.util.LocationUtil;
import org.bukkit.Location;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Objects;

public class MapTemplateStorage {

    private final File dir = new File(MLGRush.getInstance().getDataFolder().getPath() + "/maps");

    public MapTemplateStorage() {

        if(!MLGRush.getInstance().getDataFolder().exists()) {
            MLGRush.getInstance().getDataFolder().mkdir();
        }

        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    public void saveInFile(MapTemplate template, File file) {
        try {

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream out = new BukkitObjectOutputStream(byteArrayOutputStream);

            out.writeObject(template.getName());

            out.writeObject(LocationUtil.locationToString(template.getRegion().getLowerLocation()));
            out.writeObject(LocationUtil.locationToString(template.getRegion().getUpperLocation()));

            out.writeObject(template.getBedObjects()[0].toString());
            out.writeObject(template.getBedObjects()[1].toString());

            out.writeObject(LocationUtil.locationToString(template.getSpawnLocation()[0]));
            out.writeObject(LocationUtil.locationToString(template.getSpawnLocation()[1]));

            out.writeObject(LocationUtil.locationToString(template.getDeathLocation()));
            out.writeObject(LocationUtil.locationToString(template.getMaxBuildLocation()));

            String str = Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());


            FileWriter fw = new FileWriter(file);
            fw.write(str);
            fw.flush();
            fw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MapTemplate loadFromFile(File file) throws IOException, ClassNotFoundException {
        if (!file.exists())
            file.createNewFile();

        String str = new String(Files.readAllBytes(file.toPath()));

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(Base64.getDecoder().decode(str));
        BukkitObjectInputStream in = new BukkitObjectInputStream(byteArrayInputStream);

        String mapName = (String) in.readObject();

        String loc1Str = (String) in.readObject();
        String loc2Str = (String) in.readObject();
        Location loc1 = LocationUtil.locationFromString(loc1Str);
        Location loc2 = LocationUtil.locationFromString(loc2Str);

        BedObject bed1 = BedObject.fromString((String)in.readObject());
        BedObject bed2 = BedObject.fromString((String)in.readObject());

        Location spawnLocation1 = LocationUtil.locationFromString((String)in.readObject());
        Location spawnLocation2 = LocationUtil.locationFromString((String)in.readObject());

        Location deathLocation = LocationUtil.locationFromString((String)in.readObject());
        Location maxBuildLocationn = LocationUtil.locationFromString((String)in.readObject());

        return new MapTemplate(mapName, new Cuboid(loc1, loc2), spawnLocation1, spawnLocation2, bed1, bed2, deathLocation, maxBuildLocationn);
    }

    public void saveAllTemplates() {
        MLGRush.getInstance().getMapManager().getTemplates().forEach(template -> this.saveInFile(template, this.getFileFromTemplate(template)));
    }

    public ArrayList<MapTemplate> loadAllTemplates() {
        ArrayList<MapTemplate> templates = Lists.newArrayList();
        for (File file : Objects.requireNonNull(this.dir.listFiles())) {
            try {
                templates.add(this.loadFromFile(file));
            }catch (Exception e) {
                e.printStackTrace();
                System.out.println("[A-MLGRush] Error while loading map: " + file.getName());
            }
        }
        return templates;
    }

    public File getFileFromTemplate(MapTemplate template) {
        return new File(this.dir, template.getName().toLowerCase() + ".map");
    }
}
