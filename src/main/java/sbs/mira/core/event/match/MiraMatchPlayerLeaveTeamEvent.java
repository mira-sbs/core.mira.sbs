package sbs.mira.core.event.match;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import sbs.mira.core.model.MiraPlayerModel;
import sbs.mira.core.model.map.MiraTeamModel;

/**
 * event that should fire when a player leaves an active match.
 * this can occur when a player leaves the minecraft server,
 * or during the middle of a match - voluntarily - to resume spectating.
 * this event cannot be cancelled - and should fire after the transfer occurs.
 * created on 2017-09-21.
 *
 * @author jj stephen.
 * @version 1.0.1
 * @see Event
 * @since 1.0.0
 */
public
class MiraMatchPlayerLeaveTeamEvent
  extends Event
{
  private static final HandlerList handlers = new HandlerList( );
  
  private final @NotNull MiraPlayerModel<?> player;
  private final @NotNull MiraTeamModel team;
  
  public
  MiraMatchPlayerLeaveTeamEvent( @NotNull MiraPlayerModel<?> player, @NotNull MiraTeamModel team )
  {
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
  public
  HandlerList getHandlers( )
  {
    return handlers;
  }
}
