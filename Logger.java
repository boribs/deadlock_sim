package lectores_escritores;

import java.util.Vector;

/**
 * Clase de utilidad que lleva el registro de los eventos sucedidos
 * durante la simulación.
 * Refiere a eventos cuando un evento entra a computar.
 */
public class Logger {
    private long t;
    public Vector<Evento> eventos;

    public Logger() {
        eventos = new Vector<Evento>();
    }

    /**
     * Imprime un mensaje para un proceso y su tiempo de ejecución en ms.
     */
    public void log(String proceso, String msg) {
        System.out.println(String.format(
                "P(%s): %s (%d)",
                proceso,
                msg,
                System.currentTimeMillis() - t));
    }

    /**
     * Imprime el mensaje "Computando" para un proceso y su tiempo de ejecución en ms.
     * Después agrega un evento "computando" a la lista de eventos.
     * (logC -> log Computando)
     * Solo se usa cunado un proceso computa.
     */
    public void logC(String proceso, int tiempoComputo, int recursosEnUso) {
        long te = System.currentTimeMillis() - t;
        System.out.println(String.format(
            "P(%s): Computando %d ms. (%d)",
            proceso,
            tiempoComputo,
            te
        ));

        eventos.add(new Evento(
            proceso,
            recursosEnUso,
            te,
            tiempoComputo
        ));
    }

    /**
     * Imprime el mensaje "Terminado" para un proceso y su tiempo de ejecución en ms.
     * Después agrega un evento "terminado" a la lista de eventos.
     * (logT -> log Terminado)
     * Solo se usa cunado un proceso termina.
     */
    public void logT(String proceso) {
        long te = System.currentTimeMillis() - t;
        System.out.println(String.format(
            "P(%s): Terminado. (%d)",
            proceso,
            te
        ));

        eventos.add(new Evento(proceso, te));
    }

    /**
     * Asigna el tiempo inicial de ejecución al tiempo actual en ms.
     * Esto se usa para que las entradas al logger tengan tiempos pequeños
     * en vez del tiempo del sistema.
     */
    public void tiempo() {
        t = System.currentTimeMillis();
    }
}
