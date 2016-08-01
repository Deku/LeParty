package cl.josedev.WoWParty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.ChatColor;

public class PartyManager {
	private List<Party> parties;
	private List<UUID> partyChat;
	private List<UUID> spyMode;
	private Map<UUID, UUID> inviteQueue;
	private Map<UUID, Inventory> voidChest;

	public PartyManager() {
		parties = new ArrayList<Party>();
		partyChat = new ArrayList<UUID>();
		inviteQueue = new HashMap<UUID, UUID>();
		spyMode = new ArrayList<UUID>();
		voidChest = new HashMap<UUID, Inventory>();
	}

	public void addParty(Party p) {
		parties.add(p);
	}

	public void removeParty(Party p) {
		parties.remove(p);
	}

	public Party getParty(Player leader) {
		for (Party pp : parties) {
			if (pp.getLeaderID().equals(leader.getUniqueId())) {
				return pp;
			}
		}
		return null;
	}

	public Party getParty(UUID uuid) {
		for (Party pp : parties) {
			if (pp.isMember(uuid)) {
				return pp;
			}
		}
		return null;
	}

	public UUID getInvite(UUID playerId) {
		if (inviteQueue.containsKey(playerId)) {
			return inviteQueue.get(playerId);
		}
		return null;
	}

	public boolean addInvite(UUID uuid, Party p) {
		// Player already has an invitation
		if (inviteQueue.containsKey(uuid)) {
			return false;
		}
		
		inviteQueue.put(uuid, p.getId());
		return true;
	}

	public void removeInvite(UUID playerId) {
		if (inviteQueue.containsKey(playerId)) {
			inviteQueue.remove(playerId);
		}
	}

	public boolean isInvited(UUID playerId) {
		return inviteQueue.containsKey(playerId);
	}

	public List<Party> getParties() {
		return parties;
	}
	
	public Party getInvitationParty(UUID playerId) {
		UUID ptID = inviteQueue.get(playerId);
		
		for (Party pt : parties) {
			if (pt.getId().equals(ptID)) {
				return pt;
			}
		}
		
		return null;
	}
	
	public void togglePartyChat(Player player) {
		if (partyChat.contains(player.getUniqueId())) {
			partyChat.remove(player.getUniqueId());
			player.sendMessage(WoWParty.TAG + ChatColor.RED + "Chat de grupo desactivado");
		} else {
			partyChat.add(player.getUniqueId());
			player.sendMessage(WoWParty.TAG + ChatColor.GREEN + "Chat de grupo activado");
		}
	}

	public boolean inChatMode(UUID playerId) {
		return partyChat.contains(playerId);
	}
	
	public void disableChatMode(UUID playerId) {
		if (partyChat.contains(playerId)) {
			partyChat.remove(playerId);
		}
	}
	
	public void toggleSpyMode(Player player) {
		if (spyMode.contains(player.getUniqueId())) {
			spyMode.remove(player.getUniqueId());
			player.sendMessage(WoWParty.TAG + ChatColor.RED + "Modo de espía desactivado");
		} else {
			spyMode.add(player.getUniqueId());
			player.sendMessage(WoWParty.TAG + ChatColor.GREEN + "Modo de espía activado");
		}
	}
	
	public boolean isListeningChat(UUID playerId) {
		return spyMode.contains(playerId);
	}
	
	public boolean isSomeoneListening() {
		return spyMode.size() > 0;
	}

	public void sendSpyChat(Party party, Player sender, String msg) {
		if (!isSomeoneListening()) {
			return;
		}
		
		for (UUID id : spyMode) {
			if (!party.isMember(id)) {
				Player p = Bukkit.getServer().getPlayer(id);
				
				if (p != null && p.isOnline()) {
						p.sendMessage(WoWParty.TAG + ChatColor.LIGHT_PURPLE + "[" + party.getLeader().getName() + "] " + sender.getName() + ": " + ChatColor.DARK_PURPLE + msg);
				}
			}
		}
	}
	
	public void disableSpyMode(UUID playerId) {
		if (spyMode.contains(playerId)) {
			spyMode.remove(playerId);
		}
	}
	
	public void addVoidChest(UUID playerId, Inventory chest) {
		voidChest.put(playerId, chest);
	}
	
	public Inventory getVoidChest(UUID playerId) {
		if (voidChest.containsKey(playerId)) {
			return voidChest.get(playerId);
		} else {
			return null;
		}
	}
	
	public void removeVoidChest(UUID playerId) {
		if (voidChest.containsKey(playerId)) {
			voidChest.remove(playerId);
		}
	}
}
