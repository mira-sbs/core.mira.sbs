package sbs.mira.core.module;


import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sbs.mira.core.MiraModule;
import sbs.mira.core.MiraPulse;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

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
final
class MiraConfigurationModule<Pulse extends MiraPulse<?, ?>>
  extends MiraModule<Pulse>
{
  
  protected final @NotNull FileConfiguration file_config;
  protected final Map<String, String> mapping;
  
  /**
   * instantiates with a `FileConfiguration` directly provided.
   *
   * @param pulse       mira.
   * @param file_config the file configuration - usually a bukkit plugin `config.yml` file.
   */
  public
  MiraConfigurationModule( @NotNull Pulse pulse, @NotNull FileConfiguration file_config )
  {
    super( pulse );
    
    this.file_config = file_config;
    this.mapping = new TreeMap<>( );
  }
  
  /**
   * instantiates with a `YamlConfiguration` embedded resource which is loaded by name.
   *
   * @param pulse                mira.
   * @param config_resource_name the name of the embedded resource file in the plugin `.jar`.
   */
  public
  MiraConfigurationModule( @NotNull Pulse pulse, @NotNull String config_resource_name )
  {
    super( pulse );
    
    this.mapping = new TreeMap<>( );
    
    this.file_config = YamlConfiguration.loadConfiguration(
      new InputStreamReader(
        Objects.requireNonNull( this.pulse( ).plugin( ).getResource( config_resource_name ) ),
        StandardCharsets.UTF_8
      )
    );
    
    this
      .pulse( )
      .plugin( )
      .log( "(^-^) successfully loaded file configuration '%s'".formatted( config_resource_name ) );
  }
  
  /**
   * returns a string value from the provided key.
   * also translates minecraft color codes prefixed with the '&' ampersand symbol.
   *
   * @param key the yaml key.
   * @return the value associated with the key (with color coding applied).
   */
  public
  @Nullable
  String get( @NotNull String key )
  {
    if ( !mapping.containsKey( key ) )
    {
      if ( !file_config.isSet( key ) )
      {
        return null;
      }
      
      mapping.put( key, file_config.getString( key ) );
    }
    
    return ChatColor.translateAlternateColorCodes( '&', mapping.get( key ) );
  }
}