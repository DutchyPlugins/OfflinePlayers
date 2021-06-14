package dev.array21.offlineplayers.commands;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.io.Files;

import dev.array21.dutchycore.Triple;
import dev.array21.dutchycore.module.commands.ModuleCommand;
import dev.array21.dutchycore.utils.Utils;
import dev.array21.offlineplayers.OfflinePlayers;
import dev.array21.offlineplayers.events.PlayerTransferEvent;

/**
 * Executor for:
 * <pre> /transfer &ltnew username&gt </pre>
 * <pre> /transfer &ltold user name&gt &ltnew user name&gt </pre>
 */
public class TransferCommandExeuctor implements ModuleCommand {

	private OfflinePlayers module;
	
	public TransferCommandExeuctor(OfflinePlayers module) {
		this.module = module;
	}
	
	@Override
	public boolean fire(CommandSender sender, String[] args) {
		
		//Check permissions
		if(!sender.hasPermission("offlineplayers.transfer")) {
			sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
			return true;
		}
		
		//Check if the user provided arguments
		if(args.length < 1) {
			sender.sendMessage("Not enough arguments provided!");
			return true;
		}
		
		//If a play has the 'offlineplayers.transfer.admin' permission and intends to use transfer in admin mode (checked if they provide two arguments)
		//then run it the command in admin mode, else just use regular mode
		if(sender.hasPermission("offlineplayers.transfer.admin") && args.length >= 2) {
			
			//Check if the account they want to transfer to is online, this is not allowed
			UUID transferToUuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + args[1]).getBytes(StandardCharsets.UTF_8));
			if(Bukkit.getPlayer(transferToUuid) != null && Bukkit.getPlayer(transferToUuid).isOnline()) {
				sender.sendMessage(ChatColor.RED + "You cannot transfer that account to a player that is online!");
				return true;
			}
			
			//Get the player we should transfer
			Player toTransfer = Bukkit.getPlayer(args[0]);
			
			//Player is not online, so toTransfer is null
			if(toTransfer == null) {
				
				//Check the offline players to set toTransfer
				for(OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
					if(offlinePlayer.getName().equals(args[0])) {
						toTransfer = (Player) offlinePlayer;
					}
				}
			}
			
			//If toTransfer is null, the player the user wants to transfer doesn't exist. Stop here
			if(toTransfer == null) {
				String message = Utils.processColours(ChatColor.GOLD + "Unknown player: %s !", new Triple<String, ChatColor, ChatColor>("%s", ChatColor.RED, ChatColor.GOLD));
				sender.sendMessage(String.format(message, args[0]));
			}
			
			//Get the new user name from the provided arguments
			String newUserName = args[1];
			
			//Add the new user name to the pendingTransfers map
			this.module.addNewPendingTransfer(newUserName, toTransfer.getName());

			//Check if the player the user wants to transfer is online. If so, kick them
			if(toTransfer.isOnline()) {
				String message = ChatColor.GOLD + "Your account has been transferred to %s by an administrator!";
				message = Utils.processColours(message, new Triple<String, ChatColor, ChatColor>("%s", ChatColor.RED, ChatColor.GOLD));
				toTransfer.kickPlayer(String.format(message, newUserName));
			}
			
			//Transfer the data file
			transferPlayerDataFile(toTransfer.getName(), newUserName);
			
			//Fire a player transfer event
			this.module.throwModuleEvent(new PlayerTransferEvent(toTransfer.getName(), newUserName));
			
			//Send a message to the user letting them know what we did
			String message = ChatColor.GOLD + "Registered %s as new username for %s. They should now log in with their new username.";
			message = Utils.processColours(message, new Triple<String, ChatColor, ChatColor>("%s", ChatColor.RED, ChatColor.GOLD));
			
			sender.sendMessage(String.format(message, newUserName, toTransfer.getName()));
		} else {
			//Not running in admin mode
			
			//Check if the sender is a player, if not stop.
			if(!(sender instanceof Player)) {
				sender.sendMessage("Invalid usage! /transfer <old username> <new user name>");
				return true;
			}
			
			//Check if the account they want to transfer to is online
			UUID transferToUuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + args[0]).getBytes(StandardCharsets.UTF_8));
			if(Bukkit.getPlayer(transferToUuid) != null && Bukkit.getPlayer(transferToUuid).isOnline()) {
				sender.sendMessage(ChatColor.RED + "You cannot transfer your account to a player that is online!");
				return true;
			}
			
			Player player = (Player) sender;
			
			//Get the player's new user name from the arguments
			String newUserName = args[0];
			
			//Add the player to the pending transfers map
			this.module.addNewPendingTransfer(newUserName, player.getName());

			//Kick the player
			String message = "Transferring your account to %s!";
			message =  Utils.processColours(ChatColor.GOLD + message, new Triple<String, ChatColor, ChatColor>("%s", ChatColor.RED, ChatColor.GOLD));
			player.kickPlayer(String.format(message, newUserName));
			
			//Fire a transfer event
			this.module.throwModuleEvent(new PlayerTransferEvent(sender.getName(), newUserName));
			
			//Transfer the playerdata file
			transferPlayerDataFile(player.getName(), newUserName);
		}
		
		return true;
	}
	
	/**
	 * Transfer a player's playerdata file
	 * @param currentPlayerName The current username of the player
	 * @param newPlayerName The new (desired) username of the player
	 */
	private void transferPlayerDataFile(String currentPlayerName, String newPlayerName) {
		//Compute the current and new UUID based on the username
		//Source: HumanEntity class (NMS class)
		String offlinePlayerCurrentName = UUID.nameUUIDFromBytes(("OfflinePlayer:" + currentPlayerName).getBytes(StandardCharsets.UTF_8)).toString();
		String offlinePlayerNewName = UUID.nameUUIDFromBytes(("OfflinePlayer:" + newPlayerName).getBytes(StandardCharsets.UTF_8)).toString();
		
		//Get the playerdata folder
		File playerDataFolder = new File(Bukkit.getWorldContainer() + File.separator + this.module.getLevelName() + File.separator + "playerdata");
		
		//Get the current and the new .dat file
		File currentPlayerDataFile = new File(playerDataFolder, offlinePlayerCurrentName + ".dat");
		File newPlayerDataFile = new File(playerDataFolder, offlinePlayerNewName + ".dat");
		
		//Rename the .dat file to the new UUID
		try {
			Files.move(currentPlayerDataFile, newPlayerDataFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
