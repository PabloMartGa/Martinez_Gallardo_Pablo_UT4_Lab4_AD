/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package clases;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

/**
 * Subclase con tabla propia "cazas_ligeros".
 * Su PK (caza_id) es a la vez FK hacia naves(nave_id): @PrimaryKeyJoinColumn lo indica.
 * Hibernate hace INSERT en naves y luego en cazas_ligeros con el mismo id.
 */
@Entity
@Table(name = "cazas_ligeros")
@PrimaryKeyJoinColumn(name = "caza_id")
public class CazaLigero extends Nave {

    @Column(name = "potencia_fuego", nullable = false)
    private int potenciaFuego;

    public CazaLigero() {
    }

    public CazaLigero(String nombre, String modelo, String estadoSistema,
                      Planeta planetaOrigen, int potenciaFuego) {
        super(nombre, modelo, estadoSistema, planetaOrigen);
        this.potenciaFuego = potenciaFuego;
    }

    public int getPotenciaFuego() { return potenciaFuego; }
    public void setPotenciaFuego(int potenciaFuego) { this.potenciaFuego = potenciaFuego; }

    @Override
    public String toString() {
        return super.toString() + "{potenciaFuego=" + potenciaFuego + "}";
    }
}
