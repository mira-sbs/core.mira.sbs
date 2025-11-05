package sbs.mira.core.model;

import org.bukkit.craftbukkit.v1_21_R6.entity.CraftPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sbs.mira.core.MiraModel;
import sbs.mira.core.MiraPulse;
import sbs.mira.core.model.data.MiraItemHelper;
import sbs.mira.core.model.data.MiraWorldDataModel;

import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.UUID;

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
class MiraPluginModel<Pulse extends MiraPulse<?, ?>, Player extends MiraPlayerModel<?>>
  extends MiraModel<Pulse>
{
  public final @NotNull Random rng;
  private final @NotNull TreeMap<UUID, Player> players;
  
  private @Nullable MiraConfigurationModel<Pulse> core_config;
  private @Nullable MiraConfigurationModel<Pulse> core_messages;
  private @Nullable MiraItemHelper items;
  private @Nullable MiraWorldDataModel world;
  
  public
  MiraPluginModel( @NotNull Pulse pulse )
  {
    super( pulse );
    
    this.rng = new Random( 0xfdffdeadL );
    this.players = new TreeMap<>( );
    
  }
  
  public
  void breathe( )
  {
    this.pulse( ).plugin( ).saveDefaultConfig( );
    this.pulse( ).plugin( ).reloadConfig( );
    
    this.core_config = new MiraConfigurationModel<>(
      this.pulse( ),
      this.pulse( ).plugin( ).getConfig( ) );
    this.core_messages = new MiraConfigurationModel<>( this.pulse( ), "core_messages.yml" );
    
    this.items = new MiraItemHelper( this.pulse( ) );
    this.world = new MiraWorldDataModel( this.pulse( ) );
    this.world.breathe( );
  }
  
  @NotNull
  public abstract
  MiraPlayerModel<?> declares( @NotNull CraftPlayer target );
  
  /**
   * When called, this should clear a player's inventory
   * and if applicable, give the player a spectator kit.
   *
   * @param wp The target player.
   */
  public abstract
  void spectating( @NotNull Player wp );
  
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
  Map<UUID, Player> players( )
  {
    return players;
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
  MiraItemHelper items( )
  {
    assert items != null;
    
    return items;
  }
  
  @NotNull
  public
  MiraWorldDataModel world( )
  {
    assert world != null;
    
    return world;
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
      result = result.replace(
        "{%d}".formatted( placeholder_index ),
        replacements[ placeholder_index ] );
      
      placeholder_index++;
    }
    
    return result;
  }
}
