package sbs.mira.core.model.map.objective;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import sbs.mira.core.model.map.Position;
import sbs.mira.core.model.map.MiraObjective;


public
interface MiraBlockObjective
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
}
