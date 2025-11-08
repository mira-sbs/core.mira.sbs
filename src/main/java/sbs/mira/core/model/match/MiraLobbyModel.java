package sbs.mira.core.model.match;

import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sbs.mira.core.MiraModel;
import sbs.mira.core.MiraPulse;
import sbs.mira.core.model.map.MiraMapModel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * miral representation of a lobby singleton - within a minecraft server.
 * the lobby is responsible for handling the lifecycle in between matches.
 * this primarily involves transition between maps and their worlds.
 * other concerns such as lobby and pre/post game segments are the property
 * of each individual match.
 * created on 2025-11-05.
 *
 * @author jj stephen
 * @version 1.0.1
 * @since 1.0.1
 */
public
class MiraLobbyModel<Pulse extends MiraPulse<?, ?>>
  extends MiraModel<Pulse>
  implements MiraLobby<Pulse>
{
  @NotNull
  private final Scoreboard scoreboard;
  @NotNull
  private final Team team;
  @NotNull
  private final List<String> map_rotation;
  private int map_rotation_index;
  
  @Nullable
  private MiraMatchModel<Pulse> match;
  @Nullable
  private final MiraMatchModel<Pulse> previous_match;
  
  @Nullable
  private String set_next_map_label;
  
  public
  MiraLobbyModel( @NotNull Pulse pulse )
  {
    super( pulse );
    
    this.match = null;
    this.previous_match = null;
    
    this.set_next_map_label = null;
    this.map_rotation = new ArrayList<>( );
    this.map_rotation_index = 0;
    
    this.scoreboard =
      Objects.requireNonNull( this.server( ).getScoreboardManager( ) ).getNewScoreboard( );
    this.team = this.scoreboard.registerNewTeam( "observing" );
    this.team.setCanSeeFriendlyInvisibles( true );
    this.team.setAllowFriendlyFire( false );
    this.team.setPrefix( String.valueOf( ChatColor.LIGHT_PURPLE ) );
    
    try (
      Stream<String> stream = Files.lines( Paths.get( this.pulse( ).plugin( ).getDataFolder( ) +
                                                      File.separator +
                                                      "rotation" ) )
    )
    {
      stream.forEachOrdered( map_rotation::add );
    }
    catch ( IOException e )
    {
      // todo: error handling for here...
      //this.state = MiraMatchState.FAILED;
    }
  }
  
  /*—[getters/setters]————————————————————————————————————————————————————————*/
  
  /**
   * the next map to be played in the lobby is usually the next in the rotation.
   * however, any map can be manually set out of rotation and should be accounted for.
   *
   * @param set_next_map_label label of the map that should be played next (null to unset).
   */
  public
  void set_next_map_label( @Nullable String set_next_map_label )
  {
    this.set_next_map_label = set_next_map_label;
  }
  
  /**
   * @return label of the map that should be played next (null if unset).
   */
  @Nullable
  public
  String set_next_map_label( )
  {
    return this.set_next_map_label;
  }
  
  /**
   * rotation is a volatile list of maps that should ideally change often to
   * allow a structured, consistent delivery of maps - while also allowing change
   * and variety.
   *
   * @return a list of maps in a specific order - a rotation - that will be played in this order.
   */
  @NotNull
  public
  List<String> map_rotation( )
  {
    return Collections.unmodifiableList( this.map_rotation );
  }
  
  @NotNull
  public
  MiraMatchModel<Pulse> match( )
  {
    assert this.match != null;
    
    return this.match;
  }
  
  /*—[validation/guard evaluations]———————————————————————————————————————————*/
  
  /**
   * players are allowed to interact with the map or game mode if they are on
   * a team - or if admin bypass is toggled and permission is granted.
   *
   * @param entity_uuid        the uuid of the entity (ideally a player) interacting with the world.
   * @param allow_admin_bypass true - if players with the `mira.administrator.bypass` permission can bypass this check always.
   * @return true - if the player is currently allowed to interact with the world.
   */
  /*public
  boolean can_interact( @NotNull UUID entity_uuid, boolean allow_admin_bypass )
  {
    MiraVersePlayer player = this.pulse( ).model( ).player( entity_uuid );
    
    return player == null || (
      player.has_team( ) ||
      allow_admin_bypass && player.crafter( ).hasPermission( "mira.administrator.bypass" )
    );
  }*/
  
  /*—[lobby lifecycle steps]——————————————————————————————————————————————————*/
  
  /**
   * event handler.
   * fires when the mira lobby is active and a new match can begin.
   * the next map in the rotation is chosen if the next map has not been manually set.
   * the match will then take over the next steps in the lobby lifecycle.
   *
   * @throws IOException file operation failed.
   * @see MiraMatchModel#begin()
   */
  public
  void begin_match( )
  throws IOException
  {
    String chosen_map_label;
    boolean was_manually_set;
    
    if ( this.set_next_map_label == null )
    {
      chosen_map_label = this.map_rotation.get( this.map_rotation_index );
      was_manually_set = false;
      
      this.map_rotation_index++; // todo: needs to wrap.
    }
    else
    {
      chosen_map_label = this.set_next_map_label;
      was_manually_set = true;
      
      this.set_next_map_label = null;
    }
    
    MiraMapModel<Pulse> map = null; // todo: determine map?
    
    this.match = new MiraMatchModel<>( this.pulse( ), map, was_manually_set, -1 );
    this.match.begin( );
  }
  
  public
  void conclude_match( )
  {
    this.match( ).conclude( );
  }
}
