package the_fireplace.clans.commands.op;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.ClanDatabase;
import the_fireplace.clans.commands.OpClanSubCommand;
import the_fireplace.clans.util.MinecraftColors;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OpCommandSetName extends OpClanSubCommand {
	@Override
	public int getMinArgs() {
		return 1;
	}

	@Override
	public int getMaxArgs() {
		return 1;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/opclan [target clan] setname <newname>";
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) {
		String newName = args[0];
		if(!ClanCache.clanNameTaken(newName)) {
			if(opSelectedClan == null) {
				ClanDatabase.getOpClan().setClanName(newName);
				sender.sendMessage(new TextComponentString(MinecraftColors.GREEN + "Opclan name set!"));
			} else {
				String oldName = opSelectedClan.getClanName();
				opSelectedClan.setClanName(newName);
				sender.sendMessage(new TextComponentTranslation(MinecraftColors.GREEN + "%s renamed to %s!", oldName, newName));
			}
		} else
			sender.sendMessage(new TextComponentString(MinecraftColors.RED + "The clan name you have specified is already taken."));
	}
}