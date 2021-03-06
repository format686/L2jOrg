package org.l2j.gameserver.data.xml.impl;

import org.l2j.gameserver.enums.ClanHallGrade;
import org.l2j.gameserver.enums.ClanHallType;
import org.l2j.gameserver.model.L2Clan;
import org.l2j.gameserver.model.Location;
import org.l2j.gameserver.model.StatsSet;
import org.l2j.gameserver.model.actor.instance.L2DoorInstance;
import org.l2j.gameserver.model.entity.ClanHall;
import org.l2j.gameserver.model.holders.ClanHallTeleportHolder;
import org.l2j.gameserver.settings.ServerSettings;
import org.l2j.gameserver.util.IGameXmlReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static org.l2j.commons.configuration.Configurator.getSettings;

/**
 * @author St3eT
 */
public final class ClanHallData extends IGameXmlReader{
    private static final Logger LOGGER = LoggerFactory.getLogger(ClanHallData.class);
    private static final Map<Integer, ClanHall> _clanHalls = new HashMap<>();

    private ClanHallData() {
        load();
    }

    @Override
    protected Path getSchemaFilePath() {
        return getSettings(ServerSettings.class).dataPackDirectory().resolve("data/xsd/clanHall.xsd");
    }

    @Override
    public void load() {
        parseDatapackDirectory("data/residences/clanHalls", true);
        LOGGER.info(getClass().getSimpleName() + ": Succesfully loaded " + _clanHalls.size() + " Clan Halls.");
    }

    @Override
    public void parseDocument(Document doc, File f) {
        final List<L2DoorInstance> doors = new ArrayList<>();
        final List<Integer> npcs = new ArrayList<>();
        final List<ClanHallTeleportHolder> teleports = new ArrayList<>();
        final StatsSet params = new StatsSet();

        for (Node listNode = doc.getFirstChild(); listNode != null; listNode = listNode.getNextSibling()) {
            if ("list".equals(listNode.getNodeName())) {
                for (Node clanHallNode = listNode.getFirstChild(); clanHallNode != null; clanHallNode = clanHallNode.getNextSibling()) {
                    if ("clanHall".equals(clanHallNode.getNodeName())) {
                        params.set("id", parseInteger(clanHallNode.getAttributes(), "id"));
                        params.set("name", parseString(clanHallNode.getAttributes(), "name", "None"));
                        params.set("grade", parseEnum(clanHallNode.getAttributes(), ClanHallGrade.class, "grade", ClanHallGrade.GRADE_NONE));
                        params.set("type", parseEnum(clanHallNode.getAttributes(), ClanHallType.class, "type", ClanHallType.OTHER));

                        for (Node tpNode = clanHallNode.getFirstChild(); tpNode != null; tpNode = tpNode.getNextSibling()) {
                            switch (tpNode.getNodeName()) {
                                case "auction": {
                                    final NamedNodeMap at = tpNode.getAttributes();
                                    params.set("minBid", parseInteger(at, "minBid"));
                                    params.set("lease", parseInteger(at, "lease"));
                                    params.set("deposit", parseInteger(at, "deposit"));
                                    break;
                                }
                                case "npcs": {
                                    for (Node npcNode = tpNode.getFirstChild(); npcNode != null; npcNode = npcNode.getNextSibling()) {
                                        if ("npc".equals(npcNode.getNodeName())) {
                                            final NamedNodeMap np = npcNode.getAttributes();
                                            final int npcId = parseInteger(np, "id");
                                            npcs.add(npcId);
                                        }
                                    }
                                    params.set("npcList", npcs);
                                    break;
                                }
                                case "doorlist": {
                                    for (Node npcNode = tpNode.getFirstChild(); npcNode != null; npcNode = npcNode.getNextSibling()) {
                                        if ("door".equals(npcNode.getNodeName())) {
                                            final NamedNodeMap np = npcNode.getAttributes();
                                            final int doorId = parseInteger(np, "id");
                                            final L2DoorInstance door = DoorData.getInstance().getDoor(doorId);
                                            if (door != null) {
                                                doors.add(door);
                                            }
                                        }
                                    }
                                    params.set("doorList", doors);
                                    break;
                                }
                                case "teleportList": {
                                    for (Node npcNode = tpNode.getFirstChild(); npcNode != null; npcNode = npcNode.getNextSibling()) {
                                        if ("teleport".equals(npcNode.getNodeName())) {
                                            final NamedNodeMap np = npcNode.getAttributes();
                                            final int npcStringId = parseInteger(np, "npcStringId");
                                            final int x = parseInteger(np, "x");
                                            final int y = parseInteger(np, "y");
                                            final int z = parseInteger(np, "z");
                                            final int minFunctionLevel = parseInteger(np, "minFunctionLevel");
                                            final int cost = parseInteger(np, "cost");
                                            teleports.add(new ClanHallTeleportHolder(npcStringId, x, y, z, minFunctionLevel, cost));
                                        }
                                    }
                                    params.set("teleportList", teleports);
                                    break;
                                }
                                case "ownerRestartPoint": {
                                    final NamedNodeMap ol = tpNode.getAttributes();
                                    params.set("owner_loc", new Location(parseInteger(ol, "x"), parseInteger(ol, "y"), parseInteger(ol, "z")));
                                    break;
                                }
                                case "banishPoint": {
                                    final NamedNodeMap bl = tpNode.getAttributes();
                                    params.set("banish_loc", new Location(parseInteger(bl, "x"), parseInteger(bl, "y"), parseInteger(bl, "z")));
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        _clanHalls.put(params.getInt("id"), new ClanHall(params));
    }

    public ClanHall getClanHallById(int clanHallId) {
        return _clanHalls.get(clanHallId);
    }

    public Collection<ClanHall> getClanHalls() {
        return _clanHalls.values();
    }

    public ClanHall getClanHallByNpcId(int npcId) {
        return _clanHalls.values().stream().filter(ch -> ch.getNpcs().contains(npcId)).findFirst().orElse(null);
    }

    public ClanHall getClanHallByClan(L2Clan clan) {
        return _clanHalls.values().stream().filter(ch -> ch.getOwner() == clan).findFirst().orElse(null);
    }

    public ClanHall getClanHallByDoorId(int doorId) {
        final L2DoorInstance door = DoorData.getInstance().getDoor(doorId);
        return _clanHalls.values().stream().filter(ch -> ch.getDoors().contains(door)).findFirst().orElse(null);
    }

    public List<ClanHall> getFreeAuctionableHall() {
        return _clanHalls.values().stream().filter(ch -> (ch.getType() == ClanHallType.AUCTIONABLE) && (ch.getOwner() == null)).sorted(Comparator.comparingInt(ClanHall::getResidenceId)).collect(Collectors.toList());
    }

    public static ClanHallData getInstance() {
        return Singleton.INSTANCE;
    }

    private static class Singleton {
        private static final ClanHallData INSTANCE = new ClanHallData();
    }
}
