package sbs.mira.core.model.match;

import org.bukkit.Bukkit;
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
import sbs.mira.core.model.MiraConfigurationModel;
import sbs.mira.core.model.MiraEventHandlerModel;
import sbs.mira.core.model.MiraPlayerModel;
import sbs.mira.core.model.MiraScoreboardModel;
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

/**
 * miral representation of a match singleton - within a lobby.
 * matches start with a pre-game on the map - spectating only - followed by a
 * vote to choose the game mode from a pre-defined whitelist defined by the map.
 * once the game mode has been voted in, it is activated and takes control of
 * the lobby - until an objective is fulfilled.
 * there is a brief pre-game in between the voting and in-game match segments.
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
  
  @NotNull
  private final MiraMapModel<Pulse> map;
  private final boolean was_manually_set;
  private final long world_id;
  
  @NotNull
  private MiraMatchState state;
  private boolean active;
  private boolean concluded;
  @Nullable
  private final MiraGameModeModel<Pulse> game_mode;
  
  @Nullable
  private MiraScoreboardModel scoreboard_pre_game;
  @Nullable
  private MiraScoreboardModel scoreboard_post_game;
  
  public
  MiraMatchModel(
    @NotNull Pulse pulse,
    @NotNull MiraMapModel<Pulse> map,
    boolean was_manually_set,
    long previous_world_id )
  {
    super( pulse );
    
    MiraConfigurationModel<?> config = this.pulse( ).model( ).config( );
    
    this.vote_duration = Integer.parseInt( config.get( "settings.duration.vote" ) );
    this.pre_game_duration = Integer.parseInt( config.get( "settings.duration.pre_game" ) );
    this.post_game_duration = Integer.parseInt( config.get( "settings.duration.post_game" ) );
    
    this.map = map;
    this.was_manually_set = was_manually_set;
    this.world_id = MiraStringUtility.generate_random_world_id( previous_world_id );
    
    this.state = MiraMatchState.START;
    this.active = false;
    this.game_mode = null;
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
  public @NotNull
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
  public @NotNull
  MiraMatchState state( )
  {
    return this.state;
  }
  
  /*——————————————————————————————————————————————————————————————————————————*/
  
  @Override
  public @NotNull
  MiraMapModel<Pulse> map( )
  {
    return this.map;
  }
  
  /*——————————————————————————————————————————————————————————————————————————*/
  
  @Override
  public @NotNull
  MiraGameModeModel<Pulse> game_mode( )
  {
    assert this.game_mode != null;
    
    return this.game_mode;
  }
  
  @NotNull
  public
  String scoreboard_title( )
  {
    return
      String.format( "%s (%s)", this.map.label( ), this.game_mode( ).display_name( ) );
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
    
    MiraWorldUtility.remembers(
      this.pulse( ).model( ).maps_repository( ).getAbsolutePath( ),
      this.map.label( ),
      String.valueOf( world_id( ) ) );
    MiraWorldUtility.loads( String.valueOf( world_id ), true );
    
    for ( MiraPlayerModel<?> player : this.pulse( ).model( ).players( ) )
    {
      // TODO: fix update.
      //player.update( );
      player.bukkit( ).teleport( this.map.spectator_spawn_position( ).location(
        this.world( ),
        true ) );
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
    this.votes = new MiraMatchVoteModel<>( this.pulse( ), this.map.allowed_game_mode_types( ) );
    
    final MiraMatchModel<Pulse> self = this;
    
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
    
    MiraGameModeType winning_game_mode = this.votes().winning_game_mode();
    
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
    
    this.state = MiraMatchState.PRE_GAME;
    
    this.scoreboard_pre_game = new MiraScoreboardModel(
      Objects.requireNonNull( this.server( ).getScoreboardManager( ) ),
      "pre_game",
      scoreboard_title( ) );
    this.scoreboard_pre_game.initialise( 4 );
    
    this.pulse( ).model( ).players( ).forEach( this.scoreboard_pre_game::add_spectator );
    
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
        
        if ( Bukkit.getOnlinePlayers( ).isEmpty( ) )
        {
          return;
        }
        
        self.scoreboard_pre_game.set_row( 3, "  " );
        self.scoreboard_pre_game.set_row( 2, "     Starting in" );
        self.scoreboard_pre_game.set_row( 1, "     %d second(s)".formatted( seconds_remaining ) );
        self.scoreboard_pre_game.set_row( 0, " " );
        
        self.pulse( ).model( ).players( ).forEach( self.scoreboard_pre_game::show );
        
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
    this.scoreboard_pre_game = null;
    
    this.begin_game( );
  }
  
  private
  void begin_game( )
  {
    assert this.game_mode != null;
    
    this.assert_state( true, MiraMatchState.PRE_GAME, false );
    
    this.state = MiraMatchState.GAME;
    
    // randomly pick players out of a hat for team assignment until everyone has been evaluated.
    List<MiraPlayerModel<?>> players = new ArrayList<>( this.pulse( ).model( ).players( ) );
    
    while ( !players.isEmpty( ) )
    {
      MiraPlayerModel<?> player =
        players.get( this.pulse( ).model( ).rng.nextInt( players.size( ) ) );
      
      if ( player.joined( ) && !this.try_join_team( player, null ) )
      {
        return;
      }
      
      // the player did not join before the match started - or they failed to join a team.
      player.bukkit( ).setGameMode( GameMode.CREATIVE );
      // fixme: this.pulse( ).master( ).giveSpectatorKit( player );
      
      players.remove( player );
    }
    
    this.map.activate( );
    this.game_mode.activate( );
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
  
  protected
  void begin_post_game( )
  {
    this.assert_state( true, MiraMatchState.GAME, false );
    
    // fixme: needs to be at the pvp level.
    //this.pulse( ).model( ).respawn( ).clear( );
    
    String objective_display_name =
      String.format( "%s (%s)", this.map.label( ), this.game_mode( ).display_name( ) );
    
    this.scoreboard_post_game = new MiraScoreboardModel(
      Objects.requireNonNull( this.server( ).getScoreboardManager( ) ),
      "post_game",
      objective_display_name );
    this.scoreboard_post_game.initialise( 3 );
    this.scoreboard_post_game.set_row( 2, "  " );
    this.scoreboard_post_game.set_row( 1, " gg !!" );
    this.scoreboard_post_game.set_row( 0, " " );
    
    for ( MiraPlayerModel<?> mira_player : this.pulse( ).model( ).players( ) )
    {
      Player player = mira_player.bukkit( );
      
      // the match has just ended - force respawn anyone who may have died and caused a post game.
      if ( player.isDead( ) )
      {
        player.spigot( ).respawn( );
      }
      
      mira_player.leaves_team( );
      
      this.scoreboard_post_game.add_spectator( mira_player );
      this.scoreboard_post_game.show( mira_player );
      
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
    this.scoreboard_post_game = null;
    
    this.conclude( );
  }
  
  public
  void conclude( )
  {
    this.assert_state( true, MiraMatchState.POST_GAME, false );
    
    this.state = MiraMatchState.END;
  }
  
  /*—[match interactions]—————————————————————————————————————————————————————————————————————————*/
  
  private @NotNull
  MiraTeamModel smallest_team( )
  {
    MiraTeamModel result = null;
    int largest_team_size = -1;
    
    for ( MiraTeamModel team : this.map.teams( ) )
    {
      int team_size = team.bukkit( ).getSize( );
      
      if ( largest_team_size == -1 || team_size > largest_team_size )
      {
        result = team;
        largest_team_size = team_size;
      }
    }
    
    return Objects.requireNonNull( result );
  }
  
  @Override
  public
  boolean try_join_team(
    @NotNull MiraPlayerModel<?> mira_player,
    @Nullable MiraTeamModel preferred_team )
  {
    if ( !this.active )
    {
      throw new IllegalStateException( "player joins team during inactive game mode?" );
    }
    
    if ( !mira_player.joined( ) )
    {
      throw new IllegalStateException( "player joins team without being marked as joined?" );
    }
    
    if ( this.game_mode( ).permanent_death( ) )
    {
      mira_player.messages( "permanent death is enabled - you can no longer join!" );
      mira_player.joined( false );
      
      return false;
    }
    
    @NotNull MiraTeamModel given_team =
      Objects.requireNonNullElseGet( preferred_team, this::smallest_team );
    
    if ( given_team.full( ) )
    {
      if ( preferred_team == null )
      {
        mira_player.messages( "all teams are full, please try joining later." );
      }
      else
      {
        mira_player.messages( "your preferred team is full, please try joining later." );
      }
      
      mira_player.joined( false );
      
      return false;
    }
    
    MiraMatchPlayerJoinTeamEvent join_team_event =
      new MiraMatchPlayerJoinTeamEvent( mira_player, given_team );
    
    this.call_event( join_team_event );
    
    if ( join_team_event.isCancelled( ) )
    {
      return false;
    }
    
    this.log( mira_player.display_name( ) + " joins " + given_team.coloured_display_name( ) );
    
    mira_player.joins_team( given_team );
    
    MiraItemUtility.clear( mira_player );
    
    this.map.apply_inventory( mira_player );
    
    Player player = mira_player.bukkit( );
    player.teleport( this.map.random_team_spawn_location( mira_player.team( ) ) );
    player.setGameMode( GameMode.SURVIVAL );
    player.setFallDistance( 0F );
    
    player.sendMessage( "you have joined the %s.".formatted( given_team.display_name( ) ) );
    
    return true;
  }
  
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
    
    if ( !this.game_mode( ).permanent_death( ) )
    {
      mira_player.messages( "you have left the match and will not be able to re-join!" );
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
