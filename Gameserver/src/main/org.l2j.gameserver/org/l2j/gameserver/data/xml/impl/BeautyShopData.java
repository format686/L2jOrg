package org.l2j.gameserver.data.xml.impl;

import org.l2j.gameserver.enums.Race;
import org.l2j.gameserver.enums.Sex;
import org.l2j.gameserver.model.StatsSet;
import org.l2j.gameserver.model.beautyshop.BeautyData;
import org.l2j.gameserver.model.beautyshop.BeautyItem;
import org.l2j.gameserver.settings.ServerSettings;
import org.l2j.gameserver.util.IGameXmlReader;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.l2j.commons.configuration.Configurator.getSettings;

/**
 * @author Sdw
 */
public final class BeautyShopData extends IGameXmlReader{
    private final Map<Race, Map<Sex, BeautyData>> _beautyList = new HashMap<>();
    private final Map<Sex, BeautyData> _beautyData = new HashMap<>();

    private BeautyShopData() {
        load();
    }

    @Override
    protected Path getSchemaFilePath() {
        return getSettings(ServerSettings.class).dataPackDirectory().resolve("data/xsd/BeautyShop.xsd");
    }

    @Override
    public synchronized void load() {
        _beautyList.clear();
        _beautyData.clear();
        parseDatapackFile("data/BeautyShop.xml");
    }

    @Override
    public void parseDocument(Document doc, File f) {
        NamedNodeMap attrs;
        StatsSet set;
        Node att;
        Race race = null;
        Sex sex = null;

        for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling()) {
            if ("list".equalsIgnoreCase(n.getNodeName())) {
                for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
                    if ("race".equalsIgnoreCase(d.getNodeName())) {
                        att = d.getAttributes().getNamedItem("type");
                        if (att != null) {
                            race = parseEnum(att, Race.class);
                        }

                        for (Node b = d.getFirstChild(); b != null; b = b.getNextSibling()) {
                            if ("sex".equalsIgnoreCase(b.getNodeName())) {
                                att = b.getAttributes().getNamedItem("type");
                                if (att != null) {
                                    sex = parseEnum(att, Sex.class);
                                }

                                final BeautyData beautyData = new BeautyData();

                                for (Node a = b.getFirstChild(); a != null; a = a.getNextSibling()) {
                                    if ("hair".equalsIgnoreCase(a.getNodeName())) {
                                        attrs = a.getAttributes();
                                        set = new StatsSet();
                                        for (int i = 0; i < attrs.getLength(); i++) {
                                            att = attrs.item(i);
                                            set.set(att.getNodeName(), att.getNodeValue());
                                        }
                                        final BeautyItem hair = new BeautyItem(set);

                                        for (Node g = a.getFirstChild(); g != null; g = g.getNextSibling()) {
                                            if ("color".equalsIgnoreCase(g.getNodeName())) {
                                                attrs = g.getAttributes();
                                                set = new StatsSet();
                                                for (int i = 0; i < attrs.getLength(); i++) {
                                                    att = attrs.item(i);
                                                    set.set(att.getNodeName(), att.getNodeValue());
                                                }
                                                hair.addColor(set);
                                            }
                                        }
                                        beautyData.addHair(hair);
                                    } else if ("face".equalsIgnoreCase(a.getNodeName())) {
                                        attrs = a.getAttributes();
                                        set = new StatsSet();
                                        for (int i = 0; i < attrs.getLength(); i++) {
                                            att = attrs.item(i);
                                            set.set(att.getNodeName(), att.getNodeValue());
                                        }
                                        final BeautyItem face = new BeautyItem(set);
                                        beautyData.addFace(face);
                                    }
                                }

                                _beautyData.put(sex, beautyData);
                            }
                        }
                        _beautyList.put(race, _beautyData);
                    }
                }
            }
        }
    }

    public boolean hasBeautyData(Race race, Sex sex) {
        return _beautyList.containsKey(race) && _beautyList.get(race).containsKey(sex);
    }

    public BeautyData getBeautyData(Race race, Sex sex) {
        if (_beautyList.containsKey(race)) {
            return _beautyList.get(race).get(sex);
        }
        return null;
    }

    public static BeautyShopData getInstance() {
        return Singleton.INSTANCE;
    }

    private static class Singleton {
        private static final BeautyShopData INSTANCE = new BeautyShopData();
    }
}
