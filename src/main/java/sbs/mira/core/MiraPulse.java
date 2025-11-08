package sbs.mira.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sbs.mira.core.model.MiraPluginDataModel;

/**
 * miral pulse - representation of a one-way spanning tree with two branches:
 * i. reference to the bukkit plugin model (Plugin).
 * ii. reference to the core data model (Model).
 * created on 2017-03-20.
 *
 * @author jj stephen
 * @author jd rose
 * @version 1.0.1
 * @see MiraPlugin
 * @since 1.0.1
 */
public
class MiraPulse<Plugin extends MiraPlugin<?>, Model extends MiraPluginDataModel<?, ?>>
{
  @Nullable
  protected Plugin plugin;
  @Nullable
  protected Model model;
  
  public
  MiraPulse( )
  {
    this.plugin = null;
    this.model = null;
  }
  
  public
  MiraPulse( @NotNull Plugin plugin, @NotNull Model model )
  {
    this.breathe( plugin, model );
  }
  
  public
  void breathe( @NotNull Plugin plugin, @NotNull Model model )
  {
    this.plugin = plugin;
    this.model = model;
    this.model.initialise( );
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
  Model model( )
  {
    if ( model != null )
    {
      return model;
    }
    else
    {
      throw new FlatlineException( "not yet breathing." );
    }
  }
  
  /**
   * log an informational message to the jvm console via the plugin's logger.
   *
   * @param message yap to output in the console to the poor sysadmin.
   */
  public
  void log( @NotNull String message )
  {
    this.model( ).log( message );
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
