package cl.josedev.WoWParty.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import cl.josedev.WoWParty.Party;
import cl.josedev.WoWParty.WoWParty;

public class ChatListener implements Listener {

	private WoWParty plugin;
	
	public ChatListener(WoWParty instance) {
		this.plugin = instance;
	}
	
	@EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
	public void onChat(AsyncPlayerChatEvent e) {
		Player p = e.getPlayer();
		Party party = this.plugin.getManager().getParty(p.getUniqueId());
		
		if (party != null && this.plugin.getManager().inChatMode(p.getUniqueId())) {
			party.sendMessage(ChatColor.BLUE + p.getName() + ChatColor.WHITE + " : " + ChatColor.ITALIC + e.getMessage());
			e.setCancelled(true);
		}
	}
}
