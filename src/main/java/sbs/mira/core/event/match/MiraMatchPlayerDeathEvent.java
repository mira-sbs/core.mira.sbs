package sbs.mira.core.event.match;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sbs.mira.pvp.MiraVersePlayer;

/**
 * event that should fire when a player dies during an active match.
 * the death event may or may not involve a killing player - the killer.
 * this event cannot be cancelled - and should fire prior to the respawn logic.
 * created on 2017-09-21.
 *
 * @author jj stephen.
 * @author jd rose.
 * @version 1.0.1
 * @see Event
 * @since 1.0.0
 */
public
class MiraMatchPlayerDeathEvent
  extends Event
{
  private static final HandlerList handlers = new HandlerList( );
  
  private final @NotNull MiraVersePlayer killed;
  private final @Nullable MiraVersePlayer killer;
  
  public
  MiraMatchPlayerDeathEvent( @NotNull MiraVersePlayer killed, @Nullable MiraVersePlayer killer )
  {
    this.killed = killed;
    this.killer = killer;
  }
  
  /**
   * @return the player who died.
   */
  public @NotNull
  MiraVersePlayer killed( )
  {
    return killed;
  }
  
  /**
   * @return the killing player (where applicable).
   */
  public @Nullable
  MiraVersePlayer killer( )
  {
    return killer;
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
