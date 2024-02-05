package lobbyprotect;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.scheduler.BukkitRunnable;

import me.gamercoder215.mobchip.EntityBrain;
import me.gamercoder215.mobchip.ai.controller.EntityController;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;

import lobbyprotect.Main.RangeMob;

public class MobChecker  extends BukkitRunnable {

	static Logger log = Logger.getLogger("Minecraft");

	Main plugin;
	
	private static Map<String, RangeMob> rangemobs = new HashMap<>();
	private static Map<UUID, Map<String, Long>> homebound = new HashMap<>();
	
	private DecimalFormat df = new DecimalFormat("####0.00");

	MobChecker( Main plugin ) {
		this.plugin = plugin;
		rangemobs = this.plugin.getRangeMobs();
	}
	
	@Override
	public void run() {
		
		// this shouldn't actually be fired as the checker won't hit the scheduler
		// if there's no range check mobs defined in the config, but just in case
		if ( rangemobs.size() == 0 ) {
			return;
		}
			
		Iterator<Entity> eit = Bukkit.getWorld("world").getEntities().iterator();
		while (eit.hasNext()) {

			Entity entity = eit.next();

			String entityType = ChatColor.stripColor( entity.getType().name() );
			String entityName = ChatColor.stripColor( entity.getCustomName() );
			UUID entityId = entity.getUniqueId();
			String shortId = entityId.toString().substring( entityId.toString().length() - 5 );

			// looking up by mob type or name
			boolean doRangeCheck = false;
			String mobLookup = "";
			if ( rangemobs.keySet().contains( entityType ) && rangemobs.get( entityType ).getby().equals( "type" ) ) {
				doRangeCheck = true;
				mobLookup = entityType;
			} else if (	rangemobs.keySet().contains( entityName ) && rangemobs.get( entityName).getby().equals( "name" ) ) {
				doRangeCheck = true;
				mobLookup = entityName;
			}

			// next mob if this one doesn't need checking
			if ( !doRangeCheck ) {
				continue;
			}

			double speedModifier = Main.getInstance().getConfig().getDouble( "rangelimitspeedmodifier" );
			double hometomobdistance = rangemobs.get( mobLookup ).getHome().distance( entity.getLocation() );
			
			// check if mobs on the way home, reached home or taking too long to get home
			boolean thismobhomebound = homebound.containsKey( entityId );
			if ( thismobhomebound ) {
				
				long totaltimesofar = System.currentTimeMillis()/1000 - homebound.get( entityId ).get( "starttime" );
				if ( totaltimesofar > ( homebound.get( entityId ).get( "eta" ) * 4 ) ) {

					// taking too long to get home. remove and respawn
					log.log(Level.INFO,"debug - " + mobLookup + "("+shortId+") - is taking too long to get home. " + totaltimesofar + "s. Removing and respawning");
					homebound.remove( entityId );
					if ( !respawnMob( entity, mobLookup ) ) {
						log.log(Level.WARNING, Main.getInstance().getLogMsgPrefix() + "Failed to respawn " + ( mobLookup.equals( "type" ) ? entity.getType().name() : entity.getCustomName() ) + " at home location");
					}
					
				} else if ( hometomobdistance > 10 ) {

					// still plodding along
					setHomeBound( entity, mobLookup, speedModifier );
					if ( entity.isGlowing() ) { entity.setGlowing( false ); } else { entity.setGlowing( true ); }
					
				} else {
					
					// reached home. calculate how long it took the mob to get home
					long endtime = System.currentTimeMillis()/1000L;
					long journeyTime =  endtime - homebound.get( entityId ).get( "starttime" );
					log.log(Level.INFO," - " + mobLookup + "("+shortId+")," + endtime + "," + journeyTime);
					homebound.remove( entityId );
					entity.setGlowing( false );
					
				}
				
				continue;
			}

			// check if mob is out of range of home location and redirect the mob to home
			if ( hometomobdistance > rangemobs.get( mobLookup ).getRadius() ) {

				// get an estimate of how long it should take this mob to get home, multiply it up and store so we can track progress
				// use a calculated velocity as the mobs actual velocity could be anything when queried - eg stopped or falling
				double mobVelocity = 0.15;
				switch( entity.getType() ) {
					case BEE: mobVelocity = 0.18;
					break;
					case DOLPHIN: mobVelocity = 0.22;
					break;
					case PARROT: mobVelocity = 0.14;
					break;
					case SNIFFER: mobVelocity = 0.05;
					break;
					default: mobVelocity = 0.15;
				}
				double etaInTicks = hometomobdistance / mobVelocity;
				double etaInSeconds = etaInTicks / 20.0;
				long starttime = System.currentTimeMillis()/1000L;
				setHomeBound( entity, mobLookup, speedModifier);
				homebound.put( entityId, Map.of( "eta", (long) etaInSeconds, "starttime", starttime ) );
				log.log(Level.INFO," - " + mobLookup + "("+shortId+")," + mobVelocity + "," + df.format( hometomobdistance )+ "," + Math.round( etaInSeconds ) + "(" + Math.round( etaInSeconds * 4 ) +")," + starttime );
			}
		}
	}

	/*
	 * attempt to respawn a stuck mob
	 * @param entity the mob that is stuck
	 * @param lookupType how to lookup the home from the rangemobs hash
	 * @returns true or false
	 */
	private boolean respawnMob( Entity entity, String lookupType ) {
	
		LivingEntity newMob = (LivingEntity) entity.getWorld().spawnEntity( rangemobs.get( lookupType ).getHome(), entity.getType() );
		if ( newMob == null ) {
			return false;
		}
		// if lookup type is by name then set the custom name
		if ( lookupType.equals( "name" ) ) {
			newMob.setCustomName( entity.getCustomName() );
		}
		// remove the stuck mob
		entity.remove();
		
		return true;
	}

	/*
	 * make mob move to home location
	 * @param entity The entity to redirect
	 * @param mobLookup The mob to look up in the rangelimit list to get the home location
	 * @param speedModifier How fast or slow the mob returns
	 */
	private void setHomeBound( Entity entity, String mobLookup, double speedModifier ) {
		Mob mob = (Mob) entity;
		EntityBrain brain = BukkitBrain.getBrain( mob );
		EntityController controller = brain.getController();
		controller.moveTo( rangemobs.get( mobLookup ).getHome(), speedModifier );
	}

}
