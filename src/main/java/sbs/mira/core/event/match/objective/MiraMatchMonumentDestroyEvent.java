package sbs.mira.core.event.match.objective;

import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import sbs.mira.core.model.MiraPlayerModel;
import sbs.mira.core.model.map.objective.standard.MiraObjectiveDestroyMonument;

public
class MiraMatchMonumentDestroyEvent
  extends Event
{
  
  private static final HandlerList handlers = new HandlerList( );
  
  private final @NotNull MiraPlayerModel<?> player;
  private final @NotNull MiraObjectiveDestroyMonument<?> monument;
  private final @NotNull Block block;
  
  public
  MiraMatchMonumentDestroyEvent(
    @NotNull MiraPlayerModel<?> player,
    @NotNull MiraObjectiveDestroyMonument<?> monument_captured,
    @NotNull Block block )
  {
    this.player = player;
    this.monument = monument_captured;
    this.block = block;
  }
  
  @NotNull
  public
  MiraPlayerModel<?> player( )
  {
    return this.player;
  }
  
  @NotNull
  public
  MiraObjectiveDestroyMonument<?> monument( )
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
  @NotNull
  public
  HandlerList getHandlers( )
  {
    return handlers;
  }
}
