package sbs.mira.core.model.map.objective.standard;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockPlaceEvent;
import org.jetbrains.annotations.NotNull;
import sbs.mira.core.MiraPulse;
import sbs.mira.core.event.handler.MiraBlockBreakGuard;
import sbs.mira.core.event.match.objective.MiraMatchMonumentBuildEvent;
import sbs.mira.core.model.MiraEventHandlerModel;
import sbs.mira.core.model.MiraPlayerModel;
import sbs.mira.core.model.map.MiraTeamModel;
import sbs.mira.core.model.map.objective.MiraObjectiveCapturableBlockRegion;
import sbs.mira.core.model.utility.Region;

public
class MiraObjectiveBuildMonument<Pulse extends MiraPulse<?, ?>>
  extends MiraObjectiveMonument<Pulse>
  implements MiraObjectiveCapturableBlockRegion
{
  public
  MiraObjectiveBuildMonument(
    @NotNull Pulse pulse,
    @NotNull String monument_name,
    @NotNull MiraTeamModel build_team,
    @NotNull Material build_material,
    @NotNull Region build_region )
  {
    super( pulse, monument_name, build_team, build_material, build_region );
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
        
        if ( !self.monument_team.equals( mira_player.team( ) ) )
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
      ( block )->remaining_blocks( ).contains( block ) ) );
  }
  
  @Override
  public
  void deactivate( )
  {
    super.deactivate( );
  }
  
  @Override
  @NotNull
  public
  String description( )
  {
    return "[" +
           this.monument_team.color( ) +
           this.progress_bar( ) +
           ChatColor.RESET +
           ']' +
           " (%.2f%%)".formatted( this.current_progress( ) );
  }
}
