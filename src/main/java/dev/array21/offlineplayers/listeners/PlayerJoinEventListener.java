package dev.array21.offlineplayers.listeners;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import dev.array21.dutchycore.Triple;
import dev.array21.dutchycore.utils.Utils;
import dev.array21.offlineplayers.OfflinePlayers;

public class PlayerJoinEventListener implements Listener {

	private OfflinePlayers module;
	
	public PlayerJoinEventListener(OfflinePlayers module) {
		this.module = module;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoinEvent(PlayerJoinEvent event) {
		String playerName = event.getPlayer().getName();
		
		//Get the old user name from the pending transfers map
		String oldUserName = this.module.getPendingTransferOldUsername(playerName);
		
		//Check if oldUserName is null, if this is the case they were not transfering
		if(oldUserName == null) {
			return;
		}
		
		//Remove the player from the pending transfers map
		this.module.removePendingTransfer(playerName);
		
		//Send the player a message informing them about whaty happened
		String message = "Your account has been successfully transferred from %s to %s! Most of your data is migrated, but not everything might work as you expect. Data from some plugins might not be transfered!";
		message = Utils.processColours(ChatColor.GOLD + message, new Triple<String, ChatColor, ChatColor>("%s", ChatColor.RED, ChatColor.GOLD));
		
		event.getPlayer().sendMessage(String.format(message, oldUserName, playerName));
	}
}
