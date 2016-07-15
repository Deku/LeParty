package cl.josedev.WoWParty.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import cl.josedev.WoWParty.Party;
import cl.josedev.WoWParty.PartyGUI;
import cl.josedev.WoWParty.WoWParty;

public class GUIListener implements Listener {

	private WoWParty plugin;
	
	public GUIListener(WoWParty instance) {
		this.plugin = instance;
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		final Player p = (Player) e.getWhoClicked();
		Party party = this.plugin.getManager().getParty(p.getUniqueId());
		
		if (party != null) {	
			if (e.getInventory().getTitle().equals(party.getName()) && e.getInventory().getSize() == 9) {
				ItemStack item = e.getCurrentItem();
				PartyGUI gui = new PartyGUI(this.plugin, p, party);
				
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
				} else if (e.getSlot() == PartyGUI.SCOREBOARD_BUTTON_SLOT) {
					party.toggleBoard(p);
					gui.present();
				} else if (item.equals(PartyGUI.PartyChest(p, party))) {
					gui.presentChest();
				}
			
				e.setCancelled(true);
			}
		}
	}
}
