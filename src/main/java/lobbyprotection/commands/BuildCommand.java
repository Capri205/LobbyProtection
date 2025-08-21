package lobbyprotection.commands;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import lobbyprotection.listeners.Listeners;

public class BuildCommand implements CommandExecutor {
	
	Logger log = Logger.getLogger("Minecraft");
    Listeners listeners = new Listeners();

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) return false;
        Player player = (Player) commandSender;
        if (player.hasPermission("lpbld.buildCommand") || player.isOp()) {
        	listeners.onCommand(player);
        }
        return false;
    }
}
