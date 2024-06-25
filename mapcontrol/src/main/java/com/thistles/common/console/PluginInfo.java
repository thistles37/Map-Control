package com.thistles.common.console;

import org.bukkit.plugin.PluginDescriptionFile;

public class PluginInfo {

    private static String pluginVersion;
    private static String pluginName;
    private static String pluginAuthor;

    public static void init(PluginDescriptionFile description) {
        PluginInfo.pluginVersion = description.getVersion();
        PluginInfo.pluginName = description.getName();
        PluginInfo.pluginAuthor = description.getAuthors().get(0);
    }

    public static String getPluginVersion() {
        return pluginVersion;
    }

    public static String getPluginName() {
        return pluginName;
    }

    public static String getPluginAuthor() {
        return pluginAuthor;
    }
}
