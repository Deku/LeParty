package cl.josedev.WoWParty.commands;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import cl.josedev.WoWParty.WoWParty;
import cl.josedev.WoWParty.Party;
import cl.josedev.WoWParty.PartyGUI;

public class PartyCommand implements CommandExecutor {

	private final WoWParty plugin;
	
	public PartyCommand(WoWParty plugin) {
		this.plugin = plugin;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// /party
		if (cmd.getName().equalsIgnoreCase("party")) {
			
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "Este comando sólo puede ser ejecutado por jugadores!");
				return true;
			}
			
			Player p = (Player) sender;
			Party party = this.plugin.getManager().getParty(p.getUniqueId());
			
			if (args.length == 0) {
				if (party != null) {
					PartyGUI gui = new PartyGUI(this.plugin, p, party);
					gui.present(party.isLeader(p));
					
					return true;
				}
				
				info(p, "No estás en ningún grupo. Puedes crear uno invitando a otra persona con /party invite <player>");
				return true;
				
			}
			
			switch (args[0].toLowerCase()) {
				case "help":
					info(p, "Comandos disponibles:");
					info(p, "/party -  Abre la ventana del grupo");
					info(p, "/party invite <player> -  Invita a un jugador a tu grupo");
					info(p, "/party accept -  Acepta la invitación actual");
					info(p, "/party decline -  Rechaza la invitación actual");
					info(p, "/party kick <player> -  Expulsa a un jugador de tu grupo");
					info(p, "/party leave -  Abandona el grupo");
					info(p, "/party leader <player> -  Entrega el líder a otro jugador del grupo");
					info(p, "/party chat -  Activa/Desactiva el chat de grupo");
					info(p, "/party roll -  Tira un dado de 1 a 100 (lo ve sólo el grupo)");
					
					return true;
				case "leave":
					if (party != null) {
						party.remove(p);
					} else {
						error(p,"No estás en un grupo!");
					}
					return true;
				case "chat":
					if (party != null) {
						this.plugin.getManager().togglePartyChat(p);
					} else {
						error(p,"No estás en un grupo!");
					}
					
					return true;
				case "roll":
					if (party != null) {
						int r = new Random().nextInt(100);
						party.sendMessage(p.getName() + " ha lanzado un dado mágico, y obtuvo " + ChatColor.BOLD + r + " de 100!");
					} else {
						error(p,"No estás en un grupo!");
					}

					return true;
				case "invite":
					if (args.length == 1) {
						error(p, "Faltan argumentos. " + ChatColor.DARK_RED + "/party invite <player>");
						return true;
					}
					
					Player invitedPlayer = Bukkit.getServer().getPlayer(args[1]);
					
					if (invitedPlayer == null) {
						error(p, "Ese jugador no existe!");
						return true;
					}
					
					if (invitedPlayer.isOnline() && invitedPlayer != p) {
						Party o_party = this.plugin.getManager().getParty(invitedPlayer.getUniqueId());
						
						if (o_party != null && !this.plugin.getManager().isInvited(invitedPlayer.getUniqueId())) {
							error(p, invitedPlayer.getName() + " ya pertenece a un grupo o ha sido invitado a uno!");
							return true;
						}
						
						// If not in party, create one
						if (party != null) {
							if (party.isFull()) {
								error(p, "El grupo está lleno!");
								return true;
							}
							
							invite(party, p, invitedPlayer);
						} else {
							party = new Party(p);
							this.plugin.getManager().addParty(party);
							invite(party, p, invitedPlayer);
						}
					} else {
						error(p, "Jugador no encontrado!");
					}
					
					return true;
				case "accept":
					if (this.plugin.getManager().isInvited(p.getUniqueId())) {
						Party pt = this.plugin.getManager().getInvitationParty(p.getUniqueId());
						
						if (pt != null) {
							this.plugin.getManager().removeInvite(p.getUniqueId());
							pt.add(p);
							pt.update();
							pt.sendMessage(ChatColor.GREEN + p.getName() + " se ha unido al grupo!");
						}
					} else {
						error(p, "No tienes invitaciones pendientes!");
					}
					
					return true;
				case "decline":
					if (this.plugin.getManager().isInvited(p.getUniqueId())) {
						Party pt = this.plugin.getManager().getInvitationParty(p.getUniqueId());
						
						this.plugin.getManager().removeInvite(p.getUniqueId());
						info(p, "Rechazaste la invitación al grupo de " + pt.getLeader().getName());
						
						if (pt.getSize() == 1) {
							Player leader = pt.getLeader();
							leader.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
							leader.sendMessage(WoWParty.TAG + ChatColor.RED + " Tu grupo fue disuelto porque " + p.getName() + " rechazó tu invitación");
							Party.disolve(pt);
						}
					} else {
						error(p, "No tienes invitaciones pendientes!");
					}
					
					return true;
				case "kick":
					if (party != null) {
						if (args.length == 1) {
							error(p, "Faltan argumentos. " + ChatColor.DARK_RED + "/party kick <player>");
							return true;
						}
						
						Player kickedPlayer = Bukkit.getServer().getPlayer(args[1]);
						
						if (kickedPlayer != null && kickedPlayer.isOnline()) {
							if (party != null) {
								if (party.isLeader(p)) {
									party.remove(kickedPlayer);
									info(kickedPlayer, "Te han echado del " + party.getName() + "!");
									party.sendMessage(kickedPlayer.getName() + " ha sido expulsado del grupo!");
									party.update();
								} else {
									error(p, "Solo el líder puede expulsar del grupo!");
								}
							}
						} else {
							error(p, "No se ha encontrado el jugador!");
						}
					} else {
						error(p,"No estás en un grupo!");
					}
					
					return true;
				case "leader":
					if (party != null) {
						if (args.length == 1) {
							error(p, "Faltan argumentos. " + ChatColor.DARK_RED + "/party leader <player>");
							return true;
						}
						
						if (!party.isLeader(p)) {
							error(p, "No eres el líder del grupo!");
							return true;
						}
						
						Player nl = Bukkit.getServer().getPlayer(args[1]);
						
						if (nl == null) {
							error(p, "Jugador no encontrado!");
							return true;
						}
						
						if (nl.isOnline()) {
							if (party.isMember(nl.getUniqueId())) {
								party.setLeader(nl);
							} else {
								p.sendMessage(WoWParty.TAG + ChatColor.RED + "Selecciona a un jugador del grupo!");
							}
						} else {
							p.sendMessage(WoWParty.TAG + ChatColor.RED + "El jugador no está en ĺínea!");
						}
					} else {
						error(p, "No estás en un grupo!");
					}
					
					return true;
				default:
					p.sendMessage(ChatColor.RED + "Comando inválido. Usa " + ChatColor.UNDERLINE + "/party help" + ChatColor.RESET + " " + ChatColor.RED + "para ver la lista de comandos");
			}
		}
		
		return false;
	}
	
	public void invite(final Party party, final Player leader, final Player player) {
		if (!party.isMember(player.getUniqueId())) {
			if (!this.plugin.getManager().isInvited(player.getUniqueId())) {
				player.sendMessage(ChatColor.YELLOW + "Has sido invitado al grupo de " + ChatColor.UNDERLINE + party.getLeader().getName());
				player.sendMessage(ChatColor.GRAY + "La invitación expira en " + this.plugin.invitationDuration + " segundos!");
				player.sendMessage(ChatColor.GREEN + "Escribe " + ChatColor.BOLD + "/party accept" + ChatColor.YELLOW + " o " + ChatColor.RED + ChatColor.BOLD + "/party decline");
				leader.sendMessage(ChatColor.YELLOW + "Has invitado a " + player.getName() + " a tu grupo!");
				this.plugin.getManager().addInvite(player.getUniqueId(), party);
				
				final WoWParty pl = this.plugin;
				new BukkitRunnable() {

					public void run() {
						if (pl.getManager().isInvited(player.getUniqueId())) {
							pl.getManager().removeInvite(player.getUniqueId());
							player.sendMessage(WoWParty.TAG + "Tu invitación está expirado!");
							
							if (party.getMembers().size() == 1) {
								Party.disolve(party);
								leader.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
								leader.sendMessage(WoWParty.TAG + ChatColor.RED + "El grupo se ha disuelto");
							}
						}
					}
				}.runTaskLater(this.plugin, (20L * this.plugin.invitationDuration));
			} else {
				leader.sendMessage(WoWParty.TAG + ChatColor.RED + "El jugador ya ha sido invitado!");
			}	
		} else {
			leader.sendMessage(WoWParty.TAG + ChatColor.RED + "Player is already in your party!");
			return;
		}
	}

	private void  info(Player p, String msg) {
		p.sendMessage(WoWParty.TAG + ChatColor.YELLOW + msg);
	}
	
	private void  error(Player p, String msg) {
		p.sendMessage(WoWParty.TAG + ChatColor.RED + msg);
	}
}
