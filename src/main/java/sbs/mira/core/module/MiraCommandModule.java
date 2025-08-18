package sbs.mira.core.module;

import com.sk89q.minecraft.util.commands.CommandContext;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import sbs.mira.core.MiraModule;
import sbs.mira.core.MiraPulse;

public abstract
class MiraCommandModule<Pulse extends MiraPulse<?, ?>>
  extends MiraModule<Pulse>
{
  protected
  MiraCommandModule(@NotNull Pulse pulse)
  {
    super(pulse);
  }
  
  public abstract
  void execute(@NotNull CommandContext args, @NotNull CommandSender sender) throws CommandException;
}
