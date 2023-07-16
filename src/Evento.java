package lectores_escritores;

/**
 * Clase para almacenar información sobre cuándo un proceso está computando.
 * Esencial para la graficación.
 * Diseñada para interactuar con `Logger.java`.
 */
public class Evento {
    public String nombreProceso;
    public TipoEvento tipo;
    public long tiempoEjecucion, tiempoComputo;
    public int recursosEnUso;

    /**
     * Constructor cuando se trata de un proceso computando.
     */
    public Evento(
        String nombre,
        int recursosEnUso,
        long tiempoEjecucion,
        int tiempoComputo
    ) {
        nombreProceso = nombre;
        this.recursosEnUso = recursosEnUso;
        this.tiempoEjecucion = tiempoEjecucion;
        this.tiempoComputo = tiempoComputo;
        this.tipo = TipoEvento.COMPUTAR;
    }

    /**
     * Constructor cuando se trata de un proceso terminando.
     */
    public Evento(
        String nombre,
        long tiempoEjecucion
    ) {
        nombreProceso = nombre;
        this.recursosEnUso = -1;
        this.tiempoComputo = -1;
        this.tiempoEjecucion = tiempoEjecucion;
        this.tipo = TipoEvento.TERMINAR;
    }
}
