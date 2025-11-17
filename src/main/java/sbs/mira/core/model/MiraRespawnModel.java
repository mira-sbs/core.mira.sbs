package sbs.mira.core.model;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import sbs.mira.core.MiraModel;
import sbs.mira.core.MiraPulse;
import sbs.mira.core.event.match.MiraMatchPlayerRespawnEvent;
import sbs.mira.core.model.match.MiraMatch;

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
  @NotNull
  private final MiraMatch match;
  
  private final boolean can_respawn;
  @NotNull
  private final Map<UUID, Integer> respawn_timers;
  @NotNull
  private final BukkitTask respawn_timer_task;
  
  public
  MiraRespawnModel( @NotNull MiraPulse<?, ?> pulse, @NotNull MiraMatch match, boolean can_respawn )
  {
    super( pulse );
    
    this.match = match;
    this.can_respawn = can_respawn;
    this.respawn_timers = new HashMap<>( );
    
    final MiraRespawnModel self = this;
    
    this.event_handler( new MiraEventHandlerModel<PlayerDeathEvent, MiraPulse<?, ?>>( pulse )
    {
      @Override
      @EventHandler
      public
      void handle_event( PlayerDeathEvent event )
      {
        this.server( ).getScheduler( ).runTaskLater(
          this.pulse( ).plugin( ), ( )->
          {
            event.getEntity( ).spigot( ).respawn( );
          }, 8L );
      }
    } );
    
    this.event_handler( new MiraEventHandlerModel<PlayerRespawnEvent, MiraPulse<?, ?>>( pulse )
    {
      @Override
      @EventHandler
      public
      void handle_event( PlayerRespawnEvent event )
      {
        MiraPlayerModel<?> mira_player =
          self.pulse( ).model( ).player( event.getPlayer( ).getUniqueId( ) );
        Player player = mira_player.bukkit( );
        
        if ( !mira_player.has_team( ) )
        {
          player.teleport( match.map( ).spectator_spawn_position( ).location(
            player.getWorld( ),
            true ) );
          
          return;
        }
        
        player.setHealth( player.getHealthScale( ) );
        player.addPotionEffect( new PotionEffect( PotionEffectType.NAUSEA, 400, 1 ) );
        player.setGameMode( GameMode.SPECTATOR );
        
        if ( self.can_respawn )
        {
          // todo: add configuration option for respawn time...
          self.respawn_timers.put( mira_player.bukkit( ).getUniqueId( ), 8 );
          
          if ( mira_player.location( ).getBlockY( ) < -64 )
          {
            player.teleport( match.map( ).random_team_spawn_location( mira_player.team( ) ) );
          }
          
          event.setRespawnLocation( mira_player.location( ) );
          
          mira_player.messages( "respawning in 8 seconds..." );
        }
        else
        {
          event.setRespawnLocation( mira_player.location( ) );
          
          match.try_leave_team( mira_player );
        }
      }
    } );
    
    this.respawn_timer_task = this.server( ).getScheduler( ).runTaskTimer(
      this.pulse( ).plugin( ), ( )->
      {
        List<UUID> respawned_player_uuids = new ArrayList<>( );
        
        for ( UUID dead_player_uuid : this.respawn_timers.keySet( ) )
        {
          int seconds_remaining = this.respawn_timers.get( dead_player_uuid );
          
          if ( seconds_remaining < 1 )
          {
            respawned_player_uuids.add( dead_player_uuid );
            
            MiraPlayerModel<?> player = this.pulse( ).model( ).player( dead_player_uuid );
            
            this.call_event( new MiraMatchPlayerRespawnEvent( player ) );
          }
          else
          {
            this.respawn_timers.put( dead_player_uuid, seconds_remaining - 1 );
          }
        }
        
        respawned_player_uuids.forEach( this.respawn_timers::remove );
      }, 0L, 20L );
  }
  
  public
  void deactivate( )
  {
    this.respawn_timers.clear( );
    this.respawn_timer_task.cancel( );
    this.unregister_event_handlers( );
  }
}
