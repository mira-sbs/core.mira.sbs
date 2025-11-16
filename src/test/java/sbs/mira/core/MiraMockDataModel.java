package sbs.mira.core;

import org.jetbrains.annotations.NotNull;
import sbs.mira.core.model.MiraPluginDataModel;

public
class MiraMockDataModel
  extends MiraPluginDataModel<MiraMockPulse, MiraMockPlayer>
{
  public
  MiraMockDataModel( @NotNull MiraMockPulse pulse )
  {
    super( pulse );
  }
}
