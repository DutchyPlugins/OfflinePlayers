package nl.thedutchmc.offlineplayers;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.permissions.PermissionDefault;

import nl.thedutchmc.dutchycore.DutchyCore;
import nl.thedutchmc.dutchycore.module.PluginModule;
import nl.thedutchmc.dutchycore.annotations.Nullable;
import nl.thedutchmc.offlineplayers.commands.TransferCommandExeuctor;
import nl.thedutchmc.offlineplayers.listeners.PlayerJoinEventListener;
import nl.thedutchmc.offlineplayers.tabcompleters.TransferCommandTabCompleter;

public class OfflinePlayers extends PluginModule {

	//k = newUserName, v = oldUserName
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
		
		//Determine the 'level-name' using Reflection
		//First we need to get the method getProperties() in the CraftServer class
		Method getPropertiesMethod = null;
		try {
			getPropertiesMethod = Bukkit.getServer().getClass().getDeclaredMethod("getProperties");
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		
		//It's a private method, so we need to make it accessible
		getPropertiesMethod.setAccessible(true);
		
		//Invoke the method. This returns an instance of DedicatedServerProperties
		//CraftServer#getProperties()
		Object dedicatedServerProperties = null;
		try {
			dedicatedServerProperties = getPropertiesMethod.invoke(Bukkit.getServer());
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		
		//Disable access to the getProperties() method again
		getPropertiesMethod.setAccessible(false);
		
		//We need to get the field levelName in the DedicatedServerProperties class
		Field levelNameField = null;
		try {
			levelNameField = dedicatedServerProperties.getClass().getField("levelName");
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}

		//Get the value of the field levelName in the DedicatedServerProperties class
		//DedicatedServerProperties#levelName
		Object levelName = null;
		try {
			levelName = levelNameField.get(dedicatedServerProperties);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		
		this.levelName = (String) levelName;
		
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
