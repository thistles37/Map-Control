package com.thistles.common.messages;

import org.bukkit.plugin.PluginDescriptionFile;

public class Plugin {

    private static String pluginVersion;
    private static String pluginName;
    private static String pluginAuthor;

    public static void init(PluginDescriptionFile description) {
        Plugin.pluginVersion = description.getVersion();
        Plugin.pluginName = description.getName();
        Plugin.pluginAuthor = description.getAuthors().get(0);
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
