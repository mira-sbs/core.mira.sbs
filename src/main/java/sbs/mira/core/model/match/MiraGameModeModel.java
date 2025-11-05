package sbs.mira.core.model.match;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sbs.mira.core.MiraModel;
import sbs.mira.core.MiraPulse;
import sbs.mira.core.model.MiraPlayerModel;
import sbs.mira.core.model.map.MiraMapModel;
import sbs.mira.core.model.map.MiraTeamModel;
import sbs.mira.core.model.map.Position;
import sbs.mira.core.utility.MiraStringUtility;

import java.util.*;

/**
 * any game mode that involves one or more players will share commonalities.
 * extend this class when implementing a gamemode to inherit necessary logic.
 * created on 2017-03-20.
 *
 * @author jj stephen
 * @version 1.0.1
 * @since 1.0.0
 */
public abstract
class MiraGameModeModel
  extends MiraModel<MiraPulse<?, ?>>
  implements Listener
{
  /*—[game mode attributes]———————————————————————————————————————————————————*/
  
  private @Nullable String label;
  private @Nullable String display_name;
  private @Nullable String grammar;
  
  private @Nullable String description_offense;
  private @Nullable String description_defense;
  
  private final @NotNull Map<String, List<Position>> team_spawn_coordinates;
  
  /*—[match runtime attributes]———————————————————————————————————————————————*/
  
  private @Nullable BukkitTask global_task;
  private int seconds_elapsed;
  
  private final @NotNull List<String> event_log;
  
  private final @NotNull Map<UUID, Integer> kills;
  private final @NotNull Map<UUID, Integer> deaths;
  private int environmental_deaths;
  
  private @Nullable Scoreboard scoreboard;
  private @Nullable Team team_spectators;
  
  protected boolean active;
  protected boolean permanent_death;
  
  protected @Nullable String winner;
  
  public
  MiraGameModeModel( MiraPulse<?, ?> pulse )
  {
    super( pulse );
    
    this.team_spawn_coordinates = new HashMap<>( );
    this.kills = new HashMap<>( );
    this.deaths = new HashMap<>( );
    this.environmental_deaths = 0;
    this.event_log = new ArrayList<>( );
  }
  
  public abstract
  void activate( @NotNull List<MiraTeamModel> teams );
  
  public abstract
  void refresh_scoreboard( );
  
  protected
  void label( @NotNull String label )
  {
    this.label = label;
  }
  
  protected @NotNull
  String label( )
  {
    return this.label;
  }
  
  protected
  void display_name( @NotNull String display_name )
  {
    this.display_name = display_name;
  }
  
  protected @NotNull
  String display_name( )
  {
    return this.display_name;
  }
  
  /**
   * @return correct preceding grammar to verbally objectify the `label`, i.e. "a TDM" - "an FFA".
   */
  protected
  void grammar( @NotNull String grammar )
  {
    this.grammar = grammar;
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
  
  /*—[kill/death handlers]————————————————————————————————————————————————————*/
  
  @EventHandler
  public
  void playerDeathHandle( PlayerDeathEvent event )
  {
    Player bukkit_killed = event.getEntity( );
    @Nullable Player bukkit_killer = bukkit_killed.getKiller( );
    
    @NotNull MiraPlayerModel mira_killed =
      Objects.requireNonNull( this.pulse( ).model( ).player( bukkit_killed.getUniqueId( ) ) );
    @Nullable MiraPlayerModel mira_killer =
      bukkit_killer == null ? null : this.pulse( ).model( ).player( bukkit_killer.getUniqueId( ) );
    
    @Nullable String death_message = event.getDeathMessage( );
    
    if ( mira_killed.equals( mira_killer ) )
    {
      mira_killer = null;
    }
    
    if ( mira_killer == null )
    {
      if ( death_message == null )
      {
        death_message = "%s died".formatted( mira_killed.name( ) );
      }
      
      event.setDeathMessage( death_message.replaceAll(
        mira_killed.name( ),
        mira_killed.display_name( ) ) );
      
      this.log( death_message );
      this.environmental_deaths++;
      
      this.pulse( ).plugin( ).getServer( ).getPluginManager( ).callEvent( new MiraMatchPlayerDeathEvent( mira_killed,
        null ) );
      this.on_death( mira_killed );
      
      return;
    }
    
    mira_killer.team( ).increment_kills( );
    bukkit_killed.getWorld( ).playSound(
      bukkit_killed.getLocation( ),
      Sound.ENTITY_BLAZE_DEATH,
      1L,
      1L );
    
    if ( death_message == null )
    {
      death_message =
        "%s died to the cruelty of %s".formatted( mira_killed.name( ), mira_killer.name( ) );
    }
    
    event.setDeathMessage( death_message.replaceAll(
      mira_killed.name( ),
      mira_killed.display_name( ) ).replaceAll(
      mira_killer.name( ),
      mira_killer.display_name( ) ) );
    
    this.log( death_message );
    
    this.pulse( ).plugin( ).getServer( ).getPluginManager( ).callEvent( new MiraMatchPlayerDeathEvent( mira_killed,
      mira_killer ) );
    this.on_kill( mira_killed, mira_killer );
  }
  
  /**
   * event handler.
   * fires when a player kills another player in the active match (running this game mode).
   * implicitly fires the `on_death` event for the `killed` player.
   *
   * @param killed the subject player who died.
   * @param killer the player who killed the dead player.
   */
  public
  void on_kill( @NotNull MiraPlayerModel<?> killed, @NotNull MiraPlayerModel<?> killer )
  {
    this.kills.put( killer.uuid( ), this.kills.get( killer.uuid( ) ) + 1 );
    
    this.on_death( killed );
  }
  
  /*—[respawn handlers]———————————————————————————————————————————————————————*/
  
  @EventHandler
  public
  void onRespawn( PlayerRespawnEvent event )
  {
    // clears bed respawn location (ideally not even allowed).
    event.getPlayer( ).setRespawnLocation( null );
    
    MiraVersePlayer mira_player =
      this.pulse( ).model( ).player( event.getPlayer( ).getUniqueId( ) );
    
    // fixme: get respawn locations!
    /*
    event.setRespawnLocation(
      randomSpawnFrom(
        main.cache( ).getCurrentMap( ).getTeamSpawns( mira_player.getCurrentTeam( ).getTeamName( )
      )
    ).toLocation(main.match( ).getCurrentWorld( ), true ) );
    */
    
    this.map( ).apply_inventory( mira_player );
    // fixme: mira_player.game_mode(...)!
    event.getPlayer( ).setGameMode( GameMode.SURVIVAL );
  }
  
  @EventHandler
  public
  void onRespawn( MiraMatchPlayerRespawnEvent event )
  {
    MiraVersePlayer mira_player = event.player( );
    
    // during certain gamemodes - players will respawn onto the spectators team.
    if ( !mira_player.has_team( ) )
    {
      return;
    }
    
    // fixme: get respawn locations!
    /*
    event.setRespawnLocation(
      randomSpawnFrom(
        main.cache( ).getCurrentMap( ).getTeamSpawns( mira_player.getCurrentTeam( ).getTeamName( )
      )
    ).toLocation(main.match( ).getCurrentWorld( ), true ) );
    */
    
    this.map( ).apply_inventory( mira_player );
    // fixme: mira_player.game_mode(...)!
    event.getPlayer( ).setGameMode( GameMode.SURVIVAL );
  }
  
  /*—[team assignment handlers]———————————————————————————————————————————————*/
  
  /**
   * event handler.
   * fires when a player leaves their team and therefore the match (running this gamemode).
   * certain compensation may need to occur - this is game mode specific.
   *
   * @param player the subject player who just left their team.
   * @param team   the associated team.
   */
  public
  void on_leave_team( @NotNull MiraVersePlayer player, @NotNull MiraTeamModel team )
  {
    this.log( player.name( ) + " leaves " + team.coloured_label( ) );
    
    this.kills.remove( player.uuid( ) );
    this.deaths.remove( player.uuid( ) );
  }
  
  /**
   * event handler.
   * fires when a player joins a team during the match running this game mode.
   *
   * @param mira_player the subject player who just joined their team.
   */
  public
  void on_join_team( MiraVersePlayer mira_player )
  {
    this.log( mira_player.name( ) + " joins " + mira_player.team( ).coloured_label( ) );
    
    this.kills.put( mira_player.uuid( ), 0 );
    this.deaths.put( mira_player.uuid( ), 0 );
    
    // fixme: serialized locations w/o world.
    /*bukkit_player.teleport(
      randomSpawnFrom( this.team_spawn_coordinates.get( mira_team.label( ) ) ).toLocation(
        main.match( ).getCurrentWorld( ),
        true ) );*/
    
    this.map( ).apply_inventory( mira_player );
    
    Player bukkit_player = mira_player.crafter( );
    bukkit_player.setGameMode( GameMode.SURVIVAL );
    bukkit_player.setFallDistance( 0F );
    
    this.team_spectators.removeEntry( bukkit_player.getName( ) );
    
    MiraTeamModel mira_team = mira_player.team( );
    mira_team.bukkit_team( ).addEntry( mira_player.name( ) );
    
    bukkit_player.sendMessage( "you have joined the %s".formatted( mira_team.display_name( ) ) );
  }
  
  /**
   * called at the start of a match to automatically distribute all players onto a team.
   * this is only done for players who have pre-joined the match in the pre-game lobby.
   */
  private
  void assign_teams( )
  {
    // randomly pick players out of a hat for team assignment until everyone has been evaluated.
    List<MiraVersePlayer> players = new ArrayList<>( this.pulse( ).model( ).players( ).values( ) );
    
    while ( !players.isEmpty( ) )
    {
      MiraVersePlayer player = players.get( this.pulse( ).model( ).rng.nextInt( players.size( ) ) );
      
      if ( player.joined( ) )
      {
        if ( this.try_join_team( player, null ) )
        {
          return;
        }
      }
      
      // the player did not join before the match started - or they failed to join a team.
      player.crafter( ).setGameMode( GameMode.CREATIVE );
      // fixme: this.pulse( ).master( ).giveSpectatorKit( player );
      
      players.remove( player );
    }
  }
  
  private
  void try_join_team( @NotNull MiraVersePlayer player, @Nullable MiraTeamModel preferred_team )
  {
    if ( !this.active )
    {
      throw new IllegalStateException( "player joins team during inactive game mode?" );
    }
    
    if ( !player.joined( ) )
    {
      throw new IllegalStateException( "player joins team without being marked as joined?" );
    }
    
    if ( this.permanent_death )
    {
      player.messages( "permanent death is enabled - you can no longer join!" );
      player.joined( false );
      
      return;
    }
    
    @NotNull MiraTeamModel given_team =
      Objects.requireNonNullElseGet( preferred_team, this::smallest_team );
    
    if ( given_team.full( ) )
    {
      if ( preferred_team == null )
      {
        player.messages( "all teams are full, please try joining later." );
      }
      else
      {
        player.messages( "your preferred team is full, please try joining later." );
      }
      
      player.joined( false );
      
      return;
    }
    
    MiraMatchPlayerJoinTeamEvent join_team_event =
      new MiraMatchPlayerJoinTeamEvent( player, given_team );
    
    this.call_event( join_team_event );
    
    if ( join_team_event.isCancelled( ) )
    {
      return;
    }
    
    player.joins( given_team );
    
    this.on_join_team( player );
  }
  
  private
  void try_leave_team( @NotNull MiraVersePlayer mira_player )
  {
    if ( !this.active )
    {
      throw new IllegalStateException( "player leaves team during inactive game mode?" );
    }
    
    if ( !mira_player.joined( ) )
    {
      throw new IllegalStateException( "player leaves team without being marked as joined?" );
    }
    
    if ( !this.permanent_death )
    {
      mira_player.messages( "you have left the match!" );
    }
    
    MiraTeamModel mira_team = mira_player.team( );
    
    this.pulse( ).plugin( ).getServer( ).getPluginManager( ).callEvent( new MiraMatchPlayerLeaveTeamEvent( mira_player,
      mira_team ) );
    
    mira_player.joins( null );
    
    Player bukkit_player = mira_player.crafter( );
    
    bukkit_player.teleport( map( ).spectator_spawn_position( ) );
    bukkit_player.setGameMode( GameMode.CREATIVE );
    
    mira_team.bukkit_team( ).removeEntry( mira_player.name( ) );
    this.team_spectators.addEntry( mira_player.name( ) );
    
    this.pulse( ).model( ).items( ).clear( mira_player );
    //this.pulse().master().giveSpectatorKit( mira_player );
    
    this.on_leave_team( mira_player, mira_team );
  }
  
  /*—[match lifecycle handlers]———————————————————————————————————————————————*/
  
  /**
   * event handler.
   * fires when the match running this game mode should now be ended.
   * this can be due to the objective being fulfilled - or a manually induced ending.
   */
  public
  void on_game_mode_end( )
  {
    if ( this.global_task != null )
    {
      this.global_task.cancel( );
      this.determine_winner( );
    }
    
    this.on_complete( );
  }
  
  protected abstract
  void determine_winner( );
  
  /**
   * Broadcasts a winner after calculation.
   *
   * @param winners   The list of winners.
   * @param objective The objective. i.e. CTF = "captures"
   * @param highest   Used if there is a single winner.
   */
  protected
  void broadcast_winner( List<MiraTeamModel> winners, String objective, int highest )
  {
    // Is there more than one winner?
    if ( winners.size( ) > 1 )
    {
      TextComponent comp = new TextComponent( "It's a " + winners.size( ) + "-way tie! " );
      comp.addExtra( main.strings( ).winnerFormat( winners ) );
      comp.addExtra( " tied!" );
      main.broadcastSpigotMessage( comp );
      
      tempWinner = main.strings( ).sentenceFormat( winners );
    }
    else if ( winners.size( ) == 1 )
    {
      WarTeam winner = winners.get( 0 ); // Get the singleton winner!
      // ChatColor.stripColor() is used to remove the team's color from the String so it can be queried to get their points.
      TextComponent comp = winner.getHoverInformation( );
      // WHY SO GRAMMAR?
      comp.addExtra( (
                       winner.getTeamName( ).charAt( winner.getTeamName( ).length( ) - 1 ) == 's' ?
                       " are the winners" :
                       " is the winner"
                     ) + " with " + highest + " " + objective + "!" );
      main.broadcastSpigotMessage( comp );
      tempWinner = main.strings( ).sentenceFormat( winners );
    }
  }
  
  /**
   * @return the amount of seconds elapsed since the match & game mode started.
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
  
  /**
   * @return the map currently being played in tandem with this game mode.
   */
  protected @NotNull
  MiraMapModel map( )
  {
    if ( this.map == null )
    {
      throw new NullPointerException( "please set the map for the running gamemode!" );
    }
    return map;
  }
  
  /**
   * starts the game mode. done in tandem with starting the match.
   */
  public
  void begin( @NotNull MiraMapModel map )
  {
    if ( this.active )
    {
      throw new IllegalStateException( "game mode is already active!" );
    }
    
    this.map = map;
    
    this.pulse( ).plugin( ).getServer( ).getPluginManager( ).registerEvents(
      this,
      this.pulse( ).plugin( ) );
    
    /*this.team_spawn_coordinates =
      ( HashMap<String, ArrayList<SerializedLocation>> ) map( ).teamSpawns.clone( );*/
    
    this.active = true;
    this.scoreboard =
      Objects.requireNonNull( this.pulse( ).plugin( ).getServer( ).getScoreboardManager( ) ).getNewScoreboard( );
    
    for ( MiraTeamModel mira_team : this.map( ).teams( ) )
    {
      Team bukkit_team = scoreboard.registerNewTeam( mira_team.label( ) );
      mira_team.bukkit_team( bukkit_team );
      bukkit_team.setCanSeeFriendlyInvisibles( true );
      // todo: allow friendly fire lmfao? like hit each other for no damage maybe?
      bukkit_team.setAllowFriendlyFire( false );
      bukkit_team.setPrefix( String.valueOf( mira_team.colour( ) ) );
    }
    
    this.team_spectators = scoreboard.registerNewTeam( "spectators" );
    this.team_spectators.setCanSeeFriendlyInvisibles( true );
    this.team_spectators.setAllowFriendlyFire( false );
    this.team_spectators.setPrefix( String.valueOf( ChatColor.LIGHT_PURPLE ) );
    
    for ( MiraVersePlayer player : this.pulse( ).model( ).players( ).values( ) )
    {
      this.team_spectators.addEntry( player.name( ) );
    }
    
    this.global_task = Bukkit.getScheduler( ).runTaskTimer(
      this.pulse( ).plugin( ), ( )->
      {
        assert this.global_task != null;
        
        if ( this.pulse( ).model( ).match( ).getState( ) != MiraMatchModel.State.PLAYING )
        {
          this.global_task.cancel( );
          
          return;
        }
        
        on_timer_second_elapsed( );
        
        long seconds_remaining = this.match_duration( ) - this.seconds_elapsed;
        
        if ( seconds_remaining % 60 == 0 && seconds_remaining != 0 )
        {
          Bukkit.broadcastMessage( "there is %d minute(s) remaining!".formatted( seconds_remaining /
                                                                                 60 ) );
        }
        else if ( seconds_remaining == 30 )
        {
          Bukkit.broadcastMessage( "there is 30 seconds remaining!" );
        }
        else if ( seconds_remaining < 6 && seconds_remaining > 0 )
        {
          Bukkit.broadcastMessage( "there is %d second(s) remaining!".formatted( seconds_remaining ) );
        }
        
        if ( seconds_elapsed( ) >= match_duration( ) )
        {
          on_game_mode_end( ); // the match *always* ends once the match duration has been reached.
        }
        // have a 0 `tick` delay before starting the task, and repeat every 20 ticks.
        // a `tick` is a 20th of a second. minecraft servers run at 20 ticks per second (tps).
      }, 0L, 20L );
  }
  
  /**
   * event handler.
   * fires every 20 ticks (*usually* 1 second) in tandem with the global task timer.
   */
  public
  void on_timer_second_elapsed( )
  {
    seconds_elapsed++;
  }
  
  /**
   * deactivation of the gamemode involves cancelling of the global task and removing hanging references.
   */
  public
  void deactivate( )
  {
    if ( this.global_task != null )
    {
      this.global_task.cancel( );
    }
    
    this.global_task = null;
    this.map = null; // Frees up the currently playing map's assignment in memory.
    
    HandlerList.unregisterAll( this );
    
    //publishes_statistics( );
  }
  
  public @NotNull
  Scoreboard scoreboard( )
  {
    return this.scoreboard;
  }
  
  /**
   * Searches through all current teams in the match for
   * the team with the least amount of members.
   *
   * @return The team with the least members.
   */
  private @Nullable
  MiraTeamModel smallest_team( )
  {
    @Nullable Integer minimum_team_size = null;
    @Nullable MiraTeamModel result = null;
    
    boolean all_teams_full = true;
    
    for ( MiraTeamModel team : this.map( ).teams( ) )
    {
      if ( !team.full( ) )
      {
        all_teams_full = false;
        if ( minimum_team_size == null ||
             team.bukkit_team( ).getEntries( ).size( ) < minimum_team_size )
        {
          result = team;
          minimum_team_size = team.bukkit_team( ).getEntries( ).size( );
        }
      }
    }
    
    return result;
  }
  
  private
  long match_duration( )
  {
    return ( long ) map( ).attr( ).get( "matchDuration" );
  }
}
