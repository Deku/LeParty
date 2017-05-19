package cl.josedev.LeParty;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;

public class PartyGUI {
	private LeParty plugin;
	private Inventory inventory;
	private Player player;
	private Party party;
	public static final int SCOREBOARD_BUTTON_SLOT = 7;
	public static final int CHEST_BUTTON_SLOT = 8;
	

	public PartyGUI(LeParty plugin, Player player, Party party) {
		this.plugin = plugin;
		this.player = player;
		this.party = party;
	}

	public void present() {
		inventory = Bukkit.getServer().createInventory(player, 9, party.getName());
		
		// Party members' heads
		for (int i = 0; i < party.getSize(); i++) {
			UUID uuid = party.getMember(i);
			Player p = Bukkit.getServer().getPlayer(uuid);
			ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
			SkullMeta meta = (SkullMeta) item.getItemMeta();
			ArrayList<String> lore = new ArrayList<String>();
			
			// Name
			if (party.getLeaderID().equals(p.getUniqueId())) {
				meta.setDisplayName("" + ChatColor.WHITE + ChatColor.BOLD + p.getName());
			} else {
				meta.setDisplayName(ChatColor.WHITE + p.getName());
			}
			
			// Head texture
			meta.setOwner(p.getName());
			
			// Health
			lore.add(ChatColor.WHITE + "" + (int) p.getHealth() + ChatColor.RED + " ♥");

			// Distance
			if (p.getLocation().getWorld().getName() == player.getLocation().getWorld().getName()) {
				lore.add(ChatColor.WHITE + "Distancia: " + ChatColor.UNDERLINE + Math.round(p.getLocation().distance(player.getLocation())) + " bloques");
			} else {
				lore.add(ChatColor.WHITE + "Distancia: " + ChatColor.MAGIC + "11111" + ChatColor.RESET + " (en otro mundo)");
			}
			
			// Teleport
			if (p != player && this.plugin.teleportAllowed) {
				lore.add("" + ChatColor.GREEN + ChatColor.BOLD + "Click para teletransportarte");
			}
			
			// Promote
			if (p != player && party.isLeader(p)) {
				lore.add("");
				lore.add("" + ChatColor.GOLD + ChatColor.ITALIC + "Shift + Click derecho" + ChatColor.RESET + ChatColor.GOLD +" para entregar lider");
			}

			meta.setLore(lore);
			item.setItemMeta(meta);
			getInventory().setItem(i, item);
		}
		
		// Toggle scoreboard button
		if (party.isShowingBoard(player.getUniqueId())) {
			ItemStack hideButton = new ItemStack(Material.BARRIER);
			ItemMeta meta = hideButton.getItemMeta();
			List<String> lore = new ArrayList<String>();
			meta.setDisplayName("" + ChatColor.RED + ChatColor.BOLD + "Desactivar " + ChatColor.RED + "resumen de grupo");
			lore.add(ChatColor.GRAY + "Click para desactivar el");
			lore.add(ChatColor.GRAY + "listado del grupo en pantalla");
			meta.setLore(lore);
			hideButton.setItemMeta(meta);
			
			getInventory().setItem(SCOREBOARD_BUTTON_SLOT, hideButton);
		} else {
			ItemStack showButton = new ItemStack(Material.SIGN);
			ItemMeta meta = showButton.getItemMeta();
			List<String> lore = new ArrayList<String>();
			meta.setDisplayName("" + ChatColor.GREEN + ChatColor.BOLD + "Activar " + ChatColor.GREEN + "resumen de grupo");
			lore.add(ChatColor.GRAY + "Click para activar el");
			lore.add(ChatColor.GRAY + "listado del grupo en pantalla");
			meta.setLore(lore);
			showButton.setItemMeta(meta);
			
			getInventory().setItem(SCOREBOARD_BUTTON_SLOT, showButton);
		}
		
		// Chest button
		getInventory().setItem(CHEST_BUTTON_SLOT, PartyChest(player, party));
		player.openInventory(getInventory());
	}

	public void presentChest() {
		player.openInventory(party.getPartyChest());
	}

	public static ItemStack PartyChest(Player p, Party party) {
		ItemStack item = new ItemStack(Material.CHEST);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.GOLD + "Cofre del grupo");

		List<String> lore = new ArrayList<String>();
		lore.add(ChatColor.GRAY + "- " + ChatColor.WHITE + "Items compartidos");
		lore.add(ChatColor.WHITE + "  por el grupo.");
		
		meta.setLore(lore);
		item.setItemMeta(meta);
		
		return item;
	}

	public Party getParty() {
		return party;
	}

	public Player getPlayer() {
		return player;
	}

	public Inventory getInventory() {
		return inventory;
	}

	public Plugin getPlugin() {
		return this.plugin;
	}
}
