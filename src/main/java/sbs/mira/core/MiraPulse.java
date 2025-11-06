package sbs.mira.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sbs.mira.core.model.MiraPluginDataModel;

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
