package sbs.mira.core.model;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import sbs.mira.core.MiraModel;
import sbs.mira.core.MiraPulse;
import sbs.mira.core.event.match.MiraMatchEndEvent;
import sbs.mira.core.event.match.MiraMatchPlayerLeaveTeamEvent;
import sbs.mira.core.event.match.MiraMatchPlayerRespawnEvent;
import sbs.mira.core.utility.MiraItemUtility;

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
  private final boolean can_respawn;
  @NotNull
  private final Map<UUID, Integer> respawn_timers;
  @NotNull
  private final BukkitTask respawn_timer_task;
  
  public
  MiraRespawnModel( @NotNull MiraPulse<?, ?> pulse, boolean can_respawn )
  {
    super( pulse );
    
    this.can_respawn = can_respawn;
    this.respawn_timers = new HashMap<>( );
    
    final MiraRespawnModel self = this;
    
    this.event_handler( new MiraEventHandlerModel<PlayerDeathEvent, MiraPulse<?, ?>>(
      pulse )
    {
      @Override
      public
      void handle_event( PlayerDeathEvent event )
      {
        MiraPlayerModel<?> mira_player =
          self.pulse( ).model( ).player( event.getEntity( ).getUniqueId( ) );
        
        if ( mira_player.bukkit( ).getGameMode( ) != GameMode.SURVIVAL )
        {
          return;
        }
        
        MiraItemUtility.clear( mira_player );
        
        Player player = mira_player.bukkit( );
        player.setHealth( 20 );
        player.setGameMode( GameMode.SPECTATOR );
        player.addPotionEffect( new PotionEffect( PotionEffectType.NAUSEA, 400, 1 ) );
        
        if ( self.can_respawn )
        {
          // todo: add configuration option for respawn time...
          self.respawn_timers.put( mira_player.bukkit( ).getUniqueId( ), 8 );
          
          mira_player.messages( "you died! respawning in 8 seconds..." );
        }
        else
        {
          mira_player.messages( "you died and were ejected from the match and your team..." );
        }
      }
    } );
    
    this.event_handler( new MiraEventHandlerModel<MiraMatchPlayerLeaveTeamEvent, MiraPulse<?, ?>>(
      pulse )
    {
      @Override
      public
      void handle_event( MiraMatchPlayerLeaveTeamEvent event )
      {
        self.respawn_timers.remove( event.player( ).uuid( ) );
      }
    } );
    
    this.event_handler( new MiraEventHandlerModel<MiraMatchEndEvent, MiraPulse<?, ?>>( pulse )
    {
      @Override
      public
      void handle_event( MiraMatchEndEvent event )
      {
        self.respawn_timers.clear( );
      }
    } );
    
    this.respawn_timer_task = this.server( ).getScheduler( ).runTaskTimer(
      this.pulse( ).plugin( ), ( )->
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
        
        respawned_player_uuids.forEach( this.respawn_timers::remove );
      }, 0L, 20L );
  }
  
  public
  void deactivate( )
  {
    this.respawn_timer_task.cancel( );
    this.unregister_event_handlers( );
  }
}
