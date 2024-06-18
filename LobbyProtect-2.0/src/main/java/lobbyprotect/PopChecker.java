package lobbyprotect;

import java.util.ArrayList;
import java.util.HashMap;
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
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import lobbyprotect.Main.PopControl;

public class PopChecker  extends BukkitRunnable{

	static Logger log = Logger.getLogger("Minecraft");

	Main plugin;
	
	private static Map<String, PopControl> popcontrol = new HashMap<>();
	public PopChecker(Main plugin) {
		
		this.plugin = plugin;
		popcontrol = this.plugin.getPopControls();
	}

	@Override
	public void run() {

		// we don't need population control if nobody is there to benefit from it
		// also popcontrol size should never be zero here, but but just in case
		if ( Bukkit.getOnlinePlayers().size() == 0 || popcontrol.size() == 0 ) {
			return;
		}
		
		// create a list of mobs in the world
    	for ( String mob : popcontrol.keySet() ) {
    		
    		List<UUID> moblist = new ArrayList<>();
    		Stream<Entity> entityStream;
    		
	    	boolean checkByName = false;
	    	if( popcontrol.get( mob ).getGetBy().equals( "name" ) ) {
	    		checkByName = true;
			}
    		
    		// get a list current mobs. we get uuid's but really only interested in the
    		// mob count at this point. might do more with the uuids later perhaps
    		if ( checkByName ) {

    			entityStream = Bukkit.getWorld("world").getEntities().stream()
    				.filter( entity -> {
    					String customName = entity.getCustomName();
    					return customName != null && customName.equals( mob );
    				});
    			moblist = (List<UUID>) entityStream.collect(
   					Collectors.mapping( Entity::getUniqueId, Collectors.toList()));

    		} else {

    			entityStream = Bukkit.getWorld("world").getEntities().stream()
       				.filter( entity -> {
       					String type = entity.getType().name();
      					return type != null && type.equals( mob );
       				});
    			moblist = (List<UUID>) entityStream.collect(
   					Collectors.mapping( Entity::getUniqueId, Collectors.toList()));
    		}

			// check if we're under populated to max
			int max = popcontrol.get( mob ).getMax();
			if ( moblist.size() >= max ) {
				continue;
    		}
			
			int mobstoadd = ( max - moblist.size( ) );

			// get spawn and if not provided get home from range limits for this mob if there, otherwise can't spawn
			Location spawnpoint = popcontrol.get( mob ).getSpawnPoint();
			if ( spawnpoint == null ) {
				if ( Main.getInstance().getRangeMobs().containsKey( mob ) ) {
					spawnpoint = Main.getInstance().getRangeMob( mob ).getHome();
				}
				if ( spawnpoint == null ) {
					log.log(Level.WARNING, this.plugin.getLogMsgPrefix() + "Unable to find a spawn point for spawning " + mob);
					log.log(Level.WARNING, this.plugin.getLogMsgPrefix() + "Please set one in the configuration file");
					continue;
				}
			}
			
			// for named mobs we need to get the mob type from the config
			EntityType mobType = null;
			if ( checkByName ) {
				mobType = EntityType.valueOf( popcontrol.get( mob ).getMobType() );
			} else {
				mobType = EntityType.valueOf( mob );
			}
			
			// add mobs up to max specified
			while( mobstoadd > 0 ) {
				spawnMob(mobType, spawnpoint, ( checkByName ? mob : "" ) );
				mobstoadd--;
			}
			
    	}
			
	}
	
	/*
	 * attempt to spawn a mob by type or name at a location
	 * @param mobType - entity type of the mob
	 * @param spawnpoint - location to spawn
	 * @param addName - whether we add the
	 * @returns true or false
	 */
	private boolean spawnMob( EntityType mobType, Location spawnpoint, String mob ) {
	
		LivingEntity newMob;
		newMob = (LivingEntity) Bukkit.getWorld( "world" ).spawnEntity( spawnpoint, mobType );
		if ( newMob == null ) {
			log.log(Level.WARNING, this.plugin.getLogMsgPrefix() + "Warning! Failed to spawn new mob " + mob);
			return false;
		}
		if ( !mob.isEmpty() ) {
			newMob.setCustomName( mob );
		}

		newMob.setInvulnerable( true );
		newMob.setPersistent( true );
		newMob.setNoActionTicks(999999999);
		
		return true;
	}
}
