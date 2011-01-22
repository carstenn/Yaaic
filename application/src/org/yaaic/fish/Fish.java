package org.yaaic.fish;

import gnu.crypto.cipher.Blowfish;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import android.util.Log;

public class Fish
{

    private static String BEGIN_ENCODED = "+OK ";
    private static String BEGIN_DECODED = "* ";
    private static String B64 = "./0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private Fish()
    {

    }

    public static String encode(String text, String server, String conversation)
    {
        String encryptedText = text;
        String key = FishKeys.getInstance().getKey(server, conversation);

        if(key != null) {
            int apiVersion = android.os.Build.VERSION.SDK_INT;

            if(apiVersion < 9) {
                try {
                    encryptedText = encodeGnu(text, key);
                }
                catch (Exception e) {
                    encryptedText = text;
                }
            } else {
                try {
                    encryptedText = encodeNative(text, key);
                }
                catch (Exception e) {
                    encryptedText = text;
                }
            }
        }
        return encryptedText;
    }

    public static String decode(String text, String server, String conversation)
    {
        String decryptedText = text;

        String key = FishKeys.getInstance().getKey(server, conversation);

        if(key != null) {
            int apiVersion = android.os.Build.VERSION.SDK_INT;

            if(apiVersion < 9) {
                decryptedText = decodeGnu(text, key);
            } else {
                decryptedText = decodeNative(text, key);
            }
            decryptedText = BEGIN_DECODED.concat(decryptedText).trim();
        }

        return decryptedText;
    }

    private static String encodeGnu(String text, String key) throws Exception
    {
        String encryptedText = text;

        byte[] textBytes;
        byte[] encryptedTextBytes;
        Blowfish blowfish = new Blowfish();
        byte[] keyBytes = key.getBytes();
        Object keyObject = blowfish.makeKey(keyBytes, 8);

        if ((text.length() % 8) != 0) {
            while ((text.length() % 8) != 0) {
                text += " ";
            }
        }


        textBytes = text.getBytes();
        encryptedTextBytes = new byte[text.length()];

        for (int i = 0; i < Array.getLength(textBytes); i += 8) {
            blowfish.encrypt(textBytes, i, encryptedTextBytes, i, keyObject, 8);
        }

        encryptedText = bytetoB64(encryptedTextBytes);
        encryptedText = BEGIN_ENCODED.concat(encryptedText);

        return encryptedText;
    }

    private static String encodeNative(String text, String key) throws Exception
    {
        String encryptedText = text;
        SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(), "Blowfish");
        try {
            Cipher ecipher = Cipher.getInstance("Blowfish/ECB/NoPadding");
            ecipher.init(Cipher.ENCRYPT_MODE, skeySpec);

            String REncrypt = "";
            byte[] BEncrypt = text.getBytes();
            int Taille = BEncrypt.length;
            int Limit = 8 - (BEncrypt.length % 8);
            byte[] buff = new byte[Taille + Limit];

            for (int i = 0; i < Taille; i++) {
                buff[i] = BEncrypt[i];
            }

            for (int i = Taille; i < Taille + Limit; i++) {
                buff[i] = 0x0;
            }

            byte[] encrypted = ecipher.doFinal(buff);
            REncrypt = bytetoB64(encrypted);

            encryptedText = BEGIN_ENCODED.concat(REncrypt);
        }
        catch(NoSuchAlgorithmException nsae) {
            try {
                encryptedText = encodeGnu(text, key);
            }
            catch (Exception e) {
                encryptedText = text;
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return encryptedText;
    }

    private static String decodeGnu(String text, String key)
    {
        String decryptedText = text;

        if(text.startsWith("+OK ")) {text = text.substring(4,text.length());}
        if(text.startsWith("mcps ")) {text = text.substring(5,text.length());}

        byte[] encryptedTextBytes = b64tobyte(text);
        byte[] decryptedTextBytes;
        Blowfish blowfish = new Blowfish();

        byte[] keyBytes = key.getBytes();
        Object keyObject = blowfish.makeKey(keyBytes, 8);

        if ((text.length() % 8) != 0) {
            while ((text.length() % 8) != 0) {
                text += " ";
            }
        }

        decryptedTextBytes = new byte[text.length()];

        for (int i = 0; i < Array.getLength(encryptedTextBytes); i += 8) {
            blowfish.decrypt(encryptedTextBytes, i, decryptedTextBytes, i, keyObject, 8);
        }

        decryptedText = new String(decryptedTextBytes);


        return decryptedText;
    }

    private static String decodeNative(String text, String key)
    {
        String decryptedText = text;

        if(text.startsWith("+OK ")) {text = text.substring(4,text.length());}
        if(text.startsWith("mcps ")) {text = text.substring(5,text.length());}

        byte[] Again = b64tobyte(text);
        byte[] decrypted = null;


        SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(), "Blowfish");
        try {
            Cipher ecipher = Cipher.getInstance("Blowfish/ECB/NoPadding");

            ecipher.init(Cipher.DECRYPT_MODE, skeySpec);
            decrypted = ecipher.doFinal(Again);

            int leng = 0;
            while(decrypted[leng] != 0x0) {leng++;}
            byte[] Final = new byte[leng];

            int i = 0;
            while(decrypted[i] != 0x0) {
                Final[i] = decrypted[i];
                i++;
            }
            decryptedText = new String(Final,"8859_1");

        }
        catch(NoSuchAlgorithmException nsae) {
            Log.d("Fish", "decodeNative - NoSuchAlgorithmException");
            decryptedText = decodeGnu(text, key);
        }
        catch (Exception e) {
            Log.d("Fish", "decodeNative - Exception");
            e.printStackTrace();
            decryptedText = text;
        }

        return decryptedText;
    }


    private static String bytetoB64(byte[] ec) {
        String dc = "";
        int left = 0;
        int right = 0;
        int k = -1;
        int v;
        while (k < (ec.length - 1)) {
            k++;
            v=ec[k]; if (v<0) {
                v+=256;
            }
            left = v << 24;
            k++;
            v=ec[k]; if (v<0) {
                v+=256;
            }
            left += v << 16;
            k++;
            v=ec[k]; if (v<0) {
                v+=256;
            }
            left += v << 8;
            k++;
            v=ec[k]; if (v<0) {
                v+=256;
            }
            left += v;

            k++;
            v=ec[k]; if (v<0) {
                v+=256;
            }
            right = v << 24;
            k++;
            v=ec[k]; if (v<0) {
                v+=256;
            }
            right += v << 16;
            k++;
            v=ec[k]; if (v<0) {
                v+=256;
            }
            right += v << 8;
            k++;
            v=ec[k]; if (v<0) {
                v+=256;
            }
            right += v;

            for (int i = 0; i < 6; i++) {
                dc += B64.charAt(right & 0x3F);
                right = right >> 6;
            }

            for (int i = 0; i < 6; i++) {
                dc += B64.charAt(left & 0x3F);
                left = left >> 6;
            }
        }
        return dc;
    }

    private static byte[] b64tobyte(String ec) {
        String dc = "";
        int k = -1;
        while (k < (ec.length() - 1)) {

            int right = 0;
            int left = 0;
            int v = 0;
            int w = 0;
            int z = 0;

            for (int i = 0; i < 6; i++) {
                k++;
                v = B64.indexOf(ec.charAt(k));
                right |= v << (i * 6);
            }

            for (int i = 0; i < 6; i++) {
                k++;
                v = B64.indexOf(ec.charAt(k));
                left |= v << (i * 6);
            }

            for (int i = 0; i < 4; i++) {
                w = ((left & (0xFF << ((3 - i) * 8))));
                z = w >> ((3 - i) * 8);
                if(z < 0) {z = z + 256;}
                dc += (char)z;
            }

            for (int i = 0; i < 4; i++) {
                w = ((right & (0xFF << ((3 - i) * 8))));
                z = w >> ((3 - i) * 8);
                if(z < 0) {z = z + 256;}
                dc += (char)z;
            }
        }

        byte[] Result = new byte[1024];
        try {
            Result = dc.getBytes("8859_1");

        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return Result;
    }

}
