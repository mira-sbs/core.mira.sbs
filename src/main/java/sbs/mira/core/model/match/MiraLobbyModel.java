package sbs.mira.core.model.match;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sbs.mira.core.MiraModel;
import sbs.mira.core.MiraPulse;
import sbs.mira.core.model.configuration.MiraMapRotationModel;
import sbs.mira.core.model.map.MiraMapRepository;

import java.io.IOException;

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
  private final MiraMapRotationModel<Pulse> map_rotation;
  @Nullable
  private MiraMatchModel<Pulse> match;
  @Nullable
  private final MiraMatchModel<Pulse> previous_match;
  
  public
  MiraLobbyModel( @NotNull Pulse pulse )
  {
    super( pulse );
    
    this.map_rotation = new MiraMapRotationModel<>( pulse );
    this.match = null;
    this.previous_match = null;
  }
  
  /*—[getters/setters]————————————————————————————————————————————————————————*/
  
  /**
   * rotation is a volatile list of maps that should ideally change often to
   * allow a structured, consistent delivery of maps - while also allowing change
   * and variety.
   *
   * @return a list of maps in a specific order - a rotation - that will be played in this order.
   */
  @NotNull
  public
  MiraMapRotationModel<Pulse> map_rotation( )
  {
    return this.map_rotation;
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
   */
  public
  void begin_match(
    MiraMapRepository<Pulse> map_repository,
    MiraGameModeRepository<Pulse> game_mode_repository )
  throws IOException
  {
    String next_map_label = this.map_rotation.next_map_label( );
    boolean was_manually_set = this.map_rotation.set_next_map( );
    
    this.map_rotation.advance( );
    
    this.match = new MiraMatchModel<>( this.pulse( ), was_manually_set, -1 );
    this.match.begin(
      map_repository.map( this.pulse( ), this.match, next_map_label ),
      game_mode_repository );
  }
  
  public
  void conclude_match( )
  {
    this.match( ).conclude_game( );
  }
}
