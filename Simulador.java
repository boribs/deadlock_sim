package lectores_escritores;

import java.util.Vector;

public class Simulador {
    static public void main(String[] argv) {
        Vector<Proceso> procesos = new Vector<Proceso>();
        Vector<Recurso> recursos = new Vector<Recurso>();
        Logger logger = new Logger();

        // inicialización de procesos
        try {
            procesos.add(new Proceso("./procesos/a1.txt", "A0", recursos, logger));
            procesos.add(new Proceso("./procesos/a2.txt", "A1", recursos, logger));
            procesos.add(new Proceso("./procesos/a3.txt", "A2", recursos, logger));
        } catch (Exception e) {
            System.out.println("Error leyendo archivos: " + e.getMessage());
            return;
        }

        // permisos equivalentes a la cantidad de lectores
        int permisos = 0;
        for (Proceso p : procesos) {
            if (p.tipo == TipoProceso.LECTOR) {
                ++permisos;
            }
        }

        // si no hay lectores...
        if (permisos == 0) {
            permisos = 1;
        }

        // agrega un solo recurso
        recursos.add(new Recurso(permisos));

        System.out.println("");
        logger.tiempo(); // inicia tiempo de logger

        // inicia procesos
        for (Proceso p : procesos) {
            p.start();
        }

        // estar pendiente cuando acabe la simulación
        while (true) {
            int vivo = 0;
            for (Proceso p : procesos) {
                if (p.isAlive()) {
                    vivo++;
                }
            }

            if (vivo == 0) { break; }
        }

        System.out.println("\nSimulación terminada.");
        Graficador g = new Graficador(800);
        g.grafica(logger.eventos, procesos, permisos);
    }
}
