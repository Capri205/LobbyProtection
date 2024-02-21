# LobbyProtection
Simple server protection plugin with complete config control. Use /lpbld to bypass all protections.

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
- /lpbld 
- permission: build.buildCommand 
- usage: /lpbld 
- Allows just the player who executed the command to bypass all restrictions. Also the gamemode of the player gets changed to creative mode. All other players are unaffected. 
- /lpdmg 
- permission: build.dmgCommand 
- usage: /lpdmg 
- Toggles by ingame command if all players can take damage or not. Returns a message with current state: "Player damage is now true/false" 
- /lpmob
- permission: lpmob.control
- usage:<br>
- /lpmob &lt;allowed | disallowed&gt; &lt;add | remove&gt; mob [,mob ...] - add or remove a mob by name or type to/from the spawn control lists<br>
- /lpmob &lt;allowed | disallowed | popcon&gt; list - list mobs in list<br>
- /lpmob popcon add name:&lt;'Mob Name'&gt; type:&lt;type&gt; max:&lt;number&gt; [ spawnpoint:X,Y,Z ]<br>
- /lpmob popcon add type:&lt;type&gt; max:&lt;number&gt; [ spawnpoint:X,Y,Z ]<br>
- /lpmob popcon remove &lt;mob|'Mob Name'&gt; - remove a mob by name or type from population control<br>

- In-game management of mob spawning and mob spawn limits using the allowed, disallowed and population control lists.<br><br>

Config:
- config.yml
disablePlayerDamage: true<br>
disableInventoryClickEvent: true<br>
disableBlockBreak: true<br>
disableFarmBreak: true<br>
disableBlockSpread: true<br>
disableBlockIgnition: true<br>
disableIceMelt: true<br>
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
populationcontrol: {}<br>
rangelimitcheckinterval: 20<br>
rangelimitspeedmodifier: 1.5<br>
rangelimits: {}<br>

Allowedmobs and disallowed mobs are a list of living entities you want or don't want to spawn.<br>
You can mix them up as checks follow one to the other, so first checking whether the spawned mob<br>
is allowed, then checking whether it's a disallowed mob, and then finally checking the count of<br>
the mobs in the world and seeing if it breaches the limits set.<br><br>
List them up in the config like this. Dash and space prefixes the allowed and disallowed mob lists.<br>
allowedmobs:<br>
&mdash; ALLAY<br>
&mdash; DROWNED<br>
&mdash; GLOW_SQUID<br>
&mdash; PARROT<br>
&mdash; WITCH<br>
&mdash; WOLF<br>
disallowedmobs: []<br>


Population control defines how many of a mob are allowed and where to spawn these. Mobs can be defined by<br>
type or by name (nametag). You can also set the spawn point as a comma separated X, Y and Z coordinates.<br>
If spawnpoint is not set then the home setting from rangelimits will be used, and if that is not set a warning<br>
will be issued in the log and no mob spawned. The popcontrolcheckinterval setting determines how frequently the<br>
mob control checker runs in server ticks.<br>

In this example we have 10 dolphins defined by name, along with the type (required). No spawn point is set, so the<br>
one from range limits will be used. We maintain a population of 20 parrots and 10 drowned by mob type, with the<br>
drowned using their own spawn point and the parrots will use the range limits home. The pop control check is<br>
performed every 1200 server ticks (every minute).

popcontrolcheckinterval: 1200<br>
populationcontrol:<br>
&mdash; name: Dodgy Dolphin<br>
&nbsp;&nbsp;mobtype: DOLPHIN<br>
&nbsp;&nbsp;max: 10<br>
&mdash; type: PARROT<br>
&nbsp;&nbsp;max: 20<br>
&mdash; type: DROWNED<br>
&nbsp;&nbsp;max: 10<br>
&nbsp;&nbsp;spawnpoint: -0.5,33,0.5<br>


Range limits are mobs by type or custom name that you want to contain within a radius of a home locaton.<br>
The rangelimits section defines the name or type of a mob, its home location (x,y,z) and the radius from<br>
the home location they are allowed to move out to. There's a task that runs, by default, every 20 ticks or<br>
1 second and will check all the configured mobs to see if they are outside of the radius or not. Those that<br>
are will be pushed onto a list along with the estimated time it would take for the mob to get back to the<br>
home location. The mob is then given instructions to head home, and a multiplier used to speed it up a bit.<br>
The check task will check the mobs on the list to see if they are still on their way home, reached home, or<br>
exceeded the estimated time to get back (multiplied by 4). This gives mobs that get distracted more time to get<br>
back than a strict straight line run. The time is based on how far away from home the mob is. If a mob is<br>
within 10 blocks of home then it is removed from the list. If the mob is still on the way then the go home<br>
instruction is re-applied to the mob to keep it on track. If the time taken so far to get back is greater than<br>
4 times the original estimate, then the mob is removed and a new mob of that type or name spawned at the home<br>
location. In the example configuration below we are tracking only bees with a custom name "Happy Bee" and only<br>
Dolphns with the custom name "Dodgy Dolphin", and any Parrots or Sniffers by mob type.<br><br>
rangelimitcheckinterval: 20<br>
rangelimitspeedmodifier: 1.5<br>
rangelimits:<br>
&mdash; name: Happy Bee<br>
&nbsp;&nbsp;home: -180,48,-133<br>
&nbsp;&nbsp;radius: 40<br>
&mdash; type: Parrot<br>
&nbsp;&nbsp;home: -86.5,134,-74.5<br>
&nbsp;&nbsp;radius: 40<br>
&mdash; name: Dodgy Dolphin<br>
&nbsp;&nbsp;home: -0.5,33,0.5<br>
&nbsp;&nbsp;radius: 60<br>
&mdash; type: Sniffer<br>
&nbsp;&nbsp;home: -126,42,-186<br>
&nbsp;&nbsp;radius: 30<br>