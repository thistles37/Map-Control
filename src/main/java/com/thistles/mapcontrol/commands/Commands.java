package com.thistles.mapcontrol.commands;


import io.github.bananapuncher714.nbteditor.NBTEditor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.io.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Commands implements CommandExecutor {
    public Commands() {
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players may execute this command!");
            return true;
        } else {
            Player p = (Player)sender;
            if (p.hasPermission("mapcontrol.use")) {
                if (args.length == 0) {
                    p.sendMessage("Please provide a command-line argument.");
                    return true;
                }
                CraftWorld world = (CraftWorld) Bukkit.getWorlds().get(0);

                if (args[0].equalsIgnoreCase("get")) {
                    ItemStack map = getMap(world, p);
                    if (map != null) {
                        p.getInventory().addItem(map);
                    } else {
                        p.sendMessage("No maps found");
                    }
                    return true;
                }

                ItemStack heldItem = p.getInventory().getItemInMainHand();
                int id = getMapId(heldItem);

                NamedTag mapNamedTag;
                File mapFile;

                try {
                    mapFile = getMapData(world, id);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                if (!isMap(heldItem)) {
                    p.sendMessage("Please hold a map");
                    return true;
                }

                if (!mapFile.exists()) {
                    p.sendMessage("Map file does not exist. Wait for world auto-save and try again.");
                    return true;
                }

                if (args[0].equalsIgnoreCase("clear")) {
                    try {
                        mapNamedTag = readMapData(mapFile);
                        clearMap(mapNamedTag);
                        saveMapData(world, mapNamedTag, id);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    removeCache(world, id);
                    System.out.println("Map with id " + id + " has been cleared by " + p.getName());
                    return true;
                }
                if (args[0].equalsIgnoreCase("unlock")) {
                    try {
                        mapNamedTag = readMapData(mapFile);
                        unlockMap(mapNamedTag);
                        saveMapData(world, mapNamedTag, id);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    removeCache(world, id);
                    System.out.println("Map with id " + id + " has been unlocked by " + p.getName());
                    return true;
                }
                if (args[0].equalsIgnoreCase("tp")) {
                    try {
                        mapNamedTag = readMapData(mapFile);
                        Location coordinates = getMapCoordinates(world, mapNamedTag);
                        tpToMap(p, coordinates);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    System.out.println(p.getName() + " has teleported to the creation coordinates of the map with id " + id);
                    return true;
                }

                return true;
            } else {
                p.sendMessage("You do not have permission to execute this command!");
                return false;
            }
        }
    }

    public boolean isMap(ItemStack item) {
        return item.getType() == Material.MAP;
    }

    public int getMapId(ItemStack map) {
        return NBTEditor.getInt(map, "map");
    }

    public String getFileName(int id) {
        return "map_" + id + ".dat";
    }

    public File getMapData(CraftWorld world, int id) throws IOException {
        return new File(world.getWorldFolder(), "data/" + getFileName(id));
    }

    public NamedTag readMapData(File mapFile) throws IOException {
        return NBTUtil.read(mapFile);
    }

    public CompoundTag getMapCompound(NamedTag namedTag) {
        return (CompoundTag) ((CompoundTag) namedTag.getTag()).get("data");
    }

    public void clearMap(NamedTag namedTag) throws IOException {
        CompoundTag c = getMapCompound(namedTag);
        String key = "colors";
        byte[] value = new byte[16384];
        c.putByteArray(key, value);
    }

    public void unlockMap(NamedTag namedTag) throws IOException {
        CompoundTag c = getMapCompound(namedTag);
        String key = "locked";
        byte value = 0B000000;
        c.putByte(key, value);
    }

    public Location getMapCoordinates(CraftWorld world, NamedTag namedTag) {
        CompoundTag c = getMapCompound(namedTag);
        int x = c.getInt("xCenter");
        int z = c.getInt("zCenter");
        return new Location(world, x, 200, z, 1, 1);
    }

    public void tpToMap(Player p, Location coordinates) {
        p.teleport(coordinates);
    }

    public ItemStack getMap(CraftWorld world, Player p) {
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

    public void saveMapData(CraftWorld world, NamedTag namedTag, int id) throws IOException {
        String worldPath = world.getWorldFolder().getPath();
        NBTUtil.write(namedTag, worldPath + "/data/" + getFileName(id));
    }

    public void removeCache(CraftWorld world, int id) {
        ServerLevel server = world.getHandle();
        HashMap<String, SavedData> cache = (HashMap<String, SavedData>) server.getDataStorage().cache;
        cache.remove("map_" + id);
    }
}
