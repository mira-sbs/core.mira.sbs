package sbs.mira.core.model.map;

import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Hanging;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sbs.mira.core.MiraModel;
import sbs.mira.core.MiraPulse;
import sbs.mira.core.event.handler.*;
import sbs.mira.core.model.MiraPlayerModel;
import sbs.mira.core.model.match.MiraGameModeType;
import sbs.mira.core.model.match.MiraMatch;
import sbs.mira.core.model.utility.Position;
import sbs.mira.core.model.utility.Region;

import java.util.*;

/**
 * miral representation of a map.
 * forms part of the coaxis between maps, game modes and players.
 * specifically purposed to act as a bridge between game modes and players.
 * the map - always pre-built - acts as the interface that allows players to
 * interact with the game mode - and by extension: the match.
 * implementations should extend this class and use the protected methods to
 * declare the required definitions that make the map function within a match.
 * created on 2017-04-09.
 *
 * @author jj stephen.
 * @version 1.0.1
 * @since 1.0.0
 */
public abstract
class MiraMapModel<Pulse extends MiraPulse<?, ?>>
  extends MiraModel<Pulse>
{
  /*—[map attributes]—————————————————————————————————————————————————————————*/
  
  @Nullable
  private String label;
  @Nullable
  private String display_name;
  @NotNull
  private final Set<UUID> creators;
  @NotNull
  private final Set<MiraGameModeType> allowed_game_mode_types;
  @NotNull
  private final Map<String, MiraTeamModel> teams;
  @NotNull
  private final Map<String, ArrayList<Position>> team_spawn_positions;
  @Nullable
  protected Position spectator_spawn_position;
  @NotNull
  private final List<MiraObjective> objectives;
  private int match_duration;
  
  /*—[rules / limits]—————————————————————————————————————————————————————————*/
  
  private boolean allow_damage;
  private boolean allow_block_break;
  private boolean allow_block_place;
  private boolean allow_block_explode;
  private boolean allow_ender_pearl_damage;
  private boolean allow_fire_spread;
  private boolean enforce_plateau;
  private int plateau_y;
  private boolean enforce_maximum_build_height;
  private int maximum_build_height;
  private boolean enforce_build_region;
  @Nullable
  private Region build_region;
  private int ffa_kill_limit;
  private int capture_time_limit;
  private boolean time_lock;
  private int time_lock_time;
  private final @NotNull EnumSet<Material> excluded_death_drops;
  
  /*—[match attributes]———————————————————————————————————————————————————————————————————————————*/
  
  @NotNull
  private final MiraMatch match;
  private boolean active;
  
  /*—[interface]——————————————————————————————————————————————————————————————————————————————————*/
  
  /**
   * instantiates a polymorphic map model - implementation sits above.
   *
   * @param pulse reference to mira.
   * @param match instance of the currently active match.
   */
  protected
  MiraMapModel( @NotNull Pulse pulse, @NotNull MiraMatch match )
  {
    super( pulse );
    
    this.creators = new HashSet<>( );
    this.allowed_game_mode_types = new HashSet<>( );
    this.teams = new HashMap<>( );
    this.team_spawn_positions = new HashMap<>( );
    this.objectives = new ArrayList<>( );
    this.match_duration = 900;
    
    this.allow_damage = true;
    this.allow_block_break = true;
    this.allow_block_place = true;
    this.allow_block_explode = true;
    this.allow_ender_pearl_damage = true;
    this.allow_fire_spread = false;
    
    this.enforce_plateau = false;
    this.plateau_y = -1;
    
    this.enforce_maximum_build_height = false;
    this.maximum_build_height = -1;
    
    this.enforce_build_region = false;
    this.build_region = null;
    
    this.ffa_kill_limit = 20;
    this.capture_time_limit = 180;
    
    this.time_lock = false;
    this.time_lock_time = -1;
    
    this.excluded_death_drops = EnumSet.noneOf( Material.class );
    
    this.match = match;
    this.active = false;
  }
  
  /*—[implementation definitions]—————————————————————————————————————————————————————————————————*/
  
  /**
   * implementations should define all rules / limits within this procedure
   * (if applicable).
   */
  protected abstract
  void define_rules( );
  
  /**
   * implementations should define all spawn positions / regions within this
   * procedure.
   */
  protected abstract
  void define_spawns( );
  
  /**
   * implementations should define all objectives within this procedure
   * (if applicable to the available game mode[s]).
   */
  protected abstract
  void define_objectives( );
  
  /**
   * implementations should give the standard item kit to players - per player -
   * within this procedure (if applicable to the map context).
   *
   * @param player the player who requires a fresh inventory and item kit.
   */
  public abstract
  void apply_inventory( @NotNull MiraPlayerModel<?> player );
  
  /*—[match lifecycle]————————————————————————————————————————————————————————————————————————————*/
  
  /**
   * transitions this map model into its active state. this occurs when a match
   * has begun, the vote has concluded and the pre-game has also concluded.
   * the match is ready to begin and the map will:
   * i. mark itself as active.
   * ii. setup event handlers / guards to enact the defined rules / limits.
   * iii. change world rules + state to further enact these rules / limits.
   */
  public
  void activate( )
  {
    if ( this.active )
    {
      throw new IllegalStateException( "map is already active - cannot activate!" );
    }
    
    this.active = true;
    
    if ( !this.allow_damage )
    {
      this.event_handler( new MiraEntityDamageGuard<>( this.pulse( ) ) );
    }
    
    if ( !this.allow_block_explode )
    {
      this.event_handler( new MiraBlockExplodeGuard<>( this.pulse( ) ) );
    }
    
    if ( !excluded_death_drops.isEmpty( ) )
    {
      this.event_handler( new MiraPlayerDeathDropGuard<>(
        this.pulse( ),
        ( item )->excluded_death_drops.contains( item.getType( ) ) ) );
    }
    
    if ( !allow_block_break )
    {
      this.event_handler( new MiraBlockBreakGuard<>( this.pulse( ) ) );
      this.event_handler( new MiraHangingBreakGuard<>( this.pulse( ) ) );
      this.event_handler( new MiraEntityDamageGuard<>(
        this.pulse( ),
        ( entity->entity instanceof Hanging ) ) );
    }
    
    if ( !allow_block_place )
    {
      this.event_handler( new MiraBlockPlaceGuard<>( this.pulse( ) ) );
    }
    
    if ( !allow_ender_pearl_damage )
    {
      new MiraEntityDamageSourceGuard<>(
        this.pulse( ),
        ( damage_source )->damage_source.getDamageType( ) == DamageType.ENDER_PEARL );
    }
    
    if ( enforce_plateau )
    {
      this.event_handler( new MiraPlateauBuildingGuard<>( this.pulse( ), plateau_y ) );
    }
    
    if ( enforce_maximum_build_height )
    {
      this.event_handler( new MiraBlockPlaceGuard<>(
        this.pulse( ),
        ( block )->block.getY( ) > maximum_build_height ) );
    }
    
    if ( enforce_build_region )
    {
      this.event_handler( new MiraBlockPlaceGuard<>(
        this.pulse( ),
        ( block )->build_region.within( block.getLocation( ) ) ) );
    }
    
    World world = this.match.world( );
    world.setGameRule( GameRule.DO_FIRE_TICK, allow_fire_spread );
    
    if ( time_lock )
    {
      world.setFullTime( this.time_lock_time );
      world.setGameRule( GameRule.DO_DAYLIGHT_CYCLE, false );
    }
  }
  
  public
  void deactivate( )
  {
    if ( !this.active )
    {
      throw new IllegalStateException( "map is not active - cannot deactivate!" );
    }
    
    this.unregister_event_handlers( );
    
    this.active = false;
  }
  
  /*—[getters / setters]——————————————————————————————————————————————————————————————————————————*/
  
  /**
   * true = this map has been activated along with the provided game mode.
   * false = this map has not yet been activated - or has been deactivated.
   *
   * @return true - if this map has been activated.
   */
  public
  boolean active( )
  {
    return this.active;
  }
  
  /**
   * simple verbal / programmatic label of this map, serving as an identifier.
   *
   * @return the unique label for this map.
   */
  public @NotNull
  String label( )
  {
    assert this.label != null;
    
    return label;
  }
  
  /**
   * allows map implementations to set the label of this map.
   *
   * @param label the new map label.
   */
  protected
  void label( @NotNull String label )
  {
    this.label = label;
  }
  
  /**
   * full unabbreviated / unformatted / grammatical name of this map.
   * it will (of course) be displayed in various places.
   *
   * @return the display name for this map.
   */
  public @NotNull
  String display_name( )
  {
    assert this.display_name != null;
    
    return this.display_name;
  }
  
  /**
   * allows map implementations to set the display name of this map.
   *
   * @param display_name the new map display name.
   */
  protected
  void display_name( @NotNull String display_name )
  {
    this.display_name = display_name;
  }
  
  /**
   * creator credit will be given to the players with uuids contained in this set.
   *
   * @return player uuids for the defined creators of this map.
   */
  public
  Set<UUID> creators( )
  {
    return creators;
  }
  
  /**
   * gives creator credit to the given player uuid.
   *
   * @param creator_player_uuid the uuid of the player who contributed to the creation of this map.
   */
  protected
  void creator( String creator_player_uuid )
  {
    this.creators.add( UUID.fromString( creator_player_uuid ) );
  }
  
  /**
   * @param uuid player uuid to be checked.
   * @return true - if this player uuid belongs to a contributor of this map's creation.
   */
  public
  boolean is_creator( @NotNull UUID uuid )
  {
    return this.creators.contains( uuid );
  }
  
  /**
   * defines what game modes are available to play on this map.
   * certain maps only support certain game modes due to the objectives having
   * intrinsic ties to the world and gameplay elements.
   * this affects the voting stage of a match by restricting what game modes can
   * be voted for on a particular map.
   *
   * @return a set of game mode types that are available to play on this map.
   */
  @NotNull
  public
  Set<MiraGameModeType> allowed_game_mode_types( )
  {
    return this.allowed_game_mode_types;
  }
  
  /**
   * allows map implementations to make a game mode available for this map.
   *
   * @param game_mode_type the game mode to be included as available on this map.
   */
  protected
  void allow_game_mode_type( @NotNull MiraGameModeType game_mode_type )
  {
    this.allowed_game_mode_types.add( game_mode_type );
  }
  
  /**
   * maps must have an array of team identities available during game play.
   * teams are then associated with objectives - which are competed for.
   *
   * @return all teams defined for this map.
   */
  @NotNull
  public
  Collection<MiraTeamModel> teams( )
  {
    return this.teams.values( );
  }
  
  /**
   * allows map implementations to declare and store a team identity definition.
   *
   * @param team the team defined for this map.
   */
  protected
  void team( @NotNull MiraTeamModel team )
  {
    this.teams.put( team.label( ), team );
  }
  
  /**
   * maps must have an array of spawn positions available for each team during
   * game play.
   *
   * @param team the team requiring a list of its spawn positions.
   * @return the list of spawn positions associated with the given team.
   */
  @NotNull
  public
  List<Position> team_spawn_positions( MiraTeamModel team )
  {
    return this.team_spawn_positions.get( team.label( ) );
  }
  
  /**
   * allows map implementations to add spawn positions available for each team
   * during game play.
   *
   * @param team_label          the label of the team receiving a new spawn position definition.
   * @param team_spawn_position the new spawn position definition to be added.
   */
  protected
  void team_spawn( @NotNull String team_label, @NotNull Position team_spawn_position )
  {
    this.team_spawn_positions.putIfAbsent( team_label, new ArrayList<>( ) );
    this.team_spawn_positions.get( team_label ).add( team_spawn_position );
  }
  
  /**
   * positions are not real locations within minecraft worlds - and are often
   * retrieved randomly instead of sequentially.
   * this black box helper function takes a given team - picks a random spawn
   * position - and converts it into a location that can be teleported to.
   *
   * @param team the team requiring a random spawn position.
   * @return the random spawn position for the team - converted into a location.
   * @see sbs.mira.core.model.utility.Position
   */
  @NotNull
  public
  Location random_team_spawn_location( MiraTeamModel team )
  {
    List<Position> spawn_positions = this.team_spawn_positions( team );
    return spawn_positions.get( this.pulse( ).model( ).rng.nextInt( spawn_positions.size( ) ) ).location(
      this.match.world( ),
      true );
    
  }
  
  /**
   * maps must define the spawn position for spectators - who are not currently
   * participating in the match.
   *
   */
  @NotNull
  public
  Position spectator_spawn_position( )
  {
    assert this.spectator_spawn_position != null;
    
    return this.spectator_spawn_position;
  }
  
  /**
   * allows map implementations to define the spawn position for spectators.
   *
   * @param spectator_spawn_position the spawn position for spectators.
   */
  protected
  void spectator_spawn_position( @NotNull Position spectator_spawn_position )
  {
    this.spectator_spawn_position = spectator_spawn_position;
  }
  
  /**
   * maps must define the objectives - which are structures / elements built
   * into the world component to serve as the primary public interface to
   * in-game players.
   * these objectives dictate the core game play of the supported and chosen
   * game mode(s) for this map during an active match.
   *
   * @return the objectives defined for this map.
   */
  @NotNull
  public
  List<MiraObjective> objectives( )
  {
    return this.objectives;
  }
  
  /**
   * allows map implementations to add objective definitions to support the key
   * game play elements.
   *
   * @param objective the objective defined for this map.
   */
  protected
  void objective( @NotNull MiraObjective objective )
  {
    this.objectives.add( objective );
  }
  
  /**
   * defines the maximum duration of a match (in seconds) for this map.
   * set to 900 seconds (15 minutes) by default - can be overridden.
   *
   * @return the maximum duration of a match for this map (in seconds).
   */
  public
  int match_duration( )
  {
    return this.match_duration;
  }
  
  /**
   * allows map implementations to override the maximum match duration for this map.
   *
   * @param match_duration the new maximum match duration (in seconds) for this map.
   */
  protected
  void match_duration( int match_duration )
  {
    this.match_duration = match_duration;
  }
  
  /*—[map rule/limit/objective definitions]———————————————————————————————————————————————————————*/
  
  /**
   * true = allows players to receive damage (at all - no health or animation).
   * false = damage blocked - this is enforced by a guard.
   *
   * @param allow_damage true - if receiving damage is allowed on this map.
   */
  protected
  void allow_damage( boolean allow_damage )
  {
    this.allow_damage = allow_damage;
  }
  
  /**
   * true = allows players to break blocks on this map.
   * false = prevents players from breaking blocks on this map - this is
   * enforced by a guard.
   *
   * @param allow_block_break true - if players are allowed to break blocks on this map.
   */
  protected
  void allow_block_break( boolean allow_block_break )
  {
    this.allow_block_break = allow_block_break;
  }
  
  /**
   * true = allows players to place blocks on this map.
   * false = prevents players from placing blocks on this map - this is
   * enforced by a guard.
   *
   * @param allow_block_place true - if players are allowed to place blocks on this map.
   */
  protected
  void allow_block_place( boolean allow_block_place )
  {
    this.allow_block_place = allow_block_place;
  }
  
  /**
   * true = allows all blocks to be destroyed by explosions.
   * false = prevents all blocks from being destroyed by explosions.
   *
   * @param allow_block_explode true - if blocks should explode on this map.
   */
  protected
  void allow_block_explode( boolean allow_block_explode )
  {
    this.allow_block_explode = allow_block_explode;
  }
  
  /**
   * true = allows players to receive fixed fall damage from ender pearls.
   * false = prevents players from receiving ender pearl damage.
   *
   * @param allow_ender_pearl_damage true - if ender pearl damage is allowed.
   */
  protected
  void allow_ender_pearl_damage( boolean allow_ender_pearl_damage )
  {
    this.allow_ender_pearl_damage = allow_ender_pearl_damage;
  }
  
  /**
   * true = allows fire to ignite, spread and consume.
   * false = prevents fire from propagating.
   *
   * @param allow_fire_spread true - if fire spreading is allowed.
   */
  protected
  void allow_fire_spread( boolean allow_fire_spread )
  {
    
    this.allow_fire_spread = allow_fire_spread;
  }
  
  /**
   * providing a `time_lock_time` will lock the world's time in place and will
   * no longer advance.
   * the time will change to the provided `time_lock_time` and stay locked in
   * place.
   *
   * @param time_lock_time the world time to set and lock.
   */
  protected
  void time_lock_time( int time_lock_time )
  {
    this.time_lock = true;
    this.time_lock_time = time_lock_time;
  }
  
  /**
   * providing a `build_region` will prevent players from building outside the
   * given region during the match - this is enforced by a guard.
   *
   * @param build_region the region that players are allowed to build within.
   */
  protected
  void build_region( @NotNull Region build_region )
  {
    this.enforce_build_region = true;
    this.build_region = build_region;
  }
  
  /**
   * a "plateau" within the context of a map configuration refers to the bedrock
   * block slate found under specific types of map worlds.
   * the bedrock is not there to stop players from falling through the world -
   * it allows the code to easily check if the player is building within the
   * map boundaries (as "adminium" cannot be destroyed by "noobs").
   * providing a `plateau_y` coordinate value will prevent players from building
   * out of the map boundaries - by checking whether a bedrock block is found
   * at the current position with the y coordinate (altered to the plateau
   * value).
   *
   * @param plateau_y the plateau_y denoting the bedrock plane+build region on the map world.
   */
  protected
  void plateau_y( int plateau_y )
  {
    this.enforce_plateau = true;
    this.plateau_y = plateau_y;
  }
  
  /**
   * providing a `maximum_build_height` y coordinate value will prevent players
   * from building at block y coordinates higher than said y value.
   *
   * @param maximum_build_height the maximum allowed building y coordinate.
   */
  protected
  void maximum_build_height( int maximum_build_height )
  {
    this.enforce_maximum_build_height = true;
    this.maximum_build_height = maximum_build_height;
  }
  
  /**
   * materials within this set will not be dropped upon death.
   * if they are part of the item drop list, they will be removed.
   * map implementations can specify each material they want to exclude.
   *
   * @param material the item material type to be excluded from player death drops for this map.
   */
  protected
  void exclude_death_drop( @NotNull Material material )
  {
    this.excluded_death_drops.add( material );
  }
  
  /**
   * the number of kills needed to win a free-for-all (ffa) game mode.
   * set to 20 kills by default - map implementations can override it using this
   * method.
   *
   * @param ffa_kill_limit the new ffa kill limit.
   */
  protected
  void ffa_kill_limit( int ffa_kill_limit )
  {
    this.ffa_kill_limit = ffa_kill_limit;
  }
  
  /**
   * the number of seconds required while holding the flag to win a
   * king-of-the-hill (koth) game mode.
   * set to 180 seconds by default - map implementations can override it using
   * this method.
   *
   * @param capture_time_limit the new koth capture time limit.
   */
  protected
  void capture_time_limit( int capture_time_limit )
  {
    this.capture_time_limit = capture_time_limit;
  }
}
