package sbs.mira.core;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * everything good in mira, built on top of JavaPlugin.
 * created on 2017-03-20.
 *
 * @author jj.mira.sbs
 * @version 1.0.1
 * @see JavaPlugin
 * @since 1.0.0
 */
public abstract
class MiraPlugin<Pulse extends MiraPulse<?, ?>>
  extends JavaPlugin
  implements Breather<Pulse>
{
  private final @NotNull Pulse pulse;
  
  public
  MiraPlugin( @NotNull Pulse pulse )
  {
    super( );
    
    this.pulse = pulse;
  }
  
  
  @Override
  @NotNull
  public
  Pulse pulse( )
  throws MiraPulse.FlatlineException
  {
    return pulse;
    
  }
  
  protected
  String description( )
  {
    PluginDescriptionFile description = this.getDescription( );
    return "%s v%s".formatted( description.getName( ), description.getVersion( ) );
  }
  
  /**
   * log an informational message to the jvm console.
   *
   * @param message yap.
   * @see Logger
   */
  public
  void log( String message )
  {
    this.getLogger( ).log( Level.INFO, "[may i reflect again?] " + message );
  }
}
