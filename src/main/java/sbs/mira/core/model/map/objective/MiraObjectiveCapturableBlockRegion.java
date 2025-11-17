package sbs.mira.core.model.map.objective;

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
  double remaining_progress( )
  {
    int total = this.original_block_positions( ).size( );
    int remaining = this.remaining_blocks( ).size( );
    
    assert ( total > 0 );
    
    return Math.ceil( ( ( double ) remaining / total ) * 100 );
  }
  
  default
  double current_progress( )
  {
    return 1 - remaining_progress( );
  }
  
  default
  boolean captured( )
  {
    return this.remaining_blocks( ).isEmpty( );
  }
  
  @NotNull
  default
  String  progress_bar( )
  {
    StringBuilder result = new StringBuilder( );
    
    int block_char_count = this.original_block_positions( ).size( );
    
    if ( block_char_count > 8 )
    {
      block_char_count = 8;
    }
    
    // `char_index` values less than or equal to this value will add a char that
    // is fully filled in. conversely, values beyond this value will add a char
    // that is partially shaded out. this visualises the "completion" of the
    // progress bar.
    //
    // quick example:
    // remaining progress = 20 blocks; 11 broken = 0.45 or 45% remaining progress.
    // current progress = inversion of remainder = 55% current progress made.
    // monument block count = 20 blocks = capped at 8 '█'/'▓' chars.
    // calculate: 8 chars x 0.55 current progress = 4.4; floored to 4/8 chars.
    //
    // result:
    // i. 8 chars - 4 chars filled in (prefers rounding up to 4/8 instead of down to 3/8) =
    // ii. 4 chars to fill in.
    // iii. 4 chars to shade out.
    // iv. shade out char begins at index 4, or ends at index 3 (subtract 1).
    int filled_index =
      ( int ) ( block_char_count - Math.floor( block_char_count * this.current_progress( ) ) ) - 1;
    
    for ( int char_index = 0; char_index < block_char_count; char_index++ )
    {
      if ( char_index > filled_index )
      {
        result.append( '▓' );
      }
      else
      {
        result.append( '█' );
      }
    }
    
    return result.toString( );
  }
}
