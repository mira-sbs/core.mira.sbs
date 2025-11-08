package sbs.mira.core.model.match;


/**
 * all game mode types currently available / supported on `mira-verse`.
 * created on 2017-04-20.
 *
 * @author jj stephen
 * @version 1.0.1
 * @since 1.0.0
 */
public
enum MiraGameModeType
{
  TEAM_DEATH_MATCH( "tdm", "team death match" ),
  LIFE_POOL( "lp", "life pool" ),
  FREE_FOR_ALL( "ffa", "free for all" ),
  LAST_TEAM_STANDING( "lts", "last team standing" ),
  LAST_MAN_STANDING( "lms", "last man standing" ),
  KING_OF_THE_HILL( "koth", "king of the hill" ),
  DISTRICT_DEATH_MATCH( "ddm", "district death match" ),
  CAPTURE_THE_FLAG( "ctf", "capture the flag" ),
  DESTROY_THE_MONUMENT( "dtm", "destroy the monument" );
  
  final String label;
  final String display_name;
  
  MiraGameModeType( String label, String display_name )
  {
    this.label = label;
    this.display_name = display_name;
  }
  
  /**
   * labels are a shorter abbreviation of the game mode name - ideally an acronym.
   *
   * @return the label of this game mode.
   */
  public
  String label( )
  {
    return label;
  }
  
  /**
   * @return the full unabbreviated / "pretty" name of the game mode.
   */
  public
  String display_name( )
  {
    return display_name;
  }
  
  /**
   * Formats this string, similar to StringUtility#sentenceFormat
   * but specifically for this enumerated type.
   *
   * @param array The array of modes to format.
   * @return The formatted string.
   *
  public static
  TextComponent format( Mode[] array, MiraPulse main )
  {
  if ( array.length == 0 )
  {
  return new TextComponent( "None" );
  }
  TextComponent result = new TextComponent( );
  if ( array.length == 1 )
  {
  return new TextComponent( array[ 0 ].getDescriptionComponent( main, true ) );
  }
  int i = 1;
  while ( i <= array.length )
  {
  if ( i == array.length )
  {
  result.addExtra( ChatColor.WHITE + " and " );
  result.addExtra( array[ i - 1 ].getDescriptionComponent( main, true ) );
  }
  else if ( i == 1 )
  {
  result = array[ 0 ].getDescriptionComponent( main, true );
  }
  else
  {
  result.addExtra( ChatColor.WHITE + ", " );
  result.addExtra( array[ i - 1 ].getDescriptionComponent( main, true ) );
  }
  i++;
  }
  return result;
  }*/
  
  /**
   * Generates an interactive gamemode hoverable.
   *
   * @param main          Supercontroller for gamemode access.
   * @param withVoteClick Enable click this to vote?
   * @return Resulting chat component.
   *
  public
  TextComponent getDescriptionComponent( MiraPulse main, boolean withVoteClick )
  {
  TextComponent msg = new TextComponent( ChatColor.GREEN + "[" + display_name + "]" );
  WarMode assoc = main.cache( ).getGamemode( label( ) );
  msg.setHoverEvent( new HoverEvent(
  HoverEvent.Action.SHOW_TEXT, new ComponentBuilder( ChatColor.GREEN +
  "" +
  ChatColor.BOLD +
  label( ) +
  "\n" +
  ChatColor.RED +
  ChatColor.ITALIC +
  "When attacking:\n" +
  ChatColor.WHITE +
  assoc.getOffensive( ) +
  ChatColor.BLUE +
  ChatColor.ITALIC +
  "\nWhen defending:\n" +
  ChatColor.WHITE +
  assoc.getDefensive( ) +
  (
  withVoteClick ?
  "\n" +
  ChatColor.UNDERLINE +
  "Click To Vote" :
  ""
  ) ).create( )
  ) );
  if ( withVoteClick )
  {
  msg.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, "/vote " + display_name ) );
  }
  return msg;
  }*/
}