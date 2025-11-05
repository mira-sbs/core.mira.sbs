package sbs.mira.core.model;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import sbs.mira.core.MiraModel;
import sbs.mira.core.MiraPlugin;
import sbs.mira.core.MiraPulse;
import sbs.mira.core.event.match.MiraMatchEndEvent;
import sbs.mira.core.event.match.MiraMatchPlayerLeaveTeamEvent;
import sbs.mira.core.event.match.MiraMatchPlayerRespawnEvent;

import java.util.*;

/**
 * created on 2017-04-20.
 *
 * @author jj stephen
 * @version 1.0.1
 * @since 1.0.0
 */
public
class MiraRespawnModel
  extends MiraModel<MiraPulse<?, ?>>
{
  private final @NotNull Map<UUID, Integer> respawn_timers;
  
  public
  MiraRespawnModel( @NotNull MiraPulse<?, ?> pulse )
  {
    super( pulse );
    
    this.respawn_timers = new HashMap<>( );
    new RespawnModelEventListener( this.plugin( ) );
    
    Bukkit.getScheduler( ).runTaskTimer(
      this.plugin( ), ( )->
      {
        List<UUID> respawned_player_uuids = new ArrayList<>( );
        
        for ( UUID dead_player_uuid : this.respawn_timers.keySet( ) )
        {
          int seconds_remaining = this.respawn_timers.get( dead_player_uuid );
          
          if ( seconds_remaining == 0 )
          {
            respawned_player_uuids.add( dead_player_uuid );
            
            MiraPlayerModel<?> player = this.pulse( ).model( ).player( dead_player_uuid );
            
            this.call_event( new MiraMatchPlayerRespawnEvent( player ) );
          }
        }
        
        synchronized ( this.respawn_timers )
        {
          respawned_player_uuids.forEach( this.respawn_timers::remove );
        }
      }, 0L, 20L );
  }
  
  /**
   * inducts a dead player into the respawning system.
   * clears their inventory; put them into spectator mode; gives them nausea.
   * they must wait for the next respawn wave - if there is no permanent death.
   *
   * @param mira_player the player who died.
   * @param can_respawn true - if respawns are allowed and the respawn logic should fire.
   */
  public
  void handle_death( MiraPlayerModel<?> mira_player, boolean can_respawn )
  {
    if ( mira_player.crafter( ).getGameMode( ) != GameMode.SURVIVAL )
    {
      return;
    }
    
    this.pulse( ).model( ).items( ).clear( mira_player );
    
    Player player = mira_player.crafter( );
    player.setHealth( 20 );
    player.setGameMode( GameMode.SPECTATOR );
    player.addPotionEffect( new PotionEffect( PotionEffectType.NAUSEA, 400, 1 ) );
    
    if ( can_respawn )
    {
      this.respawn_timers.put( mira_player.crafter( ).getUniqueId( ), 8 );
      
      mira_player.messages( "you died! respawning in 8 seconds..." );
    }
    else
    {
      mira_player.messages( "you died and were ejected from the match and your team..." );
    }
  }
  
  public
  void do_respawn( @NotNull UUID player_uuid )
  {
    if ( !this.respawn_timers.containsKey( player_uuid ) )
    {
      throw new IllegalArgumentException( "player '%s' is not waiting to respawn!".formatted(
        player_uuid ) );
    }
    
    this.call_event( new MiraMatchPlayerRespawnEvent( this.pulse( ).model( ).player( player_uuid ) ) );
  }
  
  private final
  class RespawnModelEventListener
    implements Listener
  {
    public
    RespawnModelEventListener( @NotNull MiraPlugin<?> plugin )
    {
      plugin.getServer( ).getPluginManager( ).registerEvents( this, plugin );
    }
    
    /**
     * player respawn timers should be cancelled when leaving an active match.
     */
    @EventHandler
    public
    void on_player_leave_team( MiraMatchPlayerLeaveTeamEvent event )
    {
      respawn_timers.remove( event.player( ).uuid( ) );
    }
    
    /**
     * player respawn timers should be cancelled when quitting the server.
     */
    @EventHandler
    public
    void on_player_quit( PlayerQuitEvent event )
    {
      respawn_timers.remove( event.getPlayer( ).getUniqueId( ) );
    }
    
    /**
     * player respawn timers should be completely cleared when the match ends.
     */
    @EventHandler
    public
    void on_match_end( MiraMatchEndEvent event )
    {
      respawn_timers.clear( );
    }
  }
}
