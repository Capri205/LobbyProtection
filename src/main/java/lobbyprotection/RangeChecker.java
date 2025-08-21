package lobbyprotection;

import java.text.DecimalFormat;
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
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.gamercoder215.mobchip.EntityBrain;
import me.gamercoder215.mobchip.ai.controller.EntityController;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class RangeChecker extends BukkitRunnable {

    static Logger log = Logger.getLogger("Minecraft");

    Main plugin;
    
    private static Map<UUID, Map<String, Long>> homebound = new HashMap<>();
    
    private DecimalFormat df = new DecimalFormat("####0.00");

    RangeChecker(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        
        if (Bukkit.getOnlinePlayers().size() == 0 || plugin.getRangeMobCount() == 0) {
            return;
        }

        for (String mobKey : plugin.getRangeMobs()) {
  
            Stream<Entity> entityStream;
            
            Mob mob = plugin.getMobControlList().get(mobKey);
            
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
            log.log(Level.INFO, "debug - range: " + mobKey + " getEntities: " + mobList.size());

            if (mobList.size() == 0) {
                continue;
            }
            
            Iterator<UUID> mobit = mobList.iterator();
            while (mobit.hasNext()) {
                
                Entity entity = Bukkit.getEntity(mobit.next());

                UUID entityId = entity.getUniqueId();
                String shortId = entityId.toString().substring( entityId.toString().length() - 5 );

                double speedModifier = plugin.getConfig().getDouble("rangelimitspeedmodifier");
                double homeToMobDistance = mob.getHomeLocation().distance( entity.getLocation());
            
                // check if mobs on the way home, reached home or taking too long to get home
                if (homebound.containsKey(entityId)) {
                
                    long totaltimesofar = System.currentTimeMillis()/1000 - homebound.get( entityId ).get( "starttime" );
                    if ( totaltimesofar > ( homebound.get( entityId ).get( "eta" ) * 4 ) ) {

                        // taking too long to get home. remove and respawn
                        log.log(Level.INFO, "debug - mob " + mobKey + " taking too long to get home. Removing " + entity.getUniqueId().toString());
                        homebound.remove( entityId );
                        entity.remove();

                        if (!respawnMob(entity, mob, mobKey)) {
                          log.log(Level.WARNING, this.plugin.getLogMsgPrefix() + "Failed to respawn " + ( mob.getGetBy().equals("type") ? entity.getType().name() : entity.getCustomName() ) + " at home location");
                        }

                    } else if ( homeToMobDistance > 10 ) {

                        // still plodding along
                        log.log(Level.INFO, "debug - mob " + mobKey + "(" + entity.getUniqueId().toString() + " still plodding home");
                        setHomeBound( entity, mob, speedModifier );
                        if ( entity.isGlowing() ) { entity.setGlowing( false ); } else { entity.setGlowing( true ); }
                    
                    } else {
                    
                        // reached home. calculate how long it took the mob to get home
                        log.log(Level.INFO, "debug - mob " + mobKey + "(" + entity.getUniqueId().toString() + " reached home");
                        long endtime = System.currentTimeMillis()/1000L;
                        long journeyTime =  endtime - homebound.get( entityId ).get( "starttime" );
                        homebound.remove( entityId );
                        entity.setGlowing( false );
                    }
                
                    continue;
                }

                // check if mob is out of range of home location and redirect the mob to home
                if (homeToMobDistance > mob.getRadius()) {
                    
                    log.log(Level.INFO, "debug - mob " + mobKey + "(" + entity.getUniqueId().toString() + ") is out of range!");

                    boolean useMobChip = true;
                    // try to set home bound, or resort to teleport if not working
                    try {
                        Class.forName( "me.gamercoder215.mobchip.bukkit.BukkitBrain" );
                    } catch ( NoClassDefFoundError | ClassNotFoundException | ExceptionInInitializerError e ) {
                        useMobChip = false;
                    }
                
                    if (useMobChip) {
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
                        double etaInTicks = homeToMobDistance / mobVelocity;
                        double etaInSeconds = etaInTicks / 20.0;
                        long starttime = System.currentTimeMillis()/1000L;
                        setHomeBound( entity, mob, speedModifier);
                        homebound.put( entityId, Map.of( "eta", (long) etaInSeconds, "starttime", starttime ) );
                    } else {
                    // set a fixed time for teleport
                        long starttime = System.currentTimeMillis()/1000L;
                        homebound.put( entityId, Map.of( "eta", (long) 1, "starttime", starttime ) );
                    }
                }
            }
        }
    }

    /*
     * make mob move to home location
     * @param entity The entity to redirect
     * @param mobLookup The mob to look up in the rangelimit list to get the home location
     * @param speedModifier How fast or slow the mob returns
     */

    private void setHomeBound(Entity entity, Mob mob, double speedModifier) {
        
        org.bukkit.entity.Mob emob = (org.bukkit.entity.Mob) entity;
        
        // try to set home bound, or resort to teleport if not working
        try {
            
            Class.forName("me.gamercoder215.mobchip.bukkit.BukkitBrain");

        } catch ( NoClassDefFoundError | ClassNotFoundException | ExceptionInInitializerError e ) {
            
            //e.printStackTrace();
            log.log(Level.INFO, "debug - teleporting " + (mob.getGetBy().equals("name") ? mob.getName() : mob.getType().name()) + " home");
            emob.teleport( mob.getHomeLocation() );
            return;
        }

        try {
            
            EntityBrain brain = BukkitBrain.getBrain(emob);
            EntityController controller = brain.getController();
            controller.moveTo(mob.getHomeLocation(), speedModifier);
            
        } catch (Exception e) {
            
            emob.teleport(mob.getHomeLocation());
        }
    }

    public static boolean isRangeMob(Mob mob) {
        return mob.getRadius() > 0 ? true : false;
    }
    
    private boolean respawnMob( Entity entity, Mob mob, String mobKey ) {
        
        LivingEntity newMob;
        if (mob.getGetBy().equals("name")) {
            newMob = (LivingEntity) entity.getWorld().spawnEntity(mob.getHomeLocation(), entity.getType());
            newMob.customName(entity.customName());
        } else {
            newMob = (LivingEntity) entity.getWorld().spawnEntity(mob.getHomeLocation(), entity.getType());
        }

        if ( newMob == null ) {
            return false;
        }

        newMob.setInvulnerable( true );
        newMob.setPersistent( true );

        log.log(Level.INFO, "debug - spawned new " + mobKey + " (" + newMob.getUniqueId().toString() + ")");
        // remove the stuck mob
        entity.remove();
        log.log(Level.INFO, "debug - removed mob " + mobKey + " (" + entity.getUniqueId().toString() + ")");
        
        return true;
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
