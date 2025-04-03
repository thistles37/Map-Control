package com.thistles.nms;

import com.thistles.nmshandler.NMSHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_21_R4.CraftWorld;

import java.util.*;

public class NMSHandler_1_21_R4 implements NMSHandler {
    private final CraftWorld world;

    public NMSHandler_1_21_R4() {
        this.world = (CraftWorld) Bukkit.getWorlds().get(0);
    }

    @Override
    public void removeCache(int id) {
        ServerLevel server = world.getHandle();
        Map<SavedDataType<?>, Optional<SavedData>> cache = server.getDataStorage().cache;
        for (Map.Entry<SavedDataType<?>, Optional<SavedData>> entry : cache.entrySet()) {
            if (Objects.equals(entry.getKey().id(), "map_" + id)) {
                cache.remove(entry.getKey());
                break;
            }
        }
    }
}
