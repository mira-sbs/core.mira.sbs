package sbs.mira.core.model.map.objective.standard;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;
import sbs.mira.core.MiraPulse;
import sbs.mira.core.event.match.objective.MiraMatchMonumentDestroyEvent;
import sbs.mira.core.model.MiraEventHandlerModel;
import sbs.mira.core.model.MiraPlayerModel;
import sbs.mira.core.model.map.MiraTeamModel;
import sbs.mira.core.model.map.objective.MiraObjectiveCapturableBlockRegion;
import sbs.mira.core.model.utility.Region;

public
class MiraObjectiveDestroyMonument<Pulse extends MiraPulse<?, ?>>
  extends MiraObjectiveMonument<Pulse>
  implements MiraObjectiveCapturableBlockRegion
{
  public
  MiraObjectiveDestroyMonument(
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
      block.setType( this.monument_material );
    }
    
    final MiraObjectiveDestroyMonument<Pulse> self = this;
    
    this.event_handler( new MiraEventHandlerModel<BlockBreakEvent, Pulse>( this.pulse( ) )
    {
      @Override
      @EventHandler (priority = EventPriority.MONITOR)
      public
      void handle_event( BlockBreakEvent event )
      {
        if ( event.isCancelled( ) )
        {
          return;
        }
        
        Block block = event.getBlock( );
        
        if ( !remaining_blocks.contains( block ) )
        {
          return;
        }
        
        MiraPlayerModel<?> mira_player =
          this.pulse( ).model( ).player( event.getPlayer( ).getUniqueId( ) );
        
        if ( !mira_player.has_team( ) )
        {
          return;
        }
        
        if ( self.monument_team.equals( mira_player.team( ) ) )
        {
          mira_player.messages( "this is your own monument — protect it!" );
          
          return;
        }
        
        this.call_event( new MiraMatchMonumentDestroyEvent( mira_player, self, block ) );
        
        self.contribution( mira_player, block );
        
        // todo: add noise feedback or something?
      }
    } );
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
           " (%.0f%%)".formatted( this.current_progress( ) );
  }
}
