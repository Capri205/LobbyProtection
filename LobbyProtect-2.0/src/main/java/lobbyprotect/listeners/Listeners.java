package lobbyprotect.listeners;

import lobbyprotect.Main;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Listeners implements Listener {
//---------------------------------------------------------------------------------------------------------------------
	static Logger log = Logger.getLogger("Minecraft");
    private static Map<UUID, Boolean> map = new HashMap<>();
    private static boolean dmg = false;
//---------------------------------------------------------------------------------------------------------------------
    public void onCommand(Player player) {
   		if (map.get(player.getUniqueId())) {
   			map.put(player.getUniqueId(), false);
   			player.setGameMode(GameMode.SURVIVAL);
   		} else {
   			map.put(player.getUniqueId(), true);
  			player.setGameMode(GameMode.CREATIVE);
   		}
    }
//---------------------------------------------------------------------------------------------------------------------
    public boolean onDmgCommand() {
   		if (dmg) dmg = false;
   		else dmg = true;
   		return dmg;
    }
//---------------------------------------------------------------------------------------------------------------------
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        map.put(event.getPlayer().getUniqueId(), false);
        event.getPlayer().setGameMode(GameMode.SURVIVAL);
        event.getPlayer().setHealth(event.getPlayer().getMaxHealth());
        event.getPlayer().setFoodLevel(20);
        if (Main.getInstance().getConfig().getBoolean("disablePlayerJoinMessage")) event.setJoinMessage(null);
        if (Main.getInstance().getConfig().getBoolean("clearInventoryOnJoin")) event.getPlayer().getInventory().clear();
        if (Main.getInstance().getConfig().getBoolean("clearArmorOnJoin")) {
            event.getPlayer().getInventory().setHelmet(null);
            event.getPlayer().getInventory().setChestplate(null);
            event.getPlayer().getInventory().setLeggings(null);
            event.getPlayer().getInventory().setBoots(null);
        }
        if (Main.getInstance().getConfig().getBoolean("xpLevelResetOnJoin")) {
            event.getPlayer().setExp(0);
            event.getPlayer().setLevel(0);
        }
    }
//---------------------------------------------------------------------------------------------------------------------
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!Main.getInstance().getConfig().getBoolean("disableBlockPlace")) return;
        map.putIfAbsent(event.getPlayer().getUniqueId(), false);
        if (!map.get(event.getPlayer().getUniqueId())) event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!Main.getInstance().getConfig().getBoolean("disableBlockBreak")) return;
        map.putIfAbsent(event.getPlayer().getUniqueId(), false);
        if (!map.get(event.getPlayer().getUniqueId())) event.setCancelled(true);
    }
    
    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
    	if (!Main.getInstance().getConfig().getBoolean("disableBlockBreak")) return;
    	event.setCancelled(true);
    }

    @EventHandler
    public void onHangingBreak(HangingBreakByEntityEvent event) {
    	if (!Main.getInstance().getConfig().getBoolean("disableBlockBreak")) return;
    	if (event.getRemover() instanceof Player ) {
    		Player player = (Player) event.getRemover();
    		map.putIfAbsent(player.getUniqueId(), false);
    		if (!map.get(player.getUniqueId())) event.setCancelled(true);
    	}
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (!Main.getInstance().getConfig().getBoolean("disablePlayerDamage")) return;
        if (!dmg) event.setCancelled(true);
    }

    @EventHandler
    public void onFrameEntityDamage(EntityDamageByEntityEvent event) {
    	if (!Main.getInstance().getConfig().getBoolean("disableBlockBreak")) return;
    	if (event.getEntity() instanceof ItemFrame && event.getDamager() instanceof Player) {
    		Player player = (Player) event.getDamager();
    		if (!map.get(player.getUniqueId())) event.setCancelled(true);
    	}
    	if (event.getDamager() instanceof Projectile) {
    		event.setCancelled(true);
    	}
    }

    // prevents block spreading, like fire, mushrooms etc, whilst still allowing blocks to burn
    // blocks breaking by fire is handled by BlockBurnEvent
    @EventHandler
    public void blockIgnition(BlockIgniteEvent event) {
    	if (!Main.getInstance().getConfig().getBoolean("disableBlockSpread")) return;
    	if (event.getCause().equals(IgniteCause.SPREAD)) {
    		event.setCancelled(true);
    	}
    }

    @EventHandler
    public void FrameRotate(PlayerInteractEntityEvent event) {
    	if (!Main.getInstance().getConfig().getBoolean("disableBlockBreak")) return;
    	if (event.getRightClicked().getType().equals(EntityType.ITEM_FRAME)) {
    		Player player = event.getPlayer();
    		if (!map.get(player.getUniqueId())) event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!Main.getInstance().getConfig().getBoolean("disableFoodLevelChange")) return;
        map.putIfAbsent((event.getEntity().getUniqueId()), false);
        if (!map.get((event.getEntity().getUniqueId()))) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (!Main.getInstance().getConfig().getBoolean("disablePlayerPickupItem")) return;
        map.putIfAbsent(event.getPlayer().getUniqueId(), false);
        if (!map.get(event.getPlayer().getUniqueId())) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (!Main.getInstance().getConfig().getBoolean("disablePlayerDropItem")) return;
        map.putIfAbsent(event.getPlayer().getUniqueId(), false);
        if (!map.get(event.getPlayer().getUniqueId())) event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!Main.getInstance().getConfig().getBoolean("disableInventoryClickEvent")) return;
        map.putIfAbsent(event.getWhoClicked().getUniqueId(), false);
        if (!map.get(event.getWhoClicked().getUniqueId())) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (Main.getInstance().getConfig().getBoolean("disablePlayerQuitMessage")) event.setQuitMessage(null);
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        if (Main.getInstance().getConfig().getBoolean("disablePlayerKickMessage")) event.setLeaveMessage(null);
    }
    
    @EventHandler
    public void onMobSpawn( EntitySpawnEvent event ) {
    	
    	Entity spawnedmob = event.getEntity();
    	if ( spawnedmob instanceof org.bukkit.entity.LivingEntity ) {

    		// first check if stop all spawning is on
    		if ( Main.getInstance().getConfig().getBoolean( "stopallspawning" ) ) {
    			event.setCancelled( true );
    		} else {
    		
    			// next check allowed list
    			if ( Main.getInstance().getConfig().getList( "allowedmobs" ).size() > 0 &&
    				!Main.getInstance().getConfig().getList( "allowedmobs" ).contains( event.getEntityType().toString() ) ) {
    				event.setCancelled( true );
    				return;
    			}
    			// then check disallowed list
    			if ( Main.getInstance().getConfig().getList( "disallowedmobs" ).size() > 0 &&
    				Main.getInstance().getConfig().getList( "disallowedmobs" ).contains( event.getEntityType().toString() ) ) {
    				event.setCancelled( true );
    			}
    		}
    		
    		// finally check if we have have any population limits in place for the spawned mob
    		Map<String, Integer> moblimits = new HashMap<>();
    		for ( String key : Main.getInstance().getConfig().getConfigurationSection( "populationenforcement" ).getKeys( false ) ) {
    			moblimits.put( key,  Main.getInstance().getConfig().getConfigurationSection( "populationenforcement" ).getInt( key ) );
    		}
    		if ( moblimits.size() > 0 && moblimits.keySet().contains( spawnedmob.getType().toString() ) ) {
    			int mobcount = 0;
    			Iterator<Entity> eit = Bukkit.getWorld("world").getEntities().iterator();
				while (eit.hasNext()) {
					Entity entity = eit.next();
					if ( entity.getType().toString().equals( spawnedmob.getType().toString() ) ) {
						mobcount++;
					}
				}
				if ( mobcount >= moblimits.get( spawnedmob.getType().toString() ) ) {
					event.setCancelled( true );
				}
    		}
    	}
    }
}
