package sbs.mira.core.module;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import sbs.mira.core.MiraModule;
import sbs.mira.core.MiraPulse;

/***
 * created on 2025-08-18.
 *
 * @author jj.mira.sbs
 * @author jd.mira.sbs
 * @see MiraPulse
 */
public abstract
class MiraEventModule<Event extends org.bukkit.event.Event, Pulse extends MiraPulse<?, ?>>
  extends MiraModule<Pulse>
  implements Listener
{
  /**
   *
   * @param pulse
   */
  protected
  MiraEventModule(@NotNull Pulse pulse)
  {
    super(pulse);
  }
  
  /**
   *
   * @param event
   */
  @EventHandler
  public abstract
  void occurs(Event event);
}
