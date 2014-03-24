package me.Build_.Collective;
import code.husky.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
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
	//-----> Logger and Plug-in <-----\\
	public final Logger logger = Logger.getLogger("Minecraft");
	public static Main plugin;
	//-----> Configuration Variables <-----\\
	public int number = getConfig().getInt("interval");
	public boolean console = getConfig().getBoolean("timer-notifications");
	//-----> Grab MySQL Credentials <-----\\
	public String dbHost = getConfig().getString("hostname", "localhost");
	public int dbPort = getConfig().getInt("port", 3306);
	public String dbName = getConfig().getString("database", "minecraft");
	public String dbUser = getConfig().getString("user", "root");
	public String dbPass = getConfig().getString("password", "password");
    code.husky.mysql.MySQL MySQL = new code.husky.mysql.MySQL(plugin, "host.name", "port", "database", "user", "pass");
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
		
		//-----> Setup Tables <-----\\
		c = MySQL.openConnection();
		try {
	    	Statement tableSetup;
	    	tableSetup = MySQL.openConnection().createStatement();;
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
							int logCount = Bukkit.getServer().getOnlinePlayers().length;
						    
							//-----> Main Logger <-----\\
						    PreparedStatement log;
							try {
								log = MySQL.openConnection().prepareStatement("INSERT INTO LW (`ign`, `world`, `x`, `y`, `z`, `timestamp`) VALUES (?, ?, ?, ?, ?,);");
								for (Player p : Bukkit.getOnlinePlayers())
							    {
							      Location loc = p.getLocation();
							      log.setString(1, p.getName());
							      log.setString(2, loc.getWorld().getName());
							      log.setString(3, String.valueOf(loc.getX()));
							      log.setString(4, String.valueOf(loc.getY()));
							      log.setString(5, String.valueOf(loc.getZ()));
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
			run.sendMessage(ChatColor.GOLD + "[" + ChatColor.GREEN + "-----" + ChatColor.GOLD + "]" + ChatColor.YELLOW + " Location Watchdog " + "[" + ChatColor.GREEN + "-----" + ChatColor.GOLD + "]");
			run.sendMessage(ChatColor.YELLOW + " Location Watchdog is a plugin by Build_ that ");
			run.sendMessage(ChatColor.YELLOW + " periodically logs all player locations. It can ");
			run.sendMessage(ChatColor.YELLOW + " later show all the previous locations of a player. ");
			run.sendMessage(ChatColor.YELLOW + " It's free on dev.bukkit.org. Thanks for using LW! ");
			run.sendMessage(ChatColor.GOLD + "[" + ChatColor.GREEN + "-----------------------------" + ChatColor.GOLD + "]");
	} else if (commandLabel.equalsIgnoreCase("lw timer")) {
			int logCount = Bukkit.getServer().getOnlinePlayers().length;
			run.sendMessage(ChatColor.GOLD + "[" + ChatColor.GREEN + "LW" + ChatColor.GOLD + "]" + ChatColor.YELLOW + " Seconds before next log: " + ChatColor.DARK_RED + logCount);
		}
		return false;
	}
}