package sbs.mira.core.model;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_21_R6.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sbs.mira.core.MiraModel;
import sbs.mira.core.MiraPulse;
import sbs.mira.core.model.map.MiraTeamModel;

import java.util.UUID;

/**
 * oh look another mira stan.
 * created on 2017-03-21.
 *
 * @author jj.mira.sbs
 * @author jd.mira.sbs
 * @version 1.0.1
 * @since 1.0.0
 */
public abstract
class MiraPlayerModel<Pulse extends MiraPulse<?, ?>>
  extends MiraModel<Pulse>
{
  protected final @NotNull CraftPlayer player;
  
  private boolean joined;
  protected @Nullable MiraTeamModel team;
  
  /**
   * @param player is for me?
   * @param pulse  anchorrr.
   */
  public
  MiraPlayerModel( @NotNull CraftPlayer player, @NotNull Pulse pulse )
  {
    super( pulse );
    
    this.player = player;
    this.joined = false;
    this.team = null;
  }
  
  /*—[getters/setters]————————————————————————————————————————————————————————————————————————————*/
  
  /**
   * @see CraftPlayer
   */
  public @NotNull
  CraftPlayer bukkit( )
  {
    return player;
  }
  
  /*——————————————————————————————————————————————————————————————————————————————————————————————*/
  
  /**
   * @see #joined
   */
  public
  boolean joined( )
  {
    return joined;
  }
  
  /*——————————————————————————————————————————————————————————————————————————————————————————————*/
  
  /**
   * @see #joined
   */
  public
  void joined( boolean joined )
  {
    this.joined = joined;
  }
  
  /*——————————————————————————————————————————————————————————————————————————————————————————————*/
  
  /**
   * @return true if the mira pvp stan has an [sic, lol] designated team.
   */
  public
  boolean has_team( )
  {
    return team != null;
  }
  
  /*——————————————————————————————————————————————————————————————————————————————————————————————*/
  
  /**
   * returns the team that the player is currently associated with.
   * this is the team that the player currently on during a match.
   *
   * @return Player's associated team.
   */
  public @NotNull
  MiraTeamModel team( )
  {
    assert team != null;
    
    return team;
  }
  
  /*——————————————————————————————————————————————————————————————————————————————————————————————*/
  
  /**
   * @param new_team the player is joining this team (consensually).
   */
  public
  void joins_team( @NotNull MiraTeamModel new_team )
  {
    this.team = new_team;
    this.team( ).bukkit( ).addEntry( this.name( ) );
    
    //toggle_visibilities( );
    //changes_name( );
  }
  
  /*——————————————————————————————————————————————————————————————————————————————————————————————*/
  
  public
  void leaves_team( )
  {
    this.team( ).bukkit( ).removeEntry( this.name( ) );
    this.team = null;
    this.joined = false;
    
    //toggle_visibilities( );
    //changes_name( );
  }
  
  /*——————————————————————————————————————————————————————————————————————————————————————————————*/
  
  public @NotNull
  UUID uuid( )
  {
    return this.player.getUniqueId( );
  }
  
  /*——————————————————————————————————————————————————————————————————————————————————————————————*/
  
  /**
   * @see org.bukkit.entity.Player#sendMessage(String)
   */
  public
  void messages( @NotNull String content )
  {
    this.bukkit( ).sendMessage( content );
  }
  
  /*——————————————————————————————————————————————————————————————————————————————————————————————*/
  
  /**
   * @return the current in game name of this mira stan.
   * @see Player#getName()
   */
  public @NotNull
  String name( )
  {
    return player.getName( );
  }
  
  /*——————————————————————————————————————————————————————————————————————————————————————————————*/
  
  /**
   * @return formatted "display" name with formatting enabled+encouraged.
   * @see Player#getName()
   */
  public @NotNull
  String display_name( )
  {
    return player.getDisplayName( );
  }
  
  /*——————————————————————————————————————————————————————————————————————————————————————————————*/
  
  public @NotNull
  Location location( )
  {
    return this.player.getLocation( );
  }
}
