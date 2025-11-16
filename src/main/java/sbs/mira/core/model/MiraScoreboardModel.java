package sbs.mira.core.model;

import org.bukkit.ChatColor;
import org.bukkit.scoreboard.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sbs.mira.core.model.map.MiraTeamModel;

public
class MiraScoreboardModel
{
  @NotNull
  private final Scoreboard scoreboard;
  @NotNull
  private final Objective objective;
  @NotNull
  private final Team team_spectators;
  @Nullable
  private String[] scoreboard_entries;
  
  public
  MiraScoreboardModel(
    @NotNull ScoreboardManager scoreboard_manager,
    @NotNull String objective_label )
  {
    this.scoreboard = scoreboard_manager.getNewScoreboard( );
    
    this.objective = this.scoreboard.registerNewObjective(
      objective_label,
      Criteria.DUMMY,
      objective_label );
    this.objective.setDisplaySlot( DisplaySlot.SIDEBAR );
    
    this.team_spectators = this.scoreboard.registerNewTeam( "spectators" );
    this.team_spectators.setDisplayName( ChatColor.LIGHT_PURPLE + "observers" + ChatColor.RESET );
    this.team_spectators.setCanSeeFriendlyInvisibles( true );
    this.team_spectators.setAllowFriendlyFire( false );
    this.team_spectators.setPrefix( String.valueOf( ChatColor.LIGHT_PURPLE ) );
    this.scoreboard_entries = null;
  }
  
  public
  void display_name( @NotNull String objective_display_name )
  {
    this.objective.setDisplayName( objective_display_name );
  }
  
  public
  void initialise( int scoreboard_entries_length )
  {
    this.scoreboard_entries = new String[ scoreboard_entries_length ];
  }
  
  public
  void set_row( int row_index, @NotNull String new_value )
  {
    String old_value = this.scoreboard_entries[ row_index ];
    
    assert old_value != null;
    
    if ( new_value.equals( old_value ) )
    {
      return;
    }
    
    this.scoreboard.resetScores( old_value );
    this.scoreboard_entries[ row_index ] = new_value;
    this.objective.getScore( new_value ).setScore( row_index );
  }
  
  public
  void reset( )
  {
    if ( this.scoreboard_entries == null )
    {
      return;
    }
    
    for ( String entry : this.scoreboard_entries )
    {
      assert entry != null;
      
      this.scoreboard.resetScores( entry );
    }
    
    this.scoreboard_entries = null;
  }
  
  public
  void show( @NotNull MiraPlayerModel<?> mira_player )
  {
    mira_player.bukkit( ).setScoreboard( this.scoreboard );
  }
  
  public
  void add_spectator( @NotNull MiraPlayerModel<?> mira_player )
  {
    if ( mira_player.has_team( ) )
    {
      throw new IllegalArgumentException( "player is already part of a match team." );
    }
    
    this.team_spectators.addEntry( mira_player.name( ) );
  }
  
  public
  void remove_spectator( @NotNull MiraPlayerModel<?> mira_player )
  {
    if ( !mira_player.has_team( ) )
    {
      throw new IllegalArgumentException( "player is not part of a match team." );
    }
    
    this.team_spectators.removeEntry( mira_player.name( ) );
  }
  
  public
  void register( @NotNull MiraTeamModel mira_team )
  {
    Team result = this.scoreboard.registerNewTeam( mira_team.label( ) );
    result.setDisplayName( mira_team.coloured_display_name( ) );
    result.setCanSeeFriendlyInvisibles( true );
    result.setAllowFriendlyFire( false );
    result.setPrefix( String.valueOf( mira_team.color( ) ) );
    
    mira_team.bukkit( result );
  }
}
