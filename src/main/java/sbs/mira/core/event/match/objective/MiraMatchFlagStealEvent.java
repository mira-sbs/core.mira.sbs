package sbs.mira.core.event.match.objective;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import sbs.mira.core.model.MiraPlayerModel;
import sbs.mira.core.model.map.objective.standard.MiraObjectiveCapturableFlagBlock;

public
class MiraMatchFlagStealEvent
  extends Event
  implements Cancellable
{
  private static final @NotNull HandlerList handlers = new HandlerList( );
  private final @NotNull MiraObjectiveCapturableFlagBlock<?> flag;
  
  private boolean cancelled;
  
  private final @NotNull MiraPlayerModel<?> player;
  
  public
  MiraMatchFlagStealEvent(
    @NotNull MiraPlayerModel<?> player,
    @NotNull MiraObjectiveCapturableFlagBlock<?> flag )
  {
    this.cancelled = false;
    this.player = player;
    this.flag = flag;
  }
  
  public @NotNull
  MiraPlayerModel<?> player( )
  {
    return this.player;
  }
  
  public @NotNull
  MiraObjectiveCapturableFlagBlock<?> flag( )
  {
    return this.flag;
  }
  
  public static @NotNull
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
  
  @Override
  public
  boolean isCancelled( )
  {
    return cancelled;
  }
  
  @Override
  public
  void setCancelled( boolean cancelled )
  {
    this.cancelled = cancelled;
  }
}
