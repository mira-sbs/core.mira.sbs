package sbs.mira.core.event.match.objective;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import sbs.mira.core.model.MiraPlayerModel;
import sbs.mira.core.model.map.objective.MiraObjectiveFulfillable;

public
class MiraMatchObjectiveFulfilEvent
  extends Event
{
  private static final HandlerList handlers = new HandlerList( );
  
  @NotNull
  private final MiraObjectiveFulfillable objective;
  @NotNull
  private final MiraPlayerModel<?> player;
  
  public
  MiraMatchObjectiveFulfilEvent(
    @NotNull MiraObjectiveFulfillable objective,
    @NotNull MiraPlayerModel<?> player )
  {
    this.objective = objective;
    this.player = player;
  }
  
  @NotNull
  public final
  MiraObjectiveFulfillable objective( )
  {
    return this.objective;
  }
  
  @NotNull
  public final
  MiraPlayerModel<?> player( )
  {
    return this.player;
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
