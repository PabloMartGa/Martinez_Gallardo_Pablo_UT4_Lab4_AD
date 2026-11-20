/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Crea UNA sola SessionFactory para toda la aplicación (es costosa de construir y es
 * thread-safe). Lee hibernate.cfg.xml del classpath (src/main/resources).
 */
public final class HibernateUtil {

    private static final SessionFactory SESSION_FACTORY = construir();

    private HibernateUtil() {
    }

    private static SessionFactory construir() {
        try {
            return new Configuration().configure().buildSessionFactory();
        } catch (HibernateException ex) {
            System.err.println("❌ Error al crear la SessionFactory: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return SESSION_FACTORY;
    }

    /** Cierra la factoría (y con ella el pool de conexiones). Llamar al terminar el programa. */
    public static void cerrar() {
        if (SESSION_FACTORY != null && SESSION_FACTORY.isOpen()) {
            SESSION_FACTORY.close();
        }
    }
}
