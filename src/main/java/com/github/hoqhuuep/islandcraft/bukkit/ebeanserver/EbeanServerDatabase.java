package com.github.hoqhuuep.islandcraft.bukkit.ebeanserver;

import java.util.ArrayList;
import java.util.List;

import com.avaje.ebean.EbeanServer;
import com.github.hoqhuuep.islandcraft.common.api.ICDatabase;
import com.github.hoqhuuep.islandcraft.common.extras.BetterCompassTarget;
import com.github.hoqhuuep.islandcraft.common.type.ICIsland;
import com.github.hoqhuuep.islandcraft.common.type.ICLocation;

public class EbeanServerDatabase implements ICDatabase {
    private final EbeanServer ebean;

    public EbeanServerDatabase(final EbeanServer ebean) {
        this.ebean = ebean;
    }

    public static List<Class<?>> getDatabaseClasses() {
        final List<Class<?>> list = new ArrayList<Class<?>>();
        list.add(DeathPointBean.class);
        list.add(CompassTargetBean.class);
        list.add(IslandBean.class);
        list.add(PartyBean.class);
        list.add(WaypointBean.class);
        list.add(IslandSeedBean.class);
        return list;
    }

    @Override
    public final ICLocation loadDeathPoint(final String player) {
        final DeathPointBean bean = loadDeathPointBean(player);
        if (bean == null) {
            return null;
        }
        return new ICLocation(bean.getWorld(), bean.getX().intValue(), bean.getZ().intValue());
    }

    @Override
    public final void saveDeathPoint(final String player, final ICLocation deathPoint) {
        if (deathPoint == null) {
            final DeathPointBean bean = loadDeathPointBean(player);
            ebean.delete(bean);
            return;
        }
        // Override if exists
        DeathPointBean bean = loadDeathPointBean(player);
        if (bean == null) {
            bean = new DeathPointBean();
            bean.setPlayer(player);
        }
        bean.setWorld(deathPoint.getWorld());
        bean.setX(new Integer(deathPoint.getX()));
        bean.setZ(new Integer(deathPoint.getZ()));
        ebean.save(bean);
    }

    private DeathPointBean loadDeathPointBean(final String player) {
        return ebean.find(DeathPointBean.class).where().ieq("player", player).findUnique();
    }

    @Override
    public final String loadParty(final String player) {
        final PartyBean bean = loadPartyBean(player);
        if (bean == null) {
            return null;
        }
        return bean.getParty();
    }

    @Override
    public final List<String> loadPartyMembers(final String party) {
        final List<PartyBean> beans = ebean.find(PartyBean.class).where().ieq("party", party).findList();
        final List<String> members = new ArrayList<String>(beans.size());
        for (PartyBean bean : beans) {
            members.add(bean.getPlayer());
        }
        return members;
    }

    @Override
    public final void saveParty(final String player, final String party) {
        if (party == null) {
            final PartyBean bean = loadPartyBean(player);
            ebean.delete(bean);
            return;
        }
        // Override if exists
        PartyBean bean = loadPartyBean(player);
        if (bean == null) {
            bean = new PartyBean();
            bean.setPlayer(player);
        }
        bean.setParty(party);
        ebean.save(bean);
    }

    private PartyBean loadPartyBean(final String player) {
        return ebean.find(PartyBean.class).where().ieq("player", player).findUnique();
    }

    @Override
    public final BetterCompassTarget loadCompassTarget(final String player) {
        final CompassTargetBean bean = loadCompassTargetBean(player);
        if (bean == null) {
            return null;
        }
        return BetterCompassTarget.valueOf(bean.getTarget());
    }

    @Override
    public final void saveCompassTarget(final String player, final BetterCompassTarget target) {
        if (target == null) {
            final CompassTargetBean bean = loadCompassTargetBean(player);
            ebean.delete(bean);
            return;
        }
        // Override if exists
        CompassTargetBean bean = loadCompassTargetBean(player);
        if (bean == null) {
            bean = new CompassTargetBean();
            bean.setPlayer(player);
        }
        bean.setTarget(target.toString());
        ebean.save(bean);
    }

    private CompassTargetBean loadCompassTargetBean(final String player) {
        final String name = player;
        return ebean.find(CompassTargetBean.class).where().ieq("player", name).findUnique();
    }

    @Override
    public final ICIsland loadIsland(final ICLocation location) {
        final IslandBean bean = loadIslandBean(location);
        if (bean == null) {
            return null;
        }
        return new ICIsland(location, bean.getOwner());
    }

    @Override
    public final List<ICIsland> loadIslands(final String owner) {
        final List<IslandBean> beans = ebean.find(IslandBean.class).where().ieq("owner", owner).findList();
        final List<ICIsland> islands = new ArrayList<ICIsland>(beans.size());
        for (IslandBean bean : beans) {
            String[] args = bean.getLocation().split("(\\s*ICLocation\\s*\\(\\s*\")|(\"\\s*,\\s*)|(\\s*,\\s*)|(\\s*\\)\\s*)");
            int x = Integer.valueOf(args[2]).intValue();
            int z = Integer.valueOf(args[3]).intValue();
            islands.add(new ICIsland(new ICLocation(args[1], x, z), owner));
        }
        return islands;
    }

    @Override
    public final void saveIsland(final ICIsland island) {
        // Override if exists
        IslandBean bean = loadIslandBean(island.getLocation());
        if (bean == null) {
            bean = new IslandBean();
            bean.setLocation(island.getLocation().toString());
        }
        bean.setOwner(island.getOwner());
        ebean.save(bean);
    }

    private IslandBean loadIslandBean(final ICLocation location) {
        return ebean.find(IslandBean.class).where().ieq("location", location.toString()).findUnique();
    }

    @Override
    public List<String> loadWaypoints(String player) {
        final List<WaypointBean> beans = ebean.find(WaypointBean.class).where().istartsWith("name", player + ":").findList();
        final List<String> waypoints = new ArrayList<String>(beans.size());
        for (WaypointBean bean : beans) {
            waypoints.add(bean.getName().replaceFirst("[^:]*:", ""));
        }
        return waypoints;
    }

    @Override
    public void saveWaypoint(final String player, final String name, final ICLocation location) {
        if (location == null) {
            final WaypointBean bean = loadWaypointBean(player, name);
            ebean.delete(bean);
            return;
        }
        // Override if exists
        WaypointBean bean = loadWaypointBean(player, name);
        if (bean == null) {
            bean = new WaypointBean();
            bean.setName(player + ":" + name);
        }
        bean.setWorld(location.getWorld());
        bean.setX(new Integer(location.getX()));
        bean.setZ(new Integer(location.getZ()));
        ebean.save(bean);
    }

    private WaypointBean loadWaypointBean(final String player, final String name) {
        return ebean.find(WaypointBean.class).where().ieq("name", player + ":" + name).findUnique();
    }

    @Override
    public ICLocation loadWaypoint(final String player, final String name) {
        final WaypointBean bean = ebean.find(WaypointBean.class).where().ieq("name", player + ":" + name).findUnique();
        if (bean == null) {
            return null;
        }
        return new ICLocation(bean.getWorld(), bean.getX().intValue(), bean.getZ().intValue());
    }

    @Override
    public void saveIslandSeed(ICLocation location, Long seed) {
        if (seed == null) {
            final IslandSeedBean bean = loadIslandSeedBean(location);
            ebean.delete(bean);
            return;
        }
        IslandSeedBean bean = loadIslandSeedBean(location);
        if (bean == null) {
            bean = new IslandSeedBean();
            bean.setLocation(location.toString());
        }
        bean.setSeed(seed);
        ebean.save(bean);
    }

    @Override
    public Long loadIslandSeed(ICLocation location) {
        final IslandSeedBean bean = loadIslandSeedBean(location);
        if (bean == null) {
            return null;
        }
        return bean.getSeed();
    }

    private IslandSeedBean loadIslandSeedBean(final ICLocation location) {
        return ebean.find(IslandSeedBean.class).where().ieq("location", location.toString()).findUnique();
    }
}
