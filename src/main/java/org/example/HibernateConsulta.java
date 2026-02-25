package org.example;

import org.h2.tools.Server;
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
    private static SessionFactory sesionFabricar;
    private static Server h2Server;
    private static Server webServer;

    public static void iniciarServidor()
    {
        try {
            h2Server = Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "9092", "-ifNotExists").start();
            System.out.println("Servidor H2 TCP iniciado en: " + h2Server.getURL());

            webServer = Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082").start();
            System.out.println("Consola web H2 iniciada en: http://localhost:8082");
            sesionFabricar = buildSessionFactory();
        } catch ( Exception e )
        {
            throw new RuntimeException("Error iniciando H2: " + e.getMessage(), e);
        }
    }

    private static SessionFactory buildSessionFactory()
    {
        Configuration config = new Configuration();

        config.setProperty("hibernate.connection.driver_class", "org.h2.Driver"); //Configurar Hibernate
        config.setProperty("hibernate.connection.url", "jdbc:h2:tcp://localhost:9092/~/carritodb");
        config.setProperty("hibernate.connection.username", "sa");
        config.setProperty("hibernate.connection.password", "");

        config.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        config.setProperty("hibernate.hbm2ddl.auto", "update"); //Tablas automaticas
        config.setProperty("hibernate.show_sql", "true");
        config.setProperty("hibernate.format_sql", "true");

        config.addAnnotatedClass( org.example.Producto.class );
        config.addAnnotatedClass( org.example.Usuario.class );
        config.addAnnotatedClass( org.example.ProductoImagen.class );
        config.addAnnotatedClass( org.example.Comentario.class );
        config.addAnnotatedClass( org.example.CarritoItem.class );
        config.addAnnotatedClass(Venta.class);

        return config.buildSessionFactory(
                new StandardServiceRegistryBuilder().applySettings( config.getProperties() ).build()
        );

    }

    public static SessionFactory getSessionFactory()
    {

        if ( sesionFabricar == null )
        {
            iniciarServidor();
        }

        return sesionFabricar;
    }

    public static void detenerServidor()
    {
        if ( sesionFabricar != null && !sesionFabricar.isClosed() )
        {
            sesionFabricar.close();
        }

        if (h2Server != null )
        {
            h2Server.stop();
        }

        if ( webServer != null )
        {
            webServer.stop();
        }

        System.out.println("H2 Server detenido");

    }
}
