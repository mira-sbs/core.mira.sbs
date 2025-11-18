package sbs.mira.core.model.map.objective;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import sbs.mira.core.model.utility.Position;
import sbs.mira.core.model.utility.Region;

import java.util.List;

public
interface MiraObjectiveCapturableBlockRegion
  extends MiraNamedObjective, MiraObjectiveCapturable
{
  @NotNull
  Material material( );
  
  @NotNull
  Region region( );
  
  @NotNull
  List<Position> original_block_positions( );
  
  @NotNull
  List<Block> remaining_blocks( );
  
  default
  int remaining_progress( )
  {
    int total = this.original_block_positions( ).size( );
    int remaining = this.remaining_blocks( ).size( );
    
    if ( total == remaining )
    {
      return 100;
    }
    else if ( remaining == 0 )
    {
      return 0;
    }
    else
    {
      return ( int ) Math.floor( 100d * ( ( double ) remaining / total ) );
    }
  }
  
  default
  int current_progress( )
  {
    return 100 - remaining_progress( );
  }
  
  default
  boolean captured( )
  {
    return this.remaining_blocks( ).isEmpty( );
  }
  
  @NotNull
  default
  String progress_bar( )
  {
    StringBuilder result = new StringBuilder( );
    
    int block_char_count = this.original_block_positions( ).size( );
    
    // don't display more than 8 progress bar characters on the scoreboard.
    if ( block_char_count > 8 )
    {
      block_char_count = 8;
    }
    
    int remaining_progress = this.remaining_progress( );
    double progress_increment = 100.00d / block_char_count;
    double required_progress = 0;
    
    for ( int char_index = 0; char_index < block_char_count; char_index++ )
    {
      if ( this.captured( ) )
      {
        result.append( ChatColor.GRAY );
      }
      else
      {
        result.append( this.team( ).color( ) );
      }
      
      if ( remaining_progress == 100 || remaining_progress > required_progress )
      {
        result.append( '█' );
      }
      else
      {
        result.append( '▓' );
      }
      
      required_progress += progress_increment;
    }
    
    return result.toString( );
  }
}
