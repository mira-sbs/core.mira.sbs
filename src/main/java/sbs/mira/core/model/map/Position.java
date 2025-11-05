package sbs.mira.core.model.map;

import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

/**
 * immutable position within a known+transient minecraft world.
 * are often spawn point positions or other key positions for objectives / regions.
 * offers x+y+z+yaw+pitch data storage.
 * created on 2017-03-27.
 *
 * @author jj.mira.sbs
 * @version 1.0.1
 * @since 1.0.0
 */
public sealed
class Position
  permits PositionPlane
{
  private final double x;
  private final double y;
  private final double z;
  private final float yaw;
  private final float pitch;
  
  /**
   * @param x     entity x coordinate.
   * @param y     entity y coordinate.
   * @param z     entity z coordinate.
   * @param yaw   entity up-down / "nodding" head rotation - -90 to 90 degrees.
   * @param pitch entity left-right / "shaking" head rotation - 360 degrees.
   */
  public
  Position(
    double x,
    double y,
    double z,
    float yaw,
    float pitch )
  {
    this.x = x;
    this.y = y;
    this.z = z;
    this.yaw = yaw;
    this.pitch = pitch;
  }
  
  /**
   * shorthand constructor that zeroes the `yaw` and `pitch` automatically.
   *
   * @param x entity x coordinate.
   * @param y entity y coordinate.
   * @param z entity z coordinate.
   */
  public
  Position(
    double x,
    double y,
    double z )
  {
    this( x, y, z, 0, 0 );
  }
  
  /**
   * @param world     the world that the position should be put in - to form a location.
   * @param pitch_yaw true - if yaw and pitch should be given their non-zero values.
   * @return the position converted into bukkit location - within the provided world.
   */
  public @NotNull
  Location location(
    @NotNull World world,
    boolean pitch_yaw )
  {
    if ( pitch_yaw )
    {
      return new Location( world, x, y, z, yaw, this.pitch );
    }
    
    return new Location( world, x, y, z );
  }
  
  public
  double x( )
  {
    return x;
  }
  
  public
  double y( )
  {
    return y;
  }
  
  public
  double z( )
  {
    return z;
  }
  
  public
  float yaw( )
  {
    return yaw;
  }
  
  public
  float pitch( )
  {
    return pitch;
  }
  
}
