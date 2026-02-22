package org.example;

/*
Punto #9: crear tabla de auditoria si no existe.
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class ConfigBaseDeDatos
{
    public static void crearAuditoriaTablaSiNoExiste()
    {
        String url = System.getenv("JDBC_DATABASE_URL");

        if (url == null || url.isBlank() ) return;
        String create = """
            CREATE TABLE IF NOT EXISTS login_audit (
                id SERIAL PRIMARY KEY,
                username VARCHAR(255) NOT NULL,
                login_timestamp TIMESTAMP NOT NULL,
                ip_adress VARCHAR (50),
                user_agent TEXT
               )
        """;
        try (Connection connection = DriverManager.getConnection(url);
            Statement stmt = connection.createStatement()){
            stmt.execute(create);

        } catch (Exception e ) { e.printStackTrace(); }
    }
}
