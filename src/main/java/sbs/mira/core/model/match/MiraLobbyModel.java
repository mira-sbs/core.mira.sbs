package sbs.mira.core.model.match;

import org.bukkit.Bukkit;
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
  @NotNull
  private final MiraMapRepository<Pulse> map_repository;
  @NotNull
  private final MiraGameModeRepository<Pulse> game_mode_repository;
  
  @Nullable
  private MiraMatchModel<Pulse> match;
  @Nullable
  private MiraMatchModel<Pulse> previous_match;
  
  public
  MiraLobbyModel(
    @NotNull Pulse pulse,
    @NotNull MiraMapRepository<Pulse> map_repository,
    @NotNull MiraGameModeRepository<Pulse> game_mode_repository )
  {
    super( pulse );
    
    this.map_repository = map_repository;
    this.game_mode_repository = game_mode_repository;
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
    if ( this.match == null )
    {
      throw new IllegalStateException( "match does not exist?" );
    }
    
    return this.match;
  }
  
  /**
   * event handler.
   * fires when the mira lobby is active and a new match can begin.
   * the next map in the rotation is chosen if the next map has not been manually set.
   * the match will then take over the next steps in the lobby lifecycle.
   *
   * @throws IOException file operation failed.
   */
  public
  void begin_match( )
  throws IOException
  {
    if ( this.match != null )
    {
      throw new IllegalStateException( "match already exists?" );
    }
    
    String next_map_label = this.map_rotation.next_map_label( );
    boolean was_manually_set = this.map_rotation.set_next_map( );
    
    this.match = new MiraMatchModel<>( this.pulse( ), this, was_manually_set, -1 );
    this.match.activate(
      this.map_repository.map( this.pulse( ), this.match, next_map_label ),
      this.game_mode_repository );
  }
  
  public
  void conclude_game( )
  {
    this.match( ).conclude_game( );
  }
  
  public
  void conclude_match( )
  {
    this.previous_match = this.match( );
    this.match = null;
    
    try
    {
      this.map_rotation.advance( );
      this.begin_match( );
    }
    catch ( IOException e )
    {
      Bukkit.broadcastMessage( "wtf???" );
    }
    
    this.previous_match.deactivate( );
  }
}
