package lobbyprotection.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import lobbyprotection.Main;
import lobbyprotection.Mob;

public class MobControl implements CommandExecutor {

	static Logger log = Logger.getLogger("Minecraft");

    Main plugin;

	private String chatmsgprefix = null;
	private String logmsgprefix = null;
	
	public MobControl(Main plugin) {

        this.plugin = plugin;
        
		chatmsgprefix = Main.getInstance().getChatMsgPrefix();
		logmsgprefix = Main.getInstance().getLogMsgPrefix();
	}
	
	@Override
	public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
		
		
		// add mob to allowed list
		// remove mob from allowed list
		// add mob to disallowed list
		// remove mob from disallowed list
		// add mob to population control list
		// remove mob from population control list
		
		if (!( commandSender instanceof Player ) ) return false;
		
		if ( command.getName().equalsIgnoreCase( "lpmob" ) ) {
			
			if ( ( args.length == 2 && !args[1].equals( "list" ) ) || args.length < 2 ) {
				Usage( commandSender );
				return true;
			}
			
			FileConfiguration config = Main.getInstance().getConfig();
			
			if ( args[0].equals( "allowed" ) ) {
				
				List<String> allowedmobs = config.getStringList( "allowedmobs" );

				if ( args[1].equals( "add" ) ) { 

					List<String> moblist = new ArrayList<>( Arrays.asList( removeDuplicates( args[2].toUpperCase() ).split( "\\s*,\\s*" ) ) );
					
					String addedmobs = "";
					Iterator<String> mobit = moblist.iterator();
					while( mobit.hasNext() ) {
						
						String mob = mobit.next();
						if ( !validMob( mob ) ) {
							commandSender.sendMessage( chatmsgprefix + ChatColor.RED + "'" + mob + "' is not a valid living entity" );
							mobit.remove();
							continue;
						}
						if ( allowedmobs.contains( mob ) ) {
							commandSender.sendMessage( chatmsgprefix + ChatColor.GOLD + mob + " is already on the allowed list" );
							mobit.remove();
							continue;
						}
						allowedmobs.add( mob );
						if ( addedmobs.isBlank() ) { addedmobs += mob; } else { addedmobs += "," + mob; }
					}
					if ( moblist.isEmpty() ) {
						commandSender.sendMessage( chatmsgprefix + ChatColor.GOLD + "No mobs added to the allowed list" );
					} else {
						allowedmobs.sort( String::compareToIgnoreCase );
						config.set( "allowedmobs", allowedmobs );
						Main.getInstance().saveConfig();
						commandSender.sendMessage( chatmsgprefix + ChatColor.GREEN + "" + moblist.size() + " mob" +
							( moblist.size() != 1 ? "s were" : " was") + " added to the allowed list: " + addedmobs );
					}
					
					return true;
				}
				
				if ( args[1].equals( "remove" ) ) {
					
					if ( allowedmobs.isEmpty() ) {
						commandSender.sendMessage( chatmsgprefix + ChatColor.GOLD + "Allowed list is empty. Nothing to remove." );
						return true;
					}
					
					List<String> moblist = new ArrayList<>( Arrays.asList( removeDuplicates( args[2].toUpperCase() ).split( "\\s*,\\s*" ) ) );
					
					String removedmobs = "";
					Iterator<String> mobit = moblist.iterator();
					while( mobit.hasNext() ) {
						
						String mob = mobit.next();
						if ( !validMob( mob ) ) {
							commandSender.sendMessage( chatmsgprefix + ChatColor.RED + "'" + mob + "' is not a valid living entity" );
							mobit.remove();
							continue;
						}
						if ( !allowedmobs.contains( mob ) ) {
							commandSender.sendMessage( chatmsgprefix + ChatColor.GOLD + mob + " is not in the allowed list" );
							mobit.remove();
							continue;
						}
						allowedmobs.remove( mob );
						if ( removedmobs.isBlank() ) { removedmobs += mob; } else { removedmobs += "," + mob; }
					}
					if ( moblist.isEmpty() ) {
						commandSender.sendMessage( chatmsgprefix + ChatColor.GOLD + "No mobs removed from the allowed list" );
					} else {
						allowedmobs.sort( String::compareToIgnoreCase );
						config.set( "allowedmobs", allowedmobs );
						Main.getInstance().saveConfig();
						commandSender.sendMessage( chatmsgprefix + ChatColor.GREEN + "" + moblist.size() + " mob" +
							( moblist.size() != 1 ? "s were" : " was") + " removed from the allowed list: " + removedmobs );
					}
					
					return true;
				}
				
				if ( args[1].equals( "list" ) ) {
					if ( allowedmobs.isEmpty() ) {
						commandSender.sendMessage( chatmsgprefix + ChatColor.GOLD + "Allowed list is empty" );
					} else {
						commandSender.sendMessage( chatmsgprefix + ChatColor.GOLD + "Allowed list contains " + allowedmobs.size() + " mob" + ( allowedmobs.size() != 1 ? "s" : "" ) );
						allowedmobs.forEach( thismob -> {
							commandSender.sendMessage( chatmsgprefix + ChatColor.GOLD + "  " + thismob );
						});
					}
				}
			}
			
			if ( args[0].equals( "disallowed" ) ) {
				
				List<String> disallowedmobs = config.getStringList( "disallowedmobs" );

				if ( args[1].equals( "add" ) ) { 

					List<String> moblist = new ArrayList<>( Arrays.asList( removeDuplicates( args[2].toUpperCase() ).split( "\\s*,\\s*" ) ) );
					
					String addedmobs = "";
					Iterator<String> mobit = moblist.iterator();
					while( mobit.hasNext() ) {
						
						String mob = mobit.next();
						if ( !validMob( mob ) ) {
							commandSender.sendMessage( chatmsgprefix + ChatColor.RED + "'" + mob + "' is not a valid living entity" );
							mobit.remove();
							continue;
						}
						if ( disallowedmobs.contains( mob ) ) {
							commandSender.sendMessage( chatmsgprefix + ChatColor.GOLD + mob + " is already on the disallowed list" );
							mobit.remove();
							continue;
						}
						disallowedmobs.add( mob );
						if ( addedmobs.isBlank() ) { addedmobs += mob; } else { addedmobs += "," + mob; }
					}
					if ( moblist.isEmpty() ) {
						commandSender.sendMessage( chatmsgprefix + ChatColor.GOLD + "No mobs added to the disallowed list" );
					} else {
						disallowedmobs.sort( String::compareToIgnoreCase );
						config.set( "disallowedmobs", disallowedmobs );
						Main.getInstance().saveConfig();
						commandSender.sendMessage( chatmsgprefix + ChatColor.GREEN + "" + moblist.size() + " mob" +
							( moblist.size() != 1 ? "s were" : " was") + " added to the disallowed list: " + addedmobs );
					}
					
					return true;
				}
				
				if ( args[1].equals( "remove" ) ) {
					
					if ( disallowedmobs.isEmpty() ) {
						commandSender.sendMessage( chatmsgprefix + ChatColor.GOLD + "Disallowed list is empty. Nothing to remove." );
						return true;
					}
					
					List<String> moblist = new ArrayList<>( Arrays.asList( removeDuplicates( args[2].toUpperCase() ).split( "\\s*,\\s*" ) ) );
					
					String removedmobs = "";
					Iterator<String> mobit = moblist.iterator();
					while( mobit.hasNext() ) {
						
						String mob = mobit.next();
						if ( !validMob( mob ) ) {
							commandSender.sendMessage( chatmsgprefix + ChatColor.RED + "'" + mob + "' is not a valid living entity" );
							mobit.remove();
							continue;
						}
						if ( !disallowedmobs.contains( mob ) ) {
							commandSender.sendMessage( chatmsgprefix + ChatColor.GOLD + mob + " is not in the disallowed list" );
							mobit.remove();
							continue;
						}
						disallowedmobs.remove( mob );
						if ( removedmobs.isBlank() ) { removedmobs += mob; } else { removedmobs += "," + mob; }
					}
					if ( moblist.isEmpty() ) {
						commandSender.sendMessage( chatmsgprefix + ChatColor.GOLD + "No mobs removed from the disallowed list" );
					} else {
						disallowedmobs.sort( String::compareToIgnoreCase );
						config.set( "disallowedmobs", disallowedmobs );
						Main.getInstance().saveConfig();
						commandSender.sendMessage( chatmsgprefix + ChatColor.GREEN + "" + moblist.size() + " mob" +
							( moblist.size() != 1 ? "s were" : " was") + " removed from the disallowed list: " + removedmobs );
					}
					
					return true;
				}
				
				if ( args[1].equals( "list" ) ) {
					if ( disallowedmobs.isEmpty() ) {
						commandSender.sendMessage( chatmsgprefix + ChatColor.GOLD + "Disallowed list is empty" );
					} else {
						commandSender.sendMessage( chatmsgprefix + ChatColor.GOLD + "Disallowed list contains " + disallowedmobs.size() + " mob" + ( disallowedmobs.size() != 1 ? "s" : "" ) );
						disallowedmobs.forEach( thismob -> {
							commandSender.sendMessage( chatmsgprefix + ChatColor.GOLD + "  " + thismob );
						});
					}
					
					return true;
				}
			}

			if ( args[0].equals( "popcon" ) ) {
				
				if ( args[1].equals( "add" ) ) {

					if ( args.length < 4 ) {
						commandSender.sendMessage( chatmsgprefix + ChatColor.RED + "Insufficient arguments provided. Must be one of the following:" );
						commandSender.sendMessage( chatmsgprefix + ChatColor.WHITE + "/popcon add name:<'Mob Name'> type:<type> max:<number> [ spawnpoint:X,Y,Z ]" );
						commandSender.sendMessage( chatmsgprefix + ChatColor.WHITE + "/popcon add type:<type> max:<number> [ spawnpoint:X,Y,Z ] " );
						return false;
					}
					
					// parse out the mob entry key value pairs from the 3rd argument onwards
					Map<String, String> mobparams = new LinkedHashMap<String, String>();
					boolean invalidMobParams = false;
					for ( int i = 2; i < args.length; i++ ) {
						String arg = args[i];
						if ( !arg.contains( ":" ) ) {
							commandSender.sendMessage( chatmsgprefix + ChatColor.RED + "Incorrect argument format for parameter " + arg );
							invalidMobParams = true;
							break;
						}
						String[] keyValue = arg.split(":");
						mobparams.put( keyValue[0], keyValue[1] );
					}
					if ( invalidMobParams ) {
						return false;
					}

					// validate entries
					if ( !mobparams.containsKey( "type" ) ) {
						commandSender.sendMessage( chatmsgprefix + ChatColor.RED + "The mob type parameter must be provided for mob by type or by name" );
						return false;
					}
					String type = mobparams.get("type");
					if( !validMob( type ) ) {
						commandSender.sendMessage( chatmsgprefix + ChatColor.RED + type + " is not a valid living entity" );
						return false;
					}
					
					boolean mobByName = false;
					String mobName = null;
					if ( mobparams.containsKey( "name" ) ) {
						mobByName = true;
						mobName = mobparams.get( "name" );
					}
					if ( mobByName ) {

						if ( plugin.getRangeMobs().contains(mobName) ) {
							commandSender.sendMessage( chatmsgprefix + ChatColor.RED + "'" + mobName + "' is already in the population control list" );
							return false;
						}
					} else {
						
						if ( plugin.getMobControlList().keySet().contains( mobparams.get( "type" ).toUpperCase() ) ) {
							commandSender.sendMessage( chatmsgprefix + ChatColor.RED + type + " is already in the population control list" );
							return false;
						}
					}
					
					if ( !mobparams.containsKey( "max" ) ) {
						commandSender.sendMessage( chatmsgprefix + ChatColor.RED + "The max parameter must be provided" );
						return false;
					}
					Integer max = null;
					try {
						max = Integer.parseInt( mobparams.get( "max" ) );
					} catch ( Exception e ) {
						commandSender.sendMessage( chatmsgprefix + ChatColor.RED + "Max parameter isn't a valid integer" );
						return false;
					}
					
					Location  spawnpoint = null;
					if ( mobparams.keySet().contains( "spawnpoint" ) ) {
						String[] coords = mobparams.get( "spawnpoint" ).split( "," );
						if ( coords.length != 3 ) {
							commandSender.sendMessage( chatmsgprefix + ChatColor.RED + "Invalid spawnpoint parameter. Requires 3 comma separated numbers" );
						}
						for ( int i = 0; i < 3; i++ ) {
							try {
								Double.parseDouble( coords[i] );
							} catch ( Exception e ) {
								commandSender.sendMessage( chatmsgprefix + ChatColor.RED + "Invalid coordinate for spawnpoint" );
								return false;
							}
						}
						spawnpoint = new Location( Bukkit.getWorld( "world" ), Double.parseDouble( coords[0] ), Double.parseDouble( coords[1] ), Double.parseDouble( coords[2] ) );
					}
					
					// populate mob entry to population controls and config
        			Map<String, Object> mobentry = new HashMap<>();
    				if ( spawnpoint != null ) { mobentry.put( "spawnpoint" , mobparams.get( "spawnpoint" ) ); }
    				mobentry.put( "max" , max );
    				mobentry.put( "type" , type );
        			if ( mobByName ) {
  //      				plugin.getMobControlList().put(mobName, new Mob("name", max, spawnpoint, EntityType.valueOf(type), 0));
        				mobentry.put( "name",  mobName );
        			} else {
   //     			    plugin.getMobControlList().put(type.toUpperCase(), new Mob("type", max, spawnpoint, EntityType.valueOf(type), 0));
        			}
    				
    				List<Map<?,?>> popcontrolconfig = Main.getInstance().getConfig().getMapList( "populationcontrol" );
	        		popcontrolconfig.add( mobentry );
	        		Main.getInstance().getConfig().set( "populationcontrol", popcontrolconfig );
	        		Main.getInstance().saveConfig();
	        		
		    		Main.getInstance().setInConfigUpdate( false );
				
					commandSender.sendMessage( chatmsgprefix + ChatColor.GREEN + " Added " + ( mobByName ? "'" + mobName +"'" : type ) + " to population control with max " + max );

					return true;
				}
				
				if ( args[1].equals( "remove" ) ) {
					
					Main.getInstance().setInConfigUpdate( true );

					if ( plugin.getPopulationMobs().isEmpty() ) {
						commandSender.sendMessage( chatmsgprefix + ChatColor.GOLD + "Population control list is empty. Nothing to remove." );
						return true;
					}
					
					String mob = args[2].toUpperCase();
					
					// remove mob entry from pop controls and from pop control config
					if ( !plugin.getMobControlList().keySet().contains( mob ) ) {
						commandSender.sendMessage( chatmsgprefix + ChatColor.GOLD + mob + " is not in the population control list" );
						return false;
					}
					plugin.getMobControlList().remove( mob );

		        	Iterator<Map<?,?>> pccit = Main.getInstance().getConfig().getMapList( "populationcontrol" ).iterator();
		        	int idx = 0; int idxToRemove = -1;
		        	while( pccit.hasNext() ) {
		        		Map<?,?> mobentry = pccit.next();
		        		if ( ( mobentry.containsKey( "name" ) && mobentry.get( "name" ).equals( mob ) ) ||
							 ( mobentry.containsKey( "type" ) && mobentry.get( "type" ).equals( mob ) ) ) {
		        			idxToRemove = idx;
		        			break;
		        		}
		        		idx++;
		        	}
		        	// get population control config, remove mob entry and save back the config
		        	if ( idxToRemove > -1 ) {
		        		List<Map<?,?>> popcontrolconfig = Main.getInstance().getConfig().getMapList( "populationcontrol" );
		        		popcontrolconfig.remove(idxToRemove);
		        		Main.getInstance().getConfig().set( "populationcontrol", popcontrolconfig );
		        		Main.getInstance().saveConfig();
		        	}
	        		
		    		Main.getInstance().setInConfigUpdate( false );
		    		
					commandSender.sendMessage( chatmsgprefix + ChatColor.GREEN + "" + mob + " removed from population control" );

					return true;
				}
				
				if ( args[1].equals( "list" ) ) {
					if ( plugin.getPopulationMobs().isEmpty() ) {
						commandSender.sendMessage( chatmsgprefix + ChatColor.GOLD + "Population control list is empty" );
					} else {
					    /*
						commandSender.sendMessage( chatmsgprefix + ChatColor.GOLD + "Population control contains " + popcontrols.size() + " mob" + ( popcontrols.size() != 1 ? "s" : "" ) );
						plugin.getMobControlList().keySet().forEach( thismob -> {
							PopChecker popmob = popcontrols.get( thismob );
							String mobstring = "";
							if ( popmob.getby().equals( "name" ) ) {
								mobstring = ChatColor.LIGHT_PURPLE + "name: '" + thismob + "', type: " + popmob.getMobType();
							} else {
								mobstring = ChatColor.LIGHT_PURPLE + "type: " + thismob;
							}
							mobstring += ChatColor.WHITE + ", max: " + popmob.getMax();
							Location spawnpoint = popmob.getSpawnPoint();
							if ( spawnpoint == null ) {
								if ( Main.getInstance().getRangeMobs().containsKey( thismob ) ) {
									spawnpoint = Main.getInstance().getRangeMob( thismob ).getHome();
								}
							}
							if ( spawnpoint == null ) {
								mobstring += ", no spawn set"; 
							} else {
								mobstring += ", spawn " + spawnpoint.getX() + "," + spawnpoint.getY() + "," + spawnpoint.getZ();
							}

							commandSender.sendMessage( chatmsgprefix + ChatColor.GOLD + "  " + mobstring );
						});
						*/
					}
				}
				
				return true;
			}
		}
		
		return false;
	}

	boolean validMob( String mob ) {
		try {
			EntityType.valueOf( mob );
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	void Usage(CommandSender sender) {
    	sender.sendMessage(chatmsgprefix + "/lpmob <allowed | disallowed> <add | remove> mob [,mob ...]");
    	sender.sendMessage(chatmsgprefix + "/lpmob popcon add mob count");
    	sender.sendMessage(chatmsgprefix + "/lpmob popcon remove mob");
    	sender.sendMessage(chatmsgprefix + "/lpmob <allowed | disallowed | popcon> list");
    }
	
	String removeDuplicates( String input ) {
		LinkedHashSet<String> uniquelist = new LinkedHashSet<String>( Arrays.asList( input.split( "\\s*,\\s*", -1 ) ) );
		String output = uniquelist.stream().map( Object::toString ).collect( Collectors.joining( "," ) );
		output = output.replace( ",$", "" ).replaceAll( ",{2,}", "," );
		return output;
	}
}
