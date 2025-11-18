package sbs.mira.core.model.match;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sbs.mira.core.MiraModel;
import sbs.mira.core.MiraPulse;
import sbs.mira.core.event.match.MiraMatchPlayerDeathEvent;
import sbs.mira.core.event.match.MiraMatchPlayerJoinTeamEvent;
import sbs.mira.core.event.match.MiraMatchPlayerLeaveTeamEvent;
import sbs.mira.core.event.match.MiraMatchPlayerRespawnEvent;
import sbs.mira.core.model.MiraEventHandlerModel;
import sbs.mira.core.model.MiraPlayerModel;
import sbs.mira.core.model.MiraRespawnModel;
import sbs.mira.core.model.MiraScoreboardModel;
import sbs.mira.core.model.map.MiraTeamModel;
import sbs.mira.core.model.utility.Position;
import sbs.mira.core.utility.MiraStringUtility;

import java.util.*;

/**
 * miral representation of a game mode.
 * forms part of the coaxis between maps, game modes and players.
 * specificially purposed as the engine of the "in-game" segment of the miral
 * match lifecycle.
 * implementations of the game mode will behave differently in the presence of
 * different objectives - depending on what map it has been paired with.
 * once the match duration limit has been reached - or an objective that ends
 * the game has been fulfilled - the match is deactivated and the management of
 * the miral match lifecycle is handed back off to the match for the post-game.
 * created on 2017-03-20.
 *
 * @author jj stephen
 * @version 1.0.1
 * @since 1.0.0
 */
public abstract
class MiraGameModeModel<Pulse extends MiraPulse<?, ?>>
  extends MiraModel<Pulse>
{
  /*—[game mode attributes]———————————————————————————————————————————————————*/
  
  @Nullable
  private String label;
  @Nullable
  private String display_name;
  @Nullable
  private String grammar;
  
  @Nullable
  private String description_offense;
  @Nullable
  private String description_defense;
  
  @NotNull
  private final Map<String, List<Position>> team_spawn_coordinates;
  
  /*—[match runtime attributes]———————————————————————————————————————————————*/
  
  @NotNull
  protected final MiraMatch match;
  @Nullable
  private BukkitTask game_task_timer;
  private int seconds_elapsed;
  
  private final @NotNull List<String> event_log;
  
  @NotNull
  private final Map<String, Integer> team_points;
  @NotNull
  private final Map<UUID, Integer> player_killstreaks;
  @NotNull
  private final Map<UUID, Integer> player_kills;
  @NotNull
  private final Map<UUID, Integer> player_deaths;
  private final int environmental_deaths;
  
  @Nullable
  protected MiraScoreboardModel scoreboard;
  
  @NotNull
  private final MiraRespawnModel respawn;
  
  protected boolean active;
  protected boolean permanent_death;
  
  @Nullable
  protected String winner;
  
  /*——————————————————————————————————————————————————————————————————————————*/
  
  public
  MiraGameModeModel( @NotNull Pulse pulse, @NotNull MiraMatch match )
  {
    super( pulse );
    
    this.team_spawn_coordinates = new HashMap<>( );
    
    this.match = match;
    this.game_task_timer = null;
    this.seconds_elapsed = 0;
    this.event_log = new ArrayList<>( );
    this.team_points = new HashMap<>( );
    this.player_killstreaks = new HashMap<>( );
    this.player_kills = new HashMap<>( );
    this.player_deaths = new HashMap<>( );
    this.environmental_deaths = 0;
    this.respawn = new MiraRespawnModel( this.pulse( ), match, !this.permanent_death );
  }
  
  /*—[implementation definitions]—————————————————————————————————————————————————————————————————*/
  
  /**
   * implementations should update the state of the scoreboard when some sort
   * of objective / indicator / value has also changed state - within this
   * procedure (important for ux).
   */
  protected abstract
  void update_scoreboard( );
  
  /**
   * implementations can use this procedure to receive a callback every 20 ticks
   * or 1 second - to perform various frequently performed / timer-based tasks.
   */
  protected abstract
  void task_timer_tick( );
  
  /*—[match lifecycle]————————————————————————————————————————————————————————————————————————————*/
  
  /**
   * starts the game mode. occurs after the pre-game completes.
   */
  public
  void activate( )
  {
    if ( this.active )
    {
      throw new IllegalStateException( "game mode is already active!" );
    }
    
    this.scoreboard =
      new MiraScoreboardModel( this.server( ).getScoreboardManager( ), this.label( ) );
    
    this.active = true;
    
    for ( MiraTeamModel mira_team : this.match.map( ).teams( ) )
    {
      this.scoreboard.register( mira_team );
      this.team_points.put( mira_team.label( ), 0 );
    }
    
    for ( MiraPlayerModel<?> player : this.pulse( ).model( ).players( ) )
    {
      this.scoreboard.add_spectator( player );
    }
    
    final MiraGameModeModel<Pulse> self = this;
    
    this.event_handler( new MiraEventHandlerModel<MiraMatchPlayerJoinTeamEvent, MiraPulse<?, ?>>(
      this.pulse( ) )
    {
      @EventHandler (priority = EventPriority.LOWEST)
      public
      void handle_event( MiraMatchPlayerJoinTeamEvent event )
      {
        if ( event.isCancelled( ) )
        {
          return;
        }
        
        MiraPlayerModel<?> mira_player = event.player( );
        
        // killstreak always resets upon rejoining.
        self.player_killstreaks.put( mira_player.uuid( ), 0 );
        
        self.player_kills.putIfAbsent( mira_player.uuid( ), 0 );
        self.player_deaths.putIfAbsent( mira_player.uuid( ), 0 );
        
        self.scoreboard.remove_spectator( mira_player );
      }
    } );
    
    this.event_handler( new MiraEventHandlerModel<MiraMatchPlayerLeaveTeamEvent, MiraPulse<?, ?>>(
      this.pulse( ) )
    {
      @EventHandler (priority = EventPriority.LOWEST)
      public
      void handle_event( MiraMatchPlayerLeaveTeamEvent event )
      {
        self.player_killstreaks.put( event.player( ).uuid( ), 0 );
        
        self.scoreboard.add_spectator( event.player( ) );
      }
    } );
    
    this.event_handler( new MiraEventHandlerModel<MiraMatchPlayerDeathEvent, MiraPulse<?, ?>>( this.pulse( ) )
    {
      @EventHandler (priority = EventPriority.LOWEST)
      public
      void handle_event( MiraMatchPlayerDeathEvent event )
      {
        MiraPlayerModel<?> mira_killed = event.killed( );
        
        self.player_deaths.put(
          mira_killed.uuid( ),
          self.player_deaths.get( mira_killed.uuid( ) ) + 1 );
        self.player_killstreaks.put( mira_killed.uuid( ), 0 );
        
        if ( event.has_killer( ) )
        {
          MiraPlayerModel<?> mira_killer = event.killer( );
          
          self.player_kills.put(
            mira_killer.uuid( ),
            self.player_kills.get( mira_killer.uuid( ) ) + 1 );
          
          int player_killer_killstreak = self.player_killstreaks.get( mira_killer.uuid( ) );
          
          self.player_killstreaks.put( mira_killer.uuid( ), player_killer_killstreak + 1 );
          
          self.match.world( ).playSound(
            mira_killed.location( ),
            Sound.ENTITY_BLAZE_DEATH,
            1L,
            player_killer_killstreak );
        }
      }
    } );
    
    this.event_handler( new MiraEventHandlerModel<MiraMatchPlayerRespawnEvent, MiraPulse<?, ?>>(
      this.pulse( ) )
    {
      @Override
      @EventHandler
      public
      void handle_event( MiraMatchPlayerRespawnEvent event )
      {
        MiraPlayerModel<?> mira_player = event.player( );
        
        Player player = event.player( ).bukkit( );
        player.teleport( self.match.map( ).random_team_spawn_location( mira_player.team( ) ) );
        player.setGameMode( GameMode.SURVIVAL );
        
        self.match.map( ).apply_inventory( mira_player );
      }
    } );
    
    this.game_task_timer = Bukkit.getScheduler( ).runTaskTimer(
      this.pulse( ).plugin( ), ( )->
      {
        if ( this.game_task_timer == null )
        {
          throw new IllegalStateException( "game task timer has been cleared?" );
        }
        
        if ( this.match.state( ) != MiraMatchState.GAME )
        {
          throw new IllegalStateException( "match is no longer in game?" );
        }
        
        int seconds_remaining = this.match.map( ).match_duration( ) - this.seconds_elapsed;
        
        this.scoreboard.display_name( self.pulse( ).model( ).message(
          "match.scoreboard.game.title",
          this.match.map( ).display_name( ),
          MiraStringUtility.time_ss_to_mm_ss( seconds_remaining ) ) );
        
        if ( seconds_remaining > 0 )
        {
          if ( seconds_remaining % 60 == 0 )
          {
            this.server( ).broadcastMessage( this.pulse( ).model( ).message(
              "match.time.minutes",
              String.valueOf( seconds_remaining / 60 ) ) );
          }
          else if ( seconds_remaining == 30 || seconds_remaining <= 5 )
          {
            this.server( ).broadcastMessage( this.pulse( ).model( ).message(
              "match.time.seconds",
              String.valueOf( seconds_remaining ) ) );
          }
        }
        
        if ( seconds_elapsed( ) >= this.match.map( ).match_duration( ) )
        {
          // the match *always* ends once the match duration has been reached.
          self.match.conclude_game( );
          return;
        }
        
        self.pulse( ).model( ).players( ).forEach( self.scoreboard::show );
        
        this.seconds_elapsed++;
        
      }, 20L, 20L );
  }
  
  /**
   * deactivation of the gamemode involves cancelling of the global task and removing hanging references.
   */
  public
  void deactivate( )
  {
    if ( this.game_task_timer == null )
    {
      throw new IllegalStateException( "game task timer does not exist - cannot deactivate!" );
    }
    
    this.game_task_timer.cancel( );
    this.game_task_timer = null;
    
    this.respawn.deactivate( );
    this.unregister_event_handlers( );
    
    //publishes_statistics( );
  }
  
  
  public
  void determine_winner( )
  {
    List<MiraTeamModel> winners = new ArrayList<>( );
    int highest_points = 0;
    
    for ( MiraTeamModel team : this.match.map( ).teams( ) )
    {
      int count = this.team_points( team.label( ) );
      
      if ( count == highest_points )
      {
        winners.add( team );
      }
      else if ( count > highest_points )
      {
        winners.clear( );
        winners.add( team );
        
        highest_points = count;
      }
    }
    
    this.broadcast_winner( winners, "points", highest_points );
    
    // todo: "they captured [n] objectives:"
    // todo: "monument A (100%)"
    // todo: "monument B (75% - highest progress)"
  }
  
  /*—[getters / setters]——————————————————————————————————————————————————————————————————————————*/
  
  /**
   * simple verbal / programmatic label of this game mode, serving as an identifier.
   *
   * @param label the unique label for this map.
   */
  @NotNull
  public
  String label( )
  {
    return this.label;
  }
  
  /**
   * allows implementations to set the label of this game mode.
   *
   * @param label the new game mode label.
   */
  protected
  void label( @NotNull String label )
  {
    this.label = label;
  }
  
  /**
   * full unabbreviated / unformatted / grammatical name of this game mode.
   * it will (of course) be displayed in various places.
   *
   * @return the display name for this game mode.
   */
  @NotNull
  public
  String display_name( )
  {
    assert this.display_name != null;
    
    return this.display_name;
  }
  
  /**
   * allows implementations to set the display name of this game mode.
   *
   * @param display_name the new map display name.
   */
  protected
  void display_name( @NotNull String display_name )
  {
    this.display_name = display_name;
  }
  
  /**
   * the `grammar` value refers to the correct preceding grammar before verbally
   * objectifying the `label`, for example:
   * i. "a TDM", "a CTF", "a DDM"...
   * ii. "an LMS", "an LTS"...
   *
   * @return correct preceding grammar.
   */
  @NotNull
  public
  String grammar( )
  {
    return this.grammar;
  }
  
  /**
   * allows implementations to set the correct preceding grammar of this game mode.
   *
   * @param display_name the new preceding grammar.
   */
  protected
  void grammar( @NotNull String grammar )
  {
    this.grammar = grammar;
  }
  
  /**
   *
   * @return
   */
  @NotNull
  public
  String description_offense( )
  {
    return this.description_offense;
  }
  
  protected
  void description_offense( @NotNull String description_offense )
  {
    this.description_offense = description_offense;
  }
  
  protected
  void description_defense( @NotNull String description_defense )
  {
    this.description_defense = description_defense;
  }
  
  /**
   * @return the amount of seconds elapsed since the game mode started.
   */
  public
  int seconds_elapsed( )
  {
    return seconds_elapsed;
  }
  
  /**
   * permanent death refers to the inability to respawn once a player has died.
   * players cannot join or rejoin teams after the match starts.
   *
   * @return true - if players are subject to permanent death.
   */
  public
  boolean permanent_death( )
  {
    return permanent_death;
  }
  
  /*—[log]————————————————————————————————————————————————————————————————————————————————————————*/
  
  /**
   * Logs an event. This can be anything.
   * A kill, a death, etc.
   * A flag capture, a flag drop, etc.
   * Log it all!
   *
   * @param content What to log.
   */
  public
  void log( @NotNull String content )
  {
    event_log.add( ChatColor.GRAY +
                   MiraStringUtility.time_ss_to_mm_ss( seconds_elapsed ) +
                   ChatColor.WHITE +
                   " " +
                   content );
  }
  
  /*—[stats]——————————————————————————————————————————————————————————————————————————————————————*/
  
  public
  int team_points( @NotNull String team_label )
  {
    return this.team_points.get( team_label );
  }
  
  public
  int award_team_points( @NotNull String team_label, int point_count )
  {
    return this.team_points.put(
      team_label,
      this.team_points.getOrDefault( team_label, 0 ) + point_count );
  }
  
  public
  int player_killstreak( @NotNull UUID uuid )
  {
    return this.player_killstreaks.get( uuid );
  }
  
  public
  int player_kills( @NotNull UUID uuid )
  {
    return this.player_kills.get( uuid );
  }
  
  public
  int player_deaths( @NotNull UUID uuid )
  {
    return this.player_deaths.get( uuid );
  }
  
  /**
   * Broadcasts a winner after calculation.
   *
   * @param winners              The list of winners.
   * @param objective_quantifier The objective. i.e. CTF = "captures"
   * @param winning_score        Used if there is a single winner.
   */
  public
  void broadcast_winner(
    @NotNull List<MiraTeamModel> winners,
    @NotNull String objective_quantifier,
    int winning_score )
  {
    String winners_message;
    
    if ( winners.size( ) > 1 )
    {
      this.server( ).broadcastMessage( this.pulse( ).model( ).message(
        "match.result.tie.conclusion",
        String.valueOf( winners.size( ) ) ) );
      
      winners_message = this.pulse( ).model( ).message(
        "match.result.tie.winners",
        MiraStringUtility.verbal_list( winners ),
        String.valueOf( winning_score ) );
    }
    else if ( winners.size( ) == 1 )
    {
      this.server( ).broadcastMessage( this.pulse( ).model( ).message( "match.result.conclusion" ) );
      
      winners_message = this.pulse( ).model( ).message(
        "match.result.winner",
        winners.get( 0 ).coloured_display_name( ),
        String.valueOf( winning_score ) );
    }
    else
    {
      throw new IllegalStateException( "could not determine winner?" );
    }
    
    this.server( ).broadcastMessage( winners_message );
  }
}
