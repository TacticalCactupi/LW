package me.Build_.Collective;

import code.husky.mysql.MySQL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

//-----> Concluded Imports <-----\\
public class Main extends JavaPlugin {
	// -----> c-Logger and Plug-in <-----\\
	public final Logger logger = Logger.getLogger("Minecraft");
	public static Main plugin;
	Statement tableSetup;
	Statement grabber;
	int logCount = Bukkit.getServer().getOnlinePlayers().length;
	public final HashMap<Player, ArrayList<Block>> hashmap = new HashMap<Player, ArrayList<Block>>();
	// -----> Connection variable <-----\\
	Connection c;

	// -----> Disable Setup <-----\\
	@Override
	public void onDisable() {
		saveConfig();
		c = null;
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName()
				+ " was successfully disabled. Goodbye!");
		hashmap.clear();
	}

	// -----> Enable Setup <-----\\
	@Override
	public void onEnable() {
		// -----> Config. Generator <-----\\
		saveConfig();
		// -----> Grab MySQL Credentials <-----\\
		final String dbHost = getConfig().getString("hostname", "localhost");
		final String dbPort = getConfig().getString("port", "3306");
		final String dbName = getConfig().getString("database", "minecraft");
		final String dbUser = getConfig().getString("user", "root");
		final String dbPass = getConfig().getString("password", "password");
		final MySQL MySQL = new MySQL(plugin, dbHost, dbPort, dbName, dbUser,
				dbPass);
		// -----> Setup Tables <-----\\
		c = MySQL.openConnection();
		try {
			tableSetup = MySQL.openConnection().createStatement();
			tableSetup
					.execute("CREATE TABLE IF NOT EXISTS `LW` (`ID` INTEGER PRIMARY KEY NOT NULL, `w` TEXT NOT NULL, `x` TEXT NOT NULL, `y` TEXT NOT NULL, `z` TEXT NOT NULL)");
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// -----> Main Timer <-----\\
		this.getServer().getScheduler()
				.scheduleAsyncRepeatingTask(this, new Runnable() {
					public void run() {
						int number = getConfig().getInt("interval", 15);
						if (number != 0) {
							number--;
						} else {
							// -----> Main Logger <-----\\
							PreparedStatement log;
							try {
								log = MySQL
										.openConnection()
										.prepareStatement(
												"INSERT INTO LW (`ign`, `w`, `x`, `y`, `z`) VALUES (?, ?, ?, ?, ?,);");
								for (Player p : Bukkit.getOnlinePlayers()) {
									Location loc = p.getLocation();
									log.setString(1, p.getName());
									log.setString(2, loc.getWorld().getName());
									log.setNString(3,
											String.valueOf(loc.getX()));
									log.setNString(4,
											String.valueOf(loc.getY()));
									log.setNString(5,
											String.valueOf(loc.getZ()));
									log.execute();
								}
							} catch (SQLException e) {
								e.printStackTrace();
							}

							// -----> Notifications <-----\\
							boolean console = getConfig().getBoolean(
									"timer-notifications", false);
							if (console = true) {
								logger.info("Location Watchdog has just logged "
										+ logCount + " player locations.");
							}
							number = getConfig().getInt("interval");
						}
					}
				}, 0L, 20L);

		// -----> Main Enabler <-----\\
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " Version " + pdfFile.getVersion()
				+ " was successfully enabled!");
	}

	// -----> Main Commands <-----\\
	@Override
	public boolean onCommand(CommandSender sender, Command cmd,
			String commandLabel, String[] args) {
		Player run = (Player) sender;
		World w = run.getWorld();
		if (commandLabel.equalsIgnoreCase("lw")) {
			if (args.length == 0) {
				run.sendMessage(ChatColor.GOLD + "[" + ChatColor.GREEN
						+ "-----" + ChatColor.GOLD + "]" + ChatColor.YELLOW
						+ " Location Watchdog " + "[" + ChatColor.GREEN
						+ "-----" + ChatColor.GOLD + "]");
				run.sendMessage(ChatColor.YELLOW
						+ " Location Watchdog is a plugin by Build_ that ");
				run.sendMessage(ChatColor.YELLOW
						+ " periodically logs all player locations. It can ");
				run.sendMessage(ChatColor.YELLOW
						+ " later show all the previous locations of a player. ");
				run.sendMessage(ChatColor.YELLOW
						+ " Help: /lw help - Thanks for using LW! ");
				run.sendMessage(ChatColor.GOLD + "[" + ChatColor.GREEN
						+ "-----------------------------" + ChatColor.GOLD
						+ "]");
			} else if (args.length == 1) {
				if (args[0].equals("help")) {
					run.sendMessage(ChatColor.GOLD + "[" + ChatColor.GREEN
							+ "LW" + ChatColor.GOLD + "]" + ChatColor.YELLOW
							+ " Commands: ");
					run.sendMessage(ChatColor.GOLD + "[" + ChatColor.GREEN
							+ "LW" + ChatColor.GOLD + "]" + ChatColor.DARK_RED
							+ " /lw - About Location Watchdog.");
					run.sendMessage(ChatColor.GOLD + "[" + ChatColor.GREEN
							+ "LW" + ChatColor.GOLD + "]" + ChatColor.DARK_RED
							+ " /lw get - See LW's Bukkit page.");
					run.sendMessage(ChatColor.GOLD + "[" + ChatColor.GREEN
							+ "LW" + ChatColor.GOLD + "]" + ChatColor.DARK_RED
							+ " /lw help - Shows this page.");
					run.sendMessage(ChatColor.GOLD + "[" + ChatColor.GREEN
							+ "LW" + ChatColor.GOLD + "]" + ChatColor.DARK_RED
							+ " /lw timer - Shows time before next log.");
					run.sendMessage(ChatColor.GOLD + "[" + ChatColor.GREEN
							+ "LW" + ChatColor.GOLD + "]" + ChatColor.DARK_RED
							+ " /lw version - Check LW's installed version.");
					run.sendMessage(ChatColor.GOLD + "[" + ChatColor.GREEN
							+ "LW" + ChatColor.GOLD + "]" + ChatColor.DARK_RED
							+ " /lw check <player> - Show a players locations.");
					run.sendMessage(ChatColor.GOLD
							+ "["
							+ ChatColor.GREEN
							+ "LW"
							+ ChatColor.GOLD
							+ "]"
							+ ChatColor.DARK_RED
							+ " /lw done <player> - Finish viewing player locations.");
				} else if (args[0].equals("get")) {
					run.sendMessage(ChatColor.GOLD + "[" + ChatColor.GREEN
							+ "LW" + ChatColor.GOLD + "]" + ChatColor.YELLOW
							+ " Bukkit Dev Page:" + ChatColor.DARK_RED
							+ "dev.bukkit.org/location-watchdog/");
				} else if (args[0].equals("version")) {
					PluginDescriptionFile pdfFile = this.getDescription();
					run.sendMessage(ChatColor.GOLD + "[" + ChatColor.GREEN
							+ "LW" + ChatColor.GOLD + "]" + ChatColor.YELLOW
							+ " Installed Version: " + ChatColor.DARK_RED
							+ pdfFile.getVersion() + " By Build_.");
				} else if (args[0].equals("check")) {
					run.sendMessage(ChatColor.GOLD + "[" + ChatColor.GREEN
							+ "LW" + ChatColor.GOLD + "]" + ChatColor.YELLOW
							+ " Incorrect syntax! " + ChatColor.DARK_RED
							+ "/lw check <player>");
				}
			} else if (args.length == 2) {
				if (args[0].equals("check")) {
					if (run.hasPermission("lw.staff")) {
						String target = args[1];
						String world = run.getWorld().getName();
						try {
							grabber = ((MySQL) c).openConnection()
									.createStatement();
							ResultSet res = grabber
									.executeQuery("SELECT * FROM LW WHERE ign = '"
											+ target
											+ "' AND w = '"
											+ world
											+ "';");
							if (res.getString("ign") == null) {
								run.sendMessage(ChatColor.GOLD + "["
										+ ChatColor.GREEN + "LW"
										+ ChatColor.GOLD + "]"
										+ ChatColor.YELLOW + " Error: "
										+ ChatColor.WHITE + target
										+ ChatColor.DARK_RED
										+ " doesn't have any logged locations!");
							} else {

								while (res.next()) {
									int x = res.getInt("x");
									int y = res.getInt("y");
									int z = res.getInt("z");
									run.sendBlockChange(
											new Location(w, x, y, z), 113,
											(byte) 0);
									y++;
									run.sendBlockChange(
											new Location(w, x, y, z), 89,
											(byte) 0);
									res.next();
								}
								hashmap.put(run, null);
							}
						} catch (SQLException e) {
							e.printStackTrace();
						}
					} else {
						run.sendMessage(ChatColor.GOLD + "[" + ChatColor.GREEN
								+ "LW" + ChatColor.GOLD + "]"
								+ ChatColor.YELLOW + " Error: "
								+ ChatColor.DARK_RED
								+ " You don't have permission!");
					}
				} else if (args[0].equals("done")) {
					if (hashmap.containsKey(run)) {
						String target = args[1];
						String world = run.getWorld().getName();
						try {
							ResultSet un = grabber
									.executeQuery("SELECT * FROM LW WHERE ign = '"
											+ target
											+ "' AND w = '"
											+ world
											+ "';");
							while (un.next()) {
								int x = un.getInt("x");
								int y = un.getInt("y");
								int z = un.getInt("z");
								Block b = w.getBlockAt(x, y, z);
								run.sendBlockChange(new Location(w, x, y, z),
										b.getTypeId(), (byte) b.getData());
								y++;
								run.sendBlockChange(new Location(w, x, y, z),
										b.getTypeId(), (byte) b.getData());
								un.next();
							}
						} catch (SQLException e) {
							e.printStackTrace();
						}
						hashmap.remove(run);
					} else {
						run.sendMessage(ChatColor.GOLD + "[" + ChatColor.GREEN
								+ "LW" + ChatColor.GOLD + "]"
								+ ChatColor.YELLOW + " Error: "
								+ ChatColor.DARK_RED
								+ " You weren't viewing player locations!");
					}
				} else {
					run.sendMessage(ChatColor.GOLD + "[" + ChatColor.GREEN
							+ "LW" + ChatColor.GOLD + "]" + ChatColor.YELLOW
							+ " Error: " + ChatColor.DARK_RED
							+ "Incorrect syntax!" + ChatColor.YELLOW
							+ " Try /lw help.");
				}
			} else {
				run.sendMessage(ChatColor.GOLD + "[" + ChatColor.GREEN + "LW"
						+ ChatColor.GOLD + "]" + ChatColor.YELLOW + " Error: "
						+ ChatColor.DARK_RED + "Incorrect syntax!"
						+ ChatColor.YELLOW + " Try /lw help.");
			}
			return false;
		}
		return false;
	}
}