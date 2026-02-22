package org.example;

import org.jasypt.util.binary.AES256BinaryEncryptor;

import java.util.Base64;

/*
Punto #4: creación de cookies para guardar datos del usuario temporalmente.
 */

public class ServicioCookies
{
    private static final String KEY = "SecretPassword21";
    private static final AES256BinaryEncryptor encryptor = new AES256BinaryEncryptor();

    static
    {
        encryptor.setPassword(KEY); //Llave creada con contraseña
    }

    //Encriptar el dato
    public static String encrypt(String data)
    {
        byte[] encrypted = encryptor.encrypt(data.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    //Desencriptar el dato
    public static String decrypt(String encriptado)
    {
        byte[] bytes = Base64.getDecoder().decode(encriptado);
        byte[] decrypted = encryptor.decrypt(bytes);
        return new String(decrypted);
    }

    public static String generarRecordarUsuarioToken( String usuario )
    {
        long expires = System.currentTimeMillis() + 3600 * 1000;
        String token = usuario + "/" + expires;
        return encrypt(token);
    }

    public static String validaryExtraerUsuario( String token )
    {
        try {
            String decrypted = decrypt(token);
            String[] parts = decrypted.split("/");
            long expiry = Long.parseLong(parts[1]);

            if ( System.currentTimeMillis() > expiry ) return null;
            return parts[0];
        }catch (Exception e) {
            return null;
        }
    }

}
