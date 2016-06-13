package cl.josedev.WoWParty.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import cl.josedev.WoWParty.Party;
import cl.josedev.WoWParty.PartyGUI;
import cl.josedev.WoWParty.WoWParty;

public class PlayerListener implements Listener {

private WoWParty plugin;
	
	public PlayerListener(WoWParty instance) {
		this.plugin = instance;
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		final Player p = (Player) e.getWhoClicked();
		Party party = this.plugin.getManager().getParty(p.getUniqueId());
		
		if (party != null) {	
			if (e.getInventory().getTitle().equals(party.getName()) && e.getInventory().getSize() == 9) {
				ItemStack item = e.getCurrentItem();
				
				if (item == null)
					return;
				
				if (item.getType().equals(Material.SKULL_ITEM) && item.hasItemMeta()) {
					SkullMeta meta = (SkullMeta) item.getItemMeta();
					final Player clickedPlayer = Bukkit.getServer().getPlayer(meta.getOwner());
					
					if (e.getClick().equals(ClickType.SHIFT_RIGHT)) {
						if (party.isLeader(p)) {
							if (clickedPlayer == p) {
								e.setCancelled(true);
								p.closeInventory();
								return;
							}
							
							party.setLeader(clickedPlayer);
							
							e.setCancelled(true);
							p.closeInventory();
							return;
						}
					} else if (e.getClick().equals(ClickType.LEFT) || e.getClick().equals(ClickType.RIGHT)) {
						if (clickedPlayer == p) {
							e.setCancelled(true);
							return;
						}
						
						if (party.isMember(clickedPlayer.getUniqueId())) {
							if (this.plugin.teleportAllowed) {
								e.setCancelled(true);
								p.closeInventory();
								p.sendMessage(WoWParty.TAG + ChatColor.GREEN + "Comenzando el teletransporte a " + ChatColor.UNDERLINE + clickedPlayer.getName() + ChatColor.GREEN + " en 3 segundos...");
								
								new BukkitRunnable() {
									int time = 12;

									public void run() {
										if (time != 0 || time != -1) {
											p.getLocation().getWorld().playEffect(p.getLocation(), Effect.ENDER_SIGNAL, 5);
											p.getLocation().getWorld().playSound(p.getLocation(), Sound.BLOCK_PORTAL_AMBIENT, 5, 5);
											time--;
										}
										if (time == 12) {
											p.getLocation().getWorld().playSound(p.getLocation(), Sound.BLOCK_PORTAL_AMBIENT, 5, 5);
										}
										if (time == 0) {
											p.teleport(clickedPlayer);
											p.getLocation().getWorld().playSound(p.getLocation(), Sound.ENTITY_ENDERMEN_TELEPORT, 5, 5);
											time--;
										}
										if (time == -1) {
											cancel();
										}
									}
								}.runTaskTimer(this.plugin, 0L, 5L);
							}
								
							e.setCancelled(true);
						}
					}
				} else if (item.equals(PartyGUI.PartyChest(p, party))) {
					if (e.getClick().equals(ClickType.LEFT) || e.getClick().equals(ClickType.RIGHT)) {
						PartyGUI gui = new PartyGUI(this.plugin, p, party);
						gui.presentChest();
					}
				}
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		Party party = this.plugin.getManager().getParty(p.getUniqueId());
		
		if (party != null) {
			party.remove(p);
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
