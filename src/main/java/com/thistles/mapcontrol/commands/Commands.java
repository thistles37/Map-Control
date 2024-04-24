package com.thistles.mapcontrol.commands;


import io.github.bananapuncher714.nbteditor.NBTEditor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.io.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                // Add console logs of maps deleted and user involved
                // check if given argument is contained in the set of options
                // The current implementation will give you an unknown
                // map if the map was not saved to disk yet. So we can try
                // to create a new hashmap, get the keys and values we want from the current
                // cache, and save that to the data folder.

                // Add an undo functionality
                // and /map undo all

                CraftWorld world = (CraftWorld) Bukkit.getWorlds().get(0);
                String worldPath = world.getWorldFolder().getPath();

                int id = getMapId(p);
                String fileName = getFileName(id);
                NamedTag mapNamedTag;
                File mapFile;
                try {
                    mapFile = getMapData(world, id);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                if (!mapFile.exists()) {
                    p.sendMessage("Map file does not exist. Wait for world auto-save and try again.");
                    // Could add exception for a forced cache clearing (add command argument)
                    // If we add force clearing, maybe we could instead deliberately clear
                    // the map from disk as well to make it unusable. Obviously only use it
                    // when absolutely necessary.
                    // But moderators could ban anyone who abuses map art, so maybe not necessary
                    return true;
                }

                if (args[0].equalsIgnoreCase("clear")) {
                    if (mapFile.exists()) {
                        try {
                            mapNamedTag = readMapData(mapFile);
                            clearMap(mapNamedTag, world, id);

                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    removeCache(world, id);
                    return true;
                }
                if (args[0].equalsIgnoreCase("unlock")) {
                    if (mapFile.exists()) {
                        try {
                            mapNamedTag = readMapData(mapFile);
                            unlockMap(mapNamedTag, world, id);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    removeCache(world, id);
                    return true;
                }
                if (args[0].equalsIgnoreCase("tp")) {
                    if (mapFile.exists()) {
                        try {
                            mapNamedTag = readMapData(mapFile);
                            Location coordinates = getMapCoordinates(world, mapNamedTag);
                            tpToMap(p, coordinates);
                            return true;
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    // Could use NMS' save() function to save all cache to data
                    p.sendMessage("Could not teleport, save the world first!");
                    return true;
                }
                if (args[0].equalsIgnoreCase("get")) {
                    ItemStack map = getMap(world, p);
                    if (map != null) {
                        p.getInventory().addItem(map);
                    } else {
                        p.sendMessage("No maps found");
                    }
                    return true;

                    // Keep adding to this vector until you hit an entity. Give up after like 5 blocks
                    // Vector is already normalized
                }
                return true;
            } else {
                p.sendMessage("You do not have permission to execute this command!");
                return false;
            }
        }
    }

    public int getMapId(Player p) {
        ItemStack map = p.getInventory().getItemInMainHand();
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

    public void clearMap(NamedTag namedTag, CraftWorld world, int id) throws IOException {
        CompoundTag c = getMapCompound(namedTag);
        String key = "colors";
        byte[] value = new byte[16384];
        c.putByteArray(key, value);

        saveMapData(world, namedTag, id);
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

    public void unlockMap(NamedTag namedTag, CraftWorld world, int id) throws IOException {
        CompoundTag c = getMapCompound(namedTag);
        String key = "locked";
        byte value = 0B000000;
        c.putByte(key, value);

        // This overwrites the map saved in the data folder
        saveMapData(world, namedTag, id);
    }

    public ItemStack getMap(CraftWorld world, Player p) {
        Vector lineOfSight = p.getEyeLocation().getDirection().multiply(0.1);
        Location vCoordinates = p.getEyeLocation();
        for (int i = 0; i < 50; i++) {
            vCoordinates.add(lineOfSight);
            List<Entity> entities = (List<Entity>) world.getNearbyEntities(vCoordinates, 0.1, 0.1, 0.1);
            System.out.println(vCoordinates);
            System.out.println(entities);
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
