package me.Build_.Collective;
import code.husky.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import javax.sql.StatementEvent;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

    //-----> Concluded Imports <-----\\
@SuppressWarnings("unused")
public class Main extends JavaPlugin {
	//-----> c-Logger and Plug-in <-----\\
	public final Logger logger = Logger.getLogger("Minecraft");
	public static Main plugin;
	Statement tableSetup;
	Statement grabber;
	int logCount = Bukkit.getServer().getOnlinePlayers().length;
	//-----> Configuration Variables <-----\\
	public int number = getConfig().getInt("interval");
	public boolean console = getConfig().getBoolean("timer-notifications");
	//-----> Grab MySQL Credentials <-----\\
	public String dbHost = getConfig().getString("hostname", "localhost");
	public String dbPort = getConfig().getString("port", "3306");
	public String dbName = getConfig().getString("database", "minecraft");
	public String dbUser = getConfig().getString("user", "root");
	public String dbPass = getConfig().getString("password", "password");
    code.husky.mysql.MySQL MySQL = new code.husky.mysql.MySQL(plugin, dbHost, dbPort, dbName, dbUser, dbPass);
    //-----> Connection variable <-----\\
    Connection c = null;
    //-----> Disable Setup <-----\\
	@Override
	public void onDisable() {
		Connection c = null;
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName()
				+ " was successfully disabled. Goodbye!");
		number = 0;
	}

	//-----> Enable Setup <-----\\
	@SuppressWarnings("deprecation")
	@Override
	public void onEnable() {
		number = getConfig().getInt("interval");
		//-----> Config. Generator <-----\\
		getConfig().options().copyDefaults(true);
		saveConfig();
		//-----> Setup Tables <-----\\
		c = MySQL.openConnection();
		try {
	    	tableSetup = MySQL.openConnection().createStatement();
			tableSetup.executeQuery("CREATE TABLE IF NOT EXISTS `LW`");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		//-----> Main Timer <-----\\
		this.getServer().getScheduler()
				.scheduleAsyncRepeatingTask(this, new Runnable() {
					public void run() {
						if (number != 0) {
							number--;
						} else {
						    
							//-----> Main Logger <-----\\
						    PreparedStatement log;
							try {
								log = MySQL.openConnection().prepareStatement("INSERT INTO LW (`ign`, `w`, `x`, `y`, `z`) VALUES (?, ?, ?, ?, ?,);");
								for (Player p : Bukkit.getOnlinePlayers())
							    {
							      Location loc = p.getLocation();
							      log.setString(1, p.getName());
							      log.setString(2, loc.getWorld().getName());
							      log.setNString(3, String.valueOf(loc.getX()));
							      log.setNString(4, String.valueOf(loc.getY()));
							      log.setNString(5, String.valueOf(loc.getZ()));
							      log.execute();
							    }
							} catch (SQLException e) {
								e.printStackTrace();
							}
							
							//-----> Notifications <-----\\
							if(console = true){
								logger.info("Location Watchdog has just logged " + logCount + " player locations.");
							}
							number = getConfig().getInt("interval");
						}
					}
				}, 0L, 20L);
		
		//-----> Main Enabler <-----\\
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " Version " + pdfFile.getVersion()
				+ " was successfully enabled!");
	}
	
	//-----> Main Commands <-----\\
	@Override
	public boolean onCommand(CommandSender sender, Command cmd,
			String commandLabel, String[] args) {
		Player run = (Player) sender;
		if(commandLabel.equalsIgnoreCase("lw")){
			if(args.length == 0){
			run.sendMessage(ChatColor.GOLD + "[" + ChatColor.GREEN + "-----" + ChatColor.GOLD + "]" + ChatColor.YELLOW + " Location Watchdog " + "[" + ChatColor.GREEN + "-----" + ChatColor.GOLD + "]");
			run.sendMessage(ChatColor.YELLOW + " Location Watchdog is a plugin by Build_ that ");
			run.sendMessage(ChatColor.YELLOW + " periodically logs all player locations. It can ");
			run.sendMessage(ChatColor.YELLOW + " later show all the previous locations of a player. ");
			run.sendMessage(ChatColor.YELLOW + " Help: /lw help - Thanks for using LW! ");
			run.sendMessage(ChatColor.GOLD + "[" + ChatColor.GREEN + "-----------------------------" + ChatColor.GOLD + "]");
			} else if (args.length == 1){
				if (args[0] == "timer"){
					run.sendMessage(ChatColor.GOLD + "[" + ChatColor.GREEN + "LW" + ChatColor.GOLD + "]" + ChatColor.YELLOW + " Seconds before next log: " + ChatColor.DARK_RED + number);
				} else if (args[0] == "help"){
					run.sendMessage(ChatColor.GOLD + "[" + ChatColor.GREEN + "LW" + ChatColor.GOLD + "]" + ChatColor.YELLOW + " Commands: ");
					run.sendMessage(ChatColor.GOLD + "[" + ChatColor.GREEN + "LW" + ChatColor.GOLD + "]" + ChatColor.DARK_RED + " /lw - About Location Watchdog.");
					run.sendMessage(ChatColor.GOLD + "[" + ChatColor.GREEN + "LW" + ChatColor.GOLD + "]" + ChatColor.DARK_RED + " /lw get - See LW's Bukkit page.");
					run.sendMessage(ChatColor.GOLD + "[" + ChatColor.GREEN + "LW" + ChatColor.GOLD + "]" + ChatColor.DARK_RED + " /lw help - Shows this page.");
					run.sendMessage(ChatColor.GOLD + "[" + ChatColor.GREEN + "LW" + ChatColor.GOLD + "]" + ChatColor.DARK_RED + " /lw timer - Shows time before next log.");
					run.sendMessage(ChatColor.GOLD + "[" + ChatColor.GREEN + "LW" + ChatColor.GOLD + "]" + ChatColor.DARK_RED + " /lw version - Check LW's installed version.");
					run.sendMessage(ChatColor.GOLD + "[" + ChatColor.GREEN + "LW" + ChatColor.GOLD + "]" + ChatColor.DARK_RED + " /lw check <player> <world> - Show a players locations.");
				} else if (args[0] == "get"){
					run.sendMessage(ChatColor.GOLD + "[" + ChatColor.GREEN + "LW" + ChatColor.GOLD + "]" + ChatColor.YELLOW + " Bukkit Dev Page:" + ChatColor.DARK_RED + "dev.bukkit.org/location-watchdog/");
				} else if (args[0] == "version"){
					PluginDescriptionFile pdfFile = this.getDescription();
					run.sendMessage(ChatColor.GOLD + "[" + ChatColor.GREEN + "LW" + ChatColor.GOLD + "]" + ChatColor.YELLOW + " Installed Version: " + ChatColor.DARK_RED + pdfFile.getVersion() + " By Build_.");
				} else if (args[0] == "check"){
					run.sendMessage(ChatColor.GOLD + "[" + ChatColor.GREEN + "LW" + ChatColor.GOLD + "]" + ChatColor.YELLOW + " Incorrect syntax! " + ChatColor.DARK_RED + "/lw check <player> <world>");
 					}
				} else if (args.length == 2){
					run.sendMessage(ChatColor.GOLD + "[" + ChatColor.GREEN + "LW" + ChatColor.GOLD + "]" + ChatColor.YELLOW + " Error: " + ChatColor.DARK_RED + "Incorrect syntax!" + ChatColor.YELLOW + " Try /lw help.");
				} else if (args.length == 3){
					if(args[0] == "check"){
						if(run.hasPermission("lw.staff")){
							String target = args[1];
							
							try {
								grabber = MySQL.openConnection().createStatement();
								ResultSet res = grabber.executeQuery("SELECT * FROM LW WHERE ign = '" + target + "';");
								res.next();
								if(res.getString("ign") == null){
									run.sendMessage(ChatColor.GOLD + "[" + ChatColor.GREEN + "LW" + ChatColor.GOLD + "]" + ChatColor.YELLOW + " Error: " + ChatColor.WHITE + target + ChatColor.DARK_RED + " doesn't have any logged locations!");
								} else {
									
									// TODO Show player locations and make a way (command) to revert it.
									
								}
							} catch (SQLException e) {
								e.printStackTrace();
							}
							
						} else {
							run.sendMessage(ChatColor.GOLD + "[" + ChatColor.GREEN + "LW" + ChatColor.GOLD + "]" + ChatColor.YELLOW + " Error: " + ChatColor.DARK_RED + " You don't have permission!");
						}
					} else {
							run.sendMessage(ChatColor.GOLD + "[" + ChatColor.GREEN + "LW" + ChatColor.GOLD + "]" + ChatColor.YELLOW + " Error: " + ChatColor.DARK_RED + "Incorrect syntax!" + ChatColor.YELLOW + " Try /lw help.");
					}
				} else {
							run.sendMessage(ChatColor.GOLD + "[" + ChatColor.GREEN + "LW" + ChatColor.GOLD + "]" + ChatColor.YELLOW + " Error: " + ChatColor.DARK_RED + "Incorrect syntax!" + ChatColor.YELLOW + " Try /lw help.");
				}
			}
		return false;
	}
}