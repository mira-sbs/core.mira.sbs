package sbs.mira.core.model.map.objective;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockPlaceEvent;
import org.jetbrains.annotations.NotNull;
import sbs.mira.core.MiraPulse;
import sbs.mira.core.event.handler.MiraBlockBreakGuard;
import sbs.mira.core.event.match.MiraMatchMonumentBuildEvent;
import sbs.mira.core.model.MiraEventHandlerModel;
import sbs.mira.core.model.MiraPlayerModel;
import sbs.mira.core.model.utility.Region;

import java.util.List;

public
class MiraObjectiveBuildMonument<Pulse extends MiraPulse<?, ?>>
  extends MiraObjectiveMonument<Pulse>
{
  public
  MiraObjectiveBuildMonument(
    @NotNull Pulse pulse,
    @NotNull String monument_name,
    @NotNull String build_team_label,
    @NotNull ChatColor build_team_color,
    @NotNull Material build_material,
    @NotNull Region build_region )
  {
    super( pulse, monument_name, build_team_label, build_team_color, build_material, build_region );
  }
  
  @Override
  public
  void activate( @NotNull World world )
  {
    super.activate( world );
    
    for ( Block block : this.remaining_blocks( ) )
    {
      block.setType( Material.AIR );
    }
    
    final MiraObjectiveBuildMonument<Pulse> self = this;
    
    this.event_handler( new MiraEventHandlerModel<BlockPlaceEvent, Pulse>( this.pulse( ) )
    {
      @Override
      public
      void handle_event( BlockPlaceEvent event )
      {
        Block block = event.getBlock( );
        
        if ( !remaining_blocks.contains( block ) )
        {
          return;
        }
        
        event.setCancelled( true );
        
        MiraPlayerModel<?> mira_player =
          this.pulse( ).model( ).player( event.getPlayer( ).getUniqueId( ) );
        
        if ( !mira_player.has_team( ) )
        {
          return;
        }
        
        if ( !self.monument_team_label.equals( mira_player.team( ).label( ) ) )
        {
          mira_player.messages( "you cannot build the enemy's monument!" );
          
          return;
        }
        
        if ( block.getType( ) != material( ) )
        {
          mira_player.messages( "you cannot build your team's monument with this block!" );
          
          return;
        }
        
        MiraMatchMonumentBuildEvent build_event =
          new MiraMatchMonumentBuildEvent( mira_player, self, block );
        
        this.call_event( build_event );
        
        if ( build_event.isCancelled( ) )
        {
          return;
        }
        
        self.contribution( mira_player, block );
        
        block.setType( self.monument_material );
      }
    } );
    
    this.event_handler( new MiraBlockBreakGuard<>(
      pulse( ),
      ( block )->blocks( ).contains( block ) ) );
  }
  
  @Override
  public
  void deactivate( )
  {
    super.deactivate( );
  }
  
  @Override
  public @NotNull
  String description( )
  {
    StringBuilder result = new StringBuilder( );
    result.append( '[' );
    result.append( this.team_color( ) );
    
    if ( this.blocks( ).size( ) > 16 )
    {
      // fixme: this lol.
      throw new IllegalStateException( "build monuments only support up to 16 blocks atm. soz." );
    }
    
    for ( Block block : this.blocks( ) )
    {
      result.append( this.remaining_blocks( ).contains( block ) ? '▓' : '█' );
    }
    
    result.append( ChatColor.RESET );
    result.append( ']' );
    
    return result.toString( );
  }
  
  public @NotNull
  List<Block> remaining_blocks( )
  {
    return this.remaining_blocks;
  }
}
