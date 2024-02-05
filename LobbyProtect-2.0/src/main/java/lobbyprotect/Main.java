package lobbyprotect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import net.md_5.bungee.api.ChatColor;

import lobbyprotect.commands.BuildCommand;
import lobbyprotect.commands.DmgCommand;
import lobbyprotect.commands.MobControl;
import lobbyprotect.listeners.Listeners;

public final class Main extends JavaPlugin {

	Logger log = Logger.getLogger("Minecraft");

    private static Main instance;

	private static String plugin = "LobbyProtect";
	private static String pluginprefix = "[" + plugin + "]";
	private static String chatmsgprefix = ChatColor.AQUA + "" + ChatColor.BOLD + plugin + ChatColor.DARK_GRAY + ChatColor.BOLD + " » " + ChatColor.LIGHT_PURPLE + "";
	private static String logmsgprefix = pluginprefix + " » ";
	
	private static boolean inConfigUpdate = false;
	
	public record RangeMob( String getby, Location home, int radius ) {
		public RangeMob( String getby, Location home, int radius ) {
			this.getby = getby;
			this.home = home;
			this.radius = radius;
		}
		public String getGetby() {
			return this.getby;
		}
		public Location getHome() {
			return this.home;
		}
		public int getRadius() {
			return this.radius;
		}
	}
	private static Map<String, RangeMob> rangemobs  = new HashMap<>();
	
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
        if (!getConfig().contains("disableBlockPlace")) { getConfig().set("disableBlockPlace", true); }
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
        if (!getConfig().contains("time")) { getConfig().set("time", 12000); }
        if (!getConfig().contains("keepInventory")) { getConfig().set("keepInventory", true); }
        if (!getConfig().contains("stopallspawning")) { getConfig().set("stopallspawning", false); }
        if (!getConfig().contains("allowedmobs")) { getConfig().set("allowedmobs", new ArrayList<>()); }
        if (!getConfig().contains("disallowedmobs")) { getConfig().set("disallowedmobs", new ArrayList<>()); }
        if (!getConfig().contains("populationenforcement")) { getConfig().createSection("populationenforcement", new HashMap<String, Object>()); }
        if (!getConfig().contains("rangelimits")) { getConfig().createSection("rangelimits"); }
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
        getCommand("lpmob").setExecutor(new MobControl());

        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
            if (getConfig().getBoolean("setWeatherToClearOnStart")) Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "weather clear 1000000");
            if (getConfig().getBoolean("disableDaylightCycle")) Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gamerule doDaylightCycle false");
            if (getConfig().getBoolean("setTimeOnStart")) Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "time set " + getConfig().getInt("time"));
            if (getConfig().getBoolean("keepInventory")) Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gamerule keepInventory true");
        });
        
        // populate our range limit checker
        if ( getConfig().contains("rangelimits") ) {
        
        	List<Map<?, ?>> rangelimits = getConfig().getMapList("rangelimits");
        	if ( rangelimits.size() > 0 ) {
        		
        		Iterator<Map<?, ?>> rlkeys = rangelimits.iterator();
        		while ( rlkeys.hasNext() ) {
        			
        			HashMap<?,?> mobrl = (HashMap<?,?>) rlkeys.next();
        			//TODO: add some validation, man
        			String[] homecoords = ( (String) mobrl.get( "home" ) ).split( "," );
        			Location home = new Location( Bukkit.getWorld( "world" ), Double.parseDouble( homecoords[0] ), Double.parseDouble( homecoords[1] ), Double.parseDouble( homecoords[2] ) );
        			if ( mobrl.keySet().contains("type") ) {
        				rangemobs.put( ( (String) mobrl.get("type") ).toUpperCase(), new RangeMob( "type", home, (int)mobrl.get("radius") ) );
        			} else if ( mobrl.keySet().contains("name") ) {
        				rangemobs.put( (String) mobrl.get("name"), new RangeMob( "name", home, (int)mobrl.get("radius") ) );
        			} else {
        				log.log(Level.WARNING, getLogMsgPrefix() + "Range limit entry doesn't contain required 'name' or 'type' fields");
        				log.log(Level.WARNING, getLogMsgPrefix() + mobrl.toString());
        			}
        		}
        	}
        }

        // start up our range limit checker task
        if ( rangemobs.size() > 0 ) {
    		BukkitTask checkMobs = new MobChecker( instance ).runTaskTimer(
    				instance, getConfig().getLong( "rangelimitcheckinterval" ),
    				getConfig().getLong( "rangelimitcheckinterval" )
    		);
    		log.log(Level.INFO, getLogMsgPrefix() + "MobChecker initialized (taskid " + checkMobs.getTaskId() + ")");
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
	public Map<String, RangeMob> getRangeMobs() {
		return Main.rangemobs;
	}
}
