package cl.josedev.WoWParty;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import cl.josedev.WoWParty.commands.PartyCommand;
import cl.josedev.WoWParty.listeners.ChatListener;
import cl.josedev.WoWParty.listeners.GUIListener;
import cl.josedev.WoWParty.listeners.PlayerListener;

public class WoWParty extends JavaPlugin {
	
	public int bonusPct = 1;
	public boolean teleportAllowed = false;
	public int invitationDuration = 30;
	public static String TAG = ChatColor.WHITE + "[" + ChatColor.BLUE + ChatColor.BOLD + "Grupo" + ChatColor.WHITE + "] " + ChatColor.RESET;
	private static WoWParty instance;
	private PartyManager manager;
	
	@Override
	public void onEnable() {
		saveDefaultConfig();
		
		bonusPct = this.getConfig().getInt("fullPartyBonusPct");
		teleportAllowed = this.getConfig().getBoolean("teleportAllowed");
		invitationDuration = this.getConfig().getInt("invitationDuration");
		
		manager = new PartyManager();
		
		getCommand("party").setExecutor(new PartyCommand(this));
		getServer().getPluginManager().registerEvents(new ChatListener(this), this);
		getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
		getServer().getPluginManager().registerEvents(new GUIListener(this), this);
		instance = this;
	}
	
	@Override
	public void onDisable() {
		
	}
	
	public PartyManager getManager() {
		return this.manager;
	}

	public static WoWParty getInstance() {
		return instance;
	}
	
}
