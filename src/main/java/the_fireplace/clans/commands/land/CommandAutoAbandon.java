package the_fireplace.clans.commands.land;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import the_fireplace.clans.cache.ClanCache;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.logic.ClanManagementLogic;
import the_fireplace.clans.model.Clan;
import the_fireplace.clans.model.EnumRank;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandAutoAbandon extends ClanSubCommand {
	@Override
	public String getName() {
		return "autoabandon";
	}

	@Override
	public EnumRank getRequiredClanRank() {
		return EnumRank.ADMIN;
	}

	@Override
	public int getMinArgs() {
		return 0;
	}

	@Override
	public int getMaxArgs() {
		return 0;
	}

	@Override
	public void run(MinecraftServer server, EntityPlayerMP sender, String[] args) {
        Clan rm = ClanCache.autoAbandonClaims.remove(sender.getUniqueID());
		if(rm == null) {
            ClanCache.autoAbandonClaims.put(sender.getUniqueID(), selectedClan);
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.autoabandon.start", selectedClan.getName()).setStyle(TextStyles.GREEN));
			ClanManagementLogic.checkAndAttemptAbandon(sender, selectedClan);
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.autoabandon.stop", rm.getName()).setStyle(TextStyles.GREEN));
	}
}
