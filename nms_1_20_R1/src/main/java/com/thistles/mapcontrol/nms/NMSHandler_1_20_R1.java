package com.thistles.mapcontrol.nms;

import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import com.thistles.common.nms.NMSHandler;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;

public class NMSHandler_1_20_R1 implements NMSHandler {
    @Override
    public void removeCache(int id) {
        // TODO
    }

    @Override
    public void saveMapData(NamedTag namedTag, String fileName) throws IOException {
        // TODO
    }

    @Override
    public ItemStack getMap(Player p) {
        return null;
    }

    @Override
    public String getWorldName() {
        return null;
    }

    @Override
    public File getMapData(String fileName) throws IOException {
        return null;
    }

    @Override
    public Location getMapCoordinates(CompoundTag compoundTag) {
        return null;
    }
}

// Slang, ben je daar?
// Waarom noem ik je eigenlijk "Slang"?
//
