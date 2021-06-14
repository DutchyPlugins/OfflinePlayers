package dev.array21.offlineplayers.tabcompleters;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dev.array21.dutchycore.module.commands.ModuleTabCompleter;

public class TransferCommandTabCompleter implements ModuleTabCompleter {

	@Override
	public String[] complete(CommandSender sender, String[] args) {
		
		//transfer ?
		if(args.length == 0) {
			
			//If the player has admin permissions, we want to return a list of online players
			if(sender.hasPermission("offlineplayers.transfer.admin")) {
				
				//Iterate over all online players and put their name in a list
				List<String> onlinePlayerNames = new ArrayList<>();
				for(Player p : Bukkit.getOnlinePlayers()) {
					onlinePlayerNames.add(p.getName());
				}
				
				return onlinePlayerNames.toArray(new String[0]);
			}
			
			//Player does not have admin permissions for the /transfer command, no tab completion
			return null;
		}
		
		return null;
	}
}
