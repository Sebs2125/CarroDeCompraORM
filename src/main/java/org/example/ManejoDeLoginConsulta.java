package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;



public class ManejoDeLoginConsulta
{
    public void registrarLogin( String usuario, String ip, String userAgent )
    {
        String url = System.getenv("JDBC_DATABASE_URL");

        if ( url == null || url.isBlank() )
        {
            System.out.println("JDBC_DATABASE_URL no configurado, se salta auditoría");
            return;
        }

        String sql = "INSERT INTO login_audit (username, login_timestamp, ip_adress, user_agent) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setString(1, usuario);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(3, ip);
            ps.setString(4, userAgent);

            ps.executeUpdate();
            System.out.println("login auditado: " + usuario + " desde " + ip);
        } catch (Exception e) {
            System.err.println("Error en auditoria de login: " + e.getMessage());
        }

    }
}
