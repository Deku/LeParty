package cl.josedev.LeParty.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import cl.josedev.LeParty.Party;
import cl.josedev.LeParty.LeParty;

public class ChatListener implements Listener {

	private LeParty plugin;
	
	public ChatListener(LeParty instance) {
		this.plugin = instance;
	}
	
	@EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
	public void onChat(AsyncPlayerChatEvent e) {
		Player p = e.getPlayer();
		Party party = this.plugin.getManager().getParty(p.getUniqueId());
		
		if (party != null && this.plugin.getManager().inChatMode(p.getUniqueId())) {
			party.sendChat(p, e.getMessage());
			this.plugin.getManager().sendSpyChat(party, p, e.getMessage());
			
			e.setCancelled(true);
		}
	}
}
