package com.thistles.common.nms;

import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;

public interface NMSHandler {
    void removeCache(int id);

    void saveMapData(NamedTag namedTag, String fileName) throws IOException;

    ItemStack getMap(Player p);

    String getWorldName();

    File getMapData(String fileName) throws IOException;

    Location getMapCoordinates(CompoundTag compoundTag);
}
