package com.thistles.nms;

import com.thistles.nmshandler.NMSHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_21_R2.CraftWorld;

import java.util.Map;
import java.util.Optional;

public class NMSHandler_1_21_R2 implements NMSHandler {
    private final CraftWorld world;

    public NMSHandler_1_21_R2() {
        this.world = (CraftWorld) Bukkit.getWorlds().get(0);
    }

    @Override
    public void removeCache(int id) {
        ServerLevel server = world.getHandle();
        Map<String, Optional<SavedData>> cache = server.getDataStorage().cache;
        cache.remove("map_" + id);
    }
}
