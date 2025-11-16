package sbs.mira.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sbs.mira.core.model.MiraPluginDataModel;

/**
 * miral pulse - representation of a one-way series of linked nodes with the
 * first two layers available to entities that carry this pulse.
 * layer i. reference to the bukkit / spigot plugin model.
 * layer ii. reference to the core data model (via the plugin).
 * recursive pulse requires the revisitation to begin at the upper layers -
 * implementations may provide further shortcuts down the tunnel.
 * created on 2017-03-20.
 *
 * @author jj stephen
 * @author jd rose
 * @version 1.0.1
 * @since 1.0.1
 */
public
class MiraPulse<Plugin extends MiraPlugin<?>, DataModel extends MiraPluginDataModel<?, ?>>
{
  @Nullable
  protected Plugin plugin;
  @Nullable
  protected DataModel model;
  
  /**
   * to solve circular reference between the plugin and its data model, they
   * must both first exist separately as a plugin-owns-model composition-based
   * relationship.
   * a subsequent call to {@code revive(...)} must follow for carriers relying
   * on this pulse.
   */
  public
  MiraPulse( )
  {
    this.plugin = null;
    this.model = null;
  }
  
  /**
   * initialises the breath giving structure and direction to the carriers of
   * this pulse.
   *
   * @param plugin miral plugin - acts as the root node.
   * @param model  core miral data model - owned by the plugin, reference kept here for convenience.
   */
  public
  void revive( @NotNull Plugin plugin, @NotNull DataModel model )
  {
    this.plugin = plugin;
    this.model = model;
    this.model.initialise( );
  }
  
  /**
   * @return reference to the specified miral plugin - root node.
   */
  @NotNull
  public
  Plugin plugin( )
  {
    if ( this.plugin == null )
    {
      throw new NullPointerException( "did you forget to call revive(...)?" );
    }
    
    return this.plugin;
  }
  
  /**
   * @return reference to the specified miral model - linked to the root node.
   */
  @NotNull
  public
  DataModel model( )
  {
    if ( this.model == null )
    {
      throw new NullPointerException( "did you forget to call revive(...)?" );
    }
    
    return this.model;
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
}
