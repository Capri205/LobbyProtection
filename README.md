# LobbyProtection
Simple server protection plugin with complete config control. Use /build to bypass all protections.

SpigotMC: https://www.spigotmc.org/resources/lobbyprotection.99483/

This simple plugin allows you to stop players from: 
- placing blocks 
- breaking blocks 
- dropping items 
- picking up items 
- changing up their inventory 
- taking damage 
- getting hungry 

additional features: 

- toggle if all players can take damage (by command /dmg)
- disable player join message
- disable player quit message
- disable player kick message
- clear inventory on join
- clear armor on join
- reset xp level on join
- set the weather to clear on server start
- set gamerule doDaylightCycle to false on server start
- set the time to noon (12000) on server start
- set gamerule keepInventory to true on server start

You can disable every feature in the config file. Just change true to false.
On join, every players gamemode gets changed to survival mode.

Commands: 
- /build 
- permission: build.buildCommand 
- usage: /build 
- Allows just the player who executed the command to bypass all restrictions. Also the gamemode of the player gets changed to creative mode. All other players are unaffected. 
- /dmg 
- permission: build.dmgCommand 
- usage: /dmg 
- Toggles by ingame command if all players can take damage or not. Returns a message with current state: "Player damage is now true/false" 

Config:
- config.yml
disablePlayerDamage: true<br>
disableInventoryClickEvent: true<br>
disableBlockBreak: true<br>
disableBlockPlace: true<br>
disablePlayerDropItem: true<br>
disablePlayerPickupItem: true<br>
disableFoodLevelChange: true<br>
disablePlayerJoinMessage: true<br>
disablePlayerQuitMessage: true<br>
disablePlayerKickMessage: true<br>
clearInventoryOnJoin: true<br>
clearArmorOnJoin: true<br>
xpLevelResetOnJoin: true<br>
setWeatherToClearOnStart: true<br>
disableDaylightCycle: true<br>
setTimeOnStart: true<br>
time: 12000<br>
keepInventory: true<br>
stopallspawning: false<br>
allowedmobs: []<br>
disallowedmobs: []<br>
populationenforcement: {}<br>

Allowedmobs and disallowed mobs are a list of living entities you want or don't want to spawn.<br>
You can mix them up as checks follow one to the other, so first checking whether the spawned mob<br>
is allowed, then checking whether it's a disallowed mob, and then finally checking the count of<br>
the mobs in the world and seeing if it breaches the limits set.<br><br>
List them up in the config like this. Dash and space prefixes the allowed and disallowed mob lists,<br>
while two spaces prefix the populationenforcement mobs, with a colon, space and the number of allowed mobs<br>
following that. The number of allowed mobs must be an iteger:<br>
allowedmobs:<br><br>
&mdash; ALLAY<br>
&mdash; DROWNED<br>
&mdash; GLOW_SQUID<br>
&mdash; PARROT<br>
&mdash; WITCH<br>
&mdash; WOLF<br>
disallowedmobs: []<br>
populationenforcement:<br>
&nbsp;&nbsp;DROWNED: 10<br>

