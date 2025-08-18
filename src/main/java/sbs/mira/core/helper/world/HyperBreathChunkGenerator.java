package sbs.mira.core.helper.world;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * code shamelessly inspired by NullTerrain.
 * mira takes no credit for this ingenious piece of code.
 * created on 2025-08-19.
 *
 * @author <a href="https://github.com/Elizacat/">Elizacat</a>
 * @version 1.0.0
 * @since 1.0.0
 */

public
class HyperBreathChunkGenerator
  extends ChunkGenerator
{
  
  public
  byte[] generate( World world, Random random, int cx, int cz )
  {
    return new byte[ 65536 ];
  }
  
  @Override
  public
  Location getFixedSpawnLocation( @NotNull World world, @NotNull Random random )
  {
    return new Location( world, 0, 64, 0 );
  }
  
  public
  ChunkGenerator getDefaultWorldGenerator( String worldName, String id )
  {
    return new HyperBreathChunkGenerator( );
  }
}