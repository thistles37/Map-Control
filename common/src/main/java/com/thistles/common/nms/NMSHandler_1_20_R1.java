package com.thistles.common.nms;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class NMSHandler_1_20_R1 implements NMSHandler {
    private final CraftWorld world;

    public NMSHandler_1_20_R1() {
        this.world = (CraftWorld) Bukkit.getWorlds().get(0);
    }

    @Override
    public String getWorldName() {
        return world.getWorldFolder().getPath();
    }

    @Override
    public File getMapData(String fileName) throws IOException {
        return new File(world.getWorldFolder(), "data/" + fileName);
    }

    @Override
    public Location getMapCoordinates(CompoundTag c) {
        int x = c.getInt("xCenter");
        int z = c.getInt("zCenter");
        return new Location(world, x, 200, z, 1, 1);
    }

    @Override
    public ItemStack getMap(Player p) {
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

    @Override
    public void saveMapData(NamedTag namedTag, String fileName) throws IOException {
        String worldPath = world.getWorldFolder().getPath();
        NBTUtil.write(namedTag, worldPath + "/data/" + fileName);
    }

    @Override
    public void removeCache(int id) {
        ServerLevel server = world.getHandle();
        HashMap<String, SavedData> cache = (HashMap<String, SavedData>) server.getDataStorage().cache;
        cache.remove("map_" + id);
    }
}
