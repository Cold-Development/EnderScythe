package org.padrewin.enderscythe;

import org.bukkit.ChatColor;

public class MessageManager {
    private final ConfigManager configManager;
    private final String pluginPrefix;

    public MessageManager(ConfigManager configManager) {
        this.configManager = configManager;
        this.pluginPrefix = applyHexColors(configManager.getConfig().getString("plugin-prefix"));
    }

    public String getMessage(String key) {
        String message = configManager.getMessagesConfig().getString("messages." + key);
        return message != null ? ChatColor.translateAlternateColorCodes('&', message) : "Message not found: " + key;
    }

    public String getPrefixedMessage(String key) {
        String message = configManager.getMessagesConfig().getString("messages." + key);
        return message != null ? ChatColor.translateAlternateColorCodes('&', pluginPrefix + message) : pluginPrefix + "Message not found: " + key;
    }

    public void reloadMessagesConfig() {
        configManager.reloadMessagesConfig();
    }

    private String applyHexColors(String message) {
        StringBuilder sb = new StringBuilder();
        char[] chars = message.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '&' && i + 7 < chars.length && chars[i + 1] == '#' &&
                    isHexChar(chars[i + 2]) && isHexChar(chars[i + 3]) && isHexChar(chars[i + 4]) &&
                    isHexChar(chars[i + 5]) && isHexChar(chars[i + 6]) && isHexChar(chars[i + 7])) {
                sb.append("§x");
                for (int j = 2; j <= 7; j++) {
                    sb.append('§').append(chars[i + j]);
                }
                i += 7; // Skip the next 7 characters as they are part of the hex color code
            } else {
                sb.append(chars[i]);
            }
        }
        return sb.toString();
    }

    private boolean isHexChar(char c) {
        return (c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f');
    }
}
