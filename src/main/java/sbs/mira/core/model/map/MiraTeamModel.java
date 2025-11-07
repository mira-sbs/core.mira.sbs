package sbs.mira.core.model.map;

import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * miral representation of a team.
 * purposed to form a coaxis between maps, game modes and players.
 * created on 2017-03-20.
 *
 * @author jj stephen
 * @version 1.0.1
 * @since 1.0.0
 */
public final
class MiraTeamModel
{
  private final @NotNull String label;
  private final @NotNull String display_name;
  private final @NotNull ChatColor colour;
  private final int maximum_size;
  
  private @Nullable Team bukkit_team;
  
  public
  MiraTeamModel(
    @NotNull String label,
    @NotNull String display_name,
    @NotNull ChatColor colour,
    int maximum_size )
  {
    this.label = label;
    this.colour = colour;
    this.maximum_size = maximum_size;
    this.display_name = display_name;
    this.bukkit_team = null;
  }
  
  
  public
  MiraTeamModel( @NotNull String label, ChatColor colour, String display_name )
  {
    this( label, display_name, colour, -1 );
  }
  
  public
  MiraTeamModel( @NotNull String label, ChatColor colour, int maximum_size )
  {
    this( label, label, colour, maximum_size );
  }
  
  public
  MiraTeamModel( @NotNull String label, ChatColor colour )
  {
    this( label, label, colour, -1 );
  }
  
  /**
   * @return programmatic identifier for this team - referred to as a `label`.
   */
  public
  String label( )
  {
    return label;
  }
  
  /**
   * @return designated colour for this team - same as chat colours.
   */
  public
  ChatColor color( )
  {
    return this.colour;
  }
  
  /**
   * @return maximum number of players that can join this team at any given time.
   */
  public
  int maximum_size( )
  {
    return maximum_size;
  }
  
  /**
   * @return prettier identifier for this team - good for displaying on scoreboards/formatted text.
   */
  public @NotNull
  String display_name( )
  {
    return display_name;
  }
  
  /**
   * @return an instance of the bukkit representation of this team (during an active match).
   */
  public @NotNull
  org.bukkit.scoreboard.Team bukkit( )
  {
    assert bukkit_team != null;
    
    return bukkit_team;
  }
  
  /**
   * @param bukkit_team an instance of the bukkit representation of this team (during an active match).
   */
  public
  void bukkit( @NotNull org.bukkit.scoreboard.Team bukkit_team )
  {
    this.bukkit_team = bukkit_team;
  }
  
  /**
   * @return true - if the number of players of the team has reached the maximum.
   */
  public
  boolean full( )
  {
    return this.bukkit( ).getEntries( ).size( ) >= maximum_size;
  }
  
  /*public
  TextComponent getHoverInformation( )
  {
    TextComponent result = new TextComponent( colour( ) + "[" + label( ) + "]" + ChatColor.WHITE );
    result.setHoverEvent( new HoverEvent(
      HoverEvent.Action.SHOW_TEXT, new ComponentBuilder( "Members: " +
                                                         bukkit_team( ).getEntries( ).size( ) +
                                                         "/" +
                                                         maximum_size +
                                                         "\nKills: " +
                                                         kills +
                                                         "\nDeaths: " +
                                                         deaths ).create( )
    ) );
    return result;
  }*/
  
  /*@Override
  public
  Team clone( )
  {
    return new Team( label( ), colour( ), maximum_size( ), display_name( ) );
  }*/
  
  /**
   * @return the label with the team colour applied - followed by a color change to yellow.
   */
  public @NotNull
  String coloured_display_name( )
  {
    return this.color( ) + this.display_name( ) + ChatColor.RESET;
  }
}
