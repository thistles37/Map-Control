package com.thistles.common.messages;

import org.bukkit.ChatColor;

public class Messages {

    private static final String PREFIX = ChatColor.DARK_RED + "[" + ChatColor.RED + "MapControl" + ChatColor.DARK_RED + "] ";

    public static String CLEAR_MAP_SUCCESS;
    public static String UNDO_SUCCESS;
    public static String UNLOCK_MAP_SUCCESS;
    public static String TELEPORTING_TO_DESTINATION;
    public static String FRAMED_MAP_FOUND;

    public static String MAP_DATA_NOT_FOUND;
    public static String NO_MAP_IN_HAND;
    public static String NO_FRAMED_MAP_FOUND;
    public static String NO_UNDO_AVAILABLE;

    public static void init() {
        CLEAR_MAP_SUCCESS = PREFIX + ChatColor.GREEN + "Clear success.";
        UNDO_SUCCESS = PREFIX + ChatColor.GREEN + "Undo success.";
        UNLOCK_MAP_SUCCESS = PREFIX + ChatColor.GREEN + "Unlock success.";
        TELEPORTING_TO_DESTINATION = PREFIX + "Teleporting...";
        FRAMED_MAP_FOUND = PREFIX + ChatColor.GREEN + "Map found.";

        MAP_DATA_NOT_FOUND = PREFIX + ChatColor.RED + "Map data not found, wait for world auto-save.";
        NO_MAP_IN_HAND = PREFIX + ChatColor.RED + "Please hold a map.";
        NO_FRAMED_MAP_FOUND = PREFIX + ChatColor.RED + "No maps found.";
        NO_UNDO_AVAILABLE = PREFIX + ChatColor.RED + "No undo available.";
    }
}
