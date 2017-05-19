package cl.josedev.LeParty.commands;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import cl.josedev.LeParty.Party;
import cl.josedev.LeParty.LeParty;

public class PartyAdminCommand implements CommandExecutor {

	LeParty plugin;
	
	public PartyAdminCommand(LeParty instance) {
		this.plugin = instance;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if (cmd.getName().equalsIgnoreCase("partyadmin")) {
			if (!sender.hasPermission(LeParty.PERM_ADMIN)) {
				error(sender, "No tienes permiso para usar este comando!");
				return true;
			}
			
			if (args.length == 0) {
				info(sender, "Para ver la lista de comandos usa /partyadmin help");
				return true;
			}
			
			switch (args[0].toLowerCase()) {
				case "help":
					info(sender, "WoWParty - Admin Menu");
					info(sender, "=====================");
					info(sender, "/partyadmin list " + ChatColor.WHITE + "- Listado de los grupos activos");
					info(sender, "/partyadmin spy " + ChatColor.WHITE + "- Activa/Desactiva el modo espía para el chat de grupo");
					info(sender, "/partyadmin info <player> " + ChatColor.WHITE + "- Obtiene la información del grupo al que pertenece un jugador");
					info(sender, "/partyadmin chest <player>" + ChatColor.WHITE + "- Muestra el cofre virtual del grupo al que pertenece el jugador");
					
					break;
				case "list":
					List<Party> list = plugin.getManager().getParties();
					
					if (list.size() > 0) {
						success(sender, "Listado de grupos activos");
						success(sender, "=========================");
						
						for (Party pt : list) {
							success(sender, "- " + pt.getName() + ": " + pt.getSize() + " miembros");
						}
					} else {
						success(sender, "No existen grupos activos");
					}
					
					break;
				case "spy":
					if (sender instanceof Player) {
						Player player = (Player) sender;
						plugin.getManager().toggleSpyMode(player);
					} else {
						error(sender, "Comando sólo para jugadores, la consola puede espiar el chat de grupo por defecto");
					}
					
					break;
				case "info":
					if (args.length == 2) {
						Player player = Bukkit.getServer().getPlayer(args[1]);
						
						if (player != null) {
							Party pt = plugin.getManager().getParty(player.getUniqueId());
							
							if (pt != null) {
								success(sender, ChatColor.BOLD + pt.getName());
								success(sender, pt.getSize() + " miembros:");
								
								for (UUID id : pt.getMembers()) {
									Player p = Bukkit.getServer().getPlayer(id);
									success(sender, "- " + p.getName() 
															+ (pt.isLeader(p) ? " (Lider)" : "") 
															+ " [" + p.getLocation().getWorld().getName() + ", "
															+ p.getLocation().getBlockX() + ", "
															+ p.getLocation().getBlockZ() + "]");
								}
							} else {
								error(sender, "El jugador indicado no pertenece a un grupo!");
							}
						} else {
							error(sender, "Jugador no encontrado!");
						}
					} else {
						error(sender, "Indica un jugador: /party info <player>");
					}
					
					break;
				case "chest":
					if (!(sender instanceof Player)) {
						error(sender, "Comando sólo para jugadores");
						return true;
					}
					
					if (args.length < 2) {
						error(sender, "Indica un jugador: /party chest <player");
						return true;
					}
					
					Player player = Bukkit.getServer().getPlayer(args[1]);
					
					if (player != null) {
						Party pt = plugin.getManager().getParty(player.getUniqueId());
						
						if (pt != null) {
							Player admin = (Player) sender;
							admin.openInventory(pt.getPartyChest());
						} else {
							error(sender, "El jugador no pertenece a un grupo!");
						}
					} else {
						error(sender, "Jugador no encontrado!");
					}
					
					break;
				default:
					error(sender, "Opción inválida. Consulta las opciones con " + ChatColor.UNDERLINE + "/partyadmin help");
			}
			
			return true;
		}
		
		return false;
	}

	private void  success(CommandSender sender, String msg) {
		sender.sendMessage(LeParty.TAG + ChatColor.GREEN + msg);
	}
	
	private void  info(CommandSender sender, String msg) {
		sender.sendMessage(LeParty.TAG + ChatColor.YELLOW + msg);
	}
	
	private void  error(CommandSender sender, String msg) {
		sender.sendMessage(LeParty.TAG + ChatColor.RED + msg);
	}
}
