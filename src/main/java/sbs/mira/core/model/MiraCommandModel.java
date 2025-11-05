package sbs.mira.core.model;

import org.jetbrains.annotations.NotNull;
import sbs.mira.core.MiraModel;
import sbs.mira.core.MiraPulse;

/***
 * created on 2025-08-18.
 *
 * @author jj.mira.sbs
 * @author jd.mira.sbs
 * @see MiraPulse
 */
public abstract
class MiraCommandModel<Pulse extends MiraPulse<?, ?>>
  extends MiraModel<Pulse>
{
  /**
   *
   * @param pulse
   */
  protected
  MiraCommandModel( @NotNull Pulse pulse )
  {
    super( pulse );
  }
}
