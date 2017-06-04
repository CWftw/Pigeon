package com.jameswolfeoliver.pigeon.Server;

import android.app.Service;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.text.format.Formatter;

import com.jameswolfeoliver.pigeon.Managers.KeystoreHelper;
import com.jameswolfeoliver.pigeon.Managers.PageCacheManager;
import com.jameswolfeoliver.pigeon.R;
import com.jameswolfeoliver.pigeon.Server.Endpoints.Contacts.AvatarEndpoint;
import com.jameswolfeoliver.pigeon.Server.Endpoints.Contacts.ContactsEndpoint;
import com.jameswolfeoliver.pigeon.Server.Endpoints.Conversations.InboxEndpoint;
import com.jameswolfeoliver.pigeon.Server.Endpoints.Conversations.MessagesEndpoint;
import com.jameswolfeoliver.pigeon.Server.Endpoints.Endpoints;
import com.jameswolfeoliver.pigeon.Server.Endpoints.Login.InsecureLoginEndpoint;
import com.jameswolfeoliver.pigeon.Server.Endpoints.Login.SecureLoginEndpoint;
import com.jameswolfeoliver.pigeon.Utilities.PigeonApplication;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLServerSocketFactory;

import fi.iki.elonen.NanoHTTPD;

public class TextServer extends NanoHTTPD {
    public static final String LOG_TAG = TextServer.class.getSimpleName();
    private final static String CHARSET_UTF8 = "UTF-8";
    private final static int PORT = 8080;
    private static AtomicBoolean isSecure = new AtomicBoolean(false);
    private boolean isStarted = false;

    private static String serverIp;
    private static String serverUri;

    // Default HTML Response
    private static String BAD_REQUEST;
    private static String NOT_FOUND;
    private static String INTERNAL_ERROR;
    private static String FORBIDDEN;
    private static String LOGIN_SECURE;
    private static String LOGIN_INSECURE;

    // region Getters
    public static String getServerUri() {
        return serverUri;
    }
    public static String getServerIp() {
        return serverIp;
    }
    public static String getForbidden() {
        return FORBIDDEN;
    }
    public static String getLoginSecure() {
        return LOGIN_SECURE;
    }
    public static String getLoginInsecure() {
        return LOGIN_INSECURE;
    }
    public static String getNotFound() {
        return NOT_FOUND;
    }
    public static String getBadRequest() {
        return BAD_REQUEST;
    }
    public static String getInternalError() {
        return INTERNAL_ERROR;
    }
    public boolean isStarted() {
        return isStarted;
    }
    // endregion Getters

    // region Server Setup
    private static TextServer instance;
    private TextServer() {
        super(PORT);
    }

    public static TextServer getInstance() {
        if (instance == null) {
            instance = new TextServer();
        }
        return instance;
    }

    private static SSLServerSocketFactory makeSSLSocketFactory() {
        try {
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            InputStream keystoreStream = KeystoreHelper.getInstance().getKeystoreAsInputStream();
            char[] paraphrase = KeystoreHelper.getPassword().toCharArray();
            paraphrase = "password".toCharArray();

            if (keystoreStream == null) {
                throw new IOException("Unable to load keystore");
            }

            keystore.load(keystoreStream, paraphrase);
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keystore, paraphrase);
            return makeSSLSocketFactory(keystore, keyManagerFactory);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void start(boolean secure, ServerCallback callback) {
        WifiManager wm = (WifiManager) PigeonApplication.getAppContext().getApplicationContext().getSystemService(Service.WIFI_SERVICE);
        serverIp = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        serverUri = String.format("http%s://%s:%s", (secure ? "s" : ""), serverIp, PORT);
        initDefaultResponses();
        // TODO: handle init failures
        if (secure) {
            initSecureServer(callback);
        } else {
            initInsecureServer();
            callback.onComplete();
        }
        isSecure.compareAndSet(false, secure);
        isStarted = true;
    }

    private boolean initSecureServer(final ServerCallback callback) {
        Runnable textServerInitRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    LOGIN_SECURE = PageCacheManager.loadPageFromStorage(PageCacheManager.SECURE_LOGIN_FILE_NAME);
                    System.setProperty("javax.net.ssl.trustStore", "keystore.jks");
                    TextServer.getInstance().makeSecure(makeSSLSocketFactory(), null);
                    TextServer.getInstance().start();
                    runOnUiThread(callback);
                } catch (IOException io) {
                    io.printStackTrace();
                    runOnUiThread(callback);
                }
            }
        };
        if (helperThread  != null && helperThread.isAlive() && !helperThread.isInterrupted()) {
            helperThread.interrupt();
            return false;
        } else {
            helperThread = new Thread(textServerInitRunnable);
            helperThread.setName(THREAD_NAME);
            helperThread.start();
            return true;
        }
    }

    private boolean initInsecureServer() {
        try {
            LOGIN_INSECURE = PageCacheManager.loadPageFromStorage(PageCacheManager.INSECURE_LOGIN_FILE_NAME);
            start();
            return true;
        } catch (IOException io) {
            io.printStackTrace();
            return false;
        }
    }

    private void initDefaultResponses() {
        String generalError;
        try {
            generalError = PageCacheManager.loadPageFromStorage(PageCacheManager.ERROR_FILE_NAME);
        } catch (IOException e) {
            e.printStackTrace();
            generalError = PigeonApplication.getAppContext().getResources().getString(R.string.general_error);
        }
        NOT_FOUND = transformGeneralToSpecificError(generalError, 404);
        BAD_REQUEST = transformGeneralToSpecificError(generalError, 400);
        FORBIDDEN = transformGeneralToSpecificError(generalError, 403);
        INTERNAL_ERROR = transformGeneralToSpecificError(generalError, 500);
    }

    private String transformGeneralToSpecificError(String generalError, int error) {
        String message;
        switch (error) {
            case 400:
                message = PigeonApplication.getAppContext().getString(R.string.message_400);
                break;
            case 403:
                message = PigeonApplication.getAppContext().getString(R.string.message_403);
                break;
            case 404:
                message = PigeonApplication.getAppContext().getString(R.string.message_404);
                break;
            case 500:
                message = PigeonApplication.getAppContext().getString(R.string.message_500);
                break;
            default:
                message = "-_-";
        }
        return generalError.replace("{CODE}", Integer.toString(error)).replace("{MESSAGE}", message);
    }
    // endregion ServerSetup

    // region Helper Thread
    private Thread helperThread;
    private final static String THREAD_NAME = "TextServer Helper Thread";
    public static void runOnUiThread(final ServerCallback callback) {
        Handler uiHandler = new Handler(PigeonApplication.getAppContext().getMainLooper());
        Runnable runOnUi = new Runnable() {
            @Override
            public void run() {
                callback.onComplete();
            }
        };
        uiHandler.post(runOnUi);
    }
    public static void runOnUiThread(final Runnable runOnUi) {
        Handler uiHandler = new Handler(PigeonApplication.getAppContext().getMainLooper());
        uiHandler.post(runOnUi);
    }
    // endregion Helper Thread

    @Override
    public Response serve(IHTTPSession session) {
        switch (Endpoints.getEndpoint(session.getUri())) {
            case Endpoints.LOGIN_ENDPOINT:
                if (isSecure.get()) {
                    return SecureLoginEndpoint.serve(session);
                }
                return InsecureLoginEndpoint.serve(session);
            case Endpoints.INBOX_ENDPOINT:
                return InboxEndpoint.serve(session);
            case Endpoints.AVATAR_ENDPOINT:
                return AvatarEndpoint.serve(session);
            case Endpoints.CONTACTS_ENDPOINT:
                return ContactsEndpoint.serve(session);
            case Endpoints.MESSAGES_ENDPOINT:
                return MessagesEndpoint.serve(session);
            default:
                return Endpoint.buildHtmlResponse(NOT_FOUND, Response.Status.NOT_FOUND);
        }
    }

    // Callback interface for time consuming tasks
    public interface ServerCallback {
        void onComplete();
    }
}