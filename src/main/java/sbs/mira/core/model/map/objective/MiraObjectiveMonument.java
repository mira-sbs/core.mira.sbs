package sbs.mira.core.model.map.objective;

import org.bukkit.ChatColor;
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
import sbs.mira.core.model.map.MiraObjective;
import sbs.mira.core.model.map.MiraTeamModel;
import sbs.mira.core.model.utility.Region;

import java.util.*;

public abstract
class MiraObjectiveMonument<Pulse extends MiraPulse<?, ?>>
  extends MiraModel<Pulse>
  implements MiraObjective
{
  protected final @NotNull MiraTeamModel monument_team;
  protected final @NotNull Material monument_material;
  protected final @NotNull Region monument_region;
  
  private boolean active;
  private boolean block_progress;
  private @Nullable World world;
  
  private @Nullable List<Block> monument_blocks;
  private int original_monument_blocks_count;
  
  private @Nullable MiraBlockBreakGuard<Pulse> blocked_monument_guard;
  protected final @NotNull List<Block> remaining_blocks;
  protected final @NotNull Map<UUID, Integer> player_contributions;
  
  public
  MiraObjectiveMonument(
    @NotNull Pulse pulse,
    @NotNull MiraTeamModel monument_team,
    @NotNull Material build_material,
    @NotNull Region build_region )
  {
    super( pulse );
    
    this.monument_team = monument_team;
    this.monument_material = build_material;
    this.monument_region = build_region;
    
    this.active = false;
    this.block_progress = false;
    this.world = null;
    
    this.monument_blocks = null;
    this.original_monument_blocks_count = -1;
    
    this.blocked_monument_guard = null;
    this.remaining_blocks = new LinkedList<>( this.blocks( ) );
    this.player_contributions = new HashMap<>( );
  }
  
  /*——————————————————————————————————————————————————————————————————————————*/
  
  @Override
  public
  void activate( @NotNull World world )
  {
    if ( active )
    {
      throw new IllegalStateException( "build monument objective already active?" );
    }
    
    this.active = true;
    this.world = world;
    this.monument_blocks =
      this.region( ).blocks_matching( this.world, ( block )->block.getType( ) == Material.TARGET );
    this.original_monument_blocks_count = this.monument_blocks.size( );
    this.remaining_blocks.addAll( this.monument_blocks );
  }
  
  @Override
  public
  void deactivate( )
  {
    if ( !active )
    {
      throw new IllegalStateException( "build monument objective not active?" );
    }
    
    assert this.monument_blocks != null;
    
    this.unregister_event_handlers( );
    
    this.active = false;
    this.world = null;
    this.monument_blocks.clear( );
    this.monument_blocks = null;
    this.remaining_blocks.clear( );
  }
  
  /*——————————————————————————————————————————————————————————————————————————*/
  
  public @NotNull
  String team_label( )
  {
    return this.monument_team.label( );
  }
  
  public @NotNull
  ChatColor team_color( )
  {
    return this.monument_team.color( );
  }
  
  public @NotNull
  Material material( )
  {
    return this.monument_material;
  }
  
  public @NotNull
  Region region( )
  {
    return this.monument_region;
  }
  
  @Override
  public @NotNull
  World world( )
  {
    assert this.world != null;
    
    return this.world;
  }
  
  public @NotNull
  List<Block> blocks( )
  {
    assert this.monument_blocks != null;
    
    return this.monument_blocks;
  }
  
  public @NotNull
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
