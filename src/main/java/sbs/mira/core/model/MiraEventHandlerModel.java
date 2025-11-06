package sbs.mira.core.model;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import sbs.mira.core.MiraModel;
import sbs.mira.core.MiraPulse;

/***
 * created on 2025-08-18.
 *
 * @author jj.mira.sbs
 * @author jd.mira.sbs
 * @see MiraPulse
 */
public abstract
class MiraEventHandlerModel<Event extends org.bukkit.event.Event, Pulse extends MiraPulse<?, ?>>
  extends MiraModel<Pulse>
  implements Listener
{
  /**
   *
   * @param pulse
   */
  protected
  MiraEventHandlerModel( @NotNull Pulse pulse )
  {
    super( pulse );
    
    this.server( ).getPluginManager( ).registerEvents( this, this.pulse( ).plugin( ) );
  }
  
  /**
   *
   * @param event
   */
  @EventHandler
  public abstract
  void handle_event( Event event );
}
