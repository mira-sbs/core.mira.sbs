package sbs.mira.core.model.match;

import org.jetbrains.annotations.NotNull;
import sbs.mira.core.MiraPulse;
import sbs.mira.core.model.map.MiraMapModel;

public
interface MiraGameModeRepository<Pulse extends MiraPulse<?, ?>>
{
  @NotNull
  Class<? extends MiraGameModeModel<Pulse>> game_mode_class( @NotNull String game_mode_label );
  
  @NotNull
  MiraGameModeModel<Pulse> game_mode(
    @NotNull Pulse pulse,
    @NotNull MiraMatchModel<Pulse> match,
    @NotNull String game_mode_label );
}
