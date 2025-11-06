package sbs.mira.core.model.utility;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

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
}
