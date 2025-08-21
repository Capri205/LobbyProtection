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


Population control defines how many of a mob are allowed, their home location where they spawn or must head<br>
back to if they have a fixed range. Mobs can be defined by their type (EntityType) or by nametag (CustomName)<br>
The 'name' property indicates the mob is named, whilst the 'type' indicates to the checker to use the entity type.<br>
The home location is as a comma separated X, Y and Z coordinates and is required for all mobs.<br>
The maxAllowed defines how many of the mob should be in the world at maximum. The radius defines the number of<br>
blocks from the home location the mob can wander before being redirected back to home.<br>

The control is split into two background tasks. One for population control and the other for range checking.<br>
popcontrolcheckinterval setting determines how frequently the population checker runs, while the property<br>
rangelimitcheckinterval setting controls how frequently the range checker runs. Range checking should be<br>
considerably more frequent than population checking. Both are in server ticks. The rangelimitspeedmodifier<br>
is used to help the checker determine how long a mob should take get back to home given it's distance from home<br>
and its average travel speed. This time is then in turn used to determine if a mob is stuck and taking too long<br>
to get back. Mobs taking too long are despawned and a new mob spawned at their home location.<br>

The following is an example of a mob control config, along with an explanation:<br>

popcontrolcheckinterval: 1200<br>
rangelimitcheckinterval: 20<br>
rangelimitspeedmodifier: 1.5<br>
mobcontrol:<br>
&mdash; name: Dodgy Dolphin<br>
  type: DOLPHIN<br>
  maxallowed: 10<br>
  home: -0.5,33,0.5<br>
  radius: 45<br>
&mdash; type: PARROT<br>
  maxallowed: 20<br>
  home: -86.5,134,-74.5<br>
  radius: 40<br>
&mdash; type: DROWNED<br>
  maxallowed: 10<br>
  home: 0.5,20,0.5<br>
&mdash; type: SNIFFER<br>
  maxallowed: 7<br>
  home: -126,42,-186<br>
  radius: 30<br>
&mdash; name: Happy Bee<br>
  home: -180,48,-133<br>
  radius: 40<br>
  type: BEE<br>

We have 5 mobs under control. Two of which are named mobs using the "name" property, and three are by EntityType<br>
using the "type" property. Four of the mobs have population control using the "maxallowed" property and one doesn't<br>
use that. We are manually spawning in those named entities and being named they should persist. All have a mandatory<br>
home location in X, Y and Z coordinate format. Three are range controlled by the "radius" property. Those mobs will<br>
by directed back to the home location if they go beyond their bounds<br>
