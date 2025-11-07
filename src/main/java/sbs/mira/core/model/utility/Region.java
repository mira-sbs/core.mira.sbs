package sbs.mira.core.model.utility;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public
class Region
{
  private final @NotNull Position position_1;
  private final @NotNull Position position_2;
  
  public
  Region( @NotNull Position position1, @NotNull Position position2 )
  {
    this.position_1 = position1;
    this.position_2 = position2;
  }
  
  public
  boolean within( @NotNull Position position )
  {
    return
      position.x( ) >= this.position_1.x( ) &&
      position.y( ) >= this.position_1.y( ) &&
      position.z( ) >= this.position_1.z( ) &&
      position.x( ) <= this.position_2.x( ) &&
      position.y( ) <= this.position_2.y( ) &&
      position.z( ) <= this.position_2.z( );
  }
  
  public
  boolean within( @NotNull Location location )
  {
    return
      location.getBlockX( ) >= this.position_1.x( ) &&
      location.getBlockY( ) >= this.position_1.y( ) &&
      location.getBlockZ( ) >= this.position_1.z( ) &&
      location.getBlockX( ) <= this.position_2.x( ) &&
      location.getBlockY( ) <= this.position_2.y( ) &&
      location.getBlockZ( ) <= this.position_2.z( );
  }
  
  public @NotNull
  List<Block> blocks_matching( @NotNull World world, @NotNull Predicate<Block> block_predicate )
  {
    List<Block> result = new LinkedList<>( );
    
    for (
      int block_x = ( int ) this.position_1.x( );
      block_x <= this.position_2.x( );
      block_x++
    )
    {
      for (
        int block_y = ( int ) this.position_1.y( );
        block_y <= this.position_2.y( );
        block_y++
      )
      {
        for (
          int block_z = ( int ) this.position_1.z( );
          block_z <= this.position_2.z( );
          block_z++
        )
        {
          Block block = world.getBlockAt( block_x, block_y, block_z );
          
          if ( block_predicate.test( block ) )
          {
            result.add( block );
          }
        }
      }
    }
    
    return result;
  }
}
