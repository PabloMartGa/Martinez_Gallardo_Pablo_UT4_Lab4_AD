/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package clases;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;


@Entity
@Table(name = "naves")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Nave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nave_id")
    private Integer id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "modelo", nullable = false)
    private String modelo;

    // 'Critico', 'Estable' o 'Mantenimiento'
    @Column(name = "estado_sistema", nullable = false)
    private String estadoSistema;

    /**
     * Muchas naves -> un planeta. LAZY: el planeta NO se carga con la nave, solo cuando
     * se accede a él (y siempre que la Session siga abierta, o se haya traído con JOIN FETCH).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planeta_origen_id")
    private Planeta planetaOrigen;

    protected Nave() {
    }

    protected Nave(String nombre, String modelo, String estadoSistema, Planeta planetaOrigen) {
        this.nombre = nombre;
        this.modelo = modelo;
        this.estadoSistema = estadoSistema;
        this.planetaOrigen = planetaOrigen;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public String getEstadoSistema() { return estadoSistema; }
    public void setEstadoSistema(String estadoSistema) { this.estadoSistema = estadoSistema; }

    public Planeta getPlanetaOrigen() { return planetaOrigen; }
    public void setPlanetaOrigen(Planeta planetaOrigen) { this.planetaOrigen = planetaOrigen; }

    /** Ojo: no se imprime planetaOrigen a propósito (es LAZY y fuera de sesión daría LazyInitializationException). */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[id=" + id + ", nombre=" + nombre
                + ", modelo=" + modelo + ", estado=" + estadoSistema + "]";
    }
}
