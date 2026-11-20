/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

import org.hibernate.Session;
import org.hibernate.Transaction;
import clases.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Andamio del enunciado con los 3 TODO resueltos (versión autónoma, con su propia sesión).
 * La versión "oficial" de la operación 5 es HoloNetControl.importarFlotaDesdeCSV(), que además
 * devuelve el número de naves importadas y valida cada línea.
 *
 * Cambio respecto a la plantilla: la Session se abre ANTES del try (no como recurso del
 * try-with-resources). Si fuera recurso, se cerraría ANTES de ejecutarse el catch y el
 * tx.rollback() se llamaría con la sesión ya cerrada.
 */
public class CargadorFlotaCSV {

    public static void importarDesdeRecursos(String nombreFichero) {
        InputStream is = CargadorFlotaCSV.class.getClassLoader().getResourceAsStream(
                nombreFichero);
        if (is == null) {
            System.err.println("❌ No se encontró el fichero de despliegue: " + nombreFichero);
            return;
        }

        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            tx = session.beginTransaction();
            String linea = br.readLine(); // Saltamos la cabecera

            while ((linea = br.readLine()) != null) {
                if (linea.isBlank()) {
                    continue;
                }
                String[] datos = linea.split(";");
                String tipo = datos[0].trim();
                String nombre = datos[1].trim();
                String modelo = datos[2].trim();
                String estado = datos[3].trim();
                int idPlaneta = Integer.parseInt(datos[4].trim());
                int valorEspecifico = Integer.parseInt(datos[5].trim());

                // TODO 1: referencia al Planeta (sin SELECT) dentro de la misma sesión
                Planeta planeta = session.getReference(Planeta.class, idPlaneta);

                // TODO 2: instanciar la subclase según 'tipo'
                Nave nave;
                if ("CAZA".equalsIgnoreCase(tipo)) {
                    nave = new CazaLigero(nombre, modelo, estado, planeta, valorEspecifico);
                } else if ("DESTRUCTOR".equalsIgnoreCase(tipo)) {
                    nave = new DestructorEstelar(nombre, modelo, estado, planeta, valorEspecifico);
                } else {
                    throw new IllegalArgumentException("TIPO desconocido: " + tipo);
                }

                // TODO 3: persistir la entidad polimórfica
                session.persist(nave);
            }
            tx.commit();
            System.out.println("✔ [HoloNet] Despliegue desde CSV completado en SQLite.");
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("❌ Fallo crítico en la carga masiva: " + e.getMessage());
        } finally {
            session.close();
        }
    }
}
