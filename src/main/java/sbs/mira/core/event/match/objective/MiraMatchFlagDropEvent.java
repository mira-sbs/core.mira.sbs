package sbs.mira.core.event.match.objective;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import sbs.mira.core.model.MiraPlayerModel;
import sbs.mira.core.model.map.objective.standard.MiraObjectiveCapturableFlagBlock;

public
class MiraMatchFlagDropEvent
  extends Event
{
  private static final HandlerList handlers = new HandlerList( );
  
  @NotNull
  private final MiraPlayerModel<?> player;
  @NotNull
  private final MiraObjectiveCapturableFlagBlock<?> flag;
  
  public
  MiraMatchFlagDropEvent(
    @NotNull MiraPlayerModel<?> player,
    @NotNull MiraObjectiveCapturableFlagBlock<?> flag )
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
  MiraObjectiveCapturableFlagBlock<?> flag( )
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
