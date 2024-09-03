![image](https://github.com/user-attachments/assets/b4dab99a-e073-43f1-a95d-e291875006e4)

# ☭ EnderScythe 
![Static Badge](https://img.shields.io/badge/Version-v1.4-blue?color=A020F0)

![Static Badge](https://img.shields.io/badge/Soft_Dependency-WorldGuard-blue)

**EnderScythe** is a powerful Minecraft plugin that introduces a unique weapon - the Ender Scythe. Players can upgrade this special scythe to netherite while retaining its unique properties. The plugin supports multiple levels, with configurable damage, cooldown, and range. The scythe cannot be crafted but can be upgraded using a special item.

**Note** that this plugin is not using any custom resource pack nor a custom item. The Scythe seen in photos is from my texturepack replacing a hoe.

## 📦 Features

- **Custom Weapon:** The Ender Scythe is a unique weapon with special abilities and effects.
- **Particle Effects:** The scythe emits particles around the player, with different effects for each level. You can now customize or disable particle effects for each level in the config.
- **Laser Attack:** The scythe can shoot a laser that damages entities within its range.
- **Leveling System:** Players can upgrade the scythe from level 1 to any max level specified in config.
- **PvP Control:** Configurable option to allow or disallow the scythe's use in player vs. player combat.
- **Upgrade Compatibility:** The scythe can be converted to netherite in the smithing table while retaining its special attributes.
- **Enchanting Restrictions:** The scythe cannot be enchanted or combined in anvils, grindstones, or crafting tables.

## ⚙️ Configuration

The plugin is highly configurable through the `config.yml` and `messages.yml` files.

### ![Static Badge](https://img.shields.io/badge/config.yml-brightgreen?logo=googleforms)

```yaml
#########################################################################################################
#    Plugin now supports full hex color codes.                                                          #
#    You can use https://www.birdflop.com/resources/rgb/ to create your colors                          #
#    Developer; padrewin || Cold Development                                                            #
#    GitHub; https://github.com/padrewin || https://github.com/Cold-Development                         #
#    Links: https://colddev.dev || https://discord.colddev.dev                                          #
#########################################################################################################

# Plugin Prefix:
plugin-prefix: "&7「&#6F00CDE&#760BD0n&#7C16D3d&#8321D6e&#8A2CD9r&#9137DDS&#9742E0c&#9E4DE3y&#A558E6t&#AB63E9h&#B26EECe&7」&7» "

# Scythe Shard - item to upgrade your Scythe:
upgrade-item:
  name: "&#6F00CD✯ &#6F00CDS&#750AD0c&#7B14D3y&#811ED5t&#8728D8h&#8D32DBe &#9A46E1S&#A050E4h&#A65AE6a&#AC64E9r&#B26EECd"
  lore:
    - "&a⬆ &7Use this item to upgrade your &#6F00CDE&#760BD0n&#7C16D3d&#8321D6e&#8A2CD9r&#9137DDS&#9742E0c&#9E4DE3y&#A558E6t&#AB63E9h&#B26EECe"

# EnderScythe settings:
damage-players: false # Set to true if you want players to use this Scythe in PvP
enderscythe-damage: 20 # damage
enderscythe-cooldown: 500 # milliseconds || this means 0.5 seconds
enderscythe-range: 32
enderscythe-max-level: 2 # Max level for the EnderScythe (cannot be 0)

# Activate or deactivate your Scythe's particles in case your players are bothered by that:
enderscythe-particles: true

# Particle settings for each level
particle-settings:
  1:
    type: "PORTAL"
    count: 5
    offset: 0.5, 1, 0.5
    extra: 0
  2:
    type: "HEART"
    count: 1
    offset: 0.5, 1, 0.5
    extra: 0
  3:
    type: "REDSTONE"
    count: 10
    offsetX: 0.5
    offsetY: 1
    offsetZ: 0.5
    extra: 0
    color: "#FF0000" # Color of REDSTONE particles
    size: 1.0 # Size of particles
  4:
    type: "VILLAGER_HAPPY"
    count: 30
    offset: 0.5, 1, 0.5
    extra: 0
  5:
    type: "FLAME"
    count: 3
    offset: 0.5, 1, 0.5
    extra: 0

# Set world where EnderScythe can be used:
# if you don't put your world here, scythe won't work
enderscythe-use-worlds:
  - overworld
  - enter
  - your
  - world names
  - lobby

# EnderScythe display settings:
ender-scythe:
  name: "&#6F00CDE&#760BD0n&#7C16D3d&#8321D6e&#8A2CD9r&#9137DDS&#9742E0c&#9E4DE3y&#A558E6t&#AB63E9h&#B26EECe"
  level: "&8「%scythe_level%&8」" # Don't ever change this placeholder except its color. Colors are safe to be edited. Same applies for below lore section
  lore:
    - "&4🗡 &7ᴛʜɪꜱ ꜱᴄʏᴛʜᴇ ᴄᴀɴ ᴅᴇᴀʟ %enderscythe_damage% ᴅᴀᴍᴀɢᴇ"
    - "&c📏 &7ᴛʜɪꜱ ꜱᴄʏᴛʜᴇ ʜᴀꜱ ᴀ ʀᴀɴɢᴇ ᴏꜰ %enderscythe_range% ʙʟᴏᴄᴋꜱ"
    - "&d🕓 &7ᴛʜɪꜱ ꜱᴄʏᴛʜᴇ ʜᴀꜱ ᴀ ᴄᴏᴏʟᴅᴏᴡɴ ᴏꜰ %enderscythe_cooldown% ꜱᴇᴄᴏɴᴅꜱ"
  lore-placeholders:
    - "%enderscythe_damage%"
    - "%enderscythe_range%"
    - "%enderscythe_cooldown%"
  laser-color: "#800080" # Laser hex color (#800080) this is default PURPLE
```
![Static Badge](https://img.shields.io/badge/messages.yml-red?logo=imessage&logoColor=white)
```yaml
messages:
  receive-hoe: "&7You have received an &#6F00CDE&#760BD0n&#7C16D3d&#8321D6e&#8A2CD9r&#9137DDS&#9742E0c&#9E4DE3y&#A558E6t&#AB63E9h&#B26EECe&7!"
  config-reloaded: "&7Config &areloaded."
  give-success: "&7Successfully given an &#6F00CDE&#760BD0n&#7C16D3d&#8321D6e&#8A2CD9r&#9137DDS&#9742E0c&#9E4DE3y&#A558E6t&#AB63E9h&#B26EECe&7 to {player}."
  player-not-found: "&7Player not found."
  invalid-command: "&cInvalid command."
  upgrade-success: "&#6F00CDE&#760BD0n&#7C16D3d&#8321D6e&#8A2CD9r&#9137DDS&#9742E0c&#9E4DE3y&#A558E6t&#AB63E9h&#B26EECe &7upgraded to level &#B26EEC{level}."
  not-allowed-world: "&7You cannot use the Ender Scythe in this world."
  not-ender-scythe: "&7This is not an &#6F00CDE&#760BD0n&#7C16D3d&#8321D6e&#8A2CD9r&#9137DDS&#9742E0c&#9E4DE3y&#A558E6t&#AB63E9h&#B26EECe&7."
  receive-upgrade-item: "&7You have received an &#6F00CDS&#750AD0c&#7B14D3y&#811ED5t&#8728D8h&#8D32DBe &#9A46E1S&#A050E4h&#A65AE6a&#AC64E9r&#B26EECd&7!"
  max-level-reached: "&7This &#6F00CDE&#760BD0n&#7C16D3d&#8321D6e&#8A2CD9r&#9137DDS&#9742E0c&#9E4DE3y&#A558E6t&#AB63E9h&#B26EECe &7is at &#DA70D6level max&7."
  invalid-arguments: "&cInclude player name."
 ```

## 💻 Commands
- **/enderscythe reload:** Reload the plugin configuration. 
- **/enderscythe give <name>:** Give a player an Ender Scythe.
- **/getupgradeitem:** Command to receive the upgrade item.
### You can also use predefined aliases
- **/es give <name>:** Give a player an Ender Scythe.
- **/es reload:** Reload the plugin configuration.
- **/esgui:** Command to receive the upgrade item.
- **/getui:** Command to receive the upgrade item.


## ⚠️ Permissions
- **enderscythe.admin:** Gives you total power.

## 🔎 Installation
1. Download the latest release of the plugin.
2. Place the plugin JAR file into your server's plugins directory.
3. Restart your server to load the plugin.
4. Customize the plugin settings in config.yml and messages.yml as needed. || p.s; use /enderscythe reload :P
5. Use the provided commands to receive and upgrade the Ender Scythe.

![alt text](image.png)
![alt text](image-1.png)
![alt text](image-2.png)
![esitems](https://github.com/user-attachments/assets/0efcac6c-0d21-4a20-b708-19ed7e0bc441)
