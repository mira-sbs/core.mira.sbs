package sbs.mira.core.model.match;

import org.bukkit.*;
import org.bukkit.craftbukkit.v1_21_R6.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sbs.mira.core.MiraModel;
import sbs.mira.core.model.MiraConfigurationModel;
import sbs.mira.core.utility.MiraStringUtility;
import sbs.mira.pvp.MiraVersePlayer;
import sbs.mira.pvp.MiraVersePulse;
import sbs.mira.core.model.MiraLobbyModel;
import sbs.mira.core.model.map.MiraMapModelConcrete;

import java.io.IOException;
import java.util.*;

/**
 * models a single instance of a match within a lobby.
 * matches start with a pre-game on the map - spectating only - followed by a
 * vote to choose the game mode from a pre-defined list configured by the map.
 * once the game mode has been voted in, it is activated and takes control of
 * the lobby - until an objective is fulfilled.
 * matches end naturally (per above) and sometimes artificially - which is then
 * followed by a post-game. winners are declared - statistics are calculated and
 * saved - then finally, the world is destroyed and the match lifecycle is complete.
 * additional matches must be spawned by the lobby - currently using a map rotation.
 * created on 2017-04-20.
 *
 * @author jj stephen
 * @version 1.0.1
 * @since 1.0.0
 */
public
class MiraMatchModel
  extends MiraModel<MiraVersePulse>
{
  private final int vote_duration;
  private final int pre_game_duration;
  private final int post_game_duration;
  
  private final @NotNull Map<MiraGameModeType, List<UUID>> votes;
  private volatile @Nullable BukkitTask vote_task_timer;
  private volatile @Nullable BukkitTask pre_game_task_timer;
  private volatile @Nullable BukkitTask post_game_task_timer;
  
  private final @NotNull MiraMapModelConcrete map;
  private final boolean was_manually_set;
  private final long world_id;
  
  private @NotNull MiraMatchState state;
  private boolean active;
  private boolean concluded;
  private final @Nullable MiraGameModeModel game_mode;
  
  public
  MiraMatchModel(
    @NotNull MiraVersePulse pulse,
    @NotNull MiraMapModelConcrete map,
    boolean was_manually_set,
    long previous_world_id
                )
  {
    super( pulse );
    
    MiraConfigurationModel<?> config = this.pulse( ).model( ).config( );
    
    this.vote_duration = Integer.parseInt( config.get( "settings.duration.vote" ) );
    this.pre_game_duration = Integer.parseInt( config.get( "settings.duration.pre_game" ) );
    this.post_game_duration = Integer.parseInt( config.get( "settings.duration.post_game" ) );
    
    this.votes = new HashMap<>( );
    
    this.map = map;
    this.was_manually_set = was_manually_set;
    this.world_id = MiraStringUtility.generate_random_world_id( previous_world_id );
    
    this.state = MiraMatchState.START;
    this.active = false;
    this.game_mode = null;
  }
  
  /*—[getters/setters]————————————————————————————————————————————————————————*/
  
  /**
   * @return the temporary 5 digit identifier for the map world directory of this match.
   */
  private
  long world_id( )
  {
    return this.world_id;
  }
  
  /**
   * @return the world (fetched by id) that is currently hosting the map of this match.
   */
  public @NotNull
  World world( )
  {
    return Objects.requireNonNull( this.server( ).getWorld( String.valueOf( this.world_id ) ) );
  }
  
  /**
   * @return true - if this map was manually set / out of rotation.
   */
  public
  boolean was_manually_set( )
  {
    return this.was_manually_set;
  }
  
  public @NotNull
  MiraMatchState state( )
  {
    return this.state;
  }
  
  /**
   * @return the game mode that was voted in for this match - otherwise null if
   * this has not yet occurred.
   */
  public @NotNull
  MiraGameModeModel game_mode( )
  {
    assert this.game_mode != null;
    
    return this.game_mode;
  }
  
  /*—[match lifecycle steps]——————————————————————————————————————————————————*/
  
  private
  void assert_state(
    boolean expected_active,
    @NotNull MiraMatchState expected_state,
    boolean expected_map_active
                   )
  {
    if ( this.active != expected_active )
    {
      throw new IllegalStateException( "match active state mismatch! expected: %s".formatted(
        expected_active ) );
    }
    
    if ( this.state != expected_state )
    {
      throw new IllegalStateException( "lobby state mismatch! expected: %s".formatted(
        expected_state ) );
    }
    if ( this.map.active( ) != expected_map_active )
    {
      throw new IllegalStateException( "match map active state mismatch! expected: %s".formatted(
        expected_map_active ) );
    }
  }
  
  /**
   * event handler.
   * fires when the mira lobby has chosen the next map and a new match can begin.
   * the map world files are copied to the local server and then loaded.
   * all players are then teleported in - the vote should (ideally) begin now.
   *
   * @throws IOException file operation failed.
   */
  public
  void begin( )
  throws IOException
  {
    this.assert_state( false, MiraMatchState.START, false );
    
    this.active = true;
    
    this.pulse( ).model( ).world( ).remembers(
      this.map.label( ),
      String.valueOf( world_id( ) ) );
    this.pulse( ).model( ).world( ).loads( String.valueOf( world_id ), true );
    
    // fixme: re-add timelock flag.
    /*if ( this.map.attr( ).containsKey( "timeLock" ) )
    {
      World world = world( );
      world.setGameRule( GameRule.DO_DAYLIGHT_CYCLE, false );
      world.setFullTime( ( Long ) this.map.attr( ).get( "timeLockTime" ) );
    }*/
    
    for ( MiraVersePlayer player : this.pulse( ).model( ).players( ).values( ) )
    {
      // TODO: fix update.
      //player.update( );
      player.crafter( ).teleport( this.map.spectator_spawn_position( ) );
    }
    
    this.begin_vote( );
  }
  
  /**
   * kicks off the vote lifecycle.
   * responsible for determining available game modes of the current map,
   * then orchestrating the vote using a timer.
   * the end of the vote timer follows.
   */
  private
  void begin_vote( )
  {
    this.assert_state( true, MiraMatchState.START, false );
    
    if ( this.vote_task_timer != null )
    {
      throw new IllegalStateException( "match vote has already begun!" );
    }
    
    this.state = MiraMatchState.VOTE;
    
    // todo: determine if we should have at least *one* vote to break a no-vote tie.
    // todo: otherwise - break tie at random. :)
    
    for ( MiraGameModeType game_mode_type : this.map.game_modes( ) )
    {
      votes.put( game_mode_type, new ArrayList<>( ) );
    }
    
    final MiraMatchModel self = this;
    
    this.vote_task_timer = new BukkitRunnable( )
    {
      int seconds_remaining = vote_duration;
      
      public
      void run( )
      {
        if ( this.seconds_remaining == 0 )
        {
          self.conclude_vote( );
          
          return;
        }
        
        seconds_remaining--;
      }
    }.runTaskTimer( this.pulse( ).plugin( ), 0L, 20L );
  }
  
  /**
   * concludes the vote lifecycle. this can be done naturally (timer expiring)
   * or manually by an admin (if needed).
   * the beginning of the pre-game follows.
   */
  public
  void conclude_vote( )
  {
    this.assert_state( true, MiraMatchState.VOTE, false );
    
    if ( this.vote_task_timer == null )
    {
      throw new IllegalStateException( "match vote is not active - cannot conclude!" );
    }
    
    // variable can still become null in between the check above and the code below!
    Objects.requireNonNull( this.vote_task_timer ).cancel( );
    this.vote_task_timer = null;
    
    Integer largest_vote_count = null;
    MiraGameModeType winning_game_mode = null;
    
    for ( MiraGameModeType game_mode_type : this.votes.keySet( ) )
    {
      int vote_count = this.votes.get( game_mode_type ).size( );
      
      if ( largest_vote_count == null || vote_count > largest_vote_count )
      {
        largest_vote_count = vote_count;
        winning_game_mode = game_mode_type;
      }
    }
    
    // not meeting this assertion is an indication of poor map design. ^-^
    assert winning_game_mode != null;
    
    // todo: broadcast winning game mode + vote count!
    //Bukkit.broadcastMessage(mira().message( "votes.next", game_mode( ).getGrammar( ), game_mode( ).getName( ), getCurrent_map_label( ) ) );
    
    // todo: initialise the game mode!
    //this.game_mode = new Gamemode(...);
    
    this.begin_pre_game( );
  }
  
  private
  void begin_pre_game( )
  {
    this.assert_state( true, MiraMatchState.VOTE, false );
    
    if ( this.pre_game_task_timer != null )
    {
      throw new IllegalStateException( "match pre-game has already commenced!" );
    }
    
    final MiraLobbyModel lobby = this.pulse( ).model( ).lobby( );
    
    this.state = MiraMatchState.PRE_GAME;
    
    for ( MiraVersePlayer player : this.pulse( ).model( ).players( ).values( ) )
    {
      lobby.bukkit_team( ).addEntry( player.crafter( ).getName( ) );
    }
    
    final MiraMatchModel self = this;
    final Objective objective = lobby.global_scoreboard( ).registerNewObjective(
      "vote",
      Criteria.DUMMY,
      "vote" );
    
    String objective_display_name = String.format(
      "%s (%s)",
      this.map.label( ),
      this.game_mode.display_name( ) );
    
    if ( objective_display_name.length( ) > 32 )
    {
      objective_display_name = objective_display_name.substring( 0, 32 );
    }
    
    objective.setDisplayName( objective_display_name );
    objective.setDisplaySlot( DisplaySlot.SIDEBAR );
    
    this.pre_game_task_timer = new BukkitRunnable( )
    {
      int seconds_remaining = pre_game_duration;
      
      public
      void run( )
      {
        if ( this.seconds_remaining == 0 )
        {
          objective.setDisplaySlot( null );
          
          self.conclude_pre_game( );
          
          return;
        }
        
        if ( self.state( ) != MiraMatchState.PRE_GAME )
        {
          this.cancel( );
          
          return;
        }
        
        if ( Bukkit.getOnlinePlayers( ).isEmpty( ) )
        {
          return;
        }
        
        objective.getScore( "  " ).setScore( 3 );
        lobby.global_scoreboard( ).resetScores( "     " +
                                                ( seconds_remaining + 1 ) +
                                                " second(s)" );
        objective.getScore( "     Starting in" ).setScore( 2 );
        objective.getScore( "     " + seconds_remaining + " second(s)" ).setScore( 1 );
        objective.getScore( " " ).setScore( 0 );
        
        for ( Player online_player : Bukkit.getOnlinePlayers( ) )
        {
          online_player.setScoreboard( lobby.global_scoreboard( ) );
        }
        
        // todo: check if decrementing after displaying the time is the correct way to do it? shouldn't display 0? or should?
        this.seconds_remaining--;
      }
    }.runTaskTimer( this.pulse( ).plugin( ), 0L, 20L );
  }
  
  public
  void conclude_pre_game( )
  {
    this.assert_state( true, MiraMatchState.PRE_GAME, false );
    
    if ( this.pre_game_task_timer == null )
    {
      throw new IllegalStateException( "match pre-game is not active - cannot conclude!" );
    }
    
    // variable can still become null in between the check above and the code below!
    Objects.requireNonNull( this.pre_game_task_timer ).cancel( );
    this.pre_game_task_timer = null;
    
    this.begin_game( );
  }
  
  private
  void begin_game( )
  {
    assert this.game_mode != null;
    
    this.assert_state( true, MiraMatchState.PRE_GAME, false );
    
    this.state = MiraMatchState.GAME;
    
    this.map.activate( );
    this.game_mode.begin( this.map );
  }
  
  public
  void conclude_game( )
  {
    assert this.game_mode != null;
    
    this.assert_state( true, MiraMatchState.GAME, true );
    
    this.game_mode.deactivate( );
    this.map.deactivate( );
    this.concluded = true;
    
    this.begin_post_game( );
  }
  
  private
  void begin_post_game( )
  {
    this.assert_state( true, MiraMatchState.GAME, false );
    
    this.pulse( ).model( ).respawn( ).clear( );
    
    for ( MiraVersePlayer mira_player : this.pulse( ).model( ).players( ).values( ) )
    {
      Player bukkit_player = mira_player.crafter( );
      
      // the match has just ended - force respawn anyone who may have died and caused a post game.
      if ( bukkit_player.isDead( ) )
      {
        bukkit_player.spigot( ).respawn( );
      }
      
      mira_player.joins( null );
      mira_player.joined( false );
      
      this.pulse( ).model( ).lobby( ).bukkit_team( ).addEntry( mira_player.name( ) );
      
      bukkit_player.playSound( bukkit_player.getLocation( ), Sound.ENTITY_WITHER_DEATH, 1L, 1L );
      bukkit_player.setScoreboard( this.pulse( ).model( ).lobby( ).global_scoreboard( ) );
      bukkit_player.setGameMode( GameMode.CREATIVE );
      
      this.pulse( ).model( ).items( ).clear( mira_player );
    }
    
    this.server( ).getScheduler( ).runTaskLater(
      this.pulse( ).plugin( ), ( )->
      {
        for ( MiraVersePlayer player : this.pulse( ).model( ).players( ).values( ) )
        {
          // todo: give spectator kit - make it a common method?
          //mira( ).giveSpectatorKit( pl );
        }
      }, 1L );
    
    this.state = MiraMatchState.POST_GAME;
    
    final MiraMatchModel self = this;
    
    this.post_game_task_timer = new BukkitRunnable( )
    {
      int seconds_remaining = post_game_duration + 1;
      
      public
      void run( )
      {
        if ( seconds_remaining == 0 )
        {
          conclude_post_game( );
          
          return;
        }
        
        List<CraftPlayer> online = self.server( ).getOnlinePlayers( );
        
        // spawn up to 8 fireworks - random player every time ^-^ - every 3 seconds.
        if ( seconds_remaining % 3 == 0 && !online.isEmpty( ) )
        {
          int firework_count = Math.min( online.size( ), 8 );
          
          while ( firework_count > 0 )
          {
            Location firework_location = world( )
              .getPlayers( )
              .get( new Random( ).nextInt( world( )
                                             .getPlayers( )
                                             .size( ) ) )
              .getLocation( );
            
            pulse( ).model( ).entities( ).spawnFirework( firework_location );
            
            firework_count--;
          }
        }
        
        seconds_remaining--;
      }
    }.runTaskTimer( this.pulse( ).plugin( ), 0L, 20L );
  }
  
  public
  void conclude_post_game( )
  {
    this.assert_state( true, MiraMatchState.POST_GAME, false );
    
    if ( this.post_game_task_timer == null )
    {
      throw new IllegalStateException( "match post-game is not active - cannot conclude!" );
    }
    
    // variable can still become null in between the check above and the code below!
    Objects.requireNonNull( this.post_game_task_timer ).cancel( );
    this.post_game_task_timer = null;
    
    this.pulse( ).model( ).lobby( ).conclude_match( );
  }
  
  public
  void conclude( )
  {
    this.assert_state( true, MiraMatchState.POST_GAME, false );
    
    this.state = MiraMatchState.END;
  }
}
