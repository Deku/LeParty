package cl.josedev.WoWParty;

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
	private WoWParty plugin;
	private Inventory inventory;
	private Player player;
	private Party party;

	public PartyGUI(WoWParty plugin, Player player, Party party) {
		this.plugin = plugin;
		this.player = player;
		this.party = party;
	}

	public void present(boolean promote) {
		inventory = Bukkit.getServer().createInventory(player, 9, party.getName());
		
		for (int i = 0; i < party.getSize(); i++) {
			UUID uuid = party.getMember(i);
			Player p = Bukkit.getServer().getPlayer(uuid);
			ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
			SkullMeta meta = (SkullMeta) item.getItemMeta();
			ArrayList<String> lore = new ArrayList<String>();
			lore.add(ChatColor.WHITE + "" + (int) p.getHealth() + ChatColor.RED + " ♥");
			lore.add(ChatColor.WHITE + "Distancia: " + ChatColor.UNDERLINE + Math.round(p.getLocation().distance(player.getLocation())) + " bloques");
			meta.setOwner(p.getName());
			
			if (p != player && this.plugin.teleportAllowed) {
				lore.add("" + ChatColor.GREEN + ChatColor.BOLD + "Click para teletransportarte");
			}
			
			if (promote && p != player && party.getLeaderID().equals(player.getUniqueId())) {
				lore.add("" + ChatColor.GOLD + ChatColor.ITALIC + "Shift + Click derecho" + ChatColor.RESET + ChatColor.GOLD +" para entregar lider");
			}

			if (party.getLeaderID().equals(p.getUniqueId())) {
				meta.setDisplayName("" + ChatColor.WHITE + ChatColor.BOLD + p.getName());
			} else {
				meta.setDisplayName(ChatColor.WHITE + p.getName());
			}
			meta.setLore(lore);
			item.setItemMeta(meta);
			getInventory().setItem(i, item);
		}
		getInventory().setItem(8, PartyChest(player, party));
		player.openInventory(getInventory());
	}

	public void presentChest() {
		player.openInventory(party.getPartyChest());
	}

	public static ItemStack PartyChest(Player p, Party party) {
		ItemStack item = new ItemStack(Material.CHEST);
		ItemMeta meta = item.getItemMeta();

		List<String> lore = new ArrayList<String>();
		lore.add(ChatColor.GRAY + "- " + ChatColor.WHITE + "Items compartidos");
		lore.add(ChatColor.WHITE + "    por el grupo.");
		
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
