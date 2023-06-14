package lobbyprotect.commands;

import lobbyprotect.listeners.Listeners;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BuildCommand implements CommandExecutor {
	
	Logger log = Logger.getLogger("Minecraft");
    Listeners listeners = new Listeners();

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
    	log.log(Level.INFO, "debug - onCommand - command: " + command.getName() + ", " + command.getPermission() + ", s: " + s + ", sender: " + commandSender.getName());
        if (!(commandSender instanceof Player)) return false;
        Player player = (Player) commandSender;
        if (player.hasPermission("lpbld.buildCommand") || player.isOp()) {
        	listeners.onCommand(player);
        }
        return false;
    }
}
