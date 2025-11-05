package sbs.mira.core.model.map;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public
interface MiraObjective
{
  /**
   * activation must cause the objective to become active.
   * observation was the only thing permitted previously.
   * interaction with the objective should now be enabled.
   */
  void activate( @NotNull World world );
  
  void deactivate( );
}
