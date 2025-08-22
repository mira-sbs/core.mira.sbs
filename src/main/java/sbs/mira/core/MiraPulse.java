package sbs.mira.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * [recursive wit.]
 * created on 2017-03-20.
 *
 * @author jj.mira.sbs
 * @author jd.mira.sbs
 * @version 1.0.1
 * @see MiraPlugin
 * @since 1.0.0
 */
public
class MiraPulse<Plugin extends MiraPlugin<?>, Master extends MiraPluginMaster<?, ?>>
{
  @Nullable
  private Plugin plugin;
  @Nullable
  private Master master;
  
  public
  MiraPulse( )
  {
    this.plugin = null;
    this.master = null;
  }
  
  public
  MiraPulse( @NotNull Plugin plugin, @NotNull Master master )
  {
    this.breathe( plugin, master );
  }
  
  public
  void breathe( @NotNull Plugin plugin, @NotNull Master master )
  {
    assert this.plugin == null && this.master == null : "already breathing you fool.";
    
    this.plugin = plugin;
    this.master = master;
    this.master.breathe( );
  }
  
  @NotNull
  public
  Plugin plugin( )
  {
    if ( plugin != null )
    {
      return plugin;
    }
    else
    {
      throw new FlatlineException( "not yet breathing." );
    }
  }
  
  @NotNull
  public
  Master master( )
  {
    if ( master != null )
    {
      return master;
    }
    else
    {
      throw new FlatlineException( "not yet breathing." );
    }
  }
  
  /**
   * just set the pulse brah?
   */
  public static
  class FlatlineException
    extends RuntimeException
  {
    public
    FlatlineException( String reason )
    {
      super( reason );
    }
  }
}
