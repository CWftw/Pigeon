package com.jameswolfeoliver.pigeon.Managers;

import android.content.Context;
import android.content.SharedPreferences;
import com.jameswolfeoliver.pigeon.Utilities.PigeonApplication;
import org.spongycastle.asn1.x500.X500Name;
import org.spongycastle.asn1.x509.SubjectPublicKeyInfo;
import org.spongycastle.cert.X509CertificateHolder;
import org.spongycastle.cert.X509v3CertificateBuilder;
import org.spongycastle.cert.jcajce.JcaX509CertificateConverter;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.operator.ContentSigner;
import org.spongycastle.operator.OperatorCreationException;
import org.spongycastle.operator.jcajce.JcaContentSignerBuilder;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;

public class KeystoreHelper {
    private static final String LOG_TAG = KeystoreHelper.class.getSimpleName();
    private static final String PASSWORD_KEY = "password_key";
    private static KeystoreHelper instance;
    private KeystoreManager manager;

    public static KeystoreHelper getInstance() {
        if (instance == null) {
            instance = new KeystoreHelper();
        }
        return instance;
    }

    private KeystoreHelper() {
        manager = new KeystoreManager();
    }

    public InputStream getKeystoreAsInputStream() {
        try {
            if (!manager.keystoreExists()) {
                KeystoreHelper.getInstance().manager.generateKeystore(
                        PigeonApplication
                                .getAppContext()
                                .openFileOutput(KeystoreManager.KEYSTORE_FILE_NAME,
                                        Context.MODE_PRIVATE));
            }
            return PigeonApplication.getAppContext().openFileInput(KeystoreManager.KEYSTORE_FILE_NAME);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void savePassword(String pass) {
        Log.d(LOG_TAG, "Password: " + pass);
        SharedPreferences preferences = PigeonApplication.getSharedPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PASSWORD_KEY, pass);
        editor.commit();
    }

    public static String getPassword() {
        Log.d(LOG_TAG, "Saved pass: " + PigeonApplication.getSharedPreferences().getString(PASSWORD_KEY, null));
        return PigeonApplication.getSharedPreferences().getString(PASSWORD_KEY, null);
    }

    private class KeystoreManager {
        private static final String KEYSTORE_FILE_NAME = "keystore.jks";
        private static final String ISSUER = "CN=Pigeon Client";
        private static final int YEARS_VALID = 5;
        private static final int DEFAULT_PASSWORD_LENGTH = 16;
        private final SecureRandom random;
        private BouncyCastleProvider provider;

        private KeystoreManager() {
            Security.addProvider(new BouncyCastleProvider());
            random = new SecureRandom();
            provider = new BouncyCastleProvider();
        }

        private File getKeystoreFile() throws IOException {
            File keystoreFile = new File(PigeonApplication.getAppContext().getFilesDir(), KEYSTORE_FILE_NAME);
            if (keystoreFile.exists()) {
                if (keystoreFile.delete()) {
                    return keystoreFile;
                }
                throw new IOException("Could not delete old keystore");
            }
            return keystoreFile;
        }

        private boolean keystoreExists() {
            return PigeonApplication.getAppContext().getFileStreamPath(KeystoreManager.KEYSTORE_FILE_NAME).exists();
        }

        private void generateKeystore(FileOutputStream keystoreOutputStream) throws NoSuchAlgorithmException, OperatorCreationException, CertificateException, KeyStoreException, NoSuchProviderException, IOException {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048, random);
            KeyPair keyPair = generator.generateKeyPair();

            SubjectPublicKeyInfo publicKeyInfo = SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded());
            ContentSigner signer = new JcaContentSignerBuilder("SHA1withRSA").setProvider(provider).build(keyPair.getPrivate());

            Date startDate = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);
            calendar.add(Calendar.YEAR, YEARS_VALID);
            Date endDate = calendar.getTime();

            X509v3CertificateBuilder certBuilder = new X509v3CertificateBuilder(new X500Name(ISSUER),
                    BigInteger.ONE, startDate, endDate, new X500Name(ISSUER), publicKeyInfo);

            X509CertificateHolder certificateHolder = certBuilder.build(signer);
            X509Certificate certificate = new JcaX509CertificateConverter().setProvider(provider).getCertificate(certificateHolder);

            char[] password = buildPassword(DEFAULT_PASSWORD_LENGTH).toCharArray();
            password = "password".toCharArray();
            KeyStore keyStore = KeyStore.getInstance("BKS", provider);
            keyStore.load(null, password);
            keyStore.setKeyEntry("Pigeon Key", keyPair.getPrivate(), password, new X509Certificate[]{certificate});
            keyStore.store(keystoreOutputStream, password);
            savePassword(password.toString());
        }

        private String buildPassword(int length) {
            byte[] password = new byte[length];
            random.nextBytes(password);
            return password.toString();
        }
    }
}
