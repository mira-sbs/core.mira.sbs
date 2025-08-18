package sbs.mira.core.module;

import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;
import sbs.mira.core.MiraModule;
import sbs.mira.core.MiraPulse;

/***
 * created on 2025-08-18.
 *
 * @author jj.mira.sbs
 * @author jd.mira.sbs
 * @see MiraPulse
 */
public abstract
class MiraCommandModule<Pulse extends MiraPulse<?, ?>>
  extends MiraModule<Pulse>
{
  /**
   *
   * @param pulse
   */
  protected
  MiraCommandModule( @NotNull Pulse pulse )
  {
    super( pulse );
  }
}
