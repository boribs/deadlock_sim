package lectores_escritores;

import java.util.concurrent.Semaphore;

/**
 * Wrapper para el semáforo.
 * Esta clase representa un recurso. Bloquea la ejecución de un proceso
 * cuando no se tiene el recurso requerido.
 */
public class Recurso {
    private Semaphore disponibilidad;
    public int t;

    /**
     * Inicializa el semáforo con la cantidad de permisos especificados.
     * Debe haber tantos permisos como lectores haya.
     */
    public Recurso(int permisos) {
        t = permisos;
        disponibilidad = new Semaphore(permisos);
    }

    /**
     * Pide un permiso al semáforo.
     * Pensado para ser llamado por lectores.
     */
    public void pide() throws Exception {
        disponibilidad.acquire();
    }

    /**
     * Pide todos los permisos al semáforo.
     * Pensado para ser llamado por escritores.
     */
    public void pideTodos() throws Exception {
        disponibilidad.acquire(t);
    }

    /**
     * Libera un permiso del semáforo.
     * Pensado para ser llamado por lectores.
     */
    public void libera() {
        disponibilidad.release();
    }


    /** Libera todos los permisos del semáforo.
     * Pensado para ser llamado por escritores.
     */
    public void liberaTodos() {
        disponibilidad.release(t);
    }
}
