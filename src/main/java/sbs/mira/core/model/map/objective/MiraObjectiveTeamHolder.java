package sbs.mira.core.model.map.objective;

import org.jetbrains.annotations.NotNull;
import sbs.mira.core.model.map.MiraObjective;
import sbs.mira.core.model.map.MiraTeamModel;

public
interface MiraObjectiveTeamHolder
  extends MiraObjective
{
  @NotNull
  String team_label( );
}
