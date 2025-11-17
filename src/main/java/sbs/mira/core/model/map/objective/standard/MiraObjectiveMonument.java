package sbs.mira.core.model.map.objective.standard;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sbs.mira.core.MiraModel;
import sbs.mira.core.MiraPulse;
import sbs.mira.core.event.handler.MiraBlockBreakGuard;
import sbs.mira.core.model.MiraPlayerModel;
import sbs.mira.core.model.map.MiraTeamModel;
import sbs.mira.core.model.map.objective.MiraObjectiveCapturableBlockRegion;
import sbs.mira.core.model.utility.Position;
import sbs.mira.core.model.utility.Region;

import java.util.*;

public abstract
class MiraObjectiveMonument<Pulse extends MiraPulse<?, ?>>
  extends MiraModel<Pulse>
  implements MiraObjectiveCapturableBlockRegion
{
  @NotNull
  protected final String monument_name;
  @NotNull
  protected final MiraTeamModel monument_team;
  @NotNull
  protected final Material monument_material;
  @NotNull
  protected final Region monument_region;
  
  private boolean active;
  private boolean block_progress;
  @Nullable
  private World world;
  
  @Nullable
  private MiraBlockBreakGuard<Pulse> blocked_monument_guard;
  
  @NotNull
  protected final List<Block> remaining_blocks;
  @NotNull
  private final List<Position> original_monument_block_positions;
  @NotNull
  protected final Map<UUID, Integer> player_contributions;
  
  public
  MiraObjectiveMonument(
    @NotNull Pulse pulse,
    @NotNull String monument_name,
    @NotNull MiraTeamModel monument_team,
    @NotNull Material build_material,
    @NotNull Region build_region )
  {
    super( pulse );
    
    this.monument_name = monument_name;
    this.monument_team = monument_team;
    this.monument_material = build_material;
    this.monument_region = build_region;
    
    this.active = false;
    this.block_progress = false;
    this.world = null;
    
    this.blocked_monument_guard = null;
    
    this.original_monument_block_positions = new ArrayList<>( );
    this.remaining_blocks = new ArrayList<>( );
    this.player_contributions = new HashMap<>( );
  }
  
  @Override
  public
  void activate( @NotNull World world )
  {
    if ( this.active )
    {
      throw new IllegalStateException( "build monument objective already active?" );
    }
    
    this.active = true;
    this.world = world;
    
    this.remaining_blocks.addAll(
      this.region( ).blocks_matching( world, ( block )->block.getType( ) == Material.TARGET ) );
    
    this.original_monument_block_positions.addAll(
      this.remaining_blocks.stream( )
        .map( ( block )->new Position( block.getX( ), block.getY( ), block.getZ( ) ) )
        .toList( ) );
  }
  
  @Override
  public
  void deactivate( )
  {
    if ( !active )
    {
      throw new IllegalStateException( "build monument objective not active?" );
    }
    
    assert this.remaining_blocks != null;
    
    this.unregister_event_handlers( );
    
    this.active = false;
    this.world = null;
    this.remaining_blocks.clear( );
  }
  
  /*——————————————————————————————————————————————————————————————————————————*/
  
  @NotNull
  public
  String name( )
  {
    return this.monument_name;
  }
  
  @NotNull
  public
  MiraTeamModel team( )
  {
    return this.monument_team;
  }
  
  @NotNull
  public
  Material material( )
  {
    return this.monument_material;
  }
  
  @NotNull
  public
  Region region( )
  {
    return this.monument_region;
  }
  
  @Override
  @NotNull
  public
  World world( )
  {
    assert this.world != null;
    
    return this.world;
  }
  
  @NotNull
  public
  List<Block> remaining_blocks( )
  {
    assert this.remaining_blocks != null;
    
    return this.remaining_blocks;
  }
  
  @NotNull
  public
  List<Position> original_block_positions( )
  {
    assert this.original_monument_block_positions != null;
    
    return this.original_monument_block_positions;
  }
  
  @NotNull
  public
  Map<UUID, Integer> player_contributions( )
  {
    assert this.player_contributions != null;
    
    return this.player_contributions;
  }
  
  /*——————————————————————————————————————————————————————————————————————————*/
  
  public
  void block_progress( )
  {
    if ( this.block_progress )
    {
      throw new IllegalStateException( "build monument objective already blocked?" );
    }
    
    this.block_progress = true;
    
    for ( Block block : this.remaining_blocks )
    {
      block.setType( Material.CRYING_OBSIDIAN );
    }
    
    this.blocked_monument_guard =
      new MiraBlockBreakGuard<>( this.pulse( ), this.remaining_blocks::contains );
  }
  
  public
  void allow_progress( )
  {
    if ( !this.block_progress )
    {
      throw new IllegalStateException( "build monument objective not blocked?" );
    }
    
    assert this.blocked_monument_guard != null;
    
    HandlerList.unregisterAll( this.blocked_monument_guard );
    
    this.blocked_monument_guard = null;
    this.block_progress = false;
    
    for ( Block block : this.remaining_blocks )
    {
      block.setType( this.monument_material );
    }
  }
  
  /*——————————————————————————————————————————————————————————————————————————*/
  
  protected
  void contribution( MiraPlayerModel<?> mira_player, Block block )
  {
    if ( !this.remaining_blocks.contains( block ) )
    {
      throw new IllegalArgumentException( "irrelevant block being contributed?" );
    }
    
    int contributions = this.player_contributions.getOrDefault( mira_player.uuid( ), 0 );
    
    this.player_contributions.put( mira_player.uuid( ), contributions + 1 );
    this.remaining_blocks.remove( block );
  }
}
