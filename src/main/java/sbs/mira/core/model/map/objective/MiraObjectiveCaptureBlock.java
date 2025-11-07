package sbs.mira.core.model.map.objective;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import sbs.mira.core.model.map.MiraObjective;
import sbs.mira.core.model.utility.Position;


public
interface MiraObjectiveCaptureBlock
  extends MiraObjective
{
  @NotNull
  Material material( );
  
  @NotNull
  Position position( );
  
  default @NotNull
  Location location( @NotNull World world )
  {
    return this.position( ).location( world, true );
  }
  
  default @NotNull
  Block block( @NotNull World world )
  {
    return this.location( world ).getBlock( );
  }
  
  default @NotNull
  Block block( )
  {
    return this.location( this.world( ) ).getBlock( );
  }
}
