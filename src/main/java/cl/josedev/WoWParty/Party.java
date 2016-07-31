package cl.josedev.WoWParty;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class Party {

	public static final int MAX_PLAYERS = 5;
	private UUID id;
	private String partyName;
	private List<UUID> members = new ArrayList<UUID>();
	private UUID leaderId;
	private Scoreboard board;
	private Team team;
	private Objective objective;
	private Inventory partyChest;
	private List<UUID> hiddenBoard = new ArrayList<UUID>();
	
	public Party(Player leader) {
		this.id = UUID.randomUUID();
		this.leaderId = leader.getUniqueId();
		setName(leader);
		this.partyChest = Bukkit.getServer().createInventory(leader, (9 * 5), ChatColor.GOLD + "Cofre del grupo");
		this.board = Bukkit.getServer().getScoreboardManager().getNewScoreboard();
		this.team = board.registerNewTeam(leader.getName());
		team.setAllowFriendlyFire(false);
		team.setCanSeeFriendlyInvisibles(true);
		this.objective = board.registerNewObjective("party_hp", "dummy");
		
		add(leader);
	}
	
	public void add(Player p) {
		members.add(p.getUniqueId());
		team.addEntry(p.getName());
		
		displayBoard(p);
	}
	
	public void changeLeader(Player newLeader) {
		if (newLeader.isOnline() && isMember(newLeader.getUniqueId())) {
			this.leaderId = newLeader.getUniqueId();
		}
	}
	
	public boolean checkSize() {
		if (getSize() == 1) {
			sendMessage(ChatColor.RED + "El grupo se ha disuelto por falta de miembros");
			
			for (UUID uuid : getMembers()) {
				Player pl = Bukkit.getServer().getPlayer(uuid);
				pl.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
			}
			
			Party.disolve(this);
			return true;
		}
		
		return false;
	}

	public int countOnline() {
		int count = 0;
		
		for (UUID pID : this.members) {
			Player p = Bukkit.getServer().getPlayer(pID);
			
			if (p != null) {
				if (p.isOnline()) {
					count++;
				}
			}
		}
		
		return count;
	}
	
	public static void disolve(Party party) {
		party.leaderId = null;
		party.members.clear();
		party.team = null;
		party.board = null;
		party.objective = null;
		WoWParty.getInstance().getManager().removeParty(party);
		party = null;
	}

	public void displayBoard(Player p) {
		objective.setDisplayName(this.getName());
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		
		for (UUID uuid : members) {
			Player member = Bukkit.getServer().getPlayer(uuid);
			int hp = (int) Math.round(member.getHealth());
			objective.getScore(ChatColor.WHITE + member.getName()).setScore(hp);
		}
		
		p.setScoreboard(board);
	}
	
	public void hideBoard(Player p) { 
		p.setScoreboard(Bukkit.getServer().getScoreboardManager().getNewScoreboard());
	}
	
	public Scoreboard getBoard(String name) {
		return board;
	}
	
	public UUID getId() {
		return this.id;
	}
	
	public UUID getLeaderID() {
		return leaderId;
	}
	
	public Player getLeader() {
		return Bukkit.getServer().getPlayer(leaderId);
	}
	
	public UUID getMember(int i) {
		return this.members.get(i);
	}
	
	public List<UUID> getMembers() {
		return this.members;
	}
	
	public String getName() {
		return this.partyName;
	}

	public int getSize() {
		return members.size();
	}
	
	public Inventory getPartyChest() {
		return partyChest;
	}
	
	public Player getPlayer(int position) {
		return Bukkit.getServer().getPlayer(members.get(position));
	}
	
	public boolean isFull() {
		return members.size() == MAX_PLAYERS;
	}
	
	public boolean isLeader(Player p) {
		return p.getUniqueId().equals(this.leaderId);
	}
	
	public boolean isMember(UUID uuid) {
		return this.members.contains(uuid);
	}
	
	public boolean isShowingBoard(UUID uuid) {
		return !this.hiddenBoard.contains(uuid);
	}
	
	public void remove(Player p) {
		members.remove(p.getUniqueId());
		team.removeEntry(p.getName());
		sendMessage(ChatColor.YELLOW + p.getName() + " ha abandonado el grupo");
		
		if (!checkSize()) {
			// If the current leader was removed
			if (p.getUniqueId().equals(leaderId)) {
				Player nl = Bukkit.getServer().getPlayer(members.get(0));
				setLeader(nl);
			}
			
			if (board != null)
				board.resetScores(ChatColor.WHITE + p.getName());
				
			update();
		}
		
		if (p.isOnline()) {
			hideBoard(p);
			p.sendMessage(WoWParty.TAG + ChatColor.YELLOW + "Abandonaste el grupo");
		}
	}
	
	public void sendMessage(String msg) {
		for (UUID uuid : getMembers()) {
			Player p = Bukkit.getServer().getPlayer(uuid);
			p.sendMessage(WoWParty.TAG + msg);
		}
	}
	
	public void sendChat(Player sender, String msg) {
		msg = ChatColor.BLUE + sender.getName() + ChatColor.WHITE + " : " + ChatColor.ITALIC + msg;
		sendMessage(msg);
		Bukkit.getServer().getLogger().info(ChatColor.stripColor(WoWParty.TAG + msg));
	}

	public void shareExp(Player origin, int expAmount) {
		if (isFull()) {
			expAmount += (expAmount * WoWParty.getInstance().bonusPct) / 100;
		}
		
		int sharedExp = (int) Math.floor(expAmount / countOnline());
		
		if (sharedExp == 0) {
			return;
		}
		
		for (UUID pID : this.members) {
			Player p = Bukkit.getServer().getPlayer(pID);
			
			if (p != null && p != origin) {
				if (p.isOnline()) {
					p.giveExp(sharedExp);
					p.sendMessage(WoWParty.TAG + ChatColor.GREEN + "+ " + sharedExp + " exp!");
				}
			}
		}
	}

	public void setLeader(Player leader) {
		this.leaderId = leader.getUniqueId();
		sendMessage(ChatColor.YELLOW + "El nuevo líder es " + ChatColor.BOLD + leader.getName());
		update();
	}
	
	public void setName(Player leader) {
		this.partyName = ChatColor.BLUE + "Grupo de " + ChatColor.BOLD + leader.getName();
	}
	
	public void toggleBoard(Player p) {
		if (isShowingBoard(p.getUniqueId())) {
			this.hiddenBoard.add(p.getUniqueId());
			hideBoard(p);
		} else {
			this.hiddenBoard.remove(p.getUniqueId());
			p.setScoreboard(board);
		}
	}
	
	public void update() {
		new BukkitRunnable() {
			public void run() {
				Player leader = Bukkit.getServer().getPlayer(leaderId);
				
				if (leader != null) {
					setName(leader);
					objective.setDisplayName(getName());
				}
				
				for (UUID uuid : getMembers()) {
					Player p = Bukkit.getServer().getPlayer(uuid);
					
					if (p != null) {
						objective.getScore(ChatColor.WHITE + p.getName()).setScore((int) Math.round(p.getHealth()));
						
						if (isShowingBoard(uuid)) {
							p.setScoreboard(board);
						}
					}
				}
			}
		}.runTaskLater(WoWParty.getInstance(), 5L);
	}
}
