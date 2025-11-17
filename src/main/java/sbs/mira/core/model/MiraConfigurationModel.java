package sbs.mira.core.model;


import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sbs.mira.core.MiraModel;
import sbs.mira.core.MiraPulse;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * this class handles all procedures or functions
 * relating to the inbuilt bukkit configuration
 * system. values in config.yml will be handled,
 * stored, and accessed through here as needed.
 * created on 2017-04-25.
 *
 * @author jj stephen
 * @author jd rose
 * @version 1.0.1
 * @see org.bukkit.configuration.Configuration
 * @since 1.0.0
 */
public
class MiraConfigurationModel<Pulse extends MiraPulse<?, ?>>
  extends MiraModel<Pulse>
{
  @NotNull
  protected final FileConfiguration file_config;
  @NotNull
  protected final Map<String, String> cache;
  
  /**
   * instantiates with a `FileConfiguration` directly provided.
   *
   * @param pulse       mira.
   * @param file_config the file configuration - usually a bukkit plugin `config.yml` file.
   */
  public
  MiraConfigurationModel( @NotNull Pulse pulse, @NotNull FileConfiguration file_config )
  {
    super( pulse );
    
    this.file_config = file_config;
    this.cache = new HashMap<>( );
  }
  
  /**
   * instantiates with a `YamlConfiguration` embedded resource which is loaded by name.
   *
   * @param pulse                mira.
   * @param config_resource_name the name of the embedded resource file in the plugin `.jar`.
   */
  public
  MiraConfigurationModel( @NotNull Pulse pulse, @NotNull String config_resource_name )
  {
    super( pulse );
    
    this.cache = new TreeMap<>( );
    
    this.file_config = YamlConfiguration.loadConfiguration( new InputStreamReader(
      this.pulse( ).plugin( ).getResource( config_resource_name ),
      StandardCharsets.UTF_8
    ) );
    
    this.log( "(^-^) successfully loaded file configuration '%s'".formatted( config_resource_name ) );
  }
  
  /**
   * looks up a string value (in the configuration) from the provided key.
   * also translates minecraft color codes prefixed with the '&' ampersand symbol.
   *
   * @param key the configuration key.
   * @return the value associated with the key (with color coding applied).
   */
  @Nullable
  public
  String get( @NotNull String key )
  {
    if ( !cache.containsKey( key ) )
    {
      if ( !file_config.isSet( key ) )
      {
        return null;
      }
      
      cache.put( key, file_config.getString( key ) );
    }
    
    return ChatColor.translateAlternateColorCodes( '&', cache.get( key ) );
  }
  
  public
  int get_number( @NotNull String key )
  {
    String value = this.get( key );
    
    if ( value == null )
    {
      throw new IllegalArgumentException( "cannot find value for key '%s'".formatted( key ) );
    }
    
    return Integer.parseInt( value );
  }
}