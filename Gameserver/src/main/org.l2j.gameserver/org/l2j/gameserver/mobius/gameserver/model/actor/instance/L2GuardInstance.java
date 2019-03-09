package org.l2j.gameserver.mobius.gameserver.model.actor.instance;

import org.l2j.gameserver.mobius.gameserver.Config;
import org.l2j.gameserver.mobius.gameserver.ai.CtrlIntention;
import org.l2j.gameserver.mobius.gameserver.enums.InstanceType;
import org.l2j.gameserver.mobius.gameserver.model.L2World;
import org.l2j.gameserver.mobius.gameserver.model.actor.L2Attackable;
import org.l2j.gameserver.mobius.gameserver.model.actor.L2Character;
import org.l2j.gameserver.mobius.gameserver.model.actor.templates.L2NpcTemplate;
import org.l2j.gameserver.mobius.gameserver.model.events.EventDispatcher;
import org.l2j.gameserver.mobius.gameserver.model.events.EventType;
import org.l2j.gameserver.mobius.gameserver.model.events.impl.character.npc.OnNpcFirstTalk;
import org.l2j.gameserver.mobius.gameserver.model.skills.Skill;
import org.l2j.gameserver.mobius.gameserver.network.serverpackets.ActionFailed;

/**
 * This class manages all Guards in the world. It inherits all methods from L2Attackable and adds some more such as tracking PK and aggressive L2MonsterInstance.
 */
public class L2GuardInstance extends L2Attackable
{
    /**
     * Constructor of L2GuardInstance (use L2Character and L2NpcInstance constructor).<br>
     * <B><U> Actions</U> :</B>
     * <ul>
     * <li>Call the L2Character constructor to set the _template of the L2GuardInstance (copy skills from template to object and link _calculators to NPC_STD_CALCULATOR)</li>
     * <li>Set the name of the L2GuardInstance</li>
     * <li>Create a RandomAnimation Task that will be launched after the calculated delay if the server allow it</li>
     * </ul>
     * @param template to apply to the NPC
     */
    public L2GuardInstance(L2NpcTemplate template)
    {
        super(template);
        setInstanceType(InstanceType.L2GuardInstance);
    }

    @Override
    public boolean isAutoAttackable(L2Character attacker)
    {
        if (attacker.isMonster() && !attacker.isFakePlayer())
        {
            return true;
        }
        return super.isAutoAttackable(attacker);
    }

    @Override
    public void addDamage(L2Character attacker, int damage, Skill skill)
    {
        super.addDamage(attacker, damage, skill);
        getAI().startFollow(attacker);
        addDamageHate(attacker, 0, 10);
        L2World.getInstance().forEachVisibleObjectInRange(this, L2GuardInstance.class, 500, guard ->
        {
            guard.getAI().startFollow(attacker);
            guard.addDamageHate(attacker, 0, 10);
        });
    }

    /**
     * Set the home location of its L2GuardInstance.
     */
    @Override
    public void onSpawn()
    {
        super.onSpawn();
        setRandomWalking(getTemplate().isRandomWalkEnabled());
        getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
        // check the region where this mob is, do not activate the AI if region is inactive.
        // final L2WorldRegion region = L2World.getInstance().getRegion(this);
        // if ((region != null) && (!region.isActive()))
        // {
        // getAI().stopAITask();
        // }
    }

    /**
     * Return the pathfile of the selected HTML file in function of the L2GuardInstance Identifier and of the page number.<br>
     * <B><U> Format of the pathfile </U> :</B>
     * <ul>
     * <li>if page number = 0 : <B>data/html/guard/12006.htm</B> (npcId-page number)</li>
     * <li>if page number > 0 : <B>data/html/guard/12006-1.htm</B> (npcId-page number)</li>
     * </ul>
     * @param npcId The Identifier of the L2NpcInstance whose text must be display
     * @param val The number of the page to display
     */
    @Override
    public String getHtmlPath(int npcId, int val)
    {
        String pom = "";
        if (val == 0)
        {
            pom = Integer.toString(npcId);
        }
        else
        {
            pom = npcId + "-" + val;
        }
        return "data/html/guard/" + pom + ".htm";
    }

    /**
     * Manage actions when a player click on the L2GuardInstance.<br>
     * <B><U> Actions on first click on the L2GuardInstance (Select it)</U> :</B>
     * <ul>
     * <li>Set the L2GuardInstance as target of the L2PcInstance player (if necessary)</li>
     * <li>Send a Server->Client packet MyTargetSelected to the L2PcInstance player (display the select window)</li>
     * <li>Set the L2PcInstance Intention to AI_INTENTION_IDLE</li>
     * <li>Send a Server->Client packet ValidateLocation to correct the L2GuardInstance position and heading on the client</li>
     * </ul>
     * <B><U> Actions on second click on the L2GuardInstance (Attack it/Interact with it)</U> :</B>
     * <ul>
     * <li>If L2PcInstance is in the _aggroList of the L2GuardInstance, set the L2PcInstance Intention to AI_INTENTION_ATTACK</li>
     * <li>If L2PcInstance is NOT in the _aggroList of the L2GuardInstance, set the L2PcInstance Intention to AI_INTENTION_INTERACT (after a distance verification) and show message</li>
     * </ul>
     * <B><U> Example of use </U> :</B>
     * <ul>
     * <li>Client packet : Action, AttackRequest</li>
     * </ul>
     * @param player The L2PcInstance that start an action on the L2GuardInstance
     */
    @Override
    public void onAction(L2PcInstance player, boolean interact)
    {
        if (!canTarget(player))
        {
            return;
        }

        if (Config.FACTION_SYSTEM_ENABLED && Config.FACTION_GUARDS_ENABLED && ((player.isGood() && getTemplate().isClan(Config.FACTION_EVIL_TEAM_NAME)) || (player.isEvil() && getTemplate().isClan(Config.FACTION_GOOD_TEAM_NAME))))
        {
            interact = false;
            // TODO: Fix normal targeting
            player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
        }

        if (isFakePlayer() && isInCombat())
        {
            interact = false;
            // TODO: Fix normal targeting
            player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
        }

        // Check if the L2PcInstance already target the L2GuardInstance
        if (getObjectId() != player.getTargetId())
        {
            // Set the target of the L2PcInstance player
            player.setTarget(this);
        }
        else if (interact)
        {
            // Check if the L2PcInstance is in the _aggroList of the L2GuardInstance
            if (containsTarget(player))
            {
                // Set the L2PcInstance Intention to AI_INTENTION_ATTACK
                player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
            }
            else
            {
                // Calculate the distance between the L2PcInstance and the L2NpcInstance
                if (!canInteract(player))
                {
                    // Set the L2PcInstance Intention to AI_INTENTION_INTERACT
                    player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
                }
                else
                {
                    player.setLastFolkNPC(this);

                    // Open a chat window on client with the text of the L2GuardInstance
                    if (hasListener(EventType.ON_NPC_QUEST_START))
                    {
                        player.setLastQuestNpcObject(getObjectId());
                    }

                    if (hasListener(EventType.ON_NPC_FIRST_TALK))
                    {
                        EventDispatcher.getInstance().notifyEventAsync(new OnNpcFirstTalk(this, player), this);
                    }
                    else
                    {
                        showChatWindow(player, 0);
                    }
                }
            }
        }
        // Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
        player.sendPacket(ActionFailed.STATIC_PACKET);
    }
}