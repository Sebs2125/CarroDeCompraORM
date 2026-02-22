package org.example;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

/*
Puntos 1 y 2:
1- Motor de base de datos H2
2- El sistema debe arracanar la base de datos en modo servidor y crea la información de manera automática.
 */

public class HibernateConsulta
{
    private static final SessionFactory sesionFabricar = buildSessionFactory();

    private static SessionFactory buildSessionFactory()
    {
        try {

            Configuration config = new Configuration();

            config.setProperty("hibernate.connection.driver_class", "org.h2.Driver"); //Configurar Hibernate
            config.setProperty("hibernate.connection.url", "jdbc:h2:tcp://localhost:9092/~/carritodb:AUTO_SERVER=TRUE");
            config.setProperty("hibernate.connection.username", "sa");
            config.setProperty("hibernate.connection.password", "");
            config.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
            config.setProperty("hibernate.hbm2ddl.auto", "update"); //Tablas automaticas

            config.setProperty("hibernate.show_sql", "true");

            config.addAnnotatedClass( org.example.Producto.class );
            config.addAnnotatedClass( org.example.Usuario.class );
            config.addAnnotatedClass( org.example.ProductoImagen.class );
            config.addAnnotatedClass( org.example.Comentario.class );
            config.addAnnotatedClass( org.example.CarritoItem.class );

            return config.buildSessionFactory(
                    new StandardServiceRegistryBuilder().applySettings( config.getProperties() ).build()
            );

        }catch (Exception ex) {
            throw new RuntimeException("Error al inicializar Hibernate: " + ex.getMessage(), ex );
        }
    }

    public static SessionFactory getSessionFactory()
    {
        return sesionFabricar;
    }

}
