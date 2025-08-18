package sbs.mira.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
  
  private @Nullable Pulse pulse;
  
  protected
  MiraModule(@NotNull Pulse pulse)
  {
    this.breathe(pulse);
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
    assert this.pulse != null;
    return this.pulse;
  }
  
  @Override
  public
  void breathe(@NotNull Pulse pulse) throws IllegalStateException
  {
    if (this.pulse == null)
    {
      this.pulse = pulse;
    }
    else
    {
      throw new IllegalStateException("a breather may not have two pulses.");
    }
  }
}
