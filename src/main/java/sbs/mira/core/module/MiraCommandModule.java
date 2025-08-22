package sbs.mira.core.module;

import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandUsageException;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import sbs.mira.core.MiraModule;
import sbs.mira.core.MiraPulse;

/***
 * created on 2025-08-18.
 *
 * @author jj.mira.sbs
 * @author jd.mira.sbs
 * @see sbs.mira.core.MiraPulse
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
  
  /**
   *
   * @param args
   * @param sender
   * @throws CommandException
   */
  public abstract
  void execute( @NotNull CommandContext args, @NotNull CommandSender sender )
  throws com.sk89q.minecraft.util.commands.CommandException, CommandException;
}
