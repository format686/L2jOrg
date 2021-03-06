package org.l2j.gameserver.model;


import org.l2j.commons.util.Rnd;
import org.l2j.gameserver.Config;
import org.l2j.commons.threading.ThreadPoolManager;
import org.l2j.gameserver.data.xml.impl.NpcData;
import org.l2j.gameserver.geoengine.GeoEngine;
import org.l2j.gameserver.model.actor.L2Npc;
import org.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import org.l2j.gameserver.model.actor.templates.L2NpcTemplate;
import org.l2j.gameserver.model.interfaces.IIdentifiable;
import org.l2j.gameserver.model.interfaces.INamable;
import org.l2j.gameserver.model.spawns.NpcSpawnTemplate;
import org.l2j.gameserver.model.zone.ZoneId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;


/**
 * This class manages the spawn and respawn of a group of L2NpcInstance that are in the same are and have the same type.<br>
 * <B><U>Concept</U>:</B><br>
 * L2NpcInstance can be spawned either in a random position into a location area (if Lox=0 and Locy=0), either at an exact position.<br>
 * The heading of the L2NpcInstance can be a random heading if not defined (value= -1) or an exact heading (ex : merchant...).
 *
 * @author Nightmare
 */
public class L2Spawn extends Location implements IIdentifiable, INamable {
    protected static final Logger LOGGER = LoggerFactory.getLogger(L2Spawn.class);
    private final Deque<L2Npc> _spawnedNpcs = new ConcurrentLinkedDeque<>();
    /**
     * The current number of SpawnTask in progress or stand by of this L2Spawn
     */
    protected int _scheduledCount;
    /**
     * String identifier of this spawn
     */
    private String _name;
    /**
     * The link on the L2NpcTemplate object containing generic and static properties of this spawn (ex : RewardExp, RewardSP, AggroRange...)
     */
    private L2NpcTemplate _template;
    /**
     * The maximum number of L2NpcInstance that can manage this L2Spawn
     */
    private int _maximumCount;
    /**
     * The current number of L2NpcInstance managed by this L2Spawn
     */
    private int _currentCount;
    /**
     * The identifier of the location area where L2NpcInstance can be spawned
     */
    private int _locationId;
    /**
     * The spawn instance id
     */
    private int _instanceId = 0;
    /**
     * Minimum respawn delay
     */
    private int _respawnMinDelay;
    /**
     * Maximum respawn delay
     */
    private int _respawnMaxDelay;
    /**
     * The generic constructor of L2NpcInstance managed by this L2Spawn
     */
    private Constructor<? extends L2Npc> _constructor;
    /**
     * If True a L2NpcInstance is respawned each time that another is killed
     */
    private boolean _doRespawn = true;
    private boolean _randomWalk = false; // Is no random walk
    private NpcSpawnTemplate _spawnTemplate;

    /**
     * Constructor of L2Spawn.<br>
     * <B><U>Concept</U>:</B><br>
     * Each L2Spawn owns generic and static properties (ex : RewardExp, RewardSP, AggroRange...).<br>
     * All of those properties are stored in a different L2NpcTemplate for each type of L2Spawn. Each template is loaded once in the server cache memory (reduce memory use).<br>
     * When a new instance of L2Spawn is created, server just create a link between the instance and the template.<br>
     * This link is stored in <B>_template</B> Each L2NpcInstance is linked to a L2Spawn that manages its spawn and respawn (delay, location...).<br>
     * This link is stored in <B>_spawn</B> of the L2NpcInstance.<br>
     * <B><U> Actions</U>:</B><br>
     * <ul>
     * <li>Set the _template of the L2Spawn</li>
     * <li>Calculate the implementationName used to generate the generic constructor of L2NpcInstance managed by this L2Spawn</li>
     * <li>Create the generic constructor of L2NpcInstance managed by this L2Spawn</li>
     * </ul>
     *
     * @param template The L2NpcTemplate to link to this L2Spawn
     * @throws SecurityException
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws ClassCastException     when template type is not subclass of L2Npc
     */
    public L2Spawn(L2NpcTemplate template) throws SecurityException, ClassNotFoundException, NoSuchMethodException, ClassCastException {
        super(0, 0, 0);
        // Set the _template of the L2Spawn
        _template = template;

        if (_template == null) {
            return;
        }

        final String className = "org.l2j.gameserver.model.actor.instance." + _template.getType() + "Instance";

        // Create the generic constructor of L2Npc managed by this L2Spawn
        _constructor = Class.forName(className).asSubclass(L2Npc.class).getConstructor(L2NpcTemplate.class);
    }

    /**
     * Creates a new spawn.
     *
     * @param npcId the NPC ID
     * @throws SecurityException
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws ClassCastException
     */
    public L2Spawn(int npcId) throws SecurityException, ClassNotFoundException, NoSuchMethodException, ClassCastException {
        super(0, 0, 0);
        _template = Objects.requireNonNull(NpcData.getInstance().getTemplate(npcId), "NpcTemplate not found for NPC ID: " + npcId);

        final String className = "org.l2j.gameserver.model.actor.instance." + _template.getType() + "Instance";

        // Create the generic constructor of L2Npc managed by this L2Spawn
        _constructor = Class.forName(className).asSubclass(L2Npc.class).getConstructor(L2NpcTemplate.class);
    }

    /**
     * @return the maximum number of L2NpcInstance that this L2Spawn can manage.
     */
    public int getAmount() {
        return _maximumCount;
    }

    /**
     * Set the maximum number of L2NpcInstance that this L2Spawn can manage.
     *
     * @param amount
     */
    public void setAmount(int amount) {
        _maximumCount = amount;
    }

    /**
     * @return the String Identifier of this spawn.
     */
    @Override
    public String getName() {
        return _name;
    }

    /**
     * Set the String Identifier of this spawn.
     *
     * @param name
     */
    public void setName(String name) {
        _name = name;
    }

    /**
     * @return the Identifier of the location area where L2NpcInstance can be spawned.
     */
    public int getLocationId() {
        return _locationId;
    }

    /**
     * Set the Identifier of the location area where L2NpcInstance can be spawned.
     *
     * @param id
     */
    public void setLocationId(int id) {
        _locationId = id;
    }

    /**
     * Gets the NPC ID.
     *
     * @return the NPC ID
     */
    @Override
    public int getId() {
        return _template.getId();
    }

    /**
     * @return min respawn delay.
     */
    public int getRespawnMinDelay() {
        return _respawnMinDelay;
    }

    /**
     * Set Minimum Respawn Delay.
     *
     * @param date
     */
    public void setRespawnMinDelay(int date) {
        _respawnMinDelay = date;
    }

    /**
     * @return max respawn delay.
     */
    public int getRespawnMaxDelay() {
        return _respawnMaxDelay;
    }

    /**
     * Set Maximum Respawn Delay.
     *
     * @param date
     */
    public void setRespawnMaxDelay(int date) {
        _respawnMaxDelay = date;
    }

    /**
     * Decrease the current number of L2NpcInstance of this L2Spawn and if necessary create a SpawnTask to launch after the respawn Delay. <B><U> Actions</U> :</B>
     * <li>Decrease the current number of L2NpcInstance of this L2Spawn</li>
     * <li>Check if respawn is possible to prevent multiple respawning caused by lag</li>
     * <li>Update the current number of SpawnTask in progress or stand by of this L2Spawn</li>
     * <li>Create a new SpawnTask to launch after the respawn Delay</li> <FONT COLOR=#FF0000><B> <U>Caution</U> : A respawn is possible ONLY if _doRespawn=True and _scheduledCount + _currentCount < _maximumCount</B></FONT>
     *
     * @param oldNpc
     */
    public void decreaseCount(L2Npc oldNpc) {
        // sanity check
        if (_currentCount <= 0) {
            return;
        }

        // Decrease the current number of L2NpcInstance of this L2Spawn
        _currentCount--;

        // Remove this NPC from list of spawned
        _spawnedNpcs.remove(oldNpc);

        // Check if respawn is possible to prevent multiple respawning caused by lag
        if (_doRespawn && ((_scheduledCount + _currentCount) < _maximumCount)) {
            // Update the current number of SpawnTask in progress or stand by of this L2Spawn
            _scheduledCount++;

            // Create a new SpawnTask to launch after the respawn Delay
            ThreadPoolManager.getInstance().schedule(new SpawnTask(), hasRespawnRandom() ? Rnd.get(_respawnMinDelay, _respawnMaxDelay) : _respawnMinDelay);
        }
    }

    /**
     * Create the initial spawning and set _doRespawn to False, if respawn time set to 0, or set it to True otherwise.
     *
     * @return The number of L2NpcInstance that were spawned
     */
    public int init() {
        while (_currentCount < _maximumCount) {
            doSpawn();
        }
        _doRespawn = _respawnMinDelay != 0;

        return _currentCount;
    }

    /**
     * @return true if respawn enabled
     */
    public boolean isRespawnEnabled() {
        return _doRespawn;
    }

    /**
     * Set _doRespawn to False to stop respawn in this L2Spawn.
     */
    public void stopRespawn() {
        _doRespawn = false;
    }

    /**
     * Set _doRespawn to True to start or restart respawn in this L2Spawn.
     */
    public void startRespawn() {
        _doRespawn = true;
    }

    public L2Npc doSpawn() {
        return _doRespawn ? doSpawn(false) : null;
    }

    /**
     * Create the L2NpcInstance, add it to the world and lauch its OnSpawn action.<br>
     * <B><U>Concept</U>:</B><br>
     * L2NpcInstance can be spawned either in a random position into a location area (if Lox=0 and Locy=0), either at an exact position.<br>
     * The heading of the L2NpcInstance can be a random heading if not defined (value= -1) or an exact heading (ex : merchant...).<br>
     * <B><U>Actions for an random spawn into location area</U>:<I> (if Locx=0 and Locy=0)</I></B>
     * <ul>
     * <li>Get L2NpcInstance Init parameters and its generate an Identifier</li>
     * <li>Call the constructor of the L2NpcInstance</li>
     * <li>Calculate the random position in the location area (if Locx=0 and Locy=0) or get its exact position from the L2Spawn</li>
     * <li>Set the position of the L2NpcInstance</li>
     * <li>Set the HP and MP of the L2NpcInstance to the max</li>
     * <li>Set the heading of the L2NpcInstance (random heading if not defined : value=-1)</li>
     * <li>Link the L2NpcInstance to this L2Spawn</li>
     * <li>Init other values of the L2NpcInstance (ex : from its L2CharTemplate for INT, STR, DEX...) and add it in the world</li>
     * <li>Launch the action OnSpawn fo the L2NpcInstance</li>
     * <li>Increase the current number of L2NpcInstance managed by this L2Spawn</li>
     * </ul>
     *
     * @param isSummonSpawn
     * @return
     */
    public L2Npc doSpawn(boolean isSummonSpawn) {
        try {
            // Check if the L2Spawn is not a L2Pet or L2Minion or L2Decoy spawn
            if (_template.isType("L2Pet") || _template.isType("L2Decoy") || _template.isType("L2Trap")) {
                _currentCount++;
                return null;
            }

            // Call the constructor of the L2Npc
            final L2Npc npc = _constructor.newInstance(_template);
            npc.setInstanceById(_instanceId); // Must be done before object is spawned into visible world
            if (isSummonSpawn) {
                npc.setShowSummonAnimation(isSummonSpawn);
            }

            return initializeNpcInstance(npc);
        } catch (Exception e) {
            LOGGER.warn("Error while spawning " + _template.getId(), e);
        }
        return null;
    }

    /**
     * @param npc
     * @return
     */
    private L2Npc initializeNpcInstance(L2Npc npc) {
        int newlocx = 0;
        int newlocy = 0;
        int newlocz = 0;

        // If Locx and Locy are not defined, the L2NpcInstance must be spawned in an area defined by location or spawn territory
        // New method
        if (_spawnTemplate != null) {
            final Location loc = _spawnTemplate.getSpawnLocation();
            newlocx = loc.getX();
            newlocy = loc.getY();
            newlocz = loc.getZ();
            setLocation(loc);
        } else if ((getX() == 0) && (getY() == 0)) {
            LOGGER.warn("NPC " + npc + " doesn't have spawn location!");
            return null;
        } else {
            // The L2NpcInstance is spawned at the exact position (Lox, Locy, Locz)
            newlocx = getX();
            newlocy = getY();
            newlocz = getZ();
        }

        // If random spawn system is enabled
        if (Config.ENABLE_RANDOM_MONSTER_SPAWNS) {
            final int randX = newlocx + Rnd.get(Config.MOB_MIN_SPAWN_RANGE, Config.MOB_MAX_SPAWN_RANGE);
            final int randY = newlocy + Rnd.get(Config.MOB_MIN_SPAWN_RANGE, Config.MOB_MAX_SPAWN_RANGE);

            if (npc.isMonster() && !npc.isQuestMonster() && !npc.isWalker() && !npc.isInsideZone(ZoneId.NO_BOOKMARK) && (getInstanceId() == 0) && GeoEngine.getInstance().canMoveToTarget(newlocx, newlocy, newlocz, randX, randY, newlocz, npc.getInstanceWorld()) && !getTemplate().isUndying() && !npc.isRaid() && !npc.isRaidMinion() && !Config.MOBS_LIST_NOT_RANDOM.contains(npc.getId())) {
                newlocx = randX;
                newlocy = randY;
            }
        }

        // Set is not random walk default value
        npc.setRandomWalking(_randomWalk);

        // Set the heading of the L2NpcInstance (random heading if not defined)
        if (getHeading() == -1) {
            npc.setHeading(Rnd.get(61794));
        } else {
            npc.setHeading(getHeading());
        }

        // Set custom Npc server side name and title
        if (npc.getTemplate().isUsingServerSideName()) {
            npc.setName(npc.getTemplate().getName());
        }
        if (npc.getTemplate().isUsingServerSideTitle()) {
            npc.setTitle(npc.getTemplate().getTitle());
        }

        // Reset some variables
        npc.onRespawn();

        // Link the L2NpcInstance to this L2Spawn
        npc.setSpawn(this);

        // Spawn NPC
        npc.spawnMe(newlocx, newlocy, newlocz);

        if (_spawnTemplate != null) {
            _spawnTemplate.notifySpawnNpc(npc);
        }

        _spawnedNpcs.add(npc);

        // Increase the current number of L2NpcInstance managed by this L2Spawn
        _currentCount++;

        // Minions
        if (npc.isMonster() && NpcData.getMasterMonsterIDs().contains(npc.getId())) {
            ((L2MonsterInstance) npc).getMinionList().spawnMinions(npc.getParameters().getMinionList("Privates"));
        }

        return npc;
    }

    /**
     * Set bounds for random calculation and delay for respawn
     *
     * @param delay          delay in seconds
     * @param randomInterval random interval in seconds
     */
    public void setRespawnDelay(int delay, int randomInterval) {
        if (delay != 0) {
            if (delay < 0) {
                LOGGER.warn("respawn delay is negative for spawn:" + this);
            }

            final int minDelay = delay - randomInterval;
            final int maxDelay = delay + randomInterval;

            _respawnMinDelay = Math.max(10, minDelay) * 1000;
            _respawnMaxDelay = Math.max(10, maxDelay) * 1000;
        } else {
            _respawnMinDelay = 0;
            _respawnMaxDelay = 0;
        }
    }

    public int getRespawnDelay() {
        return (_respawnMinDelay + _respawnMaxDelay) / 2;
    }

    public void setRespawnDelay(int delay) {
        setRespawnDelay(delay, 0);
    }

    public boolean hasRespawnRandom() {
        return _respawnMinDelay != _respawnMaxDelay;
    }

    public L2Npc getLastSpawn() {
        if (!_spawnedNpcs.isEmpty()) {
            return _spawnedNpcs.peekLast();
        }

        return null;
    }

    public boolean deleteLastNpc() {
        return !_spawnedNpcs.isEmpty() && _spawnedNpcs.getLast().deleteMe();
    }

    public final Deque<L2Npc> getSpawnedNpcs() {
        return _spawnedNpcs;
    }

    public L2NpcTemplate getTemplate() {
        return _template;
    }

    public int getInstanceId() {
        return _instanceId;
    }

    public void setInstanceId(int instanceId) {
        _instanceId = instanceId;
    }

    public final boolean getRandomWalking() {
        return _randomWalk;
    }

    public final void setRandomWalking(boolean value) {
        _randomWalk = value;
    }

    public void setSpawnTemplate(NpcSpawnTemplate npcSpawnTemplate) {
        _spawnTemplate = npcSpawnTemplate;
    }

    public NpcSpawnTemplate getNpcSpawnTemplate() {
        return _spawnTemplate;
    }

    @Override
    public String toString() {
        return "L2Spawn ID: " + _template.getId() + " X: " + getX() + " Y: " + getY() + " Z: " + getZ() + " Heading: " + getHeading();
    }

    /**
     * The task launching the function doSpawn()
     */
    class SpawnTask implements Runnable {
        public SpawnTask() {
        }

        @Override
        public void run() {
            try {
                doSpawn();
            } catch (Exception e) {
                LOGGER.warn("", e);
            }

            _scheduledCount--;
        }
    }
}
