/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.sistema_holonet_sqlite_polimorfismo;

import clases.CazaLigero;
import clases.DestructorEstelar;
import clases.Nave;
import clases.Planeta;
import controlador.HolonetControl;
import util.HibernateUtil;
import java.util.List;

/**
 * Demostración lineal de las 5 operaciones del laboratorio.
 * No depende de los datos de ejemplo de las naves (solo necesita el planeta 2 = Yavin IV),
 * así que funciona igual tras ejecutar init_holonet_sqlite.sql o reset_datos_holonet.sql.
 *
 * Nota: la operación 5 (CSV) añade 5 naves cada vez que se ejecuta. Para repetirla desde
 * cero, ejecuta antes sql/reset_datos_holonet.sql.
 */
public class Sistema_Holonet_SQLite_Polimorfismo {

    public static void main(String[] args) {
        HolonetControl control = new HolonetControl();

        try {
            // ---------------------------------------------------------------
            titulo("OPERACIÓN 1 - Alta polimórfica (persist)");
            Planeta yavin = control.obtenerPlaneta(2);
            CazaLigero yWing = new CazaLigero("Caza Y-Wing", "BTL-A4", "Estable", yavin, 70);
            DestructorEstelar nebulon = new DestructorEstelar(
                    "Fragata Nebulon-B", "Nebulon-B", "Estable", yavin, 12000);

            control.registrarNuevaNave(yWing);
            control.registrarNuevaNave(nebulon);
            System.out.println("Registrado: " + yWing);
            System.out.println("Registrado: " + nebulon);

            // ---------------------------------------------------------------
            titulo("OPERACIÓN 2 - Ciclo detached con merge");
            Nave desconectada = control.obtenerNave(yWing.getId());   // sesión cerrada -> DETACHED
            System.out.println("Antes : " + desconectada);
            desconectada.setEstadoSistema("Mantenimiento");           // modificamos FUERA de sesión
            ((CazaLigero) desconectada).setPotenciaFuego(100);
            control.actualizarEstadoDesconectado(desconectada);       // merge sincroniza con la BD
            System.out.println("Después (releído de la BD): " + control.obtenerNave(yWing.getId()));

            // ---------------------------------------------------------------
            titulo("OPERACIÓN 3 - Consulta HQL polimórfica con JOIN FETCH (potencia >= 80)");
            mostrarCazas(control.obtenerCazasPorPotenciaMinima(80));

            // ---------------------------------------------------------------
            titulo("OPERACIÓN 4 - Eliminación (find + remove)");
            System.out.println("Baja del caza " + yWing.getId() + ": "
                    + control.darDeBajaNave(yWing.getId()));
            System.out.println("Baja del destructor " + nebulon.getId() + ": "
                    + control.darDeBajaNave(nebulon.getId()));
            System.out.println("Baja de la nave 9999 (no existe): " + control.darDeBajaNave(9999));

            // ---------------------------------------------------------------
            titulo("OPERACIÓN 5 - Importación masiva desde CSV (transacción única)");
            int importadas = control.importarFlotaDesdeCSV("refuerzos_borde_exterior.csv");
            System.out.println("Naves importadas: " + importadas);

            System.out.println("\nCazas con potencia >= 90 tras la importación:");
            mostrarCazas(control.obtenerCazasPorPotenciaMinima(90));

        } finally {
            HibernateUtil.cerrar();
        }
    }

    private static void mostrarCazas(List<CazaLigero> cazas) {
        if (cazas.isEmpty()) {
            System.out.println("(ninguno)");
        }
        for (CazaLigero c : cazas) {
            // getPlanetaOrigen() funciona aquí, fuera de la sesión, gracias al JOIN FETCH
            System.out.println(" - " + c.getNombre() + " | potencia " + c.getPotenciaFuego()
                    + " | planeta: " + c.getPlanetaOrigen().getNombre());
        }
    }

    private static void titulo(String texto) {
        System.out.println("\n=== " + texto + " ===");
    }
}
