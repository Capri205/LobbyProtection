package lobbyprotection.listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Dolphin;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;

import lobbyprotection.Main;

public class Listeners implements Listener {
//---------------------------------------------------------------------------------------------------------------------
	static Logger log = Logger.getLogger("Minecraft");
    private static Map<UUID, Boolean> map = new HashMap<>();
    private static boolean dmg = false;
    
	Main plugin = Main.getInstance();

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
    	if (!(event.getEntity() instanceof Player)) {
    		if ( event.getEntity() instanceof Dolphin ) {
    			if ( event.getCause().equals( EntityDamageEvent.DamageCause.DROWNING ) && Main.getInstance().getConfig().getBoolean("disableDolphinDrown") ) {
    				Dolphin dolphin = (Dolphin) event.getEntity();
    				dolphin.setRemainingAir(Integer.MAX_VALUE);
    				event.setCancelled( true );
    			}
    		}
    	} else {
    		if (!Main.getInstance().getConfig().getBoolean("disablePlayerDamage")) return;
    		if (!dmg) event.setCancelled(true);
    	}
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

    @EventHandler
    public void onEntityInteract(EntityInteractEvent event) {
    	if (!Main.getInstance().getConfig().getBoolean("disableFarmBreak")) return;
        Block block = event.getBlock();
        if(block != null && block.getType() == Material.FARMLAND) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
    	if (!Main.getInstance().getConfig().getBoolean("disableFarmBreak")) return;
        if(event.getAction() == Action.PHYSICAL) {
            if(event.getClickedBlock().getType() == Material.FARMLAND) {
                event.setCancelled(true);
            }
        }
    }
    
    // prevents block spreading, like fire, mushrooms etc, whilst still allowing blocks to burn
    @EventHandler
    public void blockIgnition(BlockSpreadEvent event) {
    	if (!Main.getInstance().getConfig().getBoolean("disableBlockSpread")) return;
    	event.setCancelled(true);
    }
    // prevents block breaking as a result of fire
    @EventHandler
    public void blockIgnition(BlockBurnEvent event) {
    	if (!Main.getInstance().getConfig().getBoolean("disableBlockBreak")) return;
    	event.setCancelled(true);
    }

    // prevent leaf decay
    @EventHandler
    public void blockIgnition( LeavesDecayEvent event) {
    	if (!Main.getInstance().getConfig().getBoolean("disableLeafDecay")) return;
    	event.setCancelled(true);
    }

    // prevent ice melting
    @EventHandler
    public void blockMelt( BlockFadeEvent event ) {
    	if ( !Main.getInstance().getConfig().getBoolean( "disableIceMelt") ) return;
    	if ( event.getBlock().getType().equals( Material.ICE ) ) {
    		event.setCancelled( true );
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
    public void onMobSpawn(EntitySpawnEvent event) {
    	
    	Entity spawnedMob = event.getEntity();
    	String spawnedMobType = spawnedMob.getType().toString();
    	
    	if (!( spawnedMob instanceof org.bukkit.entity.LivingEntity )) {
    	    return;
    	}

    	FileConfiguration config = plugin.getConfig();
    		
    	// spawning control
    	if (config.getBoolean( "stopallspawning")) {
    		event.setCancelled(true);
    		return;
    	}
		if (config.getList("allowedmobs").size() > 0 && !config.getList("allowedmobs").contains(spawnedMobType)) {
			event.setCancelled(true);
			return;
		}
		if (config.getList("disallowedmobs").size() > 0 && config.getList("disallowedmobs").contains(spawnedMobType)) {
			event.setCancelled(true);
			return;
		}

		// population control
		if (plugin.getPopulationMobCount() == 0) {
			return;
		}
	    String spawnedMobName = spawnedMob.getCustomName();
    	boolean checkByName = false;
    	if (spawnedMobName != null) {
    	    checkByName = true;
    	    if (!plugin.getPopulationMobs().contains(spawnedMobName)) {
                return;
    	    }
    	} else {
    	    if (!plugin.getPopulationMobs().contains(spawnedMobType)) {
    	        return;
    	    }
		}

		// get count of matching mobs by type or name currently in the world
		// Note that these counts aren't always perfect due to timing or things like chunk loading/unloading
		int mobCount = 0;
		if (checkByName) {
			mobCount = (int) Bukkit.getWorld("world").getEntities().stream()
			.map( m -> m.getCustomName())
			.filter( customName -> Objects.nonNull(customName))
			.filter( customName -> customName.equals(spawnedMobName)).count();
		} else {
			mobCount = (int) Bukkit.getWorld("world").getEntities().stream()
			.map( m -> m.getType().name())
			.filter( type -> Objects.nonNull(type))
			.filter( type -> type.equals(spawnedMobType)).count();
		}
		if (mobCount > plugin.getMobControlList().get(checkByName ? spawnedMobName : spawnedMobType ).getMaxAllowed()) {
			event.setCancelled( true );
    	}
    }
}
