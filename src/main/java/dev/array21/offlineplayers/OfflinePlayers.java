package dev.array21.offlineplayers;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.permissions.PermissionDefault;

import dev.array21.bukkitreflectionlib.ReflectionUtil;
import dev.array21.dutchycore.DutchyCore;
import dev.array21.dutchycore.module.PluginModule;
import dev.array21.dutchycore.utils.Utils;
import dev.array21.offlineplayers.commands.TransferCommandExeuctor;
import dev.array21.offlineplayers.listeners.PlayerJoinEventListener;
import dev.array21.offlineplayers.tabcompleters.TransferCommandTabCompleter;
import dev.array21.dutchycore.annotations.Nullable;
import dev.array21.dutchycore.annotations.RegisterModule;

@RegisterModule(name = "OfflinePlayers", version = "@VERSION@", author = "Dutchy76", infoUrl = "https://github.com/DutchyPlugins/OfflinePlayers")
public class OfflinePlayers extends PluginModule {

	/**
	 * Key = newUserName<br>
	 * Value = OldUserName
	 */
	private HashMap<String, String> pendingTransfers = new HashMap<>();
	
	//Name of the world
	private String levelName;
	
	@Override
	public void enable(DutchyCore plugin) {		
		super.logInfo("Initializing...");
	
		//If the server's running in online mode, this module should not be used
		if(Bukkit.getOnlineMode()) {
			super.logWarn("OfflinePlayers is not ment to be used on servers running in online mode. Aborting initialization.");
			return;
		}
		
		try {
			Object properties = ReflectionUtil.getObject(Bukkit.getServer(), Bukkit.getServer().getClass(), "getProperties");
			if(ReflectionUtil.isUseNewSpigotPackaging()) {
				this.levelName = (String) ReflectionUtil.getObject(properties, "p");
			} else {
				this.levelName = (String) ReflectionUtil.getObject(properties, "levelName");
			}
		} catch(Exception e) {
			super.logWarn("Failed to load. Unable to get levelName.");
			super.logWarn(Utils.getStackTrace(e));
		}
		
		//Register commands
		super.registerCommand("transfer", new TransferCommandExeuctor(this), this);
		
		//Register tab listeners
		super.registerTabCompleter("transfer", new TransferCommandTabCompleter(), this);
		
		//Permissions
		super.registerPermissionNode("offlineplayers.transfer", PermissionDefault.TRUE, "Allows user-level usage of /transfer", null);
		
		HashMap<String, Boolean> children = new HashMap<>();
		children.put("offlineplayers.transfer", true);
		super.registerPermissionNode("offlineplayers.transfer.admin", PermissionDefault.OP, "Allows admin-level usage of /transfer", children);
		
		//Event listeners
		super.registerEventListener(new PlayerJoinEventListener(this));
		
		super.logInfo("Initialization complete.");
	}
	
	/**
	 * Get the old username for a pending transfer
	 * @param newUserName The new (current) username of the Player
	 * @return Returns the old username, if there is none returns null
	 */
	@Nullable
	public String getPendingTransferOldUsername(String newUserName) {
		return this.pendingTransfers.get(newUserName);
	}
	
	/**
	 * Add a new pending transfer
	 * @param newUserName The new username for the player
	 * @param oldUserName The old (current) username for the player
	 */
	public void addNewPendingTransfer(String newUserName, String oldUserName) {
		this.pendingTransfers.put(newUserName, oldUserName);
	}
	
	/**
	 * Remove a player from pending transfers
	 * @param newUserName The new (current) username of the player
	 */
	public void removePendingTransfer(String newUserName) {
		this.pendingTransfers.remove(newUserName);
	}
	
	/**
	 * Get the level (world) name
	 * @return Returns the level name
	 */
	public String getLevelName() {
		return this.levelName;
	}
}
