package Models;

import com.google.gson.annotations.SerializedName;

public class VersionResponse {
    @SerializedName("loginInsecureVersion")
    private int loginInsecureVersion;

    @SerializedName("loginSecureVersion")
    private int loginSercureVersion;

    @SerializedName("inboxVersion")
    private int inboxVersion;

    @SerializedName("errorVersion")
    private int errorVersion;

    public int getLoginInsecureVersion() {
        return loginInsecureVersion;
    }

    public int getLoginSercureVersion() {
        return loginSercureVersion;
    }

    public int getInboxVersion() {
        return inboxVersion;
    }

    public int getErrorVersion() {
        return errorVersion;
    }
}
