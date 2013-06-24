package com.github.hoqhuuep.islandcraft.bukkit.worldguard;

import org.bukkit.World;

import com.github.hoqhuuep.islandcraft.common.api.ICProtection;
import com.github.hoqhuuep.islandcraft.common.type.ICLocation;
import com.github.hoqhuuep.islandcraft.common.type.ICRegion;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class WorldGuardProtection implements ICProtection {
    private final WorldGuardPlugin worldGuard;

    public WorldGuardProtection(final WorldGuardPlugin worldGuard) {
        this.worldGuard = worldGuard;
    }

    @Override
    public final boolean addProtectedRegion(final ICRegion region, final String owner) {
        final ProtectedRegion protectedRegion = createRegion(region);

        // Set protected region flags
        final DefaultDomain owners = new DefaultDomain();
        owners.addPlayer(owner);
        protectedRegion.setOwners(owners);

        return addRegion(protectedRegion, worldGuard.getServer().getWorld(region.getWorld()));
    }

    private boolean addRegion(final ProtectedRegion region, final World world) {
        final RegionManager regionManager = worldGuard.getRegionManager(world);
        regionManager.addRegion(region);
        try {
            regionManager.save();
        } catch (ProtectionDatabaseException e) {
            // Undo
            regionManager.removeRegion(region.getId());
            return false;
        }
        return true;
    }

    @Override
    public final boolean addVisibleRegion(final String name, final ICRegion region) {
        final ProtectedRegion visibleRegion = createRegion(region);

        // Set visible region flags
        visibleRegion.setFlag(DefaultFlag.GREET_MESSAGE, "Welcome to " + name);
        visibleRegion.setFlag(DefaultFlag.FAREWELL_MESSAGE, "Now leaving " + name);

        return addRegion(visibleRegion, worldGuard.getServer().getWorld(region.getWorld()));
    }

    private static ProtectedRegion createRegion(final ICRegion region) {
        final ICLocation min = region.getMin();
        final ICLocation max = region.getMax();
        final BlockVector bv1 = new BlockVector(min.getX(), 0, min.getZ());
        final BlockVector bv2 = new BlockVector(max.getX(), 255, max.getZ());
        return new ProtectedCuboidRegion(regionId(region), bv1, bv2);
    }

    @Override
    public final boolean removeRegion(final ICRegion region) {
        final RegionManager regionManager = worldGuard.getRegionManager(worldGuard.getServer().getWorld(region.getWorld()));
        final ProtectedRegion protectedRegion = regionManager.getRegion(regionId(region));
        regionManager.removeRegion(protectedRegion.getId());
        try {
            regionManager.save();
        } catch (ProtectionDatabaseException e) {
            // Undo
            regionManager.addRegion(protectedRegion);
            return false;
        }
        return true;
    }

    @Override
    public final boolean renameRegion(final ICRegion region, final String title) {
        final RegionManager regionManager = worldGuard.getRegionManager(worldGuard.getServer().getWorld(region.getWorld()));
        final ProtectedRegion visibleRegion = regionManager.getRegion(regionId(region));
        final String oldGreetMessage = visibleRegion.getFlag(DefaultFlag.GREET_MESSAGE);
        final String oldFarewellMessage = visibleRegion.getFlag(DefaultFlag.FAREWELL_MESSAGE);
        visibleRegion.setFlag(DefaultFlag.GREET_MESSAGE, "Welcome to " + title);
        visibleRegion.setFlag(DefaultFlag.FAREWELL_MESSAGE, "Now leaving " + title);
        try {
            regionManager.save();
        } catch (ProtectionDatabaseException e) {
            // Undo
            visibleRegion.setFlag(DefaultFlag.GREET_MESSAGE, oldGreetMessage);
            visibleRegion.setFlag(DefaultFlag.FAREWELL_MESSAGE, oldFarewellMessage);
            return false;
        }
        return true;
    }

    private static String regionId(final ICRegion region) {
        final ICLocation min = region.getMin();
        final ICLocation max = region.getMax();
        return min.getX() + ":" + min.getZ() + ":" + max.getX() + ":" + max.getZ();
    }
}
