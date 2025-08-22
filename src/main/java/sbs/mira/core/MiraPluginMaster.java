package sbs.mira.core;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_21_R5.entity.CraftPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sbs.mira.core.helper.*;

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
class MiraPluginMaster<Pulse extends MiraPulse<?, ?>, Player extends MiraPlayer<?>>
  extends MiraModule<Pulse>
{
  private final @NotNull Random rng;
  private final @NotNull TreeMap<UUID, Player> players;
  
  private @Nullable MiraFileHelper files;
  private @Nullable MiraConfiguration<Pulse> config;
  private @Nullable MiraItemHelper items;
  private @Nullable MiraStringHelper strings;
  private @Nullable MiraWorldHelper world;
  
  public
  MiraPluginMaster( @NotNull Pulse pulse )
  {
    super( pulse );
    
    this.rng = new Random( 0xfdffdeadL );
    this.players = new TreeMap<>( );
  }
  
  public
  void breathe( )
  {
    this.files = new MiraFileHelper( this.pulse() );
    this.config = new MiraConfiguration<>( this.pulse() );
    this.items = new MiraItemHelper( this.pulse() );
    this.strings = new MiraStringHelper( this.pulse() );
    this.world = new MiraWorldHelper( this.pulse() );
  }
  
  @NotNull
  public abstract
  MiraPlayer<?> declares( @NotNull CraftPlayer target );
  
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
  
  @Nullable
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
  MiraFileHelper files( )
  {
    return files;
  }
  
  @NotNull
  public
  MiraItemHelper items( )
  {
    return items;
  }
  
  @NotNull
  public
  MiraStringHelper strings( )
  {
    return strings;
  }
  
  @NotNull
  public
  MiraWorldHelper world( )
  {
    return world;
  }
  
  /**
   * retrieve and prefill a pre-configured message template.
   *
   * @param key          message key name.
   * @param replacements replaces "{0}", "{1}" and so on with the provided.
   * @throws IllegalArgumentException message key does not exist.
   */
  @NotNull
  public
  String message( String key, String... replacements ) throws IllegalArgumentException
  {
    int i = 0;
    String result = ChatColor.translateAlternateColorCodes( '&', /*config.getMessage( key )*/"??" );
    while ( result.contains( "{" + i + "}" ) )
    {
      result = result.replace( "{%d}".formatted( i ), replacements[ i ] );
      i++;
    }
    return result;
  }
  
  /**
   * Sends a TextComponent message to everyone online.
   *
   * @param comp Message to send.
   */
  public
  void broadcastSpigotMessage( TextComponent comp )
  {
    for ( MiraPlayer<?> online : players.values( ) )
    {
      online.crafter( ).spigot( ).sendMessage( comp );
    }
  }
}
