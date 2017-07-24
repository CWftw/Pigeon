package com.jameswolfeoliver.pigeon.Managers;

import android.content.Context;
import android.content.SharedPreferences;

import com.jameswolfeoliver.pigeon.Models.VersionResponse;
import com.jameswolfeoliver.pigeon.Server.Rest.RestServer;
import com.jameswolfeoliver.pigeon.Utilities.PigeonApplication;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import static com.jameswolfeoliver.pigeon.Utilities.SharedPrefKeys.ERROR_KEY;
import static com.jameswolfeoliver.pigeon.Utilities.SharedPrefKeys.INBOX_CSS_KEY;
import static com.jameswolfeoliver.pigeon.Utilities.SharedPrefKeys.INBOX_JS_KEY;
import static com.jameswolfeoliver.pigeon.Utilities.SharedPrefKeys.INBOX_KEY;
import static com.jameswolfeoliver.pigeon.Utilities.SharedPrefKeys.INSECURE_LOGIN_KEY;
import static com.jameswolfeoliver.pigeon.Utilities.SharedPrefKeys.SECURE_LOGIN_KEY;

public class PageCacheManager {
    public static final String SECURE_LOGIN_FILE_NAME = "login_secure.html";
    public static final String INSECURE_LOGIN_FILE_NAME = "login_insecure.html";
    public static final String INBOX_FILE_NAME = "inbox.html";
    public static final String INBOX_CSS_FILE_NAME = "inbox.css";
    public static final String INBOX_JS_FILE_NAME = "inbox.js";
    public static final String ERROR_FILE_NAME = "error.html";

    private static void updateCacheIndex(final String index, final int version) {
        SharedPreferences sharedPrefs = PigeonApplication.getSharedPreferences();
        sharedPrefs.edit().putInt(index, version).apply();
    }

    public static void maybeUpdateLocalPageCache(final RestServer.RestCallback<Boolean> updateCallback) {
        PigeonApplication.getRestServer().getVersionInfo(
                new RestServer.RestCallback<VersionResponse>() {
                    @Override
                    public void onResult(VersionResponse response) {
                        if (response != null) {
                            updatePages(response, updateCallback);
                        } else {
                            updateCallback.onResult(Boolean.FALSE);
                        }
                    }
                }
        );
    }

    private static void updatePages(final VersionResponse serverVersion,
                                    final RestServer.RestCallback<Boolean> updateCallback) {
        SharedPreferences sharedPrefs = PigeonApplication.getSharedPreferences();
        if (sharedPrefs.getInt(INSECURE_LOGIN_KEY, 0)
                < serverVersion.getLoginInsecureVersion()) {
            PigeonApplication.getRestServer().getPage(RestServer.INSECURE_LOGIN_PATH,
                    serverVersion.getLoginInsecureVersion(),
                    new RestServer.RestCallback<String>() {
                        @Override
                        public void onResult(String page) {
                            if (page != null && !page.isEmpty()) {
                                if (updatePage(INSECURE_LOGIN_FILE_NAME, page)) {
                                    updateCacheIndex(INSECURE_LOGIN_KEY, serverVersion.getLoginInsecureVersion());
                                    updatePages(serverVersion, updateCallback);
                                    return;
                                }
                            }
                            updateCallback.onResult(Boolean.FALSE);
                        }
                    });
        } else if (sharedPrefs.getInt(SECURE_LOGIN_KEY, 0)
                < serverVersion.getLoginSercureVersion()) {
            PigeonApplication.getRestServer().getPage(RestServer.SECURE_LOGIN_PATH,
                    serverVersion.getLoginSercureVersion(),
                    new RestServer.RestCallback<String>() {
                        @Override
                        public void onResult(String page) {
                            if (page != null && !page.isEmpty()) {
                                if (updatePage(SECURE_LOGIN_FILE_NAME, page)) {
                                    updateCacheIndex(SECURE_LOGIN_KEY, serverVersion.getLoginSercureVersion());
                                    updatePages(serverVersion, updateCallback);
                                    return;
                                }
                            }
                            updateCallback.onResult(Boolean.FALSE);
                        }
                    });
        } else if (sharedPrefs.getInt(INBOX_KEY, 0)
                < serverVersion.getInboxVersion()) {
            PigeonApplication.getRestServer().getPage(RestServer.INBOX_PATH,
                    serverVersion.getInboxVersion(),
                    new RestServer.RestCallback<String>() {
                        @Override
                        public void onResult(String page) {
                            if (page != null && !page.isEmpty()) {
                                if (updatePage(INBOX_FILE_NAME, page)) {
                                    updateCacheIndex(INBOX_KEY, serverVersion.getInboxVersion());
                                    updatePages(serverVersion, updateCallback);
                                    return;
                                }
                            }
                            updateCallback.onResult(Boolean.FALSE);
                        }
                    });
        } else if (sharedPrefs.getInt(INBOX_CSS_KEY, 0)
                < serverVersion.getInboxVersion()) {
            PigeonApplication.getRestServer().getPage(RestServer.INBOX_CSS_PATH,
                    serverVersion.getInboxVersion(),
                    new RestServer.RestCallback<String>() {
                        @Override
                        public void onResult(String page) {
                            if (page != null && !page.isEmpty()) {
                                if (updatePage(INBOX_CSS_FILE_NAME, page)) {
                                    updateCacheIndex(INBOX_CSS_KEY, serverVersion.getInboxVersion());
                                    updatePages(serverVersion, updateCallback);
                                    return;
                                }
                            }
                            updateCallback.onResult(Boolean.FALSE);
                        }
                    });
        } else if (sharedPrefs.getInt(INBOX_JS_KEY, 0)
                < serverVersion.getInboxVersion()) {
            PigeonApplication.getRestServer().getPage(RestServer.INBOX_JS_PATH,
                    serverVersion.getInboxVersion(),
                    new RestServer.RestCallback<String>() {
                        @Override
                        public void onResult(String page) {
                            if (page != null && !page.isEmpty()) {
                                if (updatePage(INBOX_JS_FILE_NAME, page)) {
                                    updateCacheIndex(INBOX_JS_KEY, serverVersion.getInboxVersion());
                                    updatePages(serverVersion, updateCallback);
                                    return;
                                }
                            }
                            updateCallback.onResult(Boolean.FALSE);
                        }
                    });
        } else if (sharedPrefs.getInt(ERROR_KEY, 0)
                < serverVersion.getErrorVersion()) {
            PigeonApplication.getRestServer().getPage(RestServer.ERROR_PATH,
                    serverVersion.getErrorVersion(),
                    new RestServer.RestCallback<String>() {
                        @Override
                        public void onResult(String page) {
                            if (page != null && !page.isEmpty()) {
                                if (updatePage(ERROR_FILE_NAME, page)) {
                                    updateCacheIndex(ERROR_KEY, serverVersion.getErrorVersion());
                                    updatePages(serverVersion, updateCallback);
                                    return;
                                }
                            }
                            updateCallback.onResult(Boolean.FALSE);
                        }
                    });
        } else {
            updateCallback.onResult(Boolean.TRUE);
        }
    }

    private static boolean updatePage(final String fileName, final String updatedPage) {
        FileOutputStream fileIO = null;
        boolean succeeded = false;

        try {
            fileIO = PigeonApplication.getAppContext().openFileOutput(fileName, Context.MODE_PRIVATE);

            fileIO.write(updatedPage.getBytes());
            succeeded = true;
        } catch (IOException e) {
            e.printStackTrace();
            succeeded = false;
        } finally {
            if (fileIO != null) {
                try {
                    fileIO.flush();
                    fileIO.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    succeeded = false;
                }
            }
        }
        return succeeded;
    }

    public static String loadPageFromStorage(String fileName) throws IOException {
        FileInputStream fileIS = PigeonApplication.getAppContext().openFileInput(fileName);

        BufferedReader buf = new BufferedReader(new InputStreamReader(fileIS));
        String line = buf.readLine();
        StringBuilder sb = new StringBuilder();

        while (line != null) {
            sb.append(line).append("\n");
            line = buf.readLine();
        }
        return sb.toString();
    }
}
