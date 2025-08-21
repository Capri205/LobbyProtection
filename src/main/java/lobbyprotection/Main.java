package lobbyprotection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import net.md_5.bungee.api.ChatColor;

import lobbyprotection.commands.BuildCommand;
import lobbyprotection.commands.DmgCommand;
import lobbyprotection.commands.MobControl;
import lobbyprotection.listeners.Listeners;

public final class Main extends JavaPlugin {

	static Logger log = Logger.getLogger("Minecraft");

    private static Main instance;

	private static String plugin = "LobbyProtection";
	private static String pluginprefix = "[" + plugin + "]";
	private static String chatmsgprefix = ChatColor.AQUA + "" + ChatColor.BOLD + plugin + ChatColor.DARK_GRAY + ChatColor.BOLD + " » " + ChatColor.LIGHT_PURPLE + "";
	private static String logmsgprefix = pluginprefix + " » ";
	
	private static boolean inConfigUpdate = false;

	private static Map<String, Mob> mobControlList  = new HashMap<>();
	private static Map<String, List<UUID>> popTracker = new HashMap<>();
	private int rangeMobCount = 0;
	private List<String> rangeMobList = new ArrayList<>();
	private int populationMobCount = 0;
	private List<String> populationMobList = new ArrayList<>();

    @Override
    public void onLoad() {

		instance = this;

        saveConfig();
        if (!getConfig().contains("disablePlayerDamage")) { getConfig().set("disablePlayerDamage", true); }
        if (!getConfig().contains("disableInventoryClickEvent")) { getConfig().set("disableInventoryClickEvent", true); }
        if (!getConfig().contains("disableBlockBreak")) { getConfig().set("disableBlockBreak", true); }
        if (!getConfig().contains("disableFarmBreak")) { getConfig().set("disableFarmBreak", true); }
        if (!getConfig().contains("disableBlockSpread")) { getConfig().set("disableBlockSpread", true); }
        if (!getConfig().contains("disableBlockIgnition")) { getConfig().set("disableBlockIgnition", true); }
        if (!getConfig().contains("disableIceMelt")) { getConfig().set("disableIceMelt", true); }
        if (!getConfig().contains("disableLeafDecay")) { getConfig().set("disableIceMelt", true); }
        if (!getConfig().contains("disableBlockPlace")) { getConfig().set("disableLeafDecay", true); }
        if (!getConfig().contains("disablePlayerDropItem")) { getConfig().set("disablePlayerDropItem", true); }
        if (!getConfig().contains("disablePlayerPickupItem")) { getConfig().set("disablePlayerPickupItem", true); }
        if (!getConfig().contains("disableFoodLevelChange")) { getConfig().set("disableFoodLevelChange", true); }
        if (!getConfig().contains("disablePlayerJoinMessage")) { getConfig().set("disablePlayerJoinMessage", true); }
        if (!getConfig().contains("disablePlayerQuitMessage")) { getConfig().set("disablePlayerQuitMessage", true); }
        if (!getConfig().contains("disablePlayerKickMessage")) { getConfig().set("disablePlayerKickMessage", true); }
        if (!getConfig().contains("disablePlayerKickMessage")) { getConfig().set("disableDolphinDrown", true); }
        if (!getConfig().contains("clearInventoryOnJoin")) { getConfig().set("clearInventoryOnJoin", true); }
        if (!getConfig().contains("clearArmorOnJoin")) { getConfig().set("clearArmorOnJoin", true); }
        if (!getConfig().contains("xpLevelResetOnJoin")) { getConfig().set("xpLevelResetOnJoin", true); }
        if (!getConfig().contains("setWeatherToClearOnStart")) { getConfig().set("setWeatherToClearOnStart", true); }
        if (!getConfig().contains("disableDaylightCycle")) { getConfig().set("disableDaylightCycle", true); }
        if (!getConfig().contains("setTimeOnStart")) { getConfig().set("setTimeOnStart", true); }
        if (!getConfig().contains("time")) { getConfig().set("time", 7000); }
        if (!getConfig().contains("keepInventory")) { getConfig().set("keepInventory", true); }
        if (!getConfig().contains("stopallspawning")) { getConfig().set("stopallspawning", false); }
        if (!getConfig().contains("allowedmobs")) { getConfig().set("allowedmobs", new ArrayList<>()); }
        if (!getConfig().contains("disallowedmobs")) { getConfig().set("disallowedmobs", new ArrayList<>()); }
        if (!getConfig().contains("mobcontrol")) { getConfig().createSection("mobcontrol"); }
        if (!getConfig().contains("popcontrolcheckinterval")) { getConfig().set("popcontrolcheckinterval", 200); }
        if (!getConfig().contains("rangelimitcheckinterval")) { getConfig().set("rangelimitcheckinterval", 60); }
        if (!getConfig().contains("rangelimitspeedmodifier")) { getConfig().set("rangelimitspeedmodifier", 3); }
        if (!getConfig().contains("rangelimitmaxattempts")) { getConfig().set("rangelimitmaxattempts", 20 ); }

        saveConfig();
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        PluginManager manager = Bukkit.getPluginManager();
        manager.registerEvents(new Listeners(), this);

        getCommand("lpbld").setExecutor(new BuildCommand());
        getCommand("lpdmg").setExecutor(new DmgCommand());
        getCommand("lpmob").setExecutor(new MobControl(instance));

        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
            if (getConfig().getBoolean("setWeatherToClearOnStart")) Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "weather clear 1000000");
            if (getConfig().getBoolean("disableDaylightCycle")) Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gamerule doDaylightCycle false");
            if (getConfig().getBoolean("setTimeOnStart")) Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "time set " + getConfig().getInt("time"));
            if (getConfig().getBoolean("keepInventory")) Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gamerule keepInventory true");
        });

        // setup the population control checker
        if (getConfig().contains("mobcontrol")) {
        	
        	List<Map<?, ?>> mobControlConfig = getConfig().getMapList("mobcontrol");
        	if (mobControlConfig.size() > 0) {

    	    
        		Iterator<Map<?, ?>> mobit = mobControlConfig.iterator();
        		while (mobit.hasNext()) {
        			
        			HashMap<?, ?> mobConfig = (HashMap<?,?>) mobit.next();

        			Mob mob = new Mob();

                    String mobKey;
                    if (mobConfig.keySet().contains("name")) {
                        mobKey = (String) mobConfig.get("name");
                        mob.setName(mobKey);
                        mob.setGetBy("name");
log.log(Level.INFO, "set name: " + mobKey);
                        
                        //mobControlList.put( ( (String) mobpc.get( "name" ) ),
                            //new MobControl( "name", (int)mobpc.get( "max" ), spawnPoint, (String) mobpc.get( "mobtype" ) ) );
                    } else if (mobConfig.keySet().contains("type") ) {
                        mobKey = (String) mobConfig.get("type");
                        mob.setGetBy("type");
                        
                        //popcontrols.put( ( (String) mobpc.get( "type" ) ).toUpperCase(),
                            //new PopControl( "type", (int)mobpc.get( "max" ), spawnPoint, (String) mobpc.get( "type" ) ) );
                    } else {
                        log.log(Level.WARNING, getLogMsgPrefix() + "Mob control entry doesn't contain required 'name' or 'type' fields");
                        log.log(Level.WARNING, getLogMsgPrefix() + mobConfig.toString());
                        continue;
                    }
log.log(Level.INFO, "mobKey: " + mobKey);
                    
                    if (mobConfig.containsKey("type")) {
                        mob.setType(EntityType.valueOf(((String) mobConfig.get("type")).toUpperCase()));
log.log(Level.INFO, "set type: " + ((String) mobConfig.get("type")).toUpperCase());
                    }
                    
                    if (mobConfig.containsKey("maxallowed")) {
                        mob.setMaxAllowed((int)mobConfig.get("maxallowed"));
                        populationMobCount++;
                        if (!populationMobList.contains(mobKey)) {
                            populationMobList.add(mobKey);
                        }
                    }

        			String[] spawnCoords;
        			if (mobConfig.containsKey("home")) {
        				spawnCoords = ((String)mobConfig.get("home")).split( "," );
        				mob.setHomeLocation(new Location(
        				    Bukkit.getWorld( "world" ),
        				    Double.parseDouble(spawnCoords[0]),
        				    Double.parseDouble(spawnCoords[1]),
        				    Double.parseDouble(spawnCoords[2])
        				));
        			}
        			
        			if (mobConfig.containsKey("radius") && (int)mobConfig.get("radius") > 0) {
        			    mob.setRadius((int)mobConfig.get("radius"));
        			    rangeMobCount++;
                        if (!rangeMobList.contains(mobKey)) {
                            rangeMobList.add(mobKey);
                        }
        			}

        			mobControlList.put(mobKey, mob);
        			
        		}
        		
        	}
        }

        if ( mobControlList.size() > 0 ) {
       	
        	for (String mobKey : mobControlList.keySet()) {

                Mob mob = mobControlList.get(mobKey);

        		List<UUID> moblist = new ArrayList<>();
        		Stream<Entity> entityStream;
        		
        		if (mob.getGetBy().equals("name")) {
        			entityStream = Bukkit.getWorld("world").getEntities().stream()
        				.filter( entity -> {
        					String customName = entity.getCustomName();
        					return customName != null && customName.equals( mobKey );
        				});
        			moblist = (List<UUID>) entityStream.collect(
        					Collectors.mapping( Entity::getUniqueId, Collectors.toList()));
        			popTracker.put( mobKey, moblist );

        		} else {
        			
        			entityStream = Bukkit.getWorld("world").getEntities().stream()
           				.filter( entity -> {
           					String type = entity.getType().name();
          					return type != null && type.equals( mob );
           				});
        			moblist = (List<UUID>) entityStream.collect(
        					Collectors.mapping( Entity::getUniqueId, Collectors.toList()));
        			popTracker.put( mobKey, moblist );
        		}
        	}

        	BukkitTask checkPopulation = new PopChecker(instance).runTaskTimer(
    				instance, getConfig().getLong("popcontrolcheckinterval"),
    				getConfig().getLong("popcontrolcheckinterval")
    		);
    		log.log(Level.INFO, getLogMsgPrefix() + "PopChecker initialized (taskid " + checkPopulation.getTaskId() + ")");
    		

            if (rangeMobCount > 0) {
                BukkitTask checkMobs = new RangeChecker(instance).runTaskTimer(
                    instance, getConfig().getLong( "rangelimitcheckinterval" ),
                    getConfig().getLong( "rangelimitcheckinterval" )
                );
                log.log(Level.INFO, getLogMsgPrefix() + "MobChecker initialized (taskid " + checkMobs.getTaskId() + ")");
            }

        }
    }

    @Override
    public void onDisable() {
		Bukkit.getScheduler().cancelTasks(this);
    }

    public static Main getInstance() {
        return instance;
    }

	// consistent messaging
	public static String getPluginName() {
		return plugin;
	}
	public static String getPluginPrefix() {
		return pluginprefix;
	}
	public String getChatMsgPrefix() {
		return chatmsgprefix;
	}
	public String getLogMsgPrefix() {
		return logmsgprefix;
	}
	public boolean getInConfigUpdate() {
		return inConfigUpdate;
	}
	public void setInConfigUpdate( boolean state ) {
		inConfigUpdate = state;
	}
	public Map<String, Mob> getMobControlList() {
		return mobControlList;
	}
//	public PopControl getPopControl( String mob ) {
//		return Main.popcontrols.get( mob );
//	}
	public List<String> getPopTrackerKeys() {
		return new ArrayList<>( Main.popTracker.keySet() );
	}
	public List<UUID> getPopTrackerUUIDs(String mob) {
		return Main.popTracker.get(mob);
	}
	public int getPopulationMobCount() {
	    return populationMobCount;
	}
	public List<String> getPopulationMobs() {
	    return populationMobList;
	}
	public int getRangeMobCount() {
	    return rangeMobCount;
	}
	public List<String> getRangeMobs() {
	    return rangeMobList;
	}
}
