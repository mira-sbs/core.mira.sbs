package sbs.mira.core.model.map.objective;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import sbs.mira.core.model.utility.Position;


public
interface MiraObjectiveCapturableBlock
  extends MiraNamedObjective, MiraObjectiveCapturable
{
  @NotNull
  Material material( );
  
  @NotNull
  Position position( );
  
  @NotNull
  default
  Location location( @NotNull World world )
  {
    return this.position( ).location( world, true );
  }
  
  @NotNull
  default
  Block block( @NotNull World world )
  {
    return this.location( world ).getBlock( );
  }
  
  @NotNull
  default
  Block block( )
  {
    return this.location( this.world( ) ).getBlock( );
  }
}
