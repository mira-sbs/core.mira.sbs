package sbs.mira.core.model;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import sbs.mira.core.MiraModel;
import sbs.mira.core.MiraPulse;

/**
 * generic helper model that allows for easier registration of & functional
 * programming with: bukkit events. mira intends to modularise listeners down
 * to simple predicates - for the sake of expression and longevity.
 * created on 2025-08-18.
 *
 * @author jj stephen
 * @author jd rose
 * @see MiraPulse
 */
public abstract
class MiraEventHandlerModel<Event extends org.bukkit.event.Event, Pulse extends MiraPulse<?, ?>>
  extends MiraModel<Pulse>
  implements Listener
{
  /**
   * instantiates a polymorphic event handler model.
   *
   * @param pulse reference to mira.
   */
  protected
  MiraEventHandlerModel( @NotNull Pulse pulse )
  {
    super( pulse );
    
    this.server( ).getPluginManager( ).registerEvents( this, this.pulse( ).plugin( ) );
  }
  
  /**
   * procedure called to handle the bukkit event.
   * the specific event is implementation detail.
   *
   * @param event the event to be handled in implementation.
   */
  public abstract
  void handle_event( Event event );
}
