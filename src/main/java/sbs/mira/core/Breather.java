package sbs.mira.core;

import org.jetbrains.annotations.NotNull;

/**
 * 🫀
 *
 * @author jj.mira.sbs
 * @version 1.0.0
 * @since 1.0.0
 */
public
interface Breather<Pulse extends MiraPulse<?, ?>>
{
  
  /**
   * @return heartbeat of mira, still going i hope.
   * @throws sbs.mira.core.MiraPulse.FlatlineException bruh...
   */
  @NotNull
  Pulse pulse() throws MiraPulse.FlatlineException;
}
