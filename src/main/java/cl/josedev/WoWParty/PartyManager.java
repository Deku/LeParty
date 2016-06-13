package cl.josedev.WoWParty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class PartyManager {
	private List<Party> parties;
	private List<UUID> partyChat;
	private Map<UUID, UUID> inviteQueue;

	public PartyManager() {
		parties = new ArrayList<Party>();
		partyChat = new ArrayList<UUID>();
		inviteQueue = new HashMap<UUID, UUID>();
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
			player.sendMessage(WoWParty.TAG + ChatColor.YELLOW + "Chat de grupo desactivado");
		} else {
			partyChat.add(player.getUniqueId());
			player.sendMessage(WoWParty.TAG + ChatColor.YELLOW + "Chat de grupo activado");
		}
	}

	public boolean inChatMode(UUID playerId) {
		return partyChat.contains(playerId);
	}
}
