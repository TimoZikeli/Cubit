package de.keks.internal.command.admin;

import static de.keks.internal.I18n.translate;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import de.keks.internal.ConfigValues;
import de.keks.internal.I18n;
import de.keks.internal.core.cubli.InternalBlockHighlight;
import de.keks.internal.core.tasks.RegionSaveTask;
import de.keks.internal.register.CommandSetupAdmin;
import de.keks.internal.register.CubitCore;

/**
 * Copyright:
 * <ul>
 * <li>Autor: Kekshaus</li>
 * <li>2016</li>
 * <li>www.minegaming.de</li>
 * </ul>
 * 
 */

public class CMD_Admin_Delete extends CubitCore {
	public CMD_Admin_Delete(CommandSetupAdmin handler) {
		super(true);
		this.setupAdmin = handler;
	}

	@Override
	public boolean execute(final CommandSender sender, final String[] args) {
		if (sender.hasPermission("cubit.admin.delete")) {

			final Player player = (Player) sender;
			final Location playerLocation = player.getLocation();
			final int chunkX = player.getLocation().getChunk().getX();
			final int chunkZ = player.getLocation().getChunk().getZ();
			final World world = player.getWorld();

			setupAdmin.executorServiceCommands.submit(new Runnable() {
				public void run() {
					Player player = (Player) sender;
					RegionManager manager = getWorldGuard().getRegionManager(world);

					String regionName = getRegionName(chunkX, chunkZ, world);

					if (!manager.hasRegion(regionName)) {
						player.sendMessage(translate("messages.noRegionHere"));
						return;
					}

					ProtectedRegion region = getRegion(world, regionName);
					if (region == null) {
						player.sendMessage(translate("messages.noRegionHere"));
						return;
					}
					String owner = region.getOwners().toUserFriendlyString();

					manager.removeRegion(regionName);
					moneyTransfer(null, player, calculateCosts(player, world, false));
					if (isSpigot()) {
						playEffect(player, Effect.INSTANT_SPELL, 1);
					}
					player.sendMessage(translate("messages.adminDeleteLand", regionName, owner));
					if (args.length < 2) {
						scheduleSyncTaskAdmin(setupAdmin, new InternalBlockHighlight(setupAdmin.getCubitInstance(),
								playerLocation.getChunk(), ConfigValues.landSellChunkBorders));
					} else if (args.length > 2 && !args[1].equalsIgnoreCase("empty")) {
						scheduleSyncTaskAdmin(setupAdmin, new InternalBlockHighlight(setupAdmin.getCubitInstance(),
								playerLocation.getChunk(), ConfigValues.landSellChunkBorders));
					}
					setupAdmin.executorServiceRegions.submit(new RegionSaveTask(getWorldGuard(), null, world));
				}
			});
		} else {
			sender.sendMessage(I18n.translate("messages.noPermission", new Object[0]));
		}
		return true;
	}
}
