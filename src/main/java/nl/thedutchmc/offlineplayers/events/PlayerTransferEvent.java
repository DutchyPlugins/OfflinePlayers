package nl.thedutchmc.offlineplayers.events;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerTransferEvent extends Event {
	
    private static final HandlerList HANDLERS = new HandlerList();
    private String oldUsername, newUsername;
    
    /**
     * @param oldUsername The old username of the player
     * @param newUsername The new username of the player
     */
    public PlayerTransferEvent(String oldUsername, String newUsername) {
		this.oldUsername = oldUsername;
		this.newUsername = newUsername;
	}
    
	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}
	
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
	
    /**
     * Get the old username of the player being transfered
     * @return Returns the old username
     */
	public String getOldUsername() {
		return this.oldUsername;
	}
	
	/**
	 * Get the old UUID of the player being transfered
	 * @return Returns the old UUID
	 */
	public UUID getOldUUID() {
		return UUID.nameUUIDFromBytes(("OfflinePlayer:" + this.oldUsername).getBytes(StandardCharsets.UTF_8));
	}
	
	/**
	 * Get the new username of the player being transfered
	 * @return Returns the new username
	 */
	public String getNewusername() {
		return this.newUsername;
	}
	
	/**
	 * Get the new UUID of the player being transfered
	 * @return Returns the new UUID
	 */
	public UUID getNewUUID() {
		return UUID.nameUUIDFromBytes(("OfflinePlayer:" + this.newUsername).getBytes(StandardCharsets.UTF_8));
	}
}
