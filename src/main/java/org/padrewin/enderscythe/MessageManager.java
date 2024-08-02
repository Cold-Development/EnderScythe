package org.padrewin.enderscythe;

import net.md_5.bungee.api.ChatColor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageManager {
    private final ConfigManager configManager;
    private String pluginPrefix;

    public MessageManager(ConfigManager configManager) {
        this.configManager = configManager;
        updatePluginPrefix();
    }

    public void updatePluginPrefix() {
        this.pluginPrefix = applyHexColors(configManager.getConfig().getString("plugin-prefix"));
    }

    public String getMessage(String key) {
        String message = configManager.getMessagesConfig().getString("messages." + key);
        return message != null ? applyHexColors(message) : "Message not found: " + key;
    }

    public String getPrefixedMessage(String key) {
        String message = configManager.getMessagesConfig().getString("messages." + key);
        return message != null ? applyHexColors(pluginPrefix + message) : pluginPrefix + "Message not found: " + key;
    }

    public void reloadMessagesConfig() {
        configManager.reloadMessagesConfig();
        updatePluginPrefix();
    }

    private String applyHexColors(String message) {
        if (message == null) return null;

        Pattern hexPattern = Pattern.compile("&#([A-Fa-f0-9]{6})");
        Matcher matcher = hexPattern.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length());

        while (matcher.find()) {
            String hexColor = matcher.group(1);
            String replacement = "§x";
            for (char c : hexColor.toCharArray()) {
                replacement += "§" + c;
            }
            matcher.appendReplacement(buffer, replacement);
        }

        matcher.appendTail(buffer);
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }
}
