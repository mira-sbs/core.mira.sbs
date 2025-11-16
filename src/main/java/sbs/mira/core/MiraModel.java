package sbs.mira.core;

import org.bukkit.Server;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import sbs.mira.core.model.MiraEventHandlerModel;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * a knower and doer of things within the mira framework.
 * has a pulse and is therefore alive.
 * created on 2017-03-20.
 *
 * @author jj stephen
 * @version 1.0.1
 * @since 1.0.0
 */
public abstract
class MiraModel<Pulse extends MiraPulse<?, ?>>
{
  @NotNull
  private final List<MiraEventHandlerModel<?, ?>> event_handlers;
  @NotNull
  private final Pulse pulse;
  
  /**
   * instantiates a polymorphic mira model.
   *
   * @param pulse reference to mira.
   */
  protected
  MiraModel( @NotNull Pulse pulse )
  {
    this.pulse = pulse;
    this.event_handlers = new ArrayList<>( );
  }
  
  /**
   * @return reference to mira.
   */
  @NotNull
  public
  Pulse pulse( )
  {
    return this.pulse;
  }
  
  /**
   * log an informational message to the jvm console via the plugin's logger.
   *
   * @param message yap to output in the console to the poor sysadmin.
   * @param level   the logging level for this message.
   */
  public
  void log( @NotNull String message, @NotNull Level level )
  {
    this.pulse.plugin( ).getLogger( ).log( level, "[mira?] %s".formatted( message ) );
  }
  
  /**
   * log an informational message to the jvm console via the plugin's logger.
   * overloaded method that automatically provides {@code Level.INFO} as the
   * logging level.
   *
   * @param message yap to output in the console to the poor sysadmin.
   */
  public
  void log( @NotNull String message )
  {
    this.log( message, Level.INFO );
  }
  
  /**
   * @return shorthand / quick reference to the server interface.
   */
  @NotNull
  public
  Server server( )
  {
    return this.pulse.plugin( ).getServer( );
  }
  
  /**
   * registers an event handler model - which is held at this level in memory.
   *
   * @param event_handler the event handler to unregister later.
   */
  public
  void event_handler( MiraEventHandlerModel<?, ?> event_handler )
  {
    this.event_handlers.add( event_handler );
  }
  
  /**
   * unregisters all registered event handlers done using `event_handler( ...) `.
   */
  public
  void unregister_event_handlers( )
  {
    for ( Listener listener : event_handlers )
    {
      HandlerList.unregisterAll( listener );
    }
  }
  
  /**
   * shorthand / quick reference to call a bukkit event.
   *
   * @param event the bukkit event to call (via this plugin).
   */
  public
  void call_event( @NotNull Event event )
  {
    this.pulse.plugin( ).getServer( ).getPluginManager( ).callEvent( event );
  }
}
