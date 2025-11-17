package sbs.mira.core.model.match;

import org.jetbrains.annotations.NotNull;
import sbs.mira.core.MiraPulse;

/**
 *
 * @param <Pulse>
 */
public
interface MiraLobby<Pulse extends MiraPulse<?, ?>>
{
  @NotNull
  MiraMatchModel<Pulse> match( );
  
  void conclude_game( );
  
  void conclude_match();
}
