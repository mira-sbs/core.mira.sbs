package sbs.mira.core.event.match;

import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import sbs.mira.core.model.MiraPlayerModel;
import sbs.mira.core.model.map.objective.MiraObjectiveBuildMonument;
import sbs.mira.core.model.map.objective.MiraObjectiveCaptureFlag;

public
class MiraMatchFlagCaptureEvent
  extends Event
  implements Cancellable
{
  
  private static final HandlerList handlers = new HandlerList( );
  
  private boolean cancelled;
  
  private final @NotNull MiraPlayerModel<?> player;
  private final @NotNull MiraObjectiveCaptureFlag<?> flag;
  private final @NotNull MiraObjectiveBuildMonument<?> monument;
  private final @NotNull Block block_captured;
  
  public
  MiraMatchFlagCaptureEvent(
    @NotNull MiraPlayerModel<?> player,
    @NotNull MiraObjectiveCaptureFlag<?> flag_captured,
    @NotNull MiraObjectiveBuildMonument<?> monument_captured,
    @NotNull Block block_captured )
  {
    this.cancelled = false;
    this.player = player;
    this.flag = flag_captured;
    this.monument = monument_captured;
    this.block_captured = block_captured;
  }
  
  public @NotNull
  MiraPlayerModel<?> player( )
  {
    return this.player;
  }
  
  public @NotNull
  MiraObjectiveCaptureFlag<?> flag_captured( )
  {
    return this.flag;
  }
  
  public @NotNull
  MiraObjectiveBuildMonument<?> monument( )
  {
    return this.monument;
  }
  
  public @NotNull
  Block block_captured( )
  {
    return this.block_captured;
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
