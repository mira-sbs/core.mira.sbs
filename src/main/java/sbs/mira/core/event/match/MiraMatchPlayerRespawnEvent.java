package sbs.mira.core.event.match;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import sbs.mira.core.model.MiraPlayerModel;

/**
 * event that should fire when a player respawns back into an active match.
 * this occurs after death and waiting out the respawn timer.
 * this event cannot be cancelled - and should fire before the respawn occurs.
 * created on 2017-04-20.
 *
 * @author jj stephen.
 * @version 1.0.1
 * @see Event
 * @since 1.0.0
 */
public
class MiraMatchPlayerRespawnEvent
  extends Event
{
  private static final HandlerList handlers = new HandlerList( );
  
  private final @NotNull MiraPlayerModel<?> player;
  
  public
  MiraMatchPlayerRespawnEvent( @NotNull MiraPlayerModel<?> player )
  {
    this.player = player;
  }
  
  public @NotNull
  MiraPlayerModel<?> player( )
  {
    return player;
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
