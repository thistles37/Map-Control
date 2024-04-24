package com.thistles.mapcontrol;

import com.thistles.mapcontrol.commands.Commands;
import com.thistles.mapcontrol.commands.TabCompletion;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        Objects.requireNonNull(this.getCommand("mapcontrol")).setExecutor(new Commands());
        Objects.requireNonNull(this.getCommand("mapcontrol")).setTabCompleter(new TabCompletion());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
