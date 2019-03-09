package org.l2j.gameserver.mobius.gameserver.model.stats;

import org.l2j.commons.util.IXmlReader;
import org.l2j.gameserver.mobius.gameserver.model.actor.L2Character;
import org.l2j.gameserver.mobius.gameserver.util.IGameXmlReader;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;

import java.io.File;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

/**
 * @author DS, Sdw, UnAfraid
 */
public enum BaseStats
{
    STR(Stats.STAT_STR),
    INT(Stats.STAT_INT),
    DEX(Stats.STAT_DEX),
    WIT(Stats.STAT_WIT),
    CON(Stats.STAT_CON),
    MEN(Stats.STAT_MEN);

    public static final int MAX_STAT_VALUE = 201;

    private final double[] _bonus = new double[MAX_STAT_VALUE];
    private final Stats _stat;

    BaseStats(Stats stat)
    {
        _stat = stat;
    }

    public Stats getStat()
    {
        return _stat;
    }

    public int calcValue(L2Character creature)
    {
        if ((creature != null) && (_stat != null))
        {
            // return (int) Math.min(_stat.finalize(creature, Optional.empty()), MAX_STAT_VALUE - 1);
            return (int) creature.getStat().getValue(_stat);
        }
        return 0;
    }

    public double calcBonus(L2Character creature)
    {
        if (creature != null)
        {
            final int value = calcValue(creature);
            if (value < 1)
            {
                return 1;
            }
            return _bonus[value];
        }

        return 1;
    }

    void setValue(int index, double value)
    {
        _bonus[index] = value;
    }

    public double getValue(int index)
    {
        return _bonus[index];
    }

    public static BaseStats valueOf(Stats stat)
    {
        for (BaseStats baseStat : values())
        {
            if (baseStat.getStat() == stat)
            {
                return baseStat;
            }
        }
        throw new NoSuchElementException("Unknown base stat '" + stat + "' for enum BaseStats");
    }

    static
    {
        new IGameXmlReader()
        {
            final Logger LOGGER = Logger.getLogger(BaseStats.class.getName());

            @Override
            public void load()
            {
                parseDatapackFile("data/stats/statBonus.xml");
            }

            @Override
            public void parseDocument(Document doc, File f)
            {
                forEach(doc, "list", listNode -> forEach(listNode, IXmlReader::isNode, statNode ->
                {
                    final BaseStats baseStat;
                    try
                    {
                        baseStat = valueOf(statNode.getNodeName());
                    }
                    catch (Exception e)
                    {
                        LOGGER.severe("Invalid base stats type: " + statNode.getNodeValue() + ", skipping");
                        return;
                    }

                    forEach(statNode, "stat", statValue ->
                    {
                        final NamedNodeMap attrs = statValue.getAttributes();
                        final int val = parseInteger(attrs, "value");
                        final double bonus = parseDouble(attrs, "bonus");
                        baseStat.setValue(val, bonus);
                    });
                }));
            }
        }.load();
    }
}