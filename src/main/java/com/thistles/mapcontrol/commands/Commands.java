package com.thistles.mapcontrol.commands;


import io.github.bananapuncher714.nbteditor.NBTEditor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.io.*;

import java.io.IOException;
import java.util.HashMap;

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
                // check if given argument is contained in the set of options
                CraftWorld world = (CraftWorld) Bukkit.getWorlds().get(0);
                String worldPath = world.getWorldFolder().getPath();

                int id = getMapId(p);
                String fileName = getFileName(id);
                NamedTag mapNamedTag;

                if (args[0].equalsIgnoreCase("clear")) {
                    try {
                        mapNamedTag = readMapData(world, id);
                        clearMap(mapNamedTag, world, id);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return true;
                }
                if (args[0].equalsIgnoreCase("unlock")) {
                    try {
                        mapNamedTag = readMapData(world, id);
                        unlockMap(mapNamedTag, world, id);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return true;
                }
                if (args[0].equalsIgnoreCase("tp")) {
                    try {
                        mapNamedTag = readMapData(world, id);
                        Location coordinates = getMapCoordinates(world, mapNamedTag);
                        //p.teleport(Objects.requireNonNull(tpToOrigin(fileName)));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return true;
                }
                if (args[0].equalsIgnoreCase("get")) {
                    Vector lineOfSight = p.getEyeLocation().getDirection();
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

    public void clearMap(NamedTag namedTag, CraftWorld world, int id) throws IOException {
        CompoundTag c = getMapCompound(namedTag);
        String key = "colors";
        byte[] value = new byte[16384];
        c.putByteArray(key, value);

        // This overwrites the map saved in the data folder
        String worldPath = world.getWorldFolder().getPath();
        NBTUtil.write(namedTag, worldPath + "/data/" + getFileName(id));

        ServerLevel server = world.getHandle();
        HashMap<String, SavedData> cache = (HashMap<String, SavedData>) server.getDataStorage().cache;
        cache.remove("map_" + id);
        System.out.println(cache);
        System.out.println(server);
        System.out.println(worldPath);
    }

    public CompoundTag getMapCompound(NamedTag namedTag) {
        return (CompoundTag) ((CompoundTag) namedTag.getTag()).get("data");
    }

    public NamedTag readMapData(CraftWorld world, int id) throws IOException {
        File mapFile = new File(world.getWorldFolder(), "data/" + getFileName(id));
        return NBTUtil.read(mapFile);
    }

    public Location getMapCoordinates(CraftWorld world, NamedTag namedTag) throws IOException {
        CompoundTag c = getMapCompound(namedTag);

        int x = c.getInt("xCenter");
        int z = c.getInt("zCenter");
        //String tpCommand = "tp " + x + " " + z;
        //data.putByteArray(key, value);
        return new Location(world, x, 200, z, 1, 1);
    }

    public void tpToMap() {
        // Teleport player to where the map was created
    }

    public void mapClear() {
        // Clear map
    }

    public void unlockMap(NamedTag namedTag, CraftWorld world, int id) throws IOException {
        // Unlock map
        CompoundTag c = getMapCompound(namedTag);
        String key = "locked";
        byte value = 0B000000;
        c.putByte(key, value);

        // This overwrites the map saved in the data folder
        String worldPath = world.getWorldFolder().getPath();
        NBTUtil.write(namedTag, worldPath + "/data/" + "map_" + id + ".dat");

        ServerLevel server = world.getHandle();
        HashMap<String, SavedData> cache = (HashMap<String, SavedData>) server.getDataStorage().cache;
        cache.remove("map_" + id);
    }

    public void getMap() {
        // Get (framed) map that a player points at
    }
}
