package sbs.mira.core.helper;


import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sbs.mira.core.MiraModule;
import sbs.mira.core.MiraPulse;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * this class handles all procedures or functions
 * relating to the inbuilt bukkit configuration
 * system. values in config.yml will be handled,
 * stored, and accessed through here as needed.
 * created on 2017-04-25.
 *
 * @author jj.mira.sbs
 * @author jd.mira.sbs
 * @version 1.0.1
 * @see org.bukkit.configuration.Configuration
 * @since 1.0.0
 */
public
class MiraConfiguration<Pulse extends MiraPulse<?, ?>>
  extends MiraModule<Pulse>
{
  
  private final @NotNull FileConfiguration config;
  private @Nullable FileConfiguration messages;
  public boolean WEBSTATS_ENABLED;
  public @NotNull String WEBSTATS_ACTION;
  public @NotNull String WEBSTATS_SECRET;
  public int WEBSTATS_POS;
  
  /**
   *
   * @param pulse
   */
  public
  MiraConfiguration(@NotNull Pulse pulse)
  {
    super(pulse);
    
    // Save the default config in case it has never existed before.
    this.pulse().plugin().saveDefaultConfig();
    
    // Reload and assign the file config.
    this.pulse().plugin().reloadConfig();
    config = this.pulse().plugin().getConfig();
    
    try
    {
      messages = YamlConfiguration.loadConfiguration(new InputStreamReader(
        this.pulse().plugin().getResource("messages.yml"),
                                                                           StandardCharsets.UTF_8
      ));
    }
    catch (Exception any)
    {
      this.pulse().plugin().log("The messages were not able to be loaded.");
      Bukkit.shutdown();
    }
    
    try
    {
      WEBSTATS_ENABLED = config.getBoolean("webstats.enable");
      WEBSTATS_ACTION = config.getString("webstats.action");
      WEBSTATS_SECRET = config.getString("webstats.secret");
      WEBSTATS_POS = config.getInt("webstats.position");
    }
    catch (Exception any)
    {
      this.pulse().plugin().log("The configuration was not able to be loaded.");
      WEBSTATS_ENABLED = false;
      WEBSTATS_ACTION = "";
      WEBSTATS_SECRET = "";
      WEBSTATS_POS = -1;
    }
  }
  
  /**
   * increment the match position.
   */
  public
  void incrementPosition()
  {
    WEBSTATS_POS++; // Increment the local value.
    config.set("webstats.position", WEBSTATS_POS); // Set the new position in the config.
    this.pulse().plugin().saveConfig(); // Save the config.
  }
  
  /**
   * returns a message value from a selected key.
   *
   * @param key the key.
   * @return the value.
   */
  public
  String getMessage(String key)
  {
    return messages.getString(key);
  }
}