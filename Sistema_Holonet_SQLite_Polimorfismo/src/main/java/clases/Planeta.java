/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package clases;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Mapeo estándar contra la tabla "planetas".
 * No tiene herencia: es la entidad "referenciada" por las naves (lado 1 de la relación 1-N).
 */
@Entity
@Table(name = "planetas")
public class Planeta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "planeta_id")
    private Integer id;

    @Column(name = "nombre", nullable = false, unique = true)
    private String nombre;

    @Column(name = "clima")
    private String clima;

    @Column(name = "afiliacion")
    private String afiliacion;

    /** Hibernate exige un constructor sin argumentos. */
    public Planeta() {
    }

    public Planeta(String nombre, String clima, String afiliacion) {
        this.nombre = nombre;
        this.clima = clima;
        this.afiliacion = afiliacion;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getClima() { return clima; }
    public void setClima(String clima) { this.clima = clima; }

    public String getAfiliacion() { return afiliacion; }
    public void setAfiliacion(String afiliacion) { this.afiliacion = afiliacion; }

    @Override
    public String toString() {
        return "Planeta[id=" + id + ", nombre=" + nombre + ", clima=" + clima
                + ", afiliacion=" + afiliacion + "]";
    }
}
