package sbs.mira.core.model.map;

import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * extended position model that offers two dimensions of randomness for a given position.
 * in this case, a radius on both horizontal axes extend the (now central) point in all directions.
 * when requesting a location translated from a position within this cluster,
 * the position will be randomly determined within this radial region.
 * final y coordinate is not randomised - therefore the spawn region is on a horizontal plane.
 * created on 2017-03-27.
 *
 * @author jj stephen.
 * @version 1.0.1
 * @since 1.0.0
 */
public final
class PositionPlane
  extends Position
{
  private final @NotNull Random rng;
  private final int radius_x;
  private final int radius_z;
  
  /**
   * @param x        central x coordinate.
   * @param y        central y coordinate.
   * @param z        central z coordinate.
   * @param yaw      entity up-down / "nodding" head rotation - -90 to 90 degrees.
   * @param pitch    entity left-right / "shaking" head rotation - 360 degrees.
   * @param radius_x radius on the x-axis.
   * @param radius_z radius on the z-axis.
   * @param rng      random number generator - for generating a position within the radial cluster.
   */
  public
  PositionPlane(
    double x,
    double y,
    double z,
    float yaw,
    float pitch,
    int radius_x,
    int radius_z,
    @NotNull Random rng )
  {
    super( x, y, z, yaw, pitch );
    
    this.radius_x = radius_x;
    this.radius_z = radius_z;
    this.rng = rng;
  }
  
  /**
   * shorthand constructor that zeroes the `yaw` and `pitch` automatically.
   *
   * @param x        central x coordinate.
   * @param y        central y coordinate.
   * @param z        central z coordinate.
   * @param radius_x radius on the x-axis.
   * @param radius_z radius on the z-axis.
   * @param rng      random number generator - for generating a position within the radial cluster.
   */
  public
  PositionPlane( double x, double y, double z, int radius_x, int radius_z, @NotNull Random rng )
  {
    this( x, y, z, 0, 0, radius_x, radius_z, rng );
  }
  
  /**
   * a position with a random positive/negative delta value on the x and z axes is determined.
   * this is then converted into a bukkit location and given as the result instead.
   * the random values generated are constrained by the given radius values.
   *
   * @param world     the world that the position should be put in - to form a location.
   * @param pitch_yaw true - if yaw and pitch should be given their non-zero values.
   * @return the final position converted into bukkit location - within the provided world.
   */
  @Override
  public @NotNull
  Location location( @NotNull World world, boolean pitch_yaw )
  {
    double delta_x = rng.nextInt( this.radius_x + 1 ) * ( rng.nextBoolean( ) ? 1 : -1 );
    double delta_z = rng.nextInt( this.radius_z + 1 ) * ( rng.nextBoolean( ) ? 1 : -1 );
    
    if ( pitch_yaw )
    {
      return new Location(
        world,
        this.x( ) + delta_x,
        this.y( ),
        this.z( ) + delta_z,
        yaw( ),
        this.pitch( ) );
    }
    return new Location( world, this.x( ) + delta_x, this.y( ), this.z( ) + delta_z );
  }
  
}
