package sbs.mira.core.model;

import org.jetbrains.annotations.NotNull;
import sbs.mira.core.MiraModel;
import sbs.mira.core.MiraPulse;

/***
 * tbd.
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
   * tbd.
   *
   * @param pulse tbd.
   */
  protected
  MiraCommandModel( @NotNull Pulse pulse )
  {
    super( pulse );
  }
}
