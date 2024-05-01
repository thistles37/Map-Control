package com.thistles.common.commands;


import com.thistles.nmshandler.NMSHandler;
import io.github.bananapuncher714.nbteditor.NBTEditor;
import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class Commands implements CommandExecutor {
    public Commands() {
    }

    private final HashMap<Player, Stack<HashMap<Integer, CompoundTag>>> mapActions = new HashMap<>();

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
                World world = p.getWorld();
                NamedTag mapNamedTag;
                File mapFile;

                // Create nms handler
                NMSHandler nms;
                try {
                    String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3].substring(1);
                    nms = (NMSHandler) Class.forName("com.thistles.nms.NMSHandler_" + version).newInstance();
                } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }

                if (args[0].equalsIgnoreCase("get")) {
                    ItemStack map = getMap(world, p);
                    if (map != null) {
                        p.getInventory().addItem(map);
                        p.sendMessage("Received map!");
                    } else {
                        p.sendMessage("No maps found!");
                    }
                    return true;
                }
                if (args[0].equalsIgnoreCase("undo")) {
                    try {
                        if (!actionExists(p)) {
                            p.sendMessage("There are no actions to be undone!");
                            return true;
                        }
                        HashMap<Integer, CompoundTag> action = retrieveAction(p);
                        int key = action.keySet().iterator().next();
                        File actionFile = getMapData(world, key);
                        NamedTag actionNamedTag = readMapData(actionFile);
                        undoAction(p, world, actionNamedTag, action);
                        nms.removeCache(key);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return true;
                    // Check if you need to do anything special in case auto-save happens before an undo. probs not
                }

                ItemStack heldItem = p.getInventory().getItemInMainHand();
                if (!isMap(heldItem)) {
                    p.sendMessage("Please hold a map!");
                    return true;
                }
                int id = getMapId(heldItem);

                try {
                    mapFile = getMapData(world, id);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                if (!mapFile.exists()) {
                    p.sendMessage("Map file does not exist, wait for world auto-save!");
                    return true;
                }

                try {
                    mapNamedTag = readMapData(mapFile);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                if (args[0].equalsIgnoreCase("clear")) {
                    try {
                        storeAction(p, mapNamedTag, id);
                        clearMap(mapNamedTag);
                        saveMapData(world, mapNamedTag, id);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    nms.removeCache(id);
                    p.sendMessage("Clear success!");
                    return true;
                }
                if (args[0].equalsIgnoreCase("unlock")) {
                    try {
                        storeAction(p, mapNamedTag, id);
                        unlockMap(mapNamedTag);
                        saveMapData(world, mapNamedTag, id);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    nms.removeCache(id);
                    p.sendMessage("Unlock success!");
                    return true;
                }
                if (args[0].equalsIgnoreCase("tp")) {
                    Location coordinates = getMapCoordinates(world, getMapCompound(mapNamedTag));
                    tpToMap(p, coordinates);
                    p.sendMessage("Teleporting...");
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

    public File getMapData(World world, int id) throws IOException {
        return new File(world.getWorldFolder(), "data/" + getFileName(id));
    }

    public NamedTag readMapData(File mapFile) throws IOException {
        return NBTUtil.read(mapFile);
    }

    public void saveMapData(World world, NamedTag namedTag, int id) throws IOException {
        String worldPath = getWorldPath(world);
        NBTUtil.write(namedTag, worldPath + "/data/" + getFileName(id));
    }

    public CompoundTag getMapCompound(NamedTag namedTag) {
        return (CompoundTag) ((CompoundTag) namedTag.getTag()).get("data");
    }

    public String getWorldPath(World world) {
        return world.getWorldFolder().getPath();
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

    public Location getMapCoordinates(World world, CompoundTag c) {
        int x = c.getInt("xCenter");
        int z = c.getInt("zCenter");
        return new Location(world, x, 200, z, 1, 1);
    }

    public void tpToMap(Player p, Location coordinates) {
        p.teleport(coordinates);
    }

    public ItemStack getMap(World world, Player p) {
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

    public void storeAction(Player p, NamedTag namedTag, int id) {
        if (!mapActions.containsKey(p)) {
            mapActions.put(p, new Stack<>());
        }
        HashMap<Integer, CompoundTag> action = new HashMap<>();
        CompoundTag c = (CompoundTag) namedTag.getTag();
        action.put(id, c.clone());
        mapActions.get(p).push(action);
    }

    public boolean actionExists(Player p) {
        if (mapActions.containsKey(p)) {
            return !mapActions.get(p).isEmpty();
        }
        return false;
    }

    public HashMap<Integer, CompoundTag> retrieveAction(Player p) {
        return mapActions.get(p).pop();
    }

    public void undoAction(Player p, World world, NamedTag namedTag, HashMap<Integer, CompoundTag> action) throws IOException {
        int key = action.keySet().iterator().next();
        namedTag.setTag(action.get(key));

        String worldPath = getWorldPath(world);
        NBTUtil.write(namedTag, worldPath + "/data/" + getFileName(key));
        p.sendMessage("Undo success!");
    }
}
