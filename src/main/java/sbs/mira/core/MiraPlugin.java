package sbs.mira.core;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import app.ashcon.intake.bukkit.BukkitIntake;
import app.ashcon.intake.bukkit.graph.BasicBukkitCommandGraph;

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
  Pulse pulse( ) throws MiraPulse.FlatlineException
  {
    return pulse;
    
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
    getLogger( ).log( Level.INFO, "[may i reflect again?] " + message );
  }
}
