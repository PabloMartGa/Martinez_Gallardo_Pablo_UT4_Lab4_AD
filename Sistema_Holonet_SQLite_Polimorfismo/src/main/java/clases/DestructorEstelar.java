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
 * Subclase con tabla propia "destructores_estelares".
 * Su PK (destructor_id) es a la vez FK hacia naves(nave_id).
 */
@Entity
@Table(name = "destructores_estelares")
@PrimaryKeyJoinColumn(name = "destructor_id")
public class DestructorEstelar extends Nave {

    @Column(name = "capacidad_escudos", nullable = false)
    private int capacidadEscudos;

    public DestructorEstelar() {
    }

    public DestructorEstelar(String nombre, String modelo, String estadoSistema,
                             Planeta planetaOrigen, int capacidadEscudos) {
        super(nombre, modelo, estadoSistema, planetaOrigen);
        this.capacidadEscudos = capacidadEscudos;
    }

    public int getCapacidadEscudos() { return capacidadEscudos; }
    public void setCapacidadEscudos(int capacidadEscudos) { this.capacidadEscudos = capacidadEscudos; }

    @Override
    public String toString() {
        return super.toString() + "{capacidadEscudos=" + capacidadEscudos + "}";
    }
}
