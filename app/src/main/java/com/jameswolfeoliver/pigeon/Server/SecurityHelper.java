package com.jameswolfeoliver.pigeon.Server;

import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;
import com.jameswolfeoliver.pigeon.Utilities.PigeonApplication;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class SecurityHelper {
    private static final String LOG_TAG = SecurityHelper.class.getSimpleName();
    private static final String TOKEN_KEY = "token_key";
    private static SecurityHelper securityHelper;
    Thread securityThread;
    private SecurityManager manager;

    public static SecurityHelper getInstance() {
        if (securityHelper == null) {
            securityHelper = new SecurityHelper();
        }
        return securityHelper;
    }

    private SecurityHelper() {
        manager = new SecurityManager();
    }

    public boolean storeUserPassword(final String password, final TokenCallback callback) {
        Runnable generateAndStoreToken = new Runnable() {
            @Override
            public void run() {
                try {
                    saveToken(manager.hash(sanitizePassword(password)));
                    callback.onTokenGenerated();
                } catch (NoSuchAlgorithmException n) {
                    Log.d(LOG_TAG, n.getLocalizedMessage());
                    n.printStackTrace();
                    callback.onTokenGenerationFailed();
                } catch (InvalidKeySpecException i) {
                    Log.d(LOG_TAG, i.getLocalizedMessage());
                    i.printStackTrace();
                    callback.onTokenGenerationFailed();
                }
            }
        };
        if (securityThread  != null && securityThread.isAlive() && !securityThread.isInterrupted()) {
            securityThread.interrupt();
            return false;
        } else {
            securityThread = new Thread(generateAndStoreToken);
            securityThread.start();
            return true;
        }
    }

    public boolean checkUserPassword(final String password, final AuthenticationCallback callback) {
        Runnable generateAndStoreToken = new Runnable() {
            @Override
            public void run() {
                try {
                    if (manager.authenticate(sanitizePassword(password), getToken())) {
                        callback.onUserAuthenticated();
                    } else {
                        callback.onUserAuthenticationFailed();
                    }
                } catch (NoSuchAlgorithmException n) {
                    Log.d(LOG_TAG, n.getLocalizedMessage());
                    n.printStackTrace();
                    callback.onUserAuthenticationFailed();
                } catch (InvalidKeySpecException i) {
                    Log.d(LOG_TAG, i.getLocalizedMessage());
                    i.printStackTrace();
                    callback.onUserAuthenticationFailed();
                }
            }
        };
        if (securityThread != null && securityThread.isAlive() && !securityThread.isInterrupted()) {
            securityThread.interrupt();
            return false;
        } else {
            securityThread = new Thread(generateAndStoreToken);
            securityThread.start();
            return true;
        }
    }

    private char[] sanitizePassword(String password) {
        return password.toCharArray();
    }

    private void saveToken(String hash) {
        Log.d(LOG_TAG, "Token: " + hash);
        SharedPreferences preferences = PigeonApplication.getSharedPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(TOKEN_KEY, hash);
        editor.commit();
    }

    private String getToken() {
        Log.d(LOG_TAG, "Saved token: " + PigeonApplication.getSharedPreferences().getString(TOKEN_KEY, null));
        return PigeonApplication.getSharedPreferences().getString(TOKEN_KEY, null);
    }

    protected class SecurityManager {
        private static final String TOKEN_ID = "$token$";
        private static final int DEFAULT_COST = 65536;
        private static final String DEFAULT_ALGORITHM = "PBKDF2WithHmacSHA1";
        private static final int DEFAULT_KEY_LENGTH = 512;
        private static final int DEFAULT_SALT_LENGTH = 16;
        private final Pattern HASHED_TOKEN_PATTERN = Pattern.compile("\\$token\\$(.*?)\\$(.{43})");
        private final SecureRandom random;
        private final int cost;

        protected SecurityManager() {
            this(DEFAULT_COST);
        }

        private SecurityManager(int cost) {
            this.cost = cost;
            this.random = new SecureRandom();
        }

        protected String hash(char[] password) throws InvalidKeySpecException, NoSuchAlgorithmException {
            byte[] salt = buildSalt(16);
            byte[] dk = pbkdf2(password, salt, 1 << cost);
            byte[] hash = new byte[salt.length + dk.length];
            System.arraycopy(salt, 0, hash, 0, salt.length);
            System.arraycopy(dk, 0, hash, salt.length, dk.length);
            return TOKEN_ID + cost + '$' + Base64.encodeToString(hash, Base64.NO_PADDING);
        }

        private byte[] buildSalt(int length) {
            byte[] salt = new byte[length];
            random.nextBytes(salt);
            return salt;
        }

        protected boolean authenticate(char[] password, String token) throws InvalidKeySpecException, NoSuchAlgorithmException {
            Matcher m = HASHED_TOKEN_PATTERN.matcher(token);
            if (!m.find()) {
                return false;
            } else {
                int iterations = 1 << Integer.parseInt(m.group(1));
                byte[] hash = Base64.decode(m.group(2), Base64.NO_PADDING);
                byte[] salt = Arrays.copyOfRange(hash, 0, DEFAULT_KEY_LENGTH / 8);
                byte[] check = pbkdf2(password, salt, iterations);

                int zero = 0;
                for (int idx = 0; idx < check.length; ++idx) {
                    zero |= hash[salt.length + idx] ^ check[idx];
                }
                return zero == 0;
            }
        }

        private byte[] pbkdf2(char[] password, byte[] salt, int iterations) throws InvalidKeySpecException, NoSuchAlgorithmException {
            KeySpec pbeKeySpec = new PBEKeySpec(password, salt, iterations, DEFAULT_KEY_LENGTH);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DEFAULT_ALGORITHM);
            return keyFactory.generateSecret(pbeKeySpec).getEncoded();
        }
    }

    public interface AuthenticationCallback {
        void onUserAuthenticated();
        void onUserAuthenticationFailed();
    }

    public interface TokenCallback {
        void onTokenGenerated();
        void onTokenGenerationFailed();
    }
}


