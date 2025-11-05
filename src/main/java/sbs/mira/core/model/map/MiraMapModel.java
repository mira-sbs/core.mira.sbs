package sbs.mira.core.model.map;

import org.bukkit.Material;
import org.bukkit.entity.Hanging;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sbs.mira.core.MiraModel;
import sbs.mira.core.MiraPulse;
import sbs.mira.core.model.MiraPlayerModel;
import sbs.mira.core.model.match.MiraGameModeType;

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
 * <p>
 * Created by Josh on 09/04/2017.
 *
 * @author s101601828 @ Swin.
 * @version 1.0
 * @since 1.0
 */
public abstract
class MiraMapModel
  extends MiraModel<MiraPulse<?, ?>>
  implements Listener
{
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
  
  // default match duration (currently set to 900 seconds / 15 minutes).
  private short match_duration;
  // default kill count needed to win the ffa game mode.
  private final byte ffa_kill_limit;
  // default flag captures needed to win the ctf game mode.
  private final byte flag_capture_limit;
  // default holding time needed to with the koth game mode.
  private final short capture_time_limit;
  private short maximum_build_height;
  private long time_lock_time;
  
  private final @NotNull Set<Material> excluded_death_drops;
  
  /*—[game attributes]————————————————————————————————————————————————————————*/
  
  private final @NotNull Set<UUID> creators;
  private @Nullable String label;
  private @Nullable String display_name;
  private final @NotNull Set<MiraGameModeType> allowed_game_mode_types;
  private final @NotNull Map<String, MiraTeamModel> teams;
  private final @NotNull Map<String, ArrayList<Position>> team_spawn_positions;
  protected @Nullable Position spectator_spawn_position;
  
  private final @NotNull List<Object> objectives;
  
  private boolean active;
  
  /*—[interface]——————————————————————————————————————————————————————————————*/
  
  protected
  MiraMapModel( @NotNull MiraPulse<?, ?> pulse )
  {
    super( pulse );
    
    this.allow_damage = true;
    this.allow_block_break = true;
    this.allow_block_place = true;
    this.allow_block_explode = true;
    this.allow_ender_pearl_damage = true;
    this.allow_fire_spread = false;
    
    this.match_duration = 900;
    this.ffa_kill_limit = 20;
    this.flag_capture_limit = 3;
    this.capture_time_limit = 180;
    this.maximum_build_height = -1;
    this.time_lock_time = -1;
    
    this.excluded_death_drops = new HashSet<>( );
    
    this.creators = new HashSet<>( );
    this.allowed_game_mode_types = new HashSet<>( );
    this.teams = new HashMap<>( );
    this.team_spawn_positions = new HashMap<>( );
    this.objectives = new ArrayList<>( );
    
    this.active = false;
  }
  
  /**
   * implementations should define all rules / flags within this method (if applicable).
   */
  protected abstract
  void define_rules( );
  
  /**
   * Extend this procedure also to define team spawns after
   * defining the attributes in the above abstract procedure.
   */
  protected abstract
  void define_spawns( );
  
  /*—[interactions]———————————————————————————————————————————————————————————*/
  
  /**
   * Applies a player's inventory then updates it.
   * <p>
   * This is the procedure your actual program should
   * use, as it clears their inventory and updates it.
   *
   * @param player The player to apply.
   */
  public
  void apply_inventory( @NotNull MiraPlayerModel<?> player )
  {
    this.pulse( ).model( ).items( ).clear( player );
    
    //this.applyInventory( target ); override in child class.
  }
  
  public
  List<Object> objectives( )
  {
    return objectives;
  }
  
  /*——————————————————————————————————————————————————————————————————————————*/
  
  /**
   * Define the region in which blocks can be interacted with.
   *
   * @param x1 Bottom left X.
   * @param z1 Bottom left Z.
   * @param x2 Top right X.
   * @param z2 Top right Z.
   */
  protected
  void setBuildBoundary( int x1, int z1, int x2, int z2 )
  {
    /*attributes.put( "boundary", true );
    attributes.put(
      "bottomLeft",
      new SerializedLocation( Math.min( x1, x2 ), 0, Math.min( z1, z2 ) ) );
    attributes.put(
      "topRight",
      new SerializedLocation( Math.max( x1, x2 ), 0, Math.max( z1, z2 ) ) );*/
  }
  
  /*—[getters / setters]——————————————————————————————————————————————————————*/
  
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
  
  /*——————————————————————————————————————————————————————————————————————————*/
  
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
  
  /*——————————————————————————————————————————————————————————————————————————*/
  
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
  
  /*——————————————————————————————————————————————————————————————————————————*/
  
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
  
  /*——————————————————————————————————————————————————————————————————————————*/
  
  protected
  void allow_game_mode_type( @NotNull MiraGameModeType game_mode_type )
  {
    this.allowed_game_mode_types.add( game_mode_type );
  }
  
  protected
  Set<MiraGameModeType> allowed_game_mode_types( )
  {
    return this.allowed_game_mode_types;
  }
  
  /*——————————————————————————————————————————————————————————————————————————*/
  
  public @NotNull
  List<Position> team_spawn_positions( MiraTeamModel team )
  {
    return this.team_spawn_positions.get( team.label( ) );
  }
  
  protected
  void team_spawn( @NotNull String team_label, @NotNull Position team_spawn_position )
  {
    this.team_spawn_positions.putIfAbsent( team_label, new ArrayList<>( ) );
    this.team_spawn_positions.get( team_label ).add( team_spawn_position );
  }
  
  /*——————————————————————————————————————————————————————————————————————————*/
  
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
  
  /*——————————————————————————————————————————————————————————————————————————*/
  
  /**
   * allows the three given building rules to be toggled.
   */
  protected
  void building_rules(
    boolean allow_block_break,
    boolean allow_block_place,
    boolean allow_block_explode )
  {
    this.allow_block_break = allow_block_break;
    this.allow_block_place = allow_block_place;
    this.allow_block_explode = allow_block_explode;
  }
  
  /*——————————————————————————————————————————————————————————————————————————*/
  
  protected
  void time_lock_time( long time_lock_time )
  {
    this.time_lock_time = time_lock_time;
  }
  
  /*——————————————————————————————————————————————————————————————————————————*/
  
  protected
  void maximum_build_height( short maximum_build_height )
  {
    this.maximum_build_height = maximum_build_height;
  }
  
  /*——————————————————————————————————————————————————————————————————————————*/
  
  protected
  void exclude_from_death_drops( @NotNull Material material )
  {
    this.excluded_death_drops.add( material );
  }
  
  /*——————————————————————————————————————————————————————————————————————————*/
  
  public
  short match_duration( int matchDuration )
  {
    return this.match_duration;
  }
  
  protected
  void match_duration( short match_duration )
  {
    this.match_duration = match_duration;
  }
  
  /*——————————————————————————————————————————————————————————————————————————*/
  
  /**
   * @return true - if this map is within an active match.
   */
  public
  boolean active( )
  {
    return active;
  }
  
  /*—[match lifecycle]————————————————————————————————————————————————————————*/
  
  public
  void activate( )
  {
    if ( this.active )
    {
      throw new IllegalStateException( "map is already active - cannot activate!" );
    }
    
    this.server( ).getPluginManager( ).registerEvents( this, this.pulse( ).plugin( ) );
    this.active = true;
    
    /*for ( Activatable obj : objectives( ) )
    {
      obj.activate( );
    }*/
  }
  
  /*——————————————————————————————————————————————————————————————————————————*/
  
  public
  void deactivate( )
  {
    if ( !this.active )
    {
      throw new IllegalStateException( "map is not active - cannot deactivate!" );
    }
    
    /*
    for ( Activatable obj : objectives( ) )
    {
      obj.deactivate( );
    }*/
    
    this.active = false;
    
    HandlerList.unregisterAll( this );
  }
  
  /*—[bukkit event handlers]——————————————————————————————————————————————————*/
  
  @EventHandler
  public
  void on_entity_explode( EntityExplodeEvent event )
  {
    if ( !allow_block_explode )
    {
      event.blockList( ).clear( );
    }
  }
  
  /*——————————————————————————————————————————————————————————————————————————*/
  
  @EventHandler
  public
  void on_player_death( PlayerDeathEvent event )
  {
    for ( ItemStack drop : event.getDrops( ) )
    {
      if ( excluded_death_drops.contains( drop.getType( ) ) )
      {
        drop.setType( Material.AIR );
      }
    }
  }
  
  /*——————————————————————————————————————————————————————————————————————————*/
  
  @EventHandler
  public
  void on_block_spread( BlockSpreadEvent event )
  {
    if ( event.getSource( ).getType( ) == Material.FIRE && !allow_fire_spread )
    {
      event.setCancelled( true );
    }
  }
  
  /*——————————————————————————————————————————————————————————————————————————*/
  
  @EventHandler
  public
  void on_block_ignite( BlockIgniteEvent event )
  {
    if ( event.getCause( ) == BlockIgniteEvent.IgniteCause.SPREAD && !allow_fire_spread )
    {
      event.setCancelled( true );
    }
  }
  
  /*——————————————————————————————————————————————————————————————————————————*/
  
  @EventHandler
  public
  void on_block_burn( BlockBurnEvent event )
  {
    if ( !allow_fire_spread )
    {
      event.setCancelled( true );
    }
  }
  
  /*——————————————————————————————————————————————————————————————————————————*/
  
  @EventHandler
  public
  void on_hanging_break( HangingBreakEvent event )
  {
    if ( !allow_block_break )
    {
      event.setCancelled( true );
    }
  }
  
  /*——————————————————————————————————————————————————————————————————————————*/
  
  @EventHandler
  public
  void on_entity_damage( EntityDamageEvent event )
  {
    if ( !allow_block_break && event.getEntity( ) instanceof Hanging )
    {
      event.setCancelled( true );
    }
  }
  
  /*——————————————————————————————————————————————————————————————————————————*/
  
  @EventHandler (priority = EventPriority.HIGHEST)
  public
  void block_break( BlockBreakEvent event )
  {
    if ( event.isCancelled( ) )
    {
      return;
    }
    
    if ( !allow_block_break )
    {
      if ( this.pulse( ).model( ).lobby( ).can_interact(
        event.getPlayer( ).getUniqueId( ),
        false ) )
      {
        event.setCancelled( true );
        
        // fixme: rate-limited guard message.
        //main.warn( event.getPlayer( ), main.message( "guard.building" ) );
      }
    }
  }
  
  /*——————————————————————————————————————————————————————————————————————————*/
  
  /**
   * If block placing is disabled, blocks will disappear when placed.
   * Also blocks building outside the defined boundary.
   *
   * @param event An event called by the server.
   */
  @EventHandler (priority = EventPriority.HIGHEST)
  public
  void block_place( BlockPlaceEvent event )
  {
    if ( event.isCancelled( ) )
    {
      return;
    }
    
    if ( !allow_block_place )
    {
      if ( this.pulse( ).model( ).lobby( ).can_interact(
        event.getPlayer( ).getUniqueId( ),
        false ) )
      {
        event.setCancelled( true );
        
        // fixme: rate-limited guard message.
        //main.warn( event.getPlayer( ), main.message( "guard.building" ) );
      }
    }
    //fixme: rest of this.
    /*
    else if ( attributes.containsKey( "boundary" ) )
    {
      Location placed = event.getBlock( ).getLocation( );
      SerializedLocation bl = ( SerializedLocation ) attributes.get( "bottomLeft" );
      SerializedLocation tr = ( SerializedLocation ) attributes.get( "topRight" );
      if ( main.match( ).isAffected( event.getPlayer( ) ) && (
        placed.getX( ) < bl.x( ) ||
        placed.getZ( ) < bl.z( ) ||
        placed.getX( ) > tr.x( ) ||
        placed.getZ( ) > tr.z( )
      ) )
      {
        event.setCancelled( true );
        main.warn( event.getPlayer( ), main.message( "guard.border" ) );
      }
    }
    else if ( attributes.containsKey( "plateau" ) )
    {
      int plateauY = ( int ) attributes.get( "plateau" );
      Location equiv = event.getBlock( ).getLocation( ).clone( );
      equiv.setY( plateauY );
      if ( equiv.getBlock( ).getType( ) != Material.BEDROCK )
      {
        event.setCancelled( true );
        main.warn( event.getPlayer( ), main.message( "guard.border" ) );
      }
    }
    else if ( attributes.containsKey( "buildHeight" ) )
    {
      int buildHeight = ( int ) attributes.get( "buildHeight" );
      if ( event.getBlock( ).getY( ) > buildHeight )
      {
        event.setCancelled( true );
        main.warn( event.getPlayer( ), main.message( "guard.highest" ) );
      }
    }*/
  }
}
