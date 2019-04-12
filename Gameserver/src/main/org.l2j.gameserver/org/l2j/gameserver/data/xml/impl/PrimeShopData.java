package org.l2j.gameserver.data.xml.impl;

import org.l2j.gameserver.datatables.ItemTable;
import org.l2j.gameserver.model.StatsSet;
import org.l2j.gameserver.model.actor.instance.L2PcInstance;
import org.l2j.gameserver.model.items.L2Item;
import org.l2j.gameserver.model.primeshop.PrimeShopGroup;
import org.l2j.gameserver.model.primeshop.PrimeShopItem;
import org.l2j.gameserver.network.serverpackets.primeshop.ExBRProductInfo;
import org.l2j.gameserver.settings.ServerSettings;
import org.l2j.gameserver.util.IGameXmlReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.l2j.commons.configuration.Configurator.getSettings;


/**
 * @author Gnacik, UnAfraid
 */
public class PrimeShopData extends IGameXmlReader{
    private static final Logger LOGGER = LoggerFactory.getLogger(PrimeShopData.class);

    private final Map<Integer, PrimeShopGroup> _primeItems = new LinkedHashMap<>();

    private PrimeShopData() {
        load();
    }

    @Override
    protected Path getSchemaFilePath() {
        return getSettings(ServerSettings.class).dataPackDirectory().resolve("data/xsd/primeShop.xsd");
    }

    @Override
    public void load() {
        _primeItems.clear();
        parseDatapackFile("data/primeShop.xml");

        if (!_primeItems.isEmpty()) {
            LOGGER.info("Loaded {} items", _primeItems.size());
        } else {
            LOGGER.info("System is disabled.");
        }
    }

    @Override
    public void parseDocument(Document doc, File f) {
        for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling()) {
            if ("list".equalsIgnoreCase(n.getNodeName())) {
                final NamedNodeMap at = n.getAttributes();
                final Node attribute = at.getNamedItem("enabled");
                if ((attribute != null) && Boolean.parseBoolean(attribute.getNodeValue())) {
                    for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
                        if ("item".equalsIgnoreCase(d.getNodeName())) {
                            NamedNodeMap attrs = d.getAttributes();
                            Node att;
                            final StatsSet set = new StatsSet();
                            for (int i = 0; i < attrs.getLength(); i++) {
                                att = attrs.item(i);
                                set.set(att.getNodeName(), att.getNodeValue());
                            }

                            final List<PrimeShopItem> items = new ArrayList<>();
                            for (Node b = d.getFirstChild(); b != null; b = b.getNextSibling()) {
                                if ("item".equalsIgnoreCase(b.getNodeName())) {
                                    attrs = b.getAttributes();

                                    final int itemId = parseInteger(attrs, "itemId");
                                    final int count = parseInteger(attrs, "count");

                                    final L2Item item = ItemTable.getInstance().getTemplate(itemId);
                                    if (item == null) {
                                        LOGGER.error(": Item template null for itemId: " + itemId + " brId: " + set.getInt("id"));
                                        return;
                                    }

                                    items.add(new PrimeShopItem(itemId, count, item.getWeight(), item.isTradeable() ? 1 : 0));
                                }
                            }

                            _primeItems.put(set.getInt("id"), new PrimeShopGroup(set, items));
                        }
                    }
                }
            }
        }
    }

    public void showProductInfo(L2PcInstance player, int brId) {
        final PrimeShopGroup item = _primeItems.get(brId);

        if ((player == null) || (item == null)) {
            return;
        }

        player.sendPacket(new ExBRProductInfo(item, player));
    }

    public PrimeShopGroup getItem(int brId) {
        return _primeItems.get(brId);
    }

    public Map<Integer, PrimeShopGroup> getPrimeItems() {
        return _primeItems;
    }

    public static PrimeShopData getInstance() {
        return Singleton.INSTANCE;
    }

    private static class Singleton {
        private static final PrimeShopData INSTANCE = new PrimeShopData();
    }
}
