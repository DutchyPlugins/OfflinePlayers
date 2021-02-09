package nl.thedutchmc.offlineplayers.tabcompleters;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import nl.thedutchmc.dutchycore.module.commands.ModuleTabCompleter;

public class TransferCommandTabCompleter implements ModuleTabCompleter {

	@Override
	public String[] complete(CommandSender sender, String[] args) {
		
		if(args.length == 0) {
			if(sender.hasPermission("offlineplayers.transfer.admin")) {
				List<String> onlinePlayerNames = new ArrayList<>();
				for(Player p : Bukkit.getOnlinePlayers()) {
					onlinePlayerNames.add(p.getName());
				}
				
				return onlinePlayerNames.toArray(new String[0]);
			}
			
			return null;
		}
		
		if(args.length == 1) {
			return null;
		}
		
		return null;
	}
}
