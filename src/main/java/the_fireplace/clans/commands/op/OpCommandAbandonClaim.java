package the_fireplace.clans.commands.op;

import com.google.common.collect.Lists;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.ClanChunkCache;
import the_fireplace.clans.clan.ClanDatabase;
import the_fireplace.clans.commands.OpClanSubCommand;
import the_fireplace.clans.util.CapHelper;
import the_fireplace.clans.util.ChunkUtils;
import the_fireplace.clans.util.TextStyles;
import the_fireplace.clans.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OpCommandAbandonClaim extends OpClanSubCommand {
	@Override
	public int getMinArgs() {
		return 0;
	}

	@Override
	public int getMaxArgs() {
		return 1;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return TranslationUtil.getRawTranslationString(sender, "commands.opclan.abandonclaim.usage");
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) {
		Clan opClan = ClanDatabase.getOpClan();
		Chunk c = sender.getEntityWorld().getChunk(sender.getPosition());
		if(c.hasCapability(Clans.CLAIMED_LAND, null)){
			UUID claimFaction = CapHelper.getClaimedLandCapability(c).getClan();
			if(claimFaction != null) {
				Clan targetClan = ClanCache.getClanById(claimFaction);
				if(claimFaction.equals(opClan.getClanId()) || (args.length == 1 && args[0].toLowerCase().equals("force")) || targetClan == null) {
					if(targetClan != null) {
						if ((args.length == 1 && args[0].toLowerCase().equals("force")) || targetClan.isOpclan() || !Clans.cfg.forceConnectedClaims || !ChunkUtils.hasConnectedClaim(c, targetClan.getClanId())) {
							//Unset clan home if it is in the chunk
							OpCommandAbandonClaim.abandonClaim(sender, c, targetClan);
							ChunkUtils.clearChunkOwner(c);
							sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.opclan.abandonclaim.success", targetClan.getClanName()).setStyle(TextStyles.GREEN));
						} else {//We are forcing connected claims and there is a claim connected
							//Prevent creation of disconnected claims
							abandonClaimWithAdjacencyCheck(sender, c, targetClan);
						}
					} else {
						ChunkUtils.clearChunkOwner(c);
						sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.abandonclaim.success").setStyle(TextStyles.GREEN));
					}
				} else
					sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.opclan.abandonclaim.wrongclan", opClan.getClanName(), targetClan.getClanName()).setStyle(TextStyles.RED));
			} else
				sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.clan.abandonclaim.notclaimed").setStyle(TextStyles.RED));
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "clans.error.nochunkcap").setStyle(TextStyles.RED));
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		return args.length == 1 ? Collections.singletonList("force") : Collections.emptyList();
	}

	public static void abandonClaim(EntityPlayerMP sender, Chunk c, Clan targetClan) {
		//Unset clan home if it is in the chunk
		if (targetClan.getHome() != null
				&& targetClan.hasHome()
				&& sender.dimension == targetClan.getHomeDim()
				&& targetClan.getHome().getX() >= c.getPos().getXStart()
				&& targetClan.getHome().getX() <= c.getPos().getXEnd()
				&& targetClan.getHome().getZ() >= c.getPos().getZStart()
				&& targetClan.getHome().getZ() <= c.getPos().getZEnd()) {
			targetClan.unsetHome();
		}

		ClanChunkCache.delChunk(targetClan, c.x, c.z, c.getWorld().provider.getDimension());
		targetClan.subClaimCount();
		Clans.getPaymentHandler().addAmount(Clans.cfg.claimChunkCost, targetClan.getClanId());
	}

	public static void abandonClaimWithAdjacencyCheck(EntityPlayerMP sender, Chunk c, Clan targetClan) {
		boolean allowed = true;
		for (Chunk checkChunk : ChunkUtils.getConnectedClaims(c, targetClan.getClanId()))
			if (ChunkUtils.getConnectedClaims(checkChunk, targetClan.getClanId()).equals(Lists.newArrayList(c))) {
				allowed = false;
				break;
			}
		if (allowed) {
			//Unset clan home if it is in the chunk
			OpCommandAbandonClaim.abandonClaim(sender, c, targetClan);
			ChunkUtils.clearChunkOwner(c);
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.opclan.abandonclaim.success", targetClan.getClanName()).setStyle(TextStyles.GREEN));
		} else
			sender.sendMessage(TranslationUtil.getTranslation(sender.getUniqueID(), "commands.opclan.abandonclaim.disconnected").setStyle(TextStyles.RED));
	}

	@Override
	protected boolean allowConsoleUsage() {
		return false;
	}
}
