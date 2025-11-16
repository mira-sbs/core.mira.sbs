package sbs.mira.core.model.map;

import org.jetbrains.annotations.NotNull;
import sbs.mira.core.MiraPulse;
import sbs.mira.core.model.match.MiraMatchModel;

public
interface MiraMapRepository<Pulse extends MiraPulse<?, ?>>
{
  
  @NotNull
  Class<? extends MiraMapModel<Pulse>> map_class( @NotNull String map_label );
  
  @NotNull
  MiraMapModel<Pulse> map(
    @NotNull Pulse pulse,
    @NotNull MiraMatchModel<Pulse> match,
    @NotNull String map_label );
}
