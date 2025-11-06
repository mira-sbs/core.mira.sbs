package sbs.mira.core.event.match;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import sbs.mira.core.model.MiraPlayerModel;
import sbs.mira.core.model.map.MiraTeamModel;

/**
 * event that should fire when a player joins a team during an active match.
 * this can occur at the start of a match (automatically for queued players),
 * or during the middle of a match if a player joins late / leaves to spectate.
 * this event can be cancelled if the player should not be allowed to join.
 * therefore, this event should fire prior to the team assignment logic (for cancelling).
 * created on 2017-09-21.
 *
 * @author jj stephen.
 * @author jd rose.
 * @version 1.0.1
 * @see Event
 * @since 1.0.0
 */
public
class MiraMatchPlayerJoinTeamEvent
  extends Event
  implements Cancellable
{
  
  private static final HandlerList handlers = new HandlerList( );
  
  private boolean cancelled;
  
  private final @NotNull MiraPlayerModel<?> player;
  private final @NotNull MiraTeamModel team;
  
  public
  MiraMatchPlayerJoinTeamEvent( @NotNull MiraPlayerModel<?> player, @NotNull MiraTeamModel team )
  {
    this.cancelled = false;
    this.player = player;
    this.team = team;
  }
  
  public @NotNull
  MiraPlayerModel<?> player( )
  {
    return player;
  }
  
  public @NotNull
  MiraTeamModel team( )
  {
    return team;
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
