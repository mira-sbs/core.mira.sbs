package sbs.mira.core;

import org.bukkit.craftbukkit.v1_21_R6.CraftServer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import sbs.mira.core.model.MiraEventHandlerModel;

import java.util.ArrayList;
import java.util.List;

/**
 * a moving cog within the mira framework.
 * slide it into place (as needed) to turn neighbouring gears.
 * created on 2017-03-20.
 *
 * @author jj.mira.sbs
 * @version 1.0.1
 * @since 1.0.0
 */
public abstract
class MiraModel<Pulse extends MiraPulse<?, ?>>
{
  private final @NotNull List<MiraEventHandlerModel<?, ?>> event_handlers;
  private final @NotNull Pulse pulse;
  
  protected
  MiraModel( @NotNull Pulse pulse )
  {
    this.pulse = pulse;
    this.event_handlers = new ArrayList<>( );
  }
  
  /**
   * don't get lost.
   *
   * @return stay with us.
   */
  @NotNull
  public
  Pulse pulse( )
  {
    return this.pulse;
  }
  
  public @NotNull
  CraftServer server( )
  {
    return ( CraftServer ) this.pulse.plugin( ).getServer( );
  }
  
  public
  void event_handler( MiraEventHandlerModel<?, ?> event_handler )
  {
    this.event_handlers.add( event_handler );
  }
  
  public
  void unregister_event_handlers( )
  {
    for ( Listener listener : event_handlers )
    {
      HandlerList.unregisterAll( listener );
    }
  }
  
  public
  void call_event( @NotNull Event event )
  {
    this.pulse.plugin( ).getServer( ).getPluginManager( ).callEvent( event );
  }
}
