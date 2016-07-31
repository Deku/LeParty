package cl.josedev.WoWParty.listeners;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import cl.josedev.WoWParty.Party;
import cl.josedev.WoWParty.WoWParty;

public class PlayerListener implements Listener {

	private WoWParty plugin;
	
	public PlayerListener(WoWParty instance) {
		this.plugin = instance;
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		UUID pId = p.getUniqueId();
		Party party = this.plugin.getManager().getParty(pId);
		
		if (party != null) {
			party.remove(p);
		}
		
		plugin.getManager().disableChatMode(pId);
		
		if (p.hasPermission(WoWParty.PERM_ADMIN)) {
			plugin.getManager().disableSpyMode(pId);
		}
	}
	
	@EventHandler
	public void onPlayerKick(PlayerKickEvent e) {
		Player p = e.getPlayer();
		Party party = this.plugin.getManager().getParty(p.getUniqueId());
		
		if (party != null) {
			party.remove(p);
		}
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onPlayerDamage(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			Party party = this.plugin.getManager().getParty(p.getUniqueId());
			
			if (party != null) {
				party.update();
			}
		}
	}
	
	@EventHandler(priority=EventPriority.HIGH,ignoreCancelled=true)
	public void onPlayerHitPlayer(EntityDamageByEntityEvent e) {
		if (e.getDamager() instanceof Player
				&& e.getEntity() instanceof Player) {
			Player attacker = (Player) e.getDamager();
			Player victim = (Player) e.getEntity();
			Party attackerPt = this.plugin.getManager().getParty(attacker.getUniqueId());
			Party victimPt = this.plugin.getManager().getParty(victim.getUniqueId());
			
			if (attackerPt != null && victimPt != null) {
				if (attackerPt.equals(victimPt)) {
					e.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onPlayerRegen(EntityRegainHealthEvent e) {
		if (e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			Party party = this.plugin.getManager().getParty(p.getUniqueId());
			
			if (party != null)
				party.update();
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		if (e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			Party party = this.plugin.getManager().getParty(p.getUniqueId());
			
			if (party != null) {
				party.sendMessage(p.getName() + " ha muerto!" + ChatColor.GRAY + " [x: " + p.getLocation().getBlockX() + " - y: " + p.getLocation().getBlockY() + " - z: " + p.getLocation().getBlockZ() + "]");
			}
		}
	}

	@EventHandler(ignoreCancelled=true)
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		Player p = e.getPlayer();
		Party party = this.plugin.getManager().getParty(p.getUniqueId());
		
		if (party != null) {
			party.update();
		}
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onExpPickup(PlayerExpChangeEvent e) {
		Player p = e.getPlayer();
		Party party = this.plugin.getManager().getParty(p.getUniqueId());
		
		if (party != null) {
			party.shareExp(p, e.getAmount());
		}
	}
}
