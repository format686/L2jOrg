package org.l2j.gameserver.settings;

import org.l2j.commons.configuration.Settings;
import org.l2j.commons.configuration.SettingsFile;
import org.l2j.gameserver.ServerType;

import java.nio.file.Path;

import static org.l2j.commons.util.Util.isNullOrEmpty;

public class ServerSettings implements Settings {

    private int serverId;
    private boolean acceptAlternativeId;
    private String authServerAddress;
    private short authServerPort;

    private byte ageLimit;
    private boolean showBrackets;
    private boolean isPvP;
    private int type;
    private short port;
    private int maximumOnlineUsers;
    private Path dataPackDirectory;

    @Override
    public void load(SettingsFile settingsFile) {
        serverId = settingsFile.getInteger("RequestServerID", 1);
        acceptAlternativeId = settingsFile.getBoolean("AcceptAlternateID", true);

        authServerAddress = settingsFile.getString("LoginHost", "127.0.0.1");
        authServerPort = settingsFile.getShort("LoginPort", (short) 9014);

        port = settingsFile.getShort("GameserverPort", (short) 7777);

        parseServerType(settingsFile);

        maximumOnlineUsers = Math.max(1, settingsFile.getInteger("MaximumOnlineUsers", 20));
        ageLimit = settingsFile.getByte("ServerListAge", (byte) 0);
        showBrackets = settingsFile.getBoolean("ServerListBrackets", false);
        isPvP = settingsFile.getBoolean("PvPServer", false);

        dataPackDirectory = Path.of(settingsFile.getString("DatapackRoot", "."));
    }

    private void parseServerType(SettingsFile settingsFile) {
        type = 0;
        var types = settingsFile.getStringArray("ServerListType");

        for (String t : types) {
            if(isNullOrEmpty(t)){
               continue;
            }
            try {
                type |= ServerType.valueOf(t.toUpperCase()).getMask();
            } catch (Exception e) {
                // do nothing
            }

        }
    }

    public int serverId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public short port() {
        return port;
    }

    public String authServerAddress() {
        return authServerAddress;
    }

    public int authServerPort() {
        return authServerPort;
    }

    public byte ageLimit() {
        return ageLimit;
    }

    public boolean isShowingBrackets() {
        return showBrackets;
    }

    public boolean isPvP() {
        return isPvP;
    }

    public int type() {
        return type;
    }

    public int maximumOnlineUsers() {
        return maximumOnlineUsers;
    }

    public boolean acceptAlternativeId() {
        return acceptAlternativeId;
    }

    public Path dataPackDirectory() {
        return dataPackDirectory;
    }
}
