package sbs.mira.core.model.match;

import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sbs.mira.core.MiraModel;
import sbs.mira.core.MiraPulse;
import sbs.mira.core.event.match.MiraMatchPlayerDeathEvent;
import sbs.mira.core.event.match.MiraMatchPlayerJoinTeamEvent;
import sbs.mira.core.event.match.MiraMatchPlayerLeaveTeamEvent;
import sbs.mira.core.model.*;
import sbs.mira.core.model.map.MiraMapModel;
import sbs.mira.core.model.map.MiraTeamModel;
import sbs.mira.core.utility.MiraEntityUtility;
import sbs.mira.core.utility.MiraItemUtility;
import sbs.mira.core.utility.MiraStringUtility;
import sbs.mira.core.utility.MiraWorldUtility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * miral representation of a match singleton - within a lobby.
 * matches start with a pre-game on the map - spectating only - followed by a
 * vote to choose the game mode from a pre-defined whitelist defined by the map.
 * once the game mode has been voted in, it is activated and takes control of
 * the lobby - until an objective is fulfilled.
 * there is a brief pre-game in between the voting and in-game match segments.
 * matches end naturally (per above) and sometimes artificially - which is then
 * followed by a post-game. winners are declared - statistics are calculated and
 * saved - then finally, the world is destroyed and the match lifecycle is
 * complete.
 * additional matches must be spawned by the lobby - currently using a map
 * rotation.
 * created on 2017-04-20.
 *
 * @author jj stephen
 * @version 1.0.1
 * @since 1.0.0
 */
public
class MiraMatchModel<Pulse extends MiraPulse<?, ?>>
  extends MiraModel<Pulse>
  implements MiraMatch
{
  private final int vote_duration;
  private final int pre_game_duration;
  private final int post_game_duration;
  
  @Nullable
  private MiraMatchVoteModel<Pulse> votes;
  
  @Nullable
  private BukkitTask vote_task_timer;
  @Nullable
  private BukkitTask pre_game_task_timer;
  @Nullable
  private BukkitTask post_game_task_timer;
  
  @Nullable
  private MiraMapModel<Pulse> map;
  private final boolean was_manually_set;
  private final long world_id;
  
  @NotNull
  private MiraMatchState state;
  private boolean active;
  private boolean concluded;
  @Nullable
  private MiraGameModeModel<Pulse> game_mode;
  
  @NotNull
  private final MiraScoreboardModel scoreboard;
  
  @Nullable
  private MiraRespawnModel respawn;
  
  public
  MiraMatchModel(
    @NotNull Pulse pulse,
    boolean was_manually_set,
    long previous_world_id )
  {
    super( pulse );
    
    MiraConfigurationModel<?> config = this.pulse( ).model( ).config( );
    
    this.vote_duration = Integer.parseInt( config.get( "settings.duration.vote" ) );
    this.pre_game_duration = Integer.parseInt( config.get( "settings.duration.pre_game" ) );
    this.post_game_duration = Integer.parseInt( config.get( "settings.duration.post_game" ) );
    
    this.map = null;
    this.was_manually_set = was_manually_set;
    this.world_id = MiraStringUtility.generate_random_world_id( previous_world_id );
    
    this.state = MiraMatchState.START;
    this.active = false;
    this.game_mode = null;
    
    this.scoreboard = new MiraScoreboardModel(
      Objects.requireNonNull( this.server( ).getScoreboardManager( ) ),
      "mira_match" );
  }
  
  /*—[getters/setters]————————————————————————————————————————————————————————————————————————————*/
  
  @NotNull
  public
  MiraMatchVoteModel<Pulse> votes( )
  {
    if ( this.votes == null )
    {
      throw new IllegalStateException( "voting model has not been defined?" );
    }
    
    return this.votes;
  }
  
  /**
   * @return the temporary 5 digit identifier for the map world directory of this match.
   */
  private
  long world_id( )
  {
    return this.world_id;
  }
  
  /*——————————————————————————————————————————————————————————————————————————*/
  
  @Override
  @NotNull
  public
  World world( )
  {
    return Objects.requireNonNull( this.server( ).getWorld( String.valueOf( this.world_id ) ) );
  }
  
  /*——————————————————————————————————————————————————————————————————————————*/
  
  /**
   * @return true - if this map was manually set / out of rotation.
   */
  public
  boolean was_manually_set( )
  {
    return this.was_manually_set;
  }
  
  /*——————————————————————————————————————————————————————————————————————————*/
  
  @Override
  @NotNull
  public
  MiraMatchState state( )
  {
    return this.state;
  }
  
  /*——————————————————————————————————————————————————————————————————————————*/
  
  @Override
  @NotNull
  public
  MiraMapModel<Pulse> map( )
  {
    if ( this.map == null )
    {
      throw new IllegalStateException( "map has not been defined?" );
    }
    
    return this.map;
  }
  
  /*——————————————————————————————————————————————————————————————————————————*/
  
  @Override
  public @NotNull
  MiraGameModeModel<Pulse> game_mode( )
  {
    if ( this.game_mode == null )
    {
      throw new IllegalStateException( "game mode has not been defined?" );
    }
    
    return this.game_mode;
  }
  
  @NotNull
  public
  String scoreboard_title( )
  {
    return
      String.format( "%s (%s)", this.map( ).label( ), this.game_mode( ).display_name( ) );
  }
  
  @Override
  public
  int seconds_remaining( )
  {
    return this.map( ).match_duration( ) - this.game_mode( ).seconds_elapsed( );
  }
  
  /*—[match lifecycle steps]——————————————————————————————————————————————————————————————————————*/
  
  private
  void assert_state(
    boolean expected_active,
    @NotNull MiraMatchState expected_state,
    boolean expected_map_active )
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
    if ( this.map( ).active( ) != expected_map_active )
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
  void begin(
    @NotNull MiraMapModel<Pulse> map,
    @NotNull MiraGameModeRepository<Pulse> game_mode_repository )
  throws IOException
  {
    if ( this.map != null )
    {
      throw new IllegalStateException( "match has already begun?" );
    }
    
    this.map = map;
    
    this.assert_state( false, MiraMatchState.START, false );
    
    this.active = true;
    
    MiraWorldUtility.remembers(
      this.pulse( ).model( ).maps_repository( ).getAbsolutePath( ),
      this.map.label( ),
      String.valueOf( world_id( ) ) );
    MiraWorldUtility.loads( String.valueOf( world_id ), true );
    
    for ( MiraPlayerModel<?> player : this.pulse( ).model( ).players( ) )
    {
      player.update( );
      player.bukkit( ).teleport( this.map.spectator_spawn_position( ).location(
        this.world( ),
        true ) );
    }
    
    this.begin_vote( game_mode_repository );
  }
  
  /**
   * kicks off the vote lifecycle.
   * responsible for determining available game modes of the current map,
   * then orchestrating the vote using a timer.
   * the end of the vote timer follows.
   */
  private
  void begin_vote( @NotNull MiraGameModeRepository<Pulse> game_mode_repository )
  {
    this.assert_state( true, MiraMatchState.START, false );
    
    if ( this.vote_task_timer != null )
    {
      throw new IllegalStateException( "match vote has already begun!" );
    }
    
    final Set<MiraGameModeType> allowed_game_mode_types = this.map( ).allowed_game_mode_types( );
    
    this.state = MiraMatchState.VOTE;
    this.votes = new MiraMatchVoteModel<>( this.pulse( ), allowed_game_mode_types );
    
    final MiraMatchModel<Pulse> self = this;
    
    final int scoreboard_row_count = allowed_game_mode_types.size( ) + 3;
    
    this.scoreboard.reset( );
    this.scoreboard.initialise( scoreboard_row_count );
    
    this.vote_task_timer = new BukkitRunnable( )
    {
      int seconds_remaining = vote_duration;
      
      public
      void run( )
      {
        if ( self.state( ) != MiraMatchState.VOTE )
        {
          this.cancel( );
          
          return;
        }
        
        if ( this.seconds_remaining == 0 )
        {
          self.conclude_vote( game_mode_repository );
          
          return;
        }
        
        if ( self.server( ).getOnlinePlayers( ).isEmpty( ) )
        {
          return;
        }
        
        this.seconds_remaining--;
        
        self.scoreboard.display_name( self.pulse( ).model( ).message(
          "match.scoreboard.vote.title",
          self.map( ).display_name( ),
          MiraStringUtility.time_ss_to_mm_ss( seconds_remaining ) ) );
        
        int row_index = scoreboard_row_count - 1;
        
        self.scoreboard.set_row( row_index--, "  " );
        self.scoreboard.set_row(
          row_index--,
          self.pulse( ).model( ).message( "match.scoreboard.vote.now_voting" ) );
        
        for ( MiraGameModeType game_mode_type : allowed_game_mode_types )
        {
          self.scoreboard.set_row(
            row_index--,
            self.pulse( ).model( ).message(
              "match.scoreboard.vote.vote_count",
              String.valueOf( self.votes.count( game_mode_type ) ),
              game_mode_type.display_name( ),
              game_mode_type.label( ) ) );
        }
        
        self.scoreboard.set_row( row_index, " " );
        
        self.pulse( ).model( ).players( ).forEach( self.scoreboard::show );
      }
    }.runTaskTimer( this.pulse( ).plugin( ), 0L, 20L );
  }
  
  /**
   * concludes the vote lifecycle. this can be done naturally (timer expiring)
   * or manually by an admin (if needed).
   * the beginning of the pre-game follows.
   */
  private
  void conclude_vote( @NotNull MiraGameModeRepository<Pulse> game_mode_repository )
  {
    this.assert_state( true, MiraMatchState.VOTE, false );
    
    if ( this.vote_task_timer == null )
    {
      throw new IllegalStateException( "match vote is not active - cannot conclude!" );
    }
    
    // variable can still become null in between the check above and the code below!
    Objects.requireNonNull( this.vote_task_timer ).cancel( );
    this.vote_task_timer = null;
    
    MiraGameModeType winning_game_mode = this.votes( ).winning_game_mode( );
    
    this.game_mode =
      game_mode_repository.game_mode( this.pulse( ), this, winning_game_mode.label( ) );
    
    this.server( ).broadcastMessage( this.pulse( ).model( ).message(
      "match.vote.broadcast.result",
      this.game_mode.display_name( ) ) );
    
    this.begin_pre_game( );
  }
  
  /**
   * following conclusion of the vote, an (ideally) brief pre-game takes place.
   * this allows the objectives relevant to the selected game mode to activate.
   * players are also given a brief time period to observe the activation of
   * these objectives.
   * players are lastly given the opportunity to join the match early and be
   * pre-assigned to a team - this is necessary for permanent death game modes.
   */
  private
  void begin_pre_game( )
  {
    this.assert_state( true, MiraMatchState.VOTE, false );
    
    if ( this.pre_game_task_timer != null )
    {
      throw new IllegalStateException( "match pre-game has already commenced!" );
    }
    
    this.state = MiraMatchState.PRE_GAME;
    
    this.scoreboard.reset( );
    this.scoreboard.display_name( this.scoreboard_title( ) );
    this.scoreboard.initialise( 4 );
    
    final MiraMatchModel<Pulse> self = this;
    
    this.pre_game_task_timer = new BukkitRunnable( )
    {
      int seconds_remaining = pre_game_duration;
      
      public
      void run( )
      {
        if ( self.state( ) != MiraMatchState.PRE_GAME )
        {
          this.cancel( );
          
          return;
        }
        
        if ( this.seconds_remaining == 0 )
        {
          self.conclude_pre_game( );
          
          return;
        }
        
        if ( self.server( ).getOnlinePlayers( ).isEmpty( ) )
        {
          return;
        }
        
        this.seconds_remaining--;
        
        self.scoreboard.display_name( self.pulse( ).model( ).message(
          "match.scoreboard.game.title",
          self.map( ).display_name( ),
          MiraStringUtility.time_ss_to_mm_ss( seconds_remaining ) ) );
        
        self.scoreboard.set_row( 3, "  " );
        self.scoreboard.set_row(
          2,
          self.pulse( ).model( ).message( "match.scoreboard.game.pre_game.line1" ) );
        self.scoreboard.set_row(
          1,
          self.pulse( ).model( ).message( "match.scoreboard.game.pre_game.line2" ) );
        self.scoreboard.set_row( 0, " " );
        
        self.pulse( ).model( ).players( ).forEach( self.scoreboard::show );
      }
    }.runTaskTimer( this.pulse( ).plugin( ), 0L, 20L );
  }
  
  /**
   * concludes the pre-game after its (again, ideally) brief timer expires.
   * this allows the game itself to begin - following conclusion of the
   * pre-game.
   * this method is public to allow external pre-conclusions of the pre-game.
   */
  public
  void conclude_pre_game( )
  {
    this.assert_state( true, MiraMatchState.PRE_GAME, false );
    
    if ( this.pre_game_task_timer == null )
    {
      throw new IllegalStateException( "match pre-game is not active - cannot conclude!" );
    }
    
    this.pre_game_task_timer.cancel( );
    this.pre_game_task_timer = null;
    
    this.begin_game( );
  }
  
  private
  void begin_game( )
  {
    this.assert_state( true, MiraMatchState.PRE_GAME, false );
    
    this.state = MiraMatchState.GAME;
    
    this.map( ).activate( );
    this.game_mode( ).activate( );
    this.event_handler( new MiraMatchPlayerKilledHandler( this.pulse( ) ) );
    
    // randomly pick players out of a hat for team assignment until everyone has been evaluated.
    List<MiraPlayerModel<?>> players = new ArrayList<>( this.pulse( ).model( ).players( ) );
    
    while ( !players.isEmpty( ) )
    {
      MiraPlayerModel<?> player =
        players.get( this.pulse( ).model( ).rng.nextInt( players.size( ) ) );
      
      if ( player.joined( ) && this.try_join_team( player, null ) )
      {
        return;
      }
      
      // the player did not join before the match started - or they failed to join a team.
      player.bukkit( ).setGameMode( GameMode.CREATIVE );
      // fixme: this.pulse( ).master( ).giveSpectatorKit( player );
      
      players.remove( player );
    }
  }
  
  public
  void conclude_game( )
  {
    this.assert_state( true, MiraMatchState.GAME, true );
    
    this.game_mode().determine_winner();
    
    this.game_mode( ).deactivate( );
    this.map( ).deactivate( );
    this.unregister_event_handlers( );
    this.concluded = true;
    
    this.begin_post_game( );
  }
  
  protected
  void begin_post_game( )
  {
    this.assert_state( true, MiraMatchState.GAME, false );
    
    // fixme: needs to be at the pvp level.
    //this.pulse( ).model( ).respawn( ).clear( );
    
    for ( MiraPlayerModel<?> mira_player : this.pulse( ).model( ).players( ) )
    {
      Player player = mira_player.bukkit( );
      
      // the match has just ended - force respawn anyone who may have died and caused a post game.
      // fixme: no longer using spigot ig?!
      if ( player.isDead( ) )
      {
        player.spigot( ).respawn( );
      }
      
      mira_player.leaves_team( );
      
      this.scoreboard.add_spectator( mira_player );
      this.scoreboard.show( mira_player );
      
      player.playSound( player.getLocation( ), Sound.ENTITY_WITHER_DEATH, 1L, 1L );
      player.setGameMode( GameMode.CREATIVE );
      
      MiraItemUtility.clear( mira_player );
    }
    
    this.server( ).getScheduler( ).runTaskLater(
      this.pulse( ).plugin( ), ( )->
      {
        for ( MiraPlayerModel<?> player : this.pulse( ).model( ).players( ) )
        {
          // todo: give spectator kit - make it a common method?
          //mira( ).giveSpectatorKit( pl );
        }
      }, 1L );
    
    this.state = MiraMatchState.POST_GAME;
    
    final MiraMatchModel<Pulse> self = this;
    
    this.post_game_task_timer = new BukkitRunnable( )
    {
      int seconds_remaining = post_game_duration + 1;
      
      public
      void run( )
      {
        if ( this.seconds_remaining == 0 )
        {
          conclude_post_game( );
          
          return;
        }
        
        List<Player> players = self.world( ).getPlayers( );
        
        // spawn up to 8 fireworks - one per in-game player - every 3 seconds.
        // locations picked at random from all in-game players.
        // random spawn locations not checked for duplicates (funny).
        if ( this.seconds_remaining % 3 == 0 && !players.isEmpty( ) )
        {
          int firework_count = Math.min( players.size( ), 8 );
          
          do
          {
            MiraEntityUtility.spawn_firework( players.get( self.pulse( ).model( ).rng.nextInt(
              players.size( ) ) ).getLocation( ) );
            
            firework_count--;
          } while ( firework_count > 0 );
        }
        
        this.seconds_remaining--;
        
        self.scoreboard.display_name( self.pulse( ).model( ).message(
          "match.scoreboard.game.title",
          self.map( ).display_name( ),
          MiraStringUtility.time_ss_to_mm_ss( seconds_remaining ) ) );
        
        self.scoreboard.set_row( 3, "  " );
        self.scoreboard.set_row(
          2,
          self.pulse( ).model( ).message( "match.scoreboard.game.post_game.line1" ) );
        self.scoreboard.set_row(
          1,
          self.pulse( ).model( ).message( "match.scoreboard.game.post_game.line2" ) );
        self.scoreboard.set_row( 0, " " );
        
        self.pulse( ).model( ).players( ).forEach( self.scoreboard::show );
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
    
    this.post_game_task_timer.cancel( );
    this.post_game_task_timer = null;
    
    this.conclude( );
  }
  
  public
  void conclude( )
  {
    this.assert_state( true, MiraMatchState.POST_GAME, false );
    
    this.state = MiraMatchState.END;
  }
  
  /*—[match interactions]—————————————————————————————————————————————————————————————————————————*/
  
  /**
   * when a player joins the match without a team preference, they should be
   * assigned to the team with the least amount of players.
   * if there are multiple "smallest teams" - just take the first.
   *
   * @return the team with the least amount of members - during this active match.
   * @throws IllegalStateException no teams are defined.
   */
  @NotNull
  private
  MiraTeamModel smallest_team( )
  throws IllegalStateException
  {
    MiraTeamModel result = null;
    int smallest_team_size = Integer.MAX_VALUE;
    
    for ( MiraTeamModel team : this.map( ).teams( ) )
    {
      int team_size = team.bukkit( ).getSize( );
      
      if ( team_size < smallest_team_size )
      {
        result = team;
        smallest_team_size = team_size;
      }
    }
    
    if ( result == null || this.map( ).teams( ).isEmpty( ) )
    {
      throw new IllegalStateException( "no valid teams available to choose from?" );
    }
    
    return result;
  }
  
  /**
   * attempts to put the given player on a team within this active match.
   * the player is not put on a team where:
   * <ul>
   *   <li>permanent death is enabled and the match has already started.</li>
   *   <li>the preferred team is full.</li>
   *   <li>all available teams are full.</li>
   *   <li>an external event listener cancels the join event.</li>
   * </ul>
   *
   * @param mira_player    the player who is joining a team.
   * @param preferred_team their preferred team - if any.
   * @return true - if the player was able to successfully join a team.
   * @throws IllegalStateException if the game mode is inactive.
   * @throws IllegalStateException if the player is not marked as joined.
   */
  @Override
  public
  boolean try_join_team(
    @NotNull MiraPlayerModel<?> mira_player,
    @Nullable MiraTeamModel preferred_team )
  throws IllegalStateException
  {
    if ( !this.active )
    {
      throw new IllegalStateException( "player joins team during inactive game mode?" );
    }
    
    if ( !mira_player.joined( ) )
    {
      throw new IllegalStateException( "player joins team while not marked as joined?" );
    }
    
    if ( this.game_mode( ).permanent_death( ) )
    {
      mira_player.messages( this.pulse( ).model( ).message( "match.team.join.late" ) );
      mira_player.joined( false );
      
      return false;
    }
    
    @NotNull MiraTeamModel given_team =
      Objects.requireNonNullElseGet( preferred_team, this::smallest_team );
    
    if ( given_team.full( ) )
    {
      if ( preferred_team == null )
      {
        mira_player.messages( this.pulse( ).model( ).message( "match.team.join.full" ) );
      }
      else
      {
        mira_player.messages( this.pulse( ).model( ).message( "match.team.preference.full" ) );
      }
      
      mira_player.joined( false );
      
      return false;
    }
    
    MiraMatchPlayerJoinTeamEvent join_team_event =
      new MiraMatchPlayerJoinTeamEvent( mira_player, given_team );
    
    this.call_event( join_team_event );
    
    if ( join_team_event.isCancelled( ) )
    {
      mira_player.joined( false );
      
      return false;
    }
    
    this.log( "%s joins %s".formatted(
      mira_player.display_name( ),
      given_team.coloured_display_name( ) ) );
    
    mira_player.joins_team( given_team );
    
    MiraItemUtility.clear( mira_player );
    
    this.map( ).apply_inventory( mira_player );
    
    Player player = mira_player.bukkit( );
    player.teleport( this.map( ).random_team_spawn_location( mira_player.team( ) ) );
    player.setGameMode( GameMode.SURVIVAL );
    player.setFallDistance( 0F );
    
    player.sendMessage( this.pulse( ).model( ).message(
      "match.team.join.ok",
      given_team.coloured_display_name( ) ) );
    
    return true;
  }
  
  /**
   * attempts to remove the player from their team within this *active* match.
   * if permanent death is enabled, the player must be additionally notified
   * that they can no longer re-join this match.
   *
   * @param mira_player the player who is leaving their team.
   * @throws IllegalStateException    if the game mode is inactive.
   * @throws IllegalArgumentException if the player is not marked as joined.
   */
  @Override
  public
  void try_leave_team( @NotNull MiraPlayerModel<?> mira_player )
  throws IllegalStateException, IllegalArgumentException
  {
    if ( !this.active )
    {
      throw new IllegalStateException( "player leaves team during inactive game mode?" );
    }
    
    if ( !mira_player.joined( ) )
    {
      throw new IllegalArgumentException( "player leaves team without being marked as joined?" );
    }
    
    MiraTeamModel mira_team = mira_player.team( );
    
    this.pulse( ).plugin( ).getServer( ).getPluginManager( ).callEvent( new MiraMatchPlayerLeaveTeamEvent(
      mira_player,
      mira_team ) );
    
    mira_player.leaves_team( );
    
    Player player = mira_player.bukkit( );
    player.teleport( map( ).spectator_spawn_position( ).location( this.world( ), true ) );
    player.setGameMode( GameMode.CREATIVE );
    
    MiraItemUtility.clear( mira_player );
    // fixme: re-add spectator kit.
    //this.pulse().master().giveSpectatorKit( mira_player );
    
    mira_player.messages( this.pulse( ).model( ).message( "match.team.leave.ok" ) );
    
    if ( this.game_mode( ).permanent_death( ) )
    {
      mira_player.messages( this.pulse( ).model( ).message( "match.team.leave.no_rejoin" ) );
    }
  }
  
  private
  class MiraMatchPlayerKilledHandler
    extends MiraEventHandlerModel<PlayerDeathEvent, Pulse>
  {
    protected
    MiraMatchPlayerKilledHandler( @NotNull Pulse pulse )
    {
      super( pulse );
    }
    
    @Override
    public
    void handle_event( PlayerDeathEvent event )
    {
      Player player_killed = event.getEntity( );
      MiraPlayerModel<?> mira_killed =
        this.pulse( ).model( ).player( player_killed.getUniqueId( ) );
      
      @Nullable Player player_killer = player_killed.getKiller( );
      @Nullable MiraPlayerModel<?> mira_killer =
        player_killer == null ?
        null :
        this.pulse( ).model( ).player( player_killer.getUniqueId( ) );
      
      @Nullable String death_message = event.getDeathMessage( );
      
      if ( mira_killed.equals( mira_killer ) )
      {
        mira_killer = null;
      }
      
      if ( death_message == null )
      {
        if ( mira_killer == null )
        {
          death_message = "%s died".formatted( mira_killed.display_name( ) );
        }
        else
        {
          death_message = "%s died to the cruelty of %s".formatted(
            mira_killed.display_name( ),
            mira_killer.display_name( ) );
          
        }
      }
      
      event.setDeathMessage( death_message );
      
      this.log( death_message );
      this.server( ).getPluginManager( ).callEvent( new MiraMatchPlayerDeathEvent(
        mira_killed,
        mira_killer ) );
    }
  }
}
