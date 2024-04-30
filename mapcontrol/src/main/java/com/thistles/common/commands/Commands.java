package com.thistles.common.commands;


import com.thistles.nmshandler.NMSHandler;
import io.github.bananapuncher714.nbteditor.NBTEditor;
import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

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
                    return false;
                }

                NMSHandler nms;
                try {
                    //Set your nms field
                    String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3].substring(1);
                    System.out.println(version);
                    nms = (NMSHandler) Class.forName("com.thistles.nms.NMSHandler_" + version).newInstance();
                } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                    e.printStackTrace();
                    return true;
                }

                if (args[0].equalsIgnoreCase("get")) {
                    ItemStack map = nms.getMap(p);
                    if (map != null) {
                        p.getInventory().addItem(map);
                        p.sendMessage("Received map");
                    } else {
                        p.sendMessage("No maps found");
                    }
                    return true;
                }
                // Change the order of checks
                ItemStack heldItem = p.getInventory().getItemInMainHand();
                int id = getMapId(heldItem);

                NamedTag mapNamedTag;
                File mapFile;

                try {
                    mapFile = nms.getMapData(getFileName(id));
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
                        nms.saveMapData(mapNamedTag, getFileName(id));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    nms.removeCache(id);
                    System.out.println("Map with id " + id + " has been cleared by " + p.getName());
                    return true;
                }
                if (args[0].equalsIgnoreCase("unlock")) {
                    try {
                        mapNamedTag = readMapData(mapFile);
                        unlockMap(mapNamedTag);
                        nms.saveMapData(mapNamedTag, getFileName(id));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    nms.removeCache(id);
                    System.out.println("Map with id " + id + " has been unlocked by " + p.getName());
                    return true;
                }
                if (args[0].equalsIgnoreCase("tp")) {
                    try {
                        mapNamedTag = readMapData(mapFile);
                        Location coordinates = nms.getMapCoordinates(getMapCompound(mapNamedTag));
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
        return item.getType() == Material.FILLED_MAP;
    }

    public int getMapId(ItemStack map) {
        return NBTEditor.getInt(map, "map");
    }

    public String getFileName(int id) {
        return "map_" + id + ".dat";
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

    public void tpToMap(Player p, Location coordinates) {
        p.teleport(coordinates);
    }
}
