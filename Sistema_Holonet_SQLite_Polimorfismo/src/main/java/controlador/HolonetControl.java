/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import org.hibernate.Session;
import org.hibernate.Transaction;
import clases.CazaLigero;
import clases.DestructorEstelar;
import clases.Nave;
import clases.Planeta;
import util.HibernateUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Controlador de persistencia del Sistema HoloNet.
 *
 * Patrón usado en TODOS los métodos de escritura:
 *   1) abrir Session  ->  2) beginTransaction  ->  3) operar  ->  4) commit
 *   Si algo falla: rollback + se relanza la excepción.  Siempre: cerrar la Session (finally).
 * (La Session se abre ANTES del try para poder cerrarla en el finally DESPUÉS del rollback.)
 */
public class HolonetControl {

    /** Cada cuántas naves se vacía la caché de sesión durante la importación masiva. */
    private static final int TAMANO_LOTE = 20;

    // =========================================================================
    // OPERACIÓN 1 - Alta polimórfica (persist)
    // =========================================================================

    /**
     * Da de alta una nave. Recibe el tipo ABSTRACTO Nave, pero el objeto real puede ser un
     * CazaLigero o un DestructorEstelar: Hibernate decide en qué tablas insertar según la
     * clase real (INSERT en "naves" + INSERT en la tabla hija).
     *
     * Tras persist(), el objeto queda con su id generado (IDENTITY ejecuta el INSERT ya).
     */
    public void registrarNuevaNave(Nave nave) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.persist(nave);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            session.close();
        }
    }

    // =========================================================================
    // OPERACIÓN 2 - Ciclo detached con merge
    // =========================================================================

    /**
     * Recibe una nave que fue modificada FUERA de sesión (estado DETACHED: tiene id pero
     * ninguna Session la gestiona). session.merge():
     *   1) carga la fila actual de la BD (SELECT en naves + tabla hija),
     *   2) copia sobre ella el estado del objeto recibido,
     *   3) en el commit, genera los UPDATE de lo que haya cambiado.
     * El objeto recibido NO pasa a ser gestionado; la copia gestionada es el valor devuelto por merge.
     */
    public void actualizarEstadoDesconectado(Nave naveModificada) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.merge(naveModificada);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            session.close();
        }
    }

    // =========================================================================
    // OPERACIÓN 3 - Consulta polimórfica HQL con JOIN FETCH
    // =========================================================================

    /**
     * HQL sobre la SUBCLASE CazaLigero: Hibernate hace JOIN naves + cazas_ligeros por nosotros.
     * JOIN FETCH c.planetaOrigen trae el planeta en la MISMA consulta, de modo que se puede
     * usar fuera de la sesión sin LazyInitializationException (y se evita el problema N+1).
     *
     * Nota: JOIN FETCH es INNER JOIN, así que un caza con planeta_origen_id NULL no saldría.
     * Si hiciera falta incluirlos: "LEFT JOIN FETCH".
     * Es una consulta de solo lectura: no necesita transacción explícita.
     */
    public List<CazaLigero> obtenerCazasPorPotenciaMinima(int potenciaMinima) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT c FROM CazaLigero c "
                    + "JOIN FETCH c.planetaOrigen "
                    + "WHERE c.potenciaFuego >= :potenciaMinima "
                    + "ORDER BY c.potenciaFuego DESC";
            return session.createQuery(hql, CazaLigero.class)
                    .setParameter("potenciaMinima", potenciaMinima)
                    .getResultList();
        }
    }

    // =========================================================================
    // OPERACIÓN 4 - Eliminación
    // =========================================================================

    /**
     * Recupera la nave con find() y la elimina con remove().
     * Hibernate borra primero la fila de la tabla hija y después la de "naves".
     *
     * @return true si existía y se eliminó; false si no había ninguna nave con ese id.
     */
    public boolean darDeBajaNave(int idNave) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Nave nave = session.find(Nave.class, idNave);   // polimórfico: devuelve CazaLigero o DestructorEstelar
            if (nave == null) {
                tx.rollback();                              // nada que borrar
                return false;
            }
            session.remove(nave);
            tx.commit();
            return true;
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            session.close();
        }
    }

    // =========================================================================
    // OPERACIÓN 5 (AMPLIACIÓN) - Importación masiva desde CSV, en una sola transacción
    // =========================================================================

    /**
     * Importa naves desde un CSV (separador ';', con cabecera) situado en el classpath
     * (src/main/resources). Todo en UNA transacción: o entran todas o no entra ninguna.
     *
     * @param rutaRecurso nombre del recurso, p. ej. "refuerzos_borde_exterior.csv"
     * @return número de naves importadas; 0 si el fichero no existe o si hubo un error (rollback).
     */
    public int importarFlotaDesdeCSV(String rutaRecurso) {
        InputStream is = HolonetControl.class.getClassLoader().getResourceAsStream(rutaRecurso);
        if (is == null) {
            System.err.println("❌ No se encontró el fichero de despliegue: " + rutaRecurso);
            return 0;
        }

        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        int importadas = 0;

        // El BufferedReader se cierra solo (y cierra el InputStream). La Session sigue abierta
        // hasta el finally, así que el rollback del catch funciona.
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            tx = session.beginTransaction();

            br.readLine();                  // 1) descartamos la cabecera
            int numeroLinea = 1;
            String linea;

            while ((linea = br.readLine()) != null) {
                numeroLinea++;
                if (linea.isBlank()) {
                    continue;               // ignoramos líneas vacías
                }

                // 2) + 3) construir la nave polimórfica y enlazarla con su planeta
                Nave nave = construirNave(session, linea, numeroLinea);
                session.persist(nave);
                importadas++;

                // Procesamiento por lotes: enviamos los INSERT y vaciamos la caché de primer nivel
                if (importadas % TAMANO_LOTE == 0) {
                    session.flush();
                    session.clear();
                }
            }

            tx.commit();                    // 4) todo OK -> se confirma
            System.out.println("✔ [HoloNet] Despliegue desde CSV completado en SQLite: "
                    + importadas + " naves.");
            return importadas;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();              // 4) algo falló -> no queda nada a medias
            }
            System.err.println("❌ Fallo crítico en la carga masiva (rollback ejecutado): " + e.getMessage());
            return 0;
        } finally {
            session.close();
        }
    }

    /** Convierte una línea del CSV en un CazaLigero o un DestructorEstelar (instanciación dinámica). */
    private Nave construirNave(Session session, String linea, int numeroLinea) {
        String[] datos = linea.split(";");
        if (datos.length < 6) {
            throw new IllegalArgumentException("Línea " + numeroLinea
                    + ": se esperaban 6 campos separados por ';' y hay " + datos.length);
        }

        String tipo = datos[0].trim();
        String nombre = datos[1].trim();
        String modelo = datos[2].trim();
        String estado = datos[3].trim();

        int idPlaneta;
        int valorEspecifico;
        try {
            idPlaneta = Integer.parseInt(datos[4].trim());
            valorEspecifico = Integer.parseInt(datos[5].trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Línea " + numeroLinea
                    + ": ID_PLANETA o ATRIBUTO_ESPECIFICO no son números enteros (" + linea + ")");
        }

        // getReference NO hace SELECT: crea un proxy con el id. Es suficiente para rellenar la FK.
        // Si el planeta no existe, la FK de SQLite (foreign_keys=on) falla al hacer INSERT -> rollback.
        Planeta planeta = session.getReference(Planeta.class, idPlaneta);

        switch (tipo.toUpperCase()) {
            case "CAZA":
                return new CazaLigero(nombre, modelo, estado, planeta, valorEspecifico);
            case "DESTRUCTOR":
                return new DestructorEstelar(nombre, modelo, estado, planeta, valorEspecifico);
            default:
                throw new IllegalArgumentException("Línea " + numeroLinea
                        + ": TIPO desconocido '" + tipo + "' (se esperaba CAZA o DESTRUCTOR)");
        }
    }

    // =========================================================================
    // MÉTODOS AUXILIARES (para poder demostrar las operaciones 1 y 2)
    // =========================================================================

    /**
     * Devuelve un Planeta ya cargado (find). Al cerrarse la sesión queda DETACHED, pero como
     * está inicializado se puede asignar a una nave nueva y persistir sin problemas.
     */
    public Planeta obtenerPlaneta(int idPlaneta) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.find(Planeta.class, idPlaneta);
        }
    }

    /**
     * Carga una nave y la devuelve DETACHED (la sesión se cierra al salir). Es el punto de partida
     * para modificarla "desconectada" y luego sincronizarla con actualizarEstadoDesconectado().
     * Ojo: su planetaOrigen es LAZY y quedará sin inicializar (no acceder a él fuera de sesión).
     */
    public Nave obtenerNave(int idNave) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.find(Nave.class, idNave);
        }
    }
}
