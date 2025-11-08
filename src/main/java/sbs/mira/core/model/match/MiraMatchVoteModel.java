package sbs.mira.core.model.match;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sbs.mira.core.MiraModel;
import sbs.mira.core.MiraPulse;
import sbs.mira.core.model.MiraPlayerModel;

import java.util.*;

public
class MiraMatchVoteModel<Pulse extends MiraPulse<?, ?>>
  extends MiraModel<Pulse>
{
  @NotNull
  private final Set<MiraGameModeType> allowed_game_mode_types;
  @NotNull
  private final Map<UUID, MiraGameModeType> votes;
  
  private boolean rigged;
  @Nullable
  private MiraGameModeType rigged_game_mode;
  
  public
  MiraMatchVoteModel( @NotNull Pulse pulse, @NotNull Set<MiraGameModeType> allowed_game_mode_types )
  {
    super( pulse );
    
    this.allowed_game_mode_types = allowed_game_mode_types;
    this.votes = new HashMap<>( );
    
    this.rigged = false;
    this.rigged_game_mode = null;
  }
  
  public
  boolean is_vote_allowed_for( @NotNull MiraGameModeType game_mode_type )
  {
    return this.allowed_game_mode_types.contains( game_mode_type );
  }
  
  public
  boolean has_voted( @NotNull MiraPlayerModel<Pulse> mira_player )
  {
    return this.votes.containsKey( mira_player.uuid( ) );
  }
  
  public
  int count( @NotNull MiraGameModeType game_mode_type )
  {
    return this.votes.values( ).stream( )
      .filter( ( game_mode_type_to_check )->game_mode_type_to_check == game_mode_type )
      .toArray( ).length;
  }
  
  public
  void try_vote(
    @NotNull MiraPlayerModel<Pulse> mira_player,
    @NotNull MiraGameModeType game_mode_type,
    boolean rig )
  {
    if ( !this.is_vote_allowed_for( game_mode_type ) )
    {
      mira_player.messages( ChatColor.RED + "this game mode is not supported on this map." );
      
      return;
    }
    
    if ( rig )
    {
      if ( this.rigged )
      {
        mira_player.messages( ChatColor.RED + "the vote has already been rigged!" );
        
        return;
      }
      
      this.rigged = true;
      this.rigged_game_mode = game_mode_type;
      
      mira_player.messages( ChatColor.LIGHT_PURPLE + "you have rigged this vote..!" );
      
      this.server( ).broadcastMessage( ChatColor.LIGHT_PURPLE + "the vote has been rigged..!" );
    }
    else
    {
      if ( this.has_voted( mira_player ) )
      {
        mira_player.messages( ChatColor.RED + "you have already voted!" );
        
        return;
      }
      
      this.votes.put( mira_player.uuid( ), game_mode_type );
      
      mira_player.messages( ChatColor.LIGHT_PURPLE + "you have cast your vote." );
      
      this.server( ).broadcastMessage( String.format(
        "%s voted for the game mode %s!",
        mira_player.name( ),
        game_mode_type.display_name( ) ) );
    }
  }
  
  @NotNull
  public
  MiraGameModeType winning_game_mode( )
  {
    if ( rigged )
    {
      assert this.rigged_game_mode != null;
      
      return this.rigged_game_mode;
    }
    
    EnumSet<MiraGameModeType> winning_game_modes = EnumSet.noneOf( MiraGameModeType.class );
    int winning_vote_count = 0;
    
    for ( MiraGameModeType game_mode_type : this.allowed_game_mode_types )
    {
      int vote_count = this.count( game_mode_type );
      
      if ( vote_count == winning_vote_count )
      {
        winning_game_modes.add( game_mode_type );
      }
      else if ( vote_count > winning_vote_count )
      {
        winning_game_modes = EnumSet.of( game_mode_type );
        winning_vote_count = vote_count;
      }
    }
    
    MiraGameModeType[] winning_game_mode_array =
      winning_game_modes.toArray( new MiraGameModeType[]{ } );
    
    if ( winning_game_modes.size( ) > 1 )
    {
      return winning_game_mode_array[ this.pulse( ).model( ).rng.nextInt( winning_game_mode_array.length ) ];
    }
    else
    {
      return winning_game_mode_array[ 0 ];
    }
  }
}
