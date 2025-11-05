package sbs.mira.core.event.match;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * event that should fire when an active match transitions into to the post-game.
 * scenarios in which this event can occur are subjective - usually by the game mode.
 * this event cannot be cancelled - and should fire before the post-game occurs.
 * created on 2017-09-21.
 *
 * @author jj stephen.
 * @author jd rose.
 * @version 1.0.0
 * @see Event
 * @since 1.0.0
 */
public
class MiraMatchEndEvent
  extends Event
{
  
  private static final HandlerList handlers = new HandlerList( );
  
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
