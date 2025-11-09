package sbs.mira.core.event.match.objective;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import sbs.mira.core.model.map.objective.MiraObjectiveFulfillable;

public
class MiraMatchObjectiveFulfilEvent
  extends Event
{
  private static final HandlerList handlers = new HandlerList( );
  
  @NotNull
  private final MiraObjectiveFulfillable objective;
  
  public
  MiraMatchObjectiveFulfilEvent( @NotNull MiraObjectiveFulfillable objective )
  {
    this.objective = objective;
  }
  
  @NotNull
  public final
  MiraObjectiveFulfillable objective( )
  {
    return this.objective;
  }
  
  public static
  HandlerList getHandlerList( )
  {
    return handlers;
  }
  
  @Override
  public @NotNull
  HandlerList getHandlers( )
  {
    return handlers;
  }
}
