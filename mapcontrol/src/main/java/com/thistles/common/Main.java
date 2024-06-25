package com.thistles.common;

import com.thistles.common.commands.Commands;
import com.thistles.common.commands.TabCompletion;
import com.thistles.common.listener.UpdateChecker;
import com.thistles.common.console.Messages;
import com.thistles.common.console.PluginInfo;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic

        Messages.init();
        PluginInfo.init(this.getDescription());

        Objects.requireNonNull(this.getCommand("mapcontrol")).setExecutor(new Commands());
        Objects.requireNonNull(this.getCommand("mapcontrol")).setTabCompleter(new TabCompletion());

        int resourceId = 116410;
        this.getServer().getPluginManager().registerEvents(new UpdateChecker(resourceId), this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
