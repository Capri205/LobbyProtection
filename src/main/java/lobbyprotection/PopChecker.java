package lobbyprotection;

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
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class PopChecker  extends BukkitRunnable{

	static Logger log = Logger.getLogger("Minecraft");

	Main plugin;
	
	public PopChecker(Main plugin) {
		
		this.plugin = plugin;
	}

	@Override
	public void run() {

		// we don't need population control if nobody is there to benefit from it
		// also popcontrol size should never be zero here, but but just in case
		if (Bukkit.getOnlinePlayers().size() == 0 || plugin.getPopulationMobCount() == 0) {
			return;
		}
		
    	for (String mobKey : plugin.getPopulationMobs()) {
    		
    		Stream<Entity> entityStream;
    		
    		Mob mob = plugin.getMobControlList().get(mobKey);
    		
            boolean hasHomeLocation = mob.getHomeLocation() != null ? true : false;

    		// skip if home chunk not loaded or no player nearby
    		if (hasHomeLocation && !isChunkLoaded(mob.getHomeLocation())) {
                log.log(Level.INFO,"debug - chunk not loaded for home for " + mob + ". Skipping mob");
                continue;
            }
    		if (!isPlayerNearby(mob.getHomeLocation(), 96)) {
    		    log.log(Level.INFO, "debug - pop: no player nearby, skip " + mobKey);
    		    continue;
    		}

    		// get a list current mobs. we get uuid's but really only interested in the
    		// mob count at this point. might do more with the uuids later perhaps
    		List<UUID> mobList;
    		if (mob.getGetBy().equals("name")) {
    			entityStream = Bukkit.getWorld("world").getEntities().stream()
    			    .filter( entity -> {
    					String customName = entity.getCustomName();
    					return customName != null && customName.equals(mobKey);
    				});
    			mobList = (List<UUID>) entityStream.collect(
    			    Collectors.mapping( Entity::getUniqueId, Collectors.toList())
    			);
    		} else {
    			entityStream = Bukkit.getWorld("world").getEntities().stream()
       				.filter( entity -> {
       					EntityType type = entity.getType();
      					return type != null && type.name().equals( mobKey );
       				});
    			mobList = (List<UUID>) entityStream.collect(
    			    Collectors.mapping( Entity::getUniqueId, Collectors.toList())
    			);
    		}
    		log.log(Level.INFO, "debug - pop: " + mobKey + " getEntities: " + mobList.size());
    		
		    //log.log(Level.INFO, "debug - " + mob + " getMobCountArea1: " + getMobCountArea(mobKey, mob.getHomeLocation(), mob.getRadius(), mob.getGetBy()));
	        //log.log(Level.INFO, "debug - " + mob + " getMobCountArea2: " + getMobCountArea(mobKey, mob.getHomeLocation(), Bukkit.getWorld("world").getWorldBorder().getSize()/2, mob.getGetBy()));

			// check if we're under populated to max
    		int mobCountToAdd = 0;
    		int mobCountToRemove = 0;
			int maxAllowed = mob.getMaxAllowed();
			mobCountToAdd = ( maxAllowed - mobList.size( ) );
			mobCountToRemove = mobList.size() - maxAllowed;
			log.log(Level.INFO, "debug - " + mobKey + " - mobCountToAdd: " + mobCountToAdd);
			log.log(Level.INFO, "debug - " + mobKey + " - mobCountToRemove: " + mobCountToRemove);

			EntityType type = null;
			if (mob.getGetBy().equals("name")) {
				type = mob.getType();
			} else {
				type = EntityType.valueOf(mobKey);
			}
			
			// add mobs up to max specified
			if (mobCountToAdd > 0) {
			    while( mobCountToAdd > 0 ) {
			        spawnMob(mobKey, mob);
			        mobCountToAdd--;
			    }
			}
			if (mobCountToRemove > 0) {
			    removeMobs(mobKey, mob, mobCountToRemove);
			}
    	}
	}
	
	/*
	 * 
	 */
	public int getMobCountArea(String mobKey, Location center, double radius, boolean byName) {
	    
	    World world = center.getWorld();
	    int mobCount = 0;

	    for (Entity entity : world.getNearbyEntities(center, radius, radius, radius)) {
	        if (byName && entity.getCustomName() != null && entity.getCustomName().equals(mobKey)) {
	            mobCount++;
	        } else if (!byName && entity.getType().name().equals(mobKey)) {
	            mobCount++;
	        }
	    }

	    return mobCount;
	}
	/*
	 * attempt to spawn a mob by type or name at a location
	 * @param mobType - entity type of the mob
	 * @param spawnpoint - location to spawn
	 * @param addName - whether we add the
	 * @returns true or false
	 */
	private boolean spawnMob(String mobKey, Mob mob) {
	
	    log.log(Level.INFO, "debug - spawning mob " + mobKey + ", type: " + mob.getType().name());
		LivingEntity newMob = (LivingEntity) Bukkit.getWorld("world").spawnEntity(mob.getHomeLocation(), mob.getType());
//		    if ( newMob == null ) {
//			log.log(Level.WARNING, this.plugin.getLogMsgPrefix() + "Warning! Failed to spawn new mob " + mob);
//			return false;
//		}
		if (mob.getGetBy().equals("name")) {
			newMob.setCustomName( mobKey );
		}
		newMob.setInvulnerable( true );
		newMob.setPersistent( true );
		newMob.setNoActionTicks(999999999);
		
		return true;
	}

	public void removeMobs(String mobKey, Mob mob, int count) {
	    log.log(Level.INFO, "debug - should be removing a " + mobKey);
	    for (Entity entity : Bukkit.getWorld("world").getEntities()) {
	        if (mob.getGetBy().equals("name") && entity.getCustomName() != null && entity.getCustomName().equals(mobKey)) {
	            entity.remove();
	            count--;
	        } else {
	            if (entity.getType() == mob.getType()) {
	                entity.remove();
	                count--;
	            }
	        }
            log.log(Level.INFO, "debug - removed mob " + mobKey + ", id " + entity.getUniqueId().toString());
            if (count == 0) {
                break;
            }
	    }
	}

    public boolean isChunkLoaded(Location loc) {
        return loc.getWorld().isChunkLoaded(loc.getChunk());
    }

	public boolean isPlayerNearby(Location home, double radius) {
	    for (Player player : home.getWorld().getPlayers()) {
	        if (player.getLocation().distanceSquared(home) <= radius * radius) {
	            return true;
	        }
	    }
	    return false;
	}

}
