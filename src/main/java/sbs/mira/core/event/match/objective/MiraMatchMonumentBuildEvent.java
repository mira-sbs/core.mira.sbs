package sbs.mira.core.event.match.objective;

import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import sbs.mira.core.model.MiraPlayerModel;
import sbs.mira.core.model.map.objective.standard.MiraObjectiveBuildMonument;

public
class MiraMatchMonumentBuildEvent
  extends Event
  implements Cancellable
{
  
  private static final HandlerList handlers = new HandlerList( );
  
  private boolean cancelled;
  
  private final @NotNull MiraPlayerModel<?> player;
  private final @NotNull MiraObjectiveBuildMonument<?> monument;
  private final @NotNull Block block;
  
  public
  MiraMatchMonumentBuildEvent(
    @NotNull MiraPlayerModel<?> player,
    @NotNull MiraObjectiveBuildMonument<?> monument_captured,
    @NotNull Block block )
  {
    this.cancelled = false;
    this.player = player;
    this.monument = monument_captured;
    this.block = block;
  }
  
  public @NotNull
  MiraPlayerModel<?> player( )
  {
    return this.player;
  }
  
  public @NotNull
  MiraObjectiveBuildMonument<?> monument( )
  {
    return this.monument;
  }
  
  public @NotNull
  Block block( )
  {
    return this.block;
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
