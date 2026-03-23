package org.example;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

public class HibernateConsulta {

    private static final SessionFactory sesionFabricar = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            // Leer variables de entorno — si no existen, usar valores locales por defecto
            // En Docker, docker-compose.yml inyecta DB_URL, DB_USER, DB_PASS
            // En desarrollo local, usa los valores hardcodeados como fallback
            String dbUrl  = System.getenv("DB_URL")  != null
                    ? System.getenv("DB_URL")
                    : "jdbc:h2:tcp://localhost:9092/~/carritodb";

            String dbUser = System.getenv("DB_USER") != null
                    ? System.getenv("DB_USER")
                    : "sa";

            String dbPass = System.getenv("DB_PASS") != null
                    ? System.getenv("DB_PASS")
                    : "";

            Configuration config = new Configuration();

            config.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
            config.setProperty("hibernate.connection.url",      dbUrl);
            config.setProperty("hibernate.connection.username", dbUser);
            config.setProperty("hibernate.connection.password", dbPass);
            config.setProperty("hibernate.dialect",             "org.hibernate.dialect.H2Dialect");
            config.setProperty("hibernate.hbm2ddl.auto",        "update");
            config.setProperty("hibernate.show_sql",             "true");

            config.addAnnotatedClass(org.example.Producto.class);
            config.addAnnotatedClass(org.example.Usuario.class);
            config.addAnnotatedClass(org.example.ProductoImagen.class);
            config.addAnnotatedClass(org.example.Comentario.class);
            config.addAnnotatedClass(org.example.CarritoItem.class);

            return config.buildSessionFactory(
                    new StandardServiceRegistryBuilder()
                            .applySettings(config.getProperties())
                            .build()
            );

        } catch (Exception ex) {
            throw new RuntimeException("Error al inicializar Hibernate: " + ex.getMessage(), ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sesionFabricar;
    }
}