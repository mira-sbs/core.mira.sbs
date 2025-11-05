package sbs.mira.core;

import org.bukkit.craftbukkit.v1_21_R6.CraftServer;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

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
  implements Breather<Pulse>
{
  
  private final @NotNull Pulse pulse;
  
  protected
  MiraModel( @NotNull Pulse pulse )
  {
    this.pulse = pulse;
  }
  
  /**
   * don't get lost.
   *
   * @return stay with us.
   */
  @Override
  @NotNull
  public
  Pulse pulse( )
  {
    return this.pulse;
  }
  
  public @NotNull
  MiraPlugin<?> plugin( )
  {
    return this.pulse.plugin( );
  }
  
  public @NotNull
  MiraModel<?> model( )
  {
    return this.pulse.model( );
  }
  
  public @NotNull
  CraftServer server( )
  {
    return ( CraftServer ) this.pulse.plugin( ).getServer( );
  }
  
  public
  void call_event( @NotNull Event event )
  {
    this.pulse.plugin( ).getServer( ).getPluginManager( ).callEvent( event );
  }
}
