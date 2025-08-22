package sbs.mira.core;

import org.jetbrains.annotations.NotNull;

/**
 * a moving cog within the mira framework.
 * slide it into place (as needed) to turn neighbouring gears.
 * created on 2017-03-20.
 *
 * @author jj.mira.sbs
 * @version 1.0.1
 * @since 1.0.0
 */
public abstract
class MiraModule<Pulse extends MiraPulse<?, ?>>
  implements Breather<Pulse>
{
  
  private final @NotNull Pulse pulse;
  
  protected
  MiraModule(@NotNull Pulse pulse)
  {
    this.pulse = pulse;
  }
  
  /**
   * don't get lost.
   *
   * @return stay with us.
   */
  @Override
  @NotNull
  public
  Pulse pulse()
  {
    return this.pulse;
  }
}
