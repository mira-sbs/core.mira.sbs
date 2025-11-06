package sbs.mira.core.event.handler;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.block.BlockPlaceEvent;
import org.jetbrains.annotations.NotNull;
import sbs.mira.core.MiraPulse;
import sbs.mira.core.model.MiraEventHandlerModel;

public
class MiraPlateauBuildingGuard<Pulse extends MiraPulse<?, ?>>
  extends MiraEventHandlerModel<BlockPlaceEvent, Pulse>
{
  private final int plateau_y;
  
  public
  MiraPlateauBuildingGuard( @NotNull Pulse pulse, int plateau_y )
  {
    super( pulse );
    
    this.plateau_y = plateau_y;
  }
  
  @Override
  public
  void handle_event( BlockPlaceEvent event )
  {
    Location location_block = event.getBlock( ).getLocation( ).clone( );
    
    if ( location_block.getBlockY( ) < this.plateau_y )
    {
      event.setCancelled( true );
      return;
    }
    
    location_block.setY( plateau_y );
    
    if ( location_block.getBlock( ).getType( ) != Material.BEDROCK )
    {
      event.setCancelled( true );
      
      //fixme: messages!
      //main.warn( event.getPlayer( ), main.message( "guard.border" ) );
    }
  }
}
