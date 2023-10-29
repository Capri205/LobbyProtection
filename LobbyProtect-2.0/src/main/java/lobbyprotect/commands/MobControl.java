package lobbyprotect.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import lobbyprotect.Main;

public class MobControl implements CommandExecutor {

	static Logger log = Logger.getLogger("Minecraft");

	private String chatmsgprefix = null;
	private String logmsgprefix = null;
	
	public MobControl() {
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
				}
			}

			if ( args[0].equals( "popcon" ) ) {
				
	    		Map<String, Integer> popcon = new HashMap<>();
	    		if ( Main.getInstance().getConfig().contains( "populationenforcement" ) ) {
	    			for ( String key : Main.getInstance().getConfig().getConfigurationSection( "populationenforcement" ).getKeys( false ) ) {
	    				popcon.put( key,  Main.getInstance().getConfig().getConfigurationSection( "populationenforcement" ).getInt( key ) );
	    			}
	    		}
	    		
				if ( args[1].equals( "add" ) ) { 

					//Main.getInstance().setInConfigUpdate( true );

					String mob = args[2].toUpperCase();
					if( !validMob( mob ) ) {
						commandSender.sendMessage( chatmsgprefix + ChatColor.RED + "'" + mob + "' is not a valid living entity" );
						return false;
					}
					
					Integer count = 0;
					try {
						count = Integer.parseInt( args[3] );
					} catch ( Exception e ) {
						commandSender.sendMessage( chatmsgprefix + ChatColor.RED + "Count isn't a valid integer" );
						return false;
					}
					
					if ( popcon.containsKey( mob ) ) {
						if ( popcon.get( mob ) == count ) {
							commandSender.sendMessage( chatmsgprefix + ChatColor.GOLD + mob + " already present with that count" );
							return false;
						} else {
							popcon.remove( mob );
							popcon.put( mob, count);
							commandSender.sendMessage( chatmsgprefix + ChatColor.GREEN + " Replaced " + mob + " population count with " + count );
						}
					} else {
						popcon.put( mob, count);
						commandSender.sendMessage( chatmsgprefix + ChatColor.GREEN + " Added " + mob + " to population control with count " + count );
					}
					config.set( "populationenforcement", popcon );
					Main.getInstance().saveConfig();
		    		Main.getInstance().reloadConfig();

					return true;
				}
				
				if ( args[1].equals( "remove" ) ) {
					
					Main.getInstance().setInConfigUpdate( true );

					if ( popcon.isEmpty() ) {
						commandSender.sendMessage( chatmsgprefix + ChatColor.GOLD + "Population enforcement list is empty. Nothing to remove." );
						return true;
					}
					
					String mob = args[2].toUpperCase();
					if( !validMob( mob ) ) {
						commandSender.sendMessage( chatmsgprefix + ChatColor.RED + "'" + mob + "' is not a valid living entity" );
						return false;
					}
					
					if ( !validMob( mob ) ) {
						commandSender.sendMessage( chatmsgprefix + ChatColor.RED + "'" + mob + "' is not a valid living entity" );
						return false;
					}
					
					if ( !popcon.keySet().contains( mob ) ) {
						commandSender.sendMessage( chatmsgprefix + ChatColor.GOLD + mob + " is not in the population control list" );
						return false;
					}
					popcon.remove( mob );
					config.set( "populationenforcement", popcon );
					Main.getInstance().saveConfig();
					commandSender.sendMessage( chatmsgprefix + ChatColor.GREEN + "" + mob + " removed from population control" );
					
					Main.getInstance().setInConfigUpdate( false );

					return true;
				}
				
				if ( args[1].equals( "list" ) ) {
					if ( popcon.isEmpty() ) {
						commandSender.sendMessage( chatmsgprefix + ChatColor.GOLD + "Population control list is empty" );
					} else {
						commandSender.sendMessage( chatmsgprefix + ChatColor.GOLD + "Population control contains " + popcon.keySet().size() + " mob" + ( popcon.keySet().size() != 1 ? "s" : "" ) );
						popcon.keySet().forEach( thismob -> {
							commandSender.sendMessage( chatmsgprefix + ChatColor.GOLD + "  " + thismob + ": " + popcon.get( thismob ) );
						});
					}
				}
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
