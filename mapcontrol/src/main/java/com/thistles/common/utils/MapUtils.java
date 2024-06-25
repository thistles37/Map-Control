package com.thistles.common.utils;

import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import io.github.bananapuncher714.nbteditor.NBTEditor;
import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class MapUtils {

    private static final HashMap<Player, Stack<HashMap<Integer, CompoundTag>>> mapActions = new HashMap<>();

    public static boolean isMap(ItemStack item) {
        return item.getType() == Material.FILLED_MAP;
    }

    public static int getMapId(ItemStack map) {
        String version = getServerVersion();
        if (getVersionInteger(version) < getVersionInteger("1_20_R4")) {
            return NBTEditor.getInt(map, "map");
        }
        ReadWriteNBT nbt = NBT.itemStackToNBT(map);
        NBTCompound components = (NBTCompound) nbt.getCompound("components");
        return components.getInteger("minecraft:map_id");
    }

    public static String getServerVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
    }

    public static int getVersionInteger(String version) {
        String numericVersion = version.replaceAll("\\D", "").substring(1);
        return Integer.parseInt(numericVersion);
    }

    public static String getFileName(int id) {
        return "map_" + id + ".dat";
    }

    public static File getMapData(World world, int id) throws IOException {
        return new File(world.getWorldFolder(), "data/" + getFileName(id));
    }

    public static NamedTag readMapData(File mapFile) throws IOException {
        return NBTUtil.read(mapFile);
    }

    public static void saveMapData(World world, NamedTag namedTag, int id) throws IOException {
        String worldPath = getWorldPath(world);
        NBTUtil.write(namedTag, worldPath + "/data/" + getFileName(id));
    }

    public static CompoundTag getMapCompound(NamedTag namedTag) {
        return (CompoundTag) ((CompoundTag) namedTag.getTag()).get("data");
    }

    public static String getWorldPath(World world) {
        return world.getWorldFolder().getPath();
    }

    public static void clearMap(NamedTag namedTag) throws IOException {
        CompoundTag c = getMapCompound(namedTag);
        String key = "colors";
        byte[] value = new byte[16384];
        c.putByteArray(key, value);
    }

    public static void unlockMap(NamedTag namedTag) throws IOException {
        CompoundTag c = getMapCompound(namedTag);
        String key = "locked";
        byte value = 0B000000;
        c.putByte(key, value);
    }

    public static Location getMapCoordinates(World world, CompoundTag c) {
        int x = c.getInt("xCenter");
        int z = c.getInt("zCenter");
        return new Location(world, x, 200, z, 1, 1);
    }

    public static void tpToMap(Player p, Location coordinates) {
        p.teleport(coordinates);
    }

    public static ItemStack getMap(World world, Player p) {
        Vector lineOfSight = p.getEyeLocation().getDirection().multiply(0.1);
        Location vCoordinates = p.getEyeLocation();
        for (int i = 0; i < 50; i++) {
            vCoordinates.add(lineOfSight);
            List<Entity> entities = (List<Entity>) world.getNearbyEntities(vCoordinates, 0.1, 0.1, 0.1);
            if (!entities.isEmpty()) {
                Entity entity = entities.get(0);
                if (entity instanceof ItemFrame) {
                    ItemStack itemFrame = ((ItemFrame) entity).getItem();
                    if (itemFrame.getType() == Material.FILLED_MAP) {
                        return itemFrame;
                    }
                }
            }
        }
        return null;
    }

    public static void storeAction(Player p, NamedTag namedTag, int id) {
        if (!mapActions.containsKey(p)) {
            mapActions.put(p, new Stack<>());
        }
        HashMap<Integer, CompoundTag> action = new HashMap<>();
        CompoundTag c = (CompoundTag) namedTag.getTag();
        action.put(id, c.clone());
        mapActions.get(p).push(action);
    }

    public static boolean actionExists(Player p) {
        if (mapActions.containsKey(p)) {
            return !mapActions.get(p).isEmpty();
        }
        return false;
    }

    public static HashMap<Integer, CompoundTag> retrieveAction(Player p) {
        return mapActions.get(p).pop();
    }

    public static void undoAction(World world, NamedTag namedTag, HashMap<Integer, CompoundTag> action) throws IOException {
        int key = action.keySet().iterator().next();
        namedTag.setTag(action.get(key));
        saveMapData(world, namedTag, key);
    }
}
