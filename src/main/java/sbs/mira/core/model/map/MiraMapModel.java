package sbs.mira.core.model.map;

import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Hanging;
import org.bukkit.event.Listener;
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
 * This extensible class stores all &amp; handles
 * some map data. Most map data is manipulated at
 * match runtime if the selected map is playing.
 * <p>
 * Do NOT use WarMap as a direct extension for
 * your map configurations. Certain procedures must
 * be defined on an extra map subclass in the
 * program that actually extends this framework.
 * <p>
 * Check out activate() and deactivate().
 * You must have this defined on another subclass,
 * and not defined in each individual map extension.
 * created on 2017-04-09.
 *
 * @author jj stephen.
 * @version 1.0.1
 * @since 1.0.0
 */
public abstract
class MiraMapModel<Pulse extends MiraPulse<?, ?>>
  extends MiraModel<Pulse>
  implements Listener
{
  /*—[map attributes]—————————————————————————————————————————————————————————*/
  
  private final @NotNull Set<UUID> creators;
  private @Nullable String label;
  private @Nullable String display_name;
  private final @NotNull Set<MiraGameModeType> allowed_game_mode_types;
  private final @NotNull Map<String, MiraTeamModel> teams;
  private final @NotNull Map<String, ArrayList<Position>> team_spawn_positions;
  protected @Nullable Position spectator_spawn_position;
  
  /*—[rules / limits]—————————————————————————————————————————————————————————*/
  
  // allows players to take physical PvP damage.
  private final boolean allow_damage;
  // allows players to break blocks.
  private boolean allow_block_break;
  // allows players to place blocks.
  private boolean allow_block_place;
  // allows blocks to be destroyed from an explosion.
  private boolean allow_block_explode;
  // allows players to take ender pearl collision damage.
  private final boolean allow_ender_pearl_damage;
  // allows fire to burn, destroy, and spread to other blocks.
  private final boolean allow_fire_spread;
  // todo: figure out what this does lol.
  private final boolean item_merging;
  
  private boolean enforce_plateau;
  private int plateau_y;
  
  private boolean enforce_maximum_build_height;
  private int maximum_build_height;
  
  private boolean enforce_build_region;
  private @NotNull Region build_region;
  
  // default match duration (currently set to 900 seconds / 15 minutes).
  private int match_duration;
  // default kill count needed to win the ffa game mode.
  private final byte ffa_kill_limit;
  // default flag captures needed to win the ctf game mode.
  private final byte flag_capture_limit;
  // default holding time needed to with the koth game mode.
  private final short capture_time_limit;
  
  private final boolean time_lock;
  private int time_lock_time;
  
  private final @NotNull EnumSet<Material> excluded_death_drops;
  
  private final @NotNull List<MiraObjective> objectives;
  
  /*—[match attributes]———————————————————————————————————————————————————————*/
  
  private final @NotNull MiraMatch match;
  
  private boolean active;
  
  /*—[interface]——————————————————————————————————————————————————————————————*/
  
  protected
  MiraMapModel( @NotNull Pulse pulse, @NotNull MiraMatch match )
  {
    super( pulse );
    
    this.allow_damage = true;
    this.allow_block_break = true;
    this.allow_block_place = true;
    this.allow_block_explode = true;
    this.allow_ender_pearl_damage = true;
    this.allow_fire_spread = false;
    this.item_merging = true;
    
    this.enforce_plateau = false;
    this.plateau_y = -1;
    
    this.enforce_maximum_build_height = false;
    this.maximum_build_height = -1;
    
    this.enforce_build_region = false;
    this.build_region = null;
    
    this.match_duration = 900;
    this.ffa_kill_limit = 20;
    this.flag_capture_limit = 3;
    this.capture_time_limit = 180;
    
    this.time_lock = false;
    this.time_lock_time = -1;
    
    this.excluded_death_drops = EnumSet.noneOf( Material.class );
    
    this.creators = new HashSet<>( );
    this.allowed_game_mode_types = new HashSet<>( );
    this.teams = new HashMap<>( );
    this.team_spawn_positions = new HashMap<>( );
    this.objectives = new ArrayList<>( );
    
    this.match = match;
    this.active = false;
  }
  
  /*—[implementation definitions]—————————————————————————————————————————————————————————————————*/
  
  /**
   * implementations should define all rules / flags within this procedure
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
   * @param player The player to apply.
   */
  public abstract
  void apply_inventory( @NotNull MiraPlayerModel<?> player );
  
  /*—[match lifecycle]————————————————————————————————————————————————————————————————————————————*/
  
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
   * @return true - if this map has been activated.
   */
  public
  boolean active( )
  {
    return this.active;
  }
  
  public @NotNull
  String label( )
  {
    assert this.label != null;
    
    return label;
  }
  
  protected
  void label( @NotNull String label )
  {
    this.label = label;
  }
  
  public @NotNull
  String display_name( )
  {
    assert this.display_name != null;
    
    return this.display_name;
  }
  
  protected
  void display_name( @NotNull String display_name )
  {
    this.display_name = display_name;
  }
  
  public @NotNull
  Position spectator_spawn_position( )
  {
    assert this.spectator_spawn_position != null;
    
    return this.spectator_spawn_position;
  }
  
  protected
  void spectator_spawn_position( @NotNull Position spectator_spawn_position )
  {
    this.spectator_spawn_position = spectator_spawn_position;
  }
  
  protected
  void team( @NotNull MiraTeamModel team )
  {
    this.teams.put( team.label( ), team );
  }
  
  public @NotNull
  Collection<MiraTeamModel> teams( )
  {
    return this.teams.values( );
  }
  
  protected
  void allow_game_mode_type( @NotNull MiraGameModeType game_mode_type )
  {
    this.allowed_game_mode_types.add( game_mode_type );
  }
  
  public
  Set<MiraGameModeType> allowed_game_mode_types( )
  {
    return this.allowed_game_mode_types;
  }
  
  public @NotNull
  List<Position> team_spawn_positions( MiraTeamModel team )
  {
    return this.team_spawn_positions.get( team.label( ) );
  }
  
  public @NotNull
  Location random_team_spawn_location( MiraTeamModel team )
  {
    List<Position> spawn_positions = this.team_spawn_positions( team );
    return spawn_positions.get( this.pulse( ).model( ).rng.nextInt( spawn_positions.size( ) ) ).location(
      this.match.world( ),
      true );
    
  }
  
  protected
  void team_spawn( @NotNull String team_label, @NotNull Position team_spawn_position )
  {
    this.team_spawn_positions.putIfAbsent( team_label, new ArrayList<>( ) );
    this.team_spawn_positions.get( team_label ).add( team_spawn_position );
  }
  
  /**
   * @return player uuids for the defined creators of this map.
   */
  public
  Set<UUID> creators( )
  {
    return creators;
  }
  
  protected
  void creator( String creator_player_uuid )
  throws IllegalArgumentException
  {
    this.creators.add( UUID.fromString( creator_player_uuid ) );
  }
  
  /**
   * @param uuid player uuid to be checked.
   * @return true - if this uuid is one of the level creators' player uuid's.
   */
  public
  boolean is_creator( @NotNull UUID uuid )
  {
    return this.creators.contains( uuid );
  }
  
  public
  int match_duration( )
  {
    return this.match_duration;
  }
  
  protected
  void match_duration( int match_duration )
  {
    this.match_duration = match_duration;
  }
  /*—[map rule/limit/objective definitions]———————————————————————————————————————————————————————*/
  
  public
  void objective( @NotNull MiraObjective objective )
  {
    this.objectives.add( objective );
  }
  
  @NotNull
  public
  List<MiraObjective> objectives( )
  {
    return this.objectives;
  }
  
  protected
  void build_region( @NotNull Region build_region )
  {
    this.enforce_build_region = true;
    this.build_region = build_region;
  }
  
  protected
  void allow_block_break( boolean allow_block_break )
  {
    this.allow_block_break = allow_block_break;
  }
  
  protected
  void allow_block_place( boolean allow_block_place )
  {
    this.allow_block_place = allow_block_place;
  }
  
  protected
  void allow_block_explode( boolean allow_block_explode )
  {
    this.allow_block_explode = allow_block_explode;
  }
  
  protected
  void time_lock_time( int time_lock_time )
  {
    this.time_lock_time = time_lock_time;
  }
  
  protected
  void plateau_y( int plateau_y )
  {
    this.enforce_plateau = true;
    this.plateau_y = plateau_y;
  }
  
  protected
  void maximum_build_height( int maximum_build_height )
  {
    this.enforce_maximum_build_height = true;
    this.maximum_build_height = maximum_build_height;
  }
  
  protected
  void exclude_death_drop( @NotNull Material material )
  {
    this.excluded_death_drops.add( material );
  }
}
