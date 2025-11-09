package sbs.mira.core;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * default constructor for framework compliance or reflective usage.
 * everything good in {@code mira} starts here — build with care. 🫧
 * created on 2017-03-20.
 *
 * @author jj stephen
 * @version 1.0.1
 * @see JavaPlugin
 * @since 1.0.0
 */
public abstract
class MiraPlugin<Pulse extends MiraPulse<?, ?>>
  extends JavaPlugin
{
  @NotNull
  private final Pulse pulse;
  
  public
  MiraPlugin( @NotNull Pulse pulse )
  {
    super( );
    
    this.pulse = pulse;
  }
  
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
}
