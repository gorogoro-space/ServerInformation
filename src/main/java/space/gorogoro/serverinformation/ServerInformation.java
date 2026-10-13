package space.gorogoro.serverinformation;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/*
 * ServerInformation
 * @license    GPLv3
 * @copyright  Copyright gorogoro.space 2021
 * @author     kubotan
 * @see        <a href="https://gorogoro.space">Gorogoro Server.</a>
 */
public class ServerInformation extends JavaPlugin implements Listener {

  // 次に自動プロファイルを開始してよい時刻(System.currentTimeMillis())
  private long nextProfileAllowedMillis = 0L;

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
      // 既存の config.yml に足りない項目があれば、jar 内の既定値を追記する
      if (!getConfig().getKeys(true).containsAll(getConfig().getDefaults().getKeys(true))) {
        getConfig().options().copyDefaults(true);
        saveConfig();
      }
      int intervalSeconds = getConfig().getInt("interval-seconds");

      getServer().getPluginManager().registerEvents(this, this);

      getServer().getScheduler().runTaskTimer(this, new Runnable() {
        public void run() {
          for (World world : getServer().getWorlds()) {
            Bukkit.getServer().getLogger().info(String.format(
                    "ServerInformation: %s %d %d %d",
                    world.getName(),
                    world.getChunkCount(),
                    world.getEntityCount(),
                    world.getTileEntityCount()
            ));
          }
          // オンラインの人数(統合版とJava版の合計。Geyser/Floodgate 経由の統合版も含まれる)
          Bukkit.getServer().getLogger().info(String.format(
                  "ServerInformation: players %d",
                  getServer().getOnlinePlayers().size()
          ));
          Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tps");
        }
      }, 0L, intervalSeconds * 20L);

      startAutoProfiler();
    } catch (Exception e) {
      logStackTrace(e);
    }
  }

  /**
   * TPS(1 分平均)がしきい値以下になったら spark profiler を開始する監視タスクを登録する。
   * 停止は spark の --timeout に任せ、このプラグインからは stop を送らない(手動の計測を止めないため)。
   */
  private void startAutoProfiler() {
    if (!getConfig().getBoolean("auto-profiler.enabled")) {
      return;
    }
    double tpsThreshold = getConfig().getDouble("auto-profiler.tps-threshold");
    int checkIntervalSeconds = getConfig().getInt("auto-profiler.check-interval-seconds");
    int durationSeconds = getConfig().getInt("auto-profiler.duration-seconds");
    int onlyTicksOverMs = getConfig().getInt("auto-profiler.only-ticks-over-ms");
    int cooldownMinutes = getConfig().getInt("auto-profiler.cooldown-minutes");
    int startupDelayMinutes = getConfig().getInt("auto-profiler.startup-delay-minutes");
    String command = String.format(
            "spark profiler start --timeout %d --save-to-file --only-ticks-over %d",
            durationSeconds, onlyTicksOverMs
    );

    getServer().getScheduler().runTaskTimer(this, () -> {
      long now = System.currentTimeMillis();
      if (now < nextProfileAllowedMillis) {
        return;
      }
      double tps = Bukkit.getTPS()[0];
      if (tps > tpsThreshold) {
        return;
      }
      // 計測が終わってからクールダウンするまでは再実行しない(spark が開始を断った場合も同じ)
      nextProfileAllowedMillis = now + (durationSeconds + cooldownMinutes * 60L) * 1000L;
      Bukkit.getServer().getLogger().info(String.format(
              "ServerInformation: TPS %.2f <= %.2f, %s",
              tps, tpsThreshold, command
      ));
      Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }, startupDelayMinutes * 60L * 20L, checkIntervalSeconds * 20L);
  }

  /**
   * Output stack trace to log file.
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
