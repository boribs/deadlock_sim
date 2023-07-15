package lectores_escritores;

import java.util.*;
import java.io.*;

/**
 * Clase que representa un proceso en la simulación.
 * Aquí es donde se llevan a cabo la limitación del problema de
 * "Lectores-Escritores", ya que la simulación termina cunado
 * todos los procesos hayan ejecutado su serie de instrucciones.
 */
public class Proceso extends Thread {
    private Vector<Recurso> recursos;
    private Stack<Comando> comandos;
    private Logger logger;
    private int recursosEnUso;

    public TipoProceso tipo;
    public String nombre;

    /**
     * Constructor que necesita una lista de recursos disponible, un logger
     * para poder registrar los eventos y el nombre del archivo del cual
     * saca sus respectivas instrucciones y el tipo de proceso al que corresponde.
     */
    public Proceso(String archivo, String nombre, Vector<Recurso> recursos, Logger logger)
    throws Exception {
        comandos = new Stack<Comando>();
        tipo = null;

        this.nombre = nombre;
        this.recursos = recursos;
        this.logger = logger;
        recursosEnUso = 0;

        leeArchivo(archivo);
    }

    /**
     * Libera cierta cantidad de permisos dependiendo el tipo de proceso.
     * Interactua con `Recurso.java`.
     */
    private synchronized void libera(int r) {
        if (tipo == TipoProceso.ESCRITOR) {
            recursos.elementAt(r).liberaTodos();
        } else {
            recursos.elementAt(r).libera();
        }
    }

    /**
     * Pide cierta cantidad de permisos dependiendo el tipo de proceso.
     * Interactua con `Recurso.java`.
     */
    private synchronized void pide(int r) {
        try {
            if (tipo == TipoProceso.ESCRITOR) {
                recursos.elementAt(r).pideTodos();
            } else {
                recursos.elementAt(r).pide();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Loop principal de cada proceso.
     * Finciona sincronizando hilos con el semáfotro del recurso,
     * ejecutando una instrucción a la vez.
     */
    public synchronized void run() {
        while (comandos.size() > 0) {
            Comando c = comandos.remove(0);

            try {
                switch (c.tipo) {
                    case COMPUTE:
                        logger.logC(nombre, c.valor, recursosEnUso);
                        wait(c.valor);
                        break;

                    case FREE:
                        logger.log(nombre, "Liberando recurso " + c.valor + ".");
                        libera(c.valor);
                        recursosEnUso--;
                        break;

                    case REQUIRE:
                        logger.log(nombre, "Pidiendo recurso " + c.valor + ".");
                        pide(c.valor);
                        logger.log(nombre, "Obtuvo recurso " + c.valor + ".");
                        recursosEnUso++;
                        break;

                    case HALT:
                        logger.logT(nombre);
                        break;
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    /**
     * Inicializa los valores del proceso con los datos obtenidos
     * de leer `archivo`.
     *
     * Se espera una sintaxis parecida a la que requiere el MOSS deadlock,
     * pero con un caracter adicional: E o L, según si el proceso es
     * lector o escritor.
     *
     * Se llama en el constructor.
     * Lanza una excepción cuando no encuentra el tipo de proceso.
     */
    private void leeArchivo(String archivo) throws Exception {
        File f = new File(archivo);
        Scanner s = new Scanner(f);

        System.out.println("Leyendo " + archivo);

        while (s.hasNextLine()) {
            String l = s.nextLine();

            if (l.isBlank()) {
                continue;
            }

            StringTokenizer t = new StringTokenizer(l);
            String token = t.nextToken().strip();

            if (token.startsWith("E")) { // Es un escritor
                tipo = TipoProceso.ESCRITOR;
            } else if (token.startsWith("L")) { // Es un lector
                tipo = TipoProceso.LECTOR;
            } else if (token.startsWith("C")) {
                comandos.push(new Comando(
                    Cmd.COMPUTE,
                    Integer.parseInt(t.nextToken().strip())
                ));
            } else if (token.startsWith("R")) {
                comandos.push(new Comando(
                    Cmd.REQUIRE,
                    Integer.parseInt(t.nextToken().strip())
                ));
            } else if (token.startsWith("F")) {
                comandos.push(new Comando(
                    Cmd.FREE,
                    Integer.parseInt(t.nextToken().strip())
                ));
            } else if (token.startsWith("H")) {
                comandos.push(new Comando(
                    Cmd.HALT,
                    -1
                ));
            }
        }
        s.close();

        if (tipo == null) { // no se definió un tipo para este proceso
            throw new Exception("No hay tipo de proceso (" + archivo + ").\n");
        }
    }
}
