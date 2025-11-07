package sbs.mira.core.event.match;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import sbs.mira.core.model.MiraPlayerModel;
import sbs.mira.core.model.map.objective.MiraObjectiveCaptureFlag;

public
class MiraMatchFlagDropEvent
  extends Event
{
  private static final HandlerList handlers = new HandlerList( );
  
  @NotNull
  private final MiraPlayerModel<?> player;
  @NotNull
  private final MiraObjectiveCaptureFlag<?> flag;
  
  public
  MiraMatchFlagDropEvent(
    @NotNull MiraPlayerModel<?> player,
    @NotNull MiraObjectiveCaptureFlag<?> flag )
  {
    this.player = player;
    this.flag = flag;
  }
  
  @NotNull
  public
  MiraPlayerModel<?> player( )
  {
    return this.player;
  }
  
  @NotNull
  public
  MiraObjectiveCaptureFlag<?> flag( )
  {
    return this.flag;
  }
  
  public static
  HandlerList getHandlerList( )
  {
    return handlers;
  }
  
  @Override
  @NotNull
  public
  HandlerList getHandlers( )
  {
    return handlers;
  }
}
