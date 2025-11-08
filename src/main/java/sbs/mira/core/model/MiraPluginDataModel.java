package sbs.mira.core.model;

import org.bukkit.craftbukkit.v1_21_R6.entity.CraftPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sbs.mira.core.MiraModel;
import sbs.mira.core.MiraPulse;

import java.io.File;
import java.util.*;

/**
 * represents the state of a bukkit server under the influence of mira.
 * created on 2017-03-20.
 *
 * @author jj.mira.sbs
 * @version 1.0.1
 * @see MiraPulse
 * @since 1.0.0
 */
public abstract
class MiraPluginDataModel<Pulse extends MiraPulse<?, ?>, Player extends MiraPlayerModel<?>>
  extends MiraModel<Pulse>
{
  @NotNull
  public final Random rng;
  @NotNull
  private final Map<UUID, Player> players;
  
  private @Nullable MiraConfigurationModel<Pulse> core_config;
  private @Nullable MiraConfigurationModel<Pulse> core_messages;
  private @Nullable String maps_repository_path;
  
  public
  MiraPluginDataModel( @NotNull Pulse pulse )
  {
    super( pulse );
    
    this.rng = new Random( 0xfdffdeadL );
    this.players = new HashMap<>( );
  }
  
  public
  void initialise( )
  {
    this.pulse( ).plugin( ).saveDefaultConfig( );
    this.pulse( ).plugin( ).reloadConfig( );
    
    this.core_config = new MiraConfigurationModel<>(
      this.pulse( ),
      this.pulse( ).plugin( ).getConfig( ) );
    this.core_messages = new MiraConfigurationModel<>( this.pulse( ), "core_messages.yml" );
    
    this.maps_repository_path = this.core_config.get( "mira.file_paths.maps_repository_path" );
  }
  
  @NotNull
  public abstract
  MiraPlayerModel<?> declares( @NotNull CraftPlayer target );
  
  public
  void destroys( @NotNull UUID victim )
  {
    players.remove( victim );
  }
  
  @NotNull
  public
  Player player( @NotNull UUID target )
  {
    return players.get( target );
  }
  
  @Nullable
  public
  Player player( @Nullable Player target )
  {
    return target == null ? null : player( target.uuid( ) );
  }
  
  @NotNull
  public
  List<Player> players( )
  {
    return new ArrayList<>( players.values( ) );
  }
  
  @NotNull
  public
  MiraConfigurationModel<Pulse> config( )
  {
    assert this.core_config != null;
    
    return core_config;
  }
  
  @NotNull
  public
  String find_message( @NotNull String key )
  {
    assert core_messages != null;
    
    String result = core_messages.get( key );
    
    if ( result == null )
    {
      throw new NullPointerException( "could not find message key '%s'.".formatted( key ) );
    }
    
    return result;
  }
  
  /**
   * retrieve and prefill a pre-configured message template.
   *
   * @param key          message key name.
   * @param replacements replaces "{0}", "{1}" and so on with the provided.
   */
  @NotNull
  public
  String message( String key, String... replacements )
  throws ArrayIndexOutOfBoundsException, NullPointerException
  {
    String result = find_message( key );
    
    int placeholder_index = 0;
    
    while ( result.contains( "{%d}".formatted( placeholder_index ) ) )
    {
      result =
        result.replace( "{%d}".formatted( placeholder_index ), replacements[ placeholder_index ] );
      
      placeholder_index++;
    }
    
    return result;
  }
  
  @NotNull
  public
  File maps_repository( )
  {
    assert this.maps_repository_path != null;
    
    return new File( this.maps_repository_path );
  }
}
