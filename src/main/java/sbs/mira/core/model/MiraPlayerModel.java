package sbs.mira.core.model;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sbs.mira.core.MiraModel;
import sbs.mira.core.MiraPulse;
import sbs.mira.core.model.map.MiraTeamModel;

import java.util.UUID;

/**
 * miral representation of an in-game player.
 * holds a reference to its bukkit counterpart.
 * able to join teams - or indicate that they would like to join a team.
 * created on 2017-03-21.
 *
 * @author jj stephen
 * @author jd rose
 * @version 1.0.1
 * @since 1.0.0
 */
public abstract
class MiraPlayerModel<Pulse extends MiraPulse<?, ?>>
  extends MiraModel<Pulse>
{
  protected final @NotNull Player player;
  
  private boolean joined;
  @Nullable
  protected MiraTeamModel team;
  
  /**
   * instantiates a polymorphic in-game player model.
   *
   * @param player reference to bukkit player instance.
   * @param pulse  reference to mira.
   */
  public
  MiraPlayerModel( @NotNull Player player, @NotNull Pulse pulse )
  {
    super( pulse );
    
    this.player = player;
    this.joined = false;
    this.team = null;
    
    this.update( );
  }
  
  public abstract
  void update( );
  
  /*—[getters/setters]————————————————————————————————————————————————————————————————————————————*/
  
  /**
   * @return reference to the bukkit player instance.
   * @see Player
   */
  @NotNull
  public
  Player bukkit( )
  {
    return this.player;
  }
  
  /**
   * @return true - if the player has marked themselves as joined.
   */
  public
  boolean joined( )
  {
    return this.joined;
  }
  
  /**
   * players are able to mark themselves as joined or not joined.
   * marking oneself as joined will put you onto a team when a match starts,
   * or immediately if a match has already commenced (and allows late joining).
   *
   * @param joined true - if the player is marking themselves as joined - otherwise false.
   */
  public
  void joined( boolean joined )
  {
    this.joined = joined;
  }
  
  /**
   * @return reference to the team that this player is a member of.
   * @throws NullPointerException player is not a member of a team - validate using `has_team()`.
   */
  @NotNull
  public
  MiraTeamModel team( )
  {
    if ( this.team == null )
    {
      throw new NullPointerException( "player is not part of a team?" );
    }
    
    return this.team;
  }
  
  /**
   * @return true - if this player is the member of a miral team.
   */
  public
  boolean has_team( )
  {
    return this.team != null;
  }
  
  /*—[player intentions]——————————————————————————————————————————————————————————————————————————*/
  
  /**
   * invoked when the in-game player has received a team to join.
   *
   * @param new_team the player is joining this team (consensually).
   * @throws IllegalStateException player was not marked as joined - validate using `joined()`.
   */
  public
  void joins_team( @NotNull MiraTeamModel new_team )
  throws IllegalStateException
  {
    if ( !this.joined )
    {
      throw new IllegalStateException( "player joins team while not marked as joined?" );
    }
    
    this.team = new_team;
    this.team( ).bukkit( ).addEntry( this.name( ) );
    this.update( );
  }
  
  /**
   * invoked when the in-game player wishes to leave their team.
   *
   * @throws IllegalStateException player was not marked as joined - validate using `joined()`.
   */
  public
  void leaves_team( )
  {
    if ( !this.joined )
    {
      throw new IllegalStateException( "player leaves team while not marked as joined?" );
    }
    
    this.team( ).bukkit( ).removeEntry( this.name( ) );
    this.team = null;
    this.joined = false;
    this.update( );
  }
  
  /*—[bukkit shorthand + helper methods]——————————————————————————————————————————————————————————*/
  
  /**
   * @return the player's in-game uuid.
   */
  @NotNull
  public
  UUID uuid( )
  {
    return this.player.getUniqueId( );
  }
  
  /**
   * @see org.bukkit.entity.Player#sendMessage(String)
   */
  public
  void messages( @NotNull String content )
  {
    this.bukkit( ).sendMessage( content );
  }
  
  /**
   * @return the player's in-game name (ign).
   * @see Player#getName()
   */
  @NotNull
  public
  String name( )
  {
    return this.player.getName( );
  }
  
  /**
   * @return formatted "display" name with formatting enabled+encouraged.
   * @see Player#getDisplayName()
   */
  @NotNull
  public
  String display_name( )
  {
    return this.player.getDisplayName( );
  }
  
  /**
   * @return the player's in-game location.
   * @see Player#getLocation()
   */
  @NotNull
  public
  Location location( )
  {
    return this.player.getLocation( );
  }
}
