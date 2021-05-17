package space.gorogoro.serverinformation;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/*
 * ServerInformation
 * @license    LGPLv3
 * @copyright  Copyright gorogoro.space 2021
 * @author     kubotan
 * @see        <a href="https://gorogoro.space">Gorogoro Server.</a>
 */
public class ServerInformation extends JavaPlugin implements Listener {

  /**
   * JavaPlugin method onEnable.
   */
  @Override
  public void onEnable() {
    try {
      getLogger().info("The Plugin Has Been Enabled!");

      // If there is no setting file, it is created
      if(!getDataFolder().exists()){
        getDataFolder().mkdir();
      }

      File configFile = new File(getDataFolder(), "config.yml");
      if(!configFile.exists()){
        saveDefaultConfig();
      }
      int intervalSeconds = getConfig().getInt("interval-seconds");

      getServer().getPluginManager().registerEvents(this, this);

      getServer().getScheduler().runTaskTimer(this, new Runnable() {
        public void run() {
          int countChunks = 0;
          int countEntities = 0;
          int countTileEntities = 0;
          for (World world : getServer().getWorlds()) {
            countChunks = 0;
            countEntities = 0;
            countTileEntities = 0;
            for (Chunk c : world.getLoadedChunks()) {
              countChunks++;
              countEntities += c.getEntities().length;
              countTileEntities += c.getTileEntities().length;
            }
            Bukkit.getServer().getLogger().info( String.format(
              "ServerInformation: %s %d %d %d"
              , world.getName()
              , countChunks
              , countEntities
              , countTileEntities
              ));
          }
          Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tps");
        }
      }, 0L, intervalSeconds * 20L);
    } catch (Exception e) {
      logStackTrace(e);
    }
  }

  /**
   * Output stack trace to log file.
   * @param Exception Exception
   */
  public void logStackTrace(Exception e){
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      pw.flush();
      getLogger().warning(sw.toString());
  }
  
  /**
   * JavaPlugin method onDisable.
   */
  @Override
  public void onDisable() {
    try {
      getLogger().info("The Plugin Has Been Disabled!");
    } catch (Exception e) {
      logStackTrace(e);
    }
  }
}
