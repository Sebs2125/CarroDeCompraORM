package org.example;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

/*
 * Punto #4: cookies para guardar datos del usuario temporalmente.
 *
 * CORRECCION aplicada:
 *   El codigo original usaba org.jasypt.util.binary.AES256BinaryEncryptor
 *   (libreria Jasypt), que no estaba en build.gradle y generaba:
 *     Cannot resolve symbol 'jasypt'
 *     Cannot resolve symbol 'AES256BinaryEncryptor'
 *     Cannot resolve method 'setPassword'
 *     Cannot resolve method 'encrypt' / 'decrypt'
 *
 *   Solucion: se reemplazo completamente por AES-256-CBC usando unicamente
 *   javax.crypto, que viene incluido en el JDK — sin ninguna dependencia extra.
 *   La interfaz publica (encrypt, decrypt, generarRecordarUsuarioToken,
 *   validaryExtraerUsuario) es identica a la version original, por lo que
 *   el resto del codigo (AutenticarUsuarioControlador, AutorizarServicio)
 *   no necesita ningun cambio.
 */
public class ServicioCookies {

    private static final String PASSWORD  = "SecretPassword21";
    // Salt fijo (en produccion usaria uno aleatorio por usuario, pero aqui
    // mantenemos la misma logica simple del original)
    private static final byte[] SALT = "CarritoSalt1234!".getBytes();
    private static final String ALGORITHM  = "AES/CBC/PKCS5Padding";
    private static final int    KEY_LENGTH = 256;
    private static final int    ITERATIONS = 65536;

    /** Genera una SecretKey AES-256 derivada de PASSWORD */
    private static SecretKey derivarClave() throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(PASSWORD.toCharArray(), SALT, ITERATIONS, KEY_LENGTH);
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * Encripta el texto plano con AES-256-CBC.
     * El IV aleatorio se antepone al texto cifrado antes de codificar en Base64,
     * para que decrypt pueda extraerlo.
     */
    public static String encrypt(String data) {
        try {
            SecretKey key = derivarClave();
            Cipher cipher = Cipher.getInstance(ALGORITHM);

            byte[] iv = new byte[16];
            new SecureRandom().nextBytes(iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));

            byte[] cifrado = cipher.doFinal(data.getBytes("UTF-8"));

            // Formato: [16 bytes IV] + [cifrado]
            byte[] resultado = new byte[iv.length + cifrado.length];
            System.arraycopy(iv,      0, resultado, 0,         iv.length);
            System.arraycopy(cifrado, 0, resultado, iv.length, cifrado.length);

            return Base64.getEncoder().encodeToString(resultado);
        } catch (Exception e) {
            throw new RuntimeException("Error al encriptar", e);
        }
    }

    /**
     * Desencripta el texto producido por encrypt().
     * Extrae el IV de los primeros 16 bytes y descifra el resto.
     */
    public static String decrypt(String encriptado) {
        try {
            byte[] datos = Base64.getDecoder().decode(encriptado);

            byte[] iv      = new byte[16];
            byte[] cifrado = new byte[datos.length - 16];
            System.arraycopy(datos, 0,  iv,      0, 16);
            System.arraycopy(datos, 16, cifrado, 0, cifrado.length);

            SecretKey key = derivarClave();
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));

            byte[] plano = cipher.doFinal(cifrado);
            return new String(plano, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("Error al desencriptar", e);
        }
    }

    /** Genera un token cifrado con el nombre de usuario y su fecha de expiracion */
    public static String generarRecordarUsuarioToken(String usuario) {
        long expires = System.currentTimeMillis() + 7L * 24 * 3600 * 1000; // 1 semana
        String token = usuario + "/" + expires;
        return encrypt(token);
    }

    /**
     * Valida el token y extrae el nombre de usuario.
     * Retorna null si el token esta vencido o es invalido.
     */
    public static String validaryExtraerUsuario(String token) {
        try {
            String decrypted = decrypt(token);
            String[] parts   = decrypted.split("/");
            long expiry      = Long.parseLong(parts[1]);

            if (System.currentTimeMillis() > expiry) return null;
            return parts[0];
        } catch (Exception e) {
            return null;
        }
    }
}