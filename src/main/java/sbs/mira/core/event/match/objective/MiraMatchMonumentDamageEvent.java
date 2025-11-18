package sbs.mira.core.event.match.objective;

import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import sbs.mira.core.model.MiraPlayerModel;
import sbs.mira.core.model.map.objective.standard.MiraObjectiveDestroyMonument;

public
class MiraMatchMonumentDamageEvent
  extends Event
{
  
  private static final HandlerList handlers = new HandlerList( );
  
  @NotNull
  private final MiraPlayerModel<?> player;
  @NotNull
  private final MiraObjectiveDestroyMonument<?> monument;
  private final boolean was_pristine;
  @NotNull
  private final Block block;
  
  public
  MiraMatchMonumentDamageEvent(
    @NotNull MiraPlayerModel<?> player,
    @NotNull MiraObjectiveDestroyMonument<?> monument_captured,
    boolean was_pristine,
    @NotNull Block block )
  {
    this.player = player;
    this.monument = monument_captured;
    this.was_pristine = was_pristine;
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
  
  public
  boolean was_pristine()
  {
    return was_pristine;
  }
  
  @NotNull
  public
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
