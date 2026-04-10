/* DON'T FORGET TO IMPORT THE CORRECT CLASSES */
import java.io.FileOutputStream;
import java.security.*;
import java.util.HexFormat;
import javax.crypto.*;
import javax.crypto.spec.*;

public class SolutionExercises09042026 {

    /*
    * Treasure Hunt! Calculate the hash using SHA-256 algorithm on the string generated in this way:
    *    "tutorato" + year of foundation of Bologna Calcio + 
    *   secret word hidden into the slides + slide number with the comparison of the Linux penguin with ECB encryption
    * The string is all lowercase, all attached, without spaces.
    * Return the hash as byte array.
    */
    public static byte[] firstExercise(String password) {

        byte[] hash;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            hash = md.digest(password.getBytes());
            return hash;
        } catch (NoSuchAlgorithmException e) {
            return null;
        }

    }

    /*
    *   Create a PRNG using the SHA1PRNG algorithm, using as seed the result of the previous exercise.
    *   Calculate after how many iterations the PRNG returns the integer value: 1328311680
    *   The first iteration counts as 1, not as 0.
    *   Return the number of iterations needed to get the desired value.
    */
    public static int secondExercise(byte[] seed) {

        SecureRandom prng;
        try {
            prng = SecureRandom.getInstance("SHA1PRNG");
            prng.setSeed(seed);
        } catch (NoSuchAlgorithmException e) {
            return -1;
        }
        int iterations = 1;
        while(prng.nextInt() != 1328311680){
            iterations++;
        }
        return iterations;

    }

    /*
     *   A plaintext has been encrypted 3 times.
     * - The first time was encrypted using AES-GCM with additional data.
     * - The second time was encrypted using ChaCha20.
     * - The third time was encrypted using ECB.
     *
     * Ciphertext = E_ECB(E_ChaCha(E_GCM(plaintext))).
     * 
     *
     * ⚠️ For the AES-GCM Encryption:
     * - The key is key1.
     * - The IV is the FIRST value obtained by nextBytes() using the same PRNG as in the previous exercise (16 bytes).
     * - MAC dimension is 128.
     * - The additional data is the string used in the first exercise, converted in bytes.
     *
     * ⚠️ For the ChaCha20 Encryption:
     * - The key is key2.
     * - The nonce is the SECOND value obtained by nextBytes() using the same PRNG as in the previous exercise (12 bytes).
     * - The counter is the output of the second exercise.
     * 
     * ⚠️ For the ECB Encryption:
     * - The key is key1.
     * 
     * Finally, return the plaintext.
    */
    public static String thirdExercise(byte[] seed, int counter, byte[] ciphertext, SecretKey key1, SecretKey key2, String password) {

        // Get PRNG

        SecureRandom prng;
        try {
            prng = SecureRandom.getInstance("SHA1PRNG");
            prng.setSeed(seed);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        // --- Decryption of ECB ---

        byte[] decrypted1;
        try {
            Cipher c1 = Cipher.getInstance("AES/ECB/PKCS5Padding");
            c1.init(Cipher.DECRYPT_MODE, key1);
            decrypted1 = c1.doFinal(ciphertext);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // --- Decryption of ChaCha20 ---

        byte[] iv = new byte[16];
        prng.nextBytes(iv);

        byte[] nonce = new byte[12];
        prng.nextBytes(nonce);
        ChaCha20ParameterSpec ChaCha20Params = new ChaCha20ParameterSpec(nonce, counter);
        byte[] decrypted2;
        try {
            Cipher c2 = Cipher.getInstance("ChaCha20");
            c2.init(Cipher.DECRYPT_MODE, key2, ChaCha20Params);
            decrypted2 = c2.doFinal(decrypted1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // --- Decryption of AES-GCM ---
        GCMParameterSpec GCMParams = new GCMParameterSpec(128, iv);
        byte[] AAD = password.getBytes();

        byte[] decrypted3;
        try{
            Cipher c3 = Cipher.getInstance("AES/GCM/NoPadding");
            c3.init(Cipher.DECRYPT_MODE, key1, GCMParams);
            c3.updateAAD(AAD);
            decrypted3 = c3.doFinal(decrypted2);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        String decryptedPlaintext = new String(decrypted3);
        return decryptedPlaintext;

    }

    /*
     *   Calculate the MAC of the ciphertext and check if it is valid.
     *   Mac is calculated using HMAC-SHA256.
     *   The key is macKey.
     *   Return true if the MAC is valid, false otherwise.
     */
    public static boolean fourthExercise(SecretKey macKey, byte[] ciphertext, byte[] mac) {

        byte[] macValue;
        try {
            Mac m = Mac.getInstance("HmacSHA256");
            m.init(macKey);
            macValue = m.doFinal(ciphertext);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return MessageDigest.isEqual(macValue, mac);
    }

    /*
    *   Encrypt again the message and save it into a file using CipherOutputStream class.
    *   Use the encryption algorithm you prefer.
    */
    public static void fifthExercise(SecretKey key, String filename, String message) {

        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            try (FileOutputStream fos = new FileOutputStream(filename);
                CipherOutputStream cos = new CipherOutputStream(fos, cipher)) {

                cos.write(message.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {

        byte[] keyBytes1 = HexFormat.of().parseHex("15c02ffef35dd17d0084b17a8ae448046f8a31fe89e0e253216b795c44216252");
        SecretKey key1 = new javax.crypto.spec.SecretKeySpec(keyBytes1, "AES");
        byte[] keyBytes2 = HexFormat.of().parseHex("bed7f74d35e479aec9073fda0845cdfd49c54637d35ea4ab9f0c0a0566c0bb04");
        SecretKey key2 = new javax.crypto.spec.SecretKeySpec(keyBytes2, "ChaCha20");
        byte[] macKeyBytes = HexFormat.of().parseHex("5bfa7047a4c034d90ac98f2c62c0f5bb7fbcfd24f2f8917a36f01dd6085e16c4");
        SecretKey macKey = new javax.crypto.spec.SecretKeySpec(macKeyBytes, "HMACSHA256");

        byte[] ciphertext = HexFormat.of().parseHex("529b43613598c1b9db5d3f87887bdee6b949ce42ca3d154e6209593f5eed8ce2658c71192916499b22222fbf56e8e3568a80f7b6a9bbc020cd8d9cadddd8e007e1bdc6cc96b6bd4aa7cbb4903f416abd");
        byte[] mac = HexFormat.of().parseHex("2a36ab7dfc0d3cf25796127c9a5dd4f308ed803b7daadad47d14a0ef57a6ee02");

        /* FIRST EXERCISE */
        String password = "tutorato1909penguin32";
        byte[] seed = firstExercise(password);
        System.out.println("Seed: " + HexFormat.of().formatHex(seed));

        /* SECOND EXERCISE */
        int counter = secondExercise(seed);
        System.out.println("Counter: " + counter);

        /* THIRD EXERCISE */
        String plaintext = thirdExercise(seed, counter, ciphertext, key1, key2, password);
        System.out.println("Plaintext: " + plaintext);

        /* FOURTH EXERCISE */
        boolean isMacValid;
        isMacValid = fourthExercise(macKey, ciphertext, mac);
        System.out.println("Is the MAC valid? " + isMacValid);

        /* FIFTH EXERCISE */
        fifthExercise(key1, "a.txt", plaintext);
        System.out.println("File created with the encryted message.");

        return;

    }
    
}
