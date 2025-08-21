package lobbyprotection.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import lobbyprotection.listeners.Listeners;

public class DmgCommand implements CommandExecutor {
    Listeners listeners = new Listeners();

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
    	Player player = (Player) commandSender;
        if (player.hasPermission("lpdmg.damageCommand") || player.isOp()) {
        	boolean dmg = listeners.onDmgCommand();
        	commandSender.sendMessage("Player Damage is now " + dmg);
        }
        return false;
    }
}
