package sbs.mira.core.model.match;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sbs.mira.core.model.MiraPlayerModel;
import sbs.mira.core.model.map.MiraMapModel;
import sbs.mira.core.model.map.MiraTeamModel;

public
interface MiraMatch
{
  @NotNull
  MiraMatchState state( );
  
  @NotNull
  MiraMapModel<?> map( );
  
  @NotNull
  MiraGameModeModel<?> game_mode( );
  
  void conclude_game( );
  
  /**
   * @return the world (fetched by id) that is currently hosting the map of this match.
   */
  @NotNull
  World world( );
  
  int seconds_remaining( );
  
  boolean try_join_team(
    @NotNull MiraPlayerModel<?> player,
    @Nullable MiraTeamModel preferred_team );
  
  void try_leave_team( @NotNull MiraPlayerModel<?> player );
}
