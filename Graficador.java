package lectores_escritores;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.lang.Math;

/**
 * Esta clase es la responsable de generar la gráfica de la simulación.
 * Crea un diagrama de flechas a partir de un vector de eventos (`Evento.java`).
 * Para esto se usa el método `grafica()`.
 *
 * Consideraciones:
 *    - Solo grafica un recurso.
 *    - Los recursos deben tener nombres diferentes.
 *
 * Se pueden modificar muchas propiedades para cambiar la apariencia de la gráfica:
 *    Las siguientes propiedades están en términso de píxeles.
 *
 * 1. Anchuras de columnas.
 *    - anchoRecursosEnEspera: la anchura necesaria para mostrar la lista
 *                             de recursos que se encuentran en espera.
 *    - anchoRecursoEnEspera: el ancho aproximado del nombre de cada proceso.
 *    - minAnchoRecursosEnEspera: el mínimo valor de anchura para la columna
 *                                "Recursos en espera".
 *     - anchoValorRecurso: el ancho de la columna "Valor Semáforo".
 *     - anchoColProceso: el ancho de la columna correspondiente a cada proceso.
 *                        La imagen final tendrá un ancho igual a la suma de las
 *                        anchuras anteriores + (anchoColProceso * número de procesos).
 *     - anchoGuia: anchura de la columna donde se dibuja la guía.
 *     - margenX: el margen entre el borde de la imagen y cualquier dibujo
 *                dentro de esta.
 *     - amplitudFlecha: amplitud de la cabeza de las flechas.
 *
 * 2. Alturas de elementos.
 *     - altoFlechaGuia: altura (longitud) de las flechas mostradas en la guía.
 *     - margenYTop: margen entre el borde superior de la imagen y el inicio del
 *                   área donde se permite dibujar las flechas.
 *     - margenYBottom: margen entre el borde inferior de la imagen y el final del
 *                      área donde se permite dibujar flechas.
 *     - margenFlecha: distancia a considerar entre el inicio/final de una flecha
 *                     para su dibujo.
 *                     Se usa para separar visualmente el inicio de una flecha con
 *                     el final de la anterior.
 *     - margenPunteada: igual al margen anterior, pero para flechas punteadas.
 *     - margenFlechaGuia: igual al margen anterior, pero considerado en las flechas
 *                         dibujadas en la guía.
 *
 * 3. Otros.
 *     - textoY: posición vertical sobre la cual se dibujan los nombres de las columnas.
 *     - maxTiempo: tiempo final de la simulación. Utilizado durante el escalado.
 */
public class Graficador {
    private HashMap<String, Vector<Flecha>> flechas;
    private Vector<Linea> lineas;

    private final int TOLERANCIA_MS = 3;
    private final int pxPerIntervalo = 5;

    private int altura, anchura; // calculado por graficador

    private int anchoRecursosEnEspera; // calculado por graficador
    private int anchoRecursoEnEspera = 33;
    private int minAnchoRecursosEnEspera = 200;
    private int anchoValorRecurso = 150;
    private int anchoGuia = 240;
    private int anchoColProceso = 100;
    private int altoFlechaGuia = 40;

    private int margenX = 30;
    private int margenYTop = 60;
    private int margenYBottom = 30;
    private int margenFlecha = 5;
    private int margenPunteada = 5;
    private int margenFlechaGuia = 20;

    private int textoY = 40;
    private int amplitudFlecha = 10;

    private long maxTiempo = 0;

    /**
     * Construye un Graficador, se le pasa la altura deseada del archivo de salida.
     * Es recomendable una altura superior a 500px;
     */
    public Graficador(int alto) {
        lineas = new Vector<Linea>();
        flechas = new HashMap<String, Vector<Flecha>>();
        altura = alto;
    }

    /**
     * Crea una gráfica "grafica.jpg" a partir de una serie de eventos generados
     * en el simulador.
     */
    public void grafica(Vector<Evento> eventos, Vector<Proceso> procesos, int permisos) {
        creaFlechas(eventos, procesos);

        // calcula el ancho de la columna donde se muestra la lista de procesos esperando
        anchoRecursosEnEspera = (procesos.size() * anchoRecursoEnEspera);
        if (anchoRecursosEnEspera < minAnchoRecursosEnEspera) {
            anchoRecursosEnEspera = minAnchoRecursosEnEspera;
        }

        // calcula el ancho de la imagen resultante
        anchura = (procesos.size() * anchoColProceso) +
                    anchoRecursosEnEspera +
                    anchoValorRecurso +
                    anchoGuia +
                    margenX;

        BufferedImage img = new BufferedImage(anchura, altura, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();

        g.setColor(Color.white);
        g.fillRect(0, 0, anchura, altura);

        g.setFont(new Font("Purisa", Font.PLAIN, 15));
        // dibujar lineas horizontales y sus datos
        for (Linea l : lineas) {

            // dibuja linea horizontal
            g.setColor(Color.lightGray);
            lineaPunteadaH(margenX, anchura - anchoGuia - margenX, escala(l.altura), g);

            l.obtenInfo(permisos);
            // dibuja el valor del semáforo
            g.drawString(
                    String.format("%d", l.valSem),
                    anchoRecursosEnEspera + (int) (anchoValorRecurso / 1.5),
                    escala(l.altura - 1));

            // dibuja la lista de procesos esperando
            g.setColor(Color.gray);
            g.drawString(
                    l.esperando(),
                    margenX + minAnchoRecursosEnEspera / 5,
                    escala(l.altura - 1));
        }

        g.setColor(Color.black);
        g.drawString(
                "Valor Semáforo",
                anchoRecursosEnEspera + (int) (anchoValorRecurso / 2.9),
                textoY);
        g.drawString(
                "Procesos en espera",
                (int) (anchoRecursosEnEspera / 3.4),
                textoY);

        // dibujar flechas
        int x = anchoRecursosEnEspera + anchoValorRecurso;
        g.setColor(Color.black);

        for (String k : flechas.keySet()) {
            x += anchoColProceso / 2;

            g.drawString(
                    k + (procesos.stream()
                            .filter(p -> p.nombre == k)
                            .findFirst()
                            .get().tipo == TipoProceso.ESCRITOR ? " (E)" : " (L)"),
                    x - 7,
                    textoY);

            for (Flecha f : flechas.get(k)) {
                switch (f.tipoFlecha) {
                    case NORMAL:
                        flecha(x, escala(f.inicio) + margenFlecha, escala(f.fin) - margenFlecha, g);
                        break;
                    case PUNTEADA:
                        flechaPunteada(x, escala(f.inicio) + margenPunteada, escala(f.fin) - margenPunteada, g);
                        break;
                    case CRITICA:
                        flechaCritica(x, escala(f.inicio) + margenFlecha, escala(f.fin) - margenFlecha, g);
                        break;
                }
            }
            x += anchoColProceso / 2;
        }

        // dibujar guía
        x += 2 * margenX;
        int y = altura / 8;

        int ax = x - 2 * amplitudFlecha;
        int ay = y - margenFlechaGuia;
        int bx = x + anchoGuia - 2 * margenX + amplitudFlecha / 2;
        int by = y + (3 * (margenFlechaGuia + altoFlechaGuia)) + margenFlechaGuia;

        g.setColor(Color.gray);
        g.setStroke(new BasicStroke(2.0f));

        g.drawLine(ax, ay, ax, by);
        g.drawLine(ax, by, bx, by);
        g.drawLine(bx, by, bx, ay);
        g.drawLine(bx, ay, ax, ay);
        g.setStroke(new BasicStroke());

        g.drawString("Guia", x + 3 * margenFlechaGuia, y);

        y += margenFlechaGuia;
        flecha(x, y, y + altoFlechaGuia, g);
        g.drawString("Ejecución normal", x + margenFlechaGuia, y + altoFlechaGuia / 2);

        y += margenFlechaGuia + altoFlechaGuia;
        flechaPunteada(x, y, y + altoFlechaGuia, g);
        g.drawString("En espera", x + margenFlechaGuia, y + altoFlechaGuia / 2);

        y += margenFlechaGuia + altoFlechaGuia;
        flechaCritica(x, y, y + altoFlechaGuia, g);
        g.drawString("Ejecución con recurso", x + margenFlechaGuia, y + altoFlechaGuia / 2);

        g.dispose();

        // guardar archivo
        File f = new File("grafica.jpg");
        try {
            ImageIO.write(img, "jpg", f);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Crea flechas a partir de la lista de eventos.
     * Las flechas creadas almacenan su inicio y su final en términos de milisegundos,
     * aquellos proporcionados por el simulador.
     * Asigna la variable `maxTiempo`.
     */
    private void creaFlechas(Vector<Evento> eventos, Vector<Proceso> procesos) {
        // agrega flechas existentes
        for (Evento e : eventos) {
            if (flechas.get(e.nombreProceso) == null) {
                flechas.put(e.nombreProceso, new Vector<Flecha>());
            }

            long tiempoFinal = e.tiempoEjecucion + e.tiempoComputo;

            if (tiempoFinal > e.tiempoEjecucion) {
                if (e.tipo != TipoEvento.TERMINAR) {
                    if (lineas.stream().filter(l -> l.altura == tiempoFinal).findAny().isEmpty()) {
                        lineas.add(new Linea(tiempoFinal));
                    }
                }

                if (tiempoFinal > maxTiempo) {
                    maxTiempo = tiempoFinal;
                }

                Flecha f = new Flecha(
                        e.tiempoEjecucion,
                        tiempoFinal,
                        e.recursosEnUso == 0 ? TipoFlecha.NORMAL : TipoFlecha.CRITICA,
                        procesos.stream().filter(p -> e.nombreProceso == p.nombre).findFirst().get());

                flechas.get(e.nombreProceso).add(f);
                lineas.stream().filter(l -> l.altura == tiempoFinal).findAny().get().flechas.add(f);
            }
        }

        alineaFlechas();

        // agrega flechas punteadas
        for (String k : flechas.keySet()) {
            for (int i = 0; i < flechas.get(k).size() - 1; ++i) {
                Flecha a = flechas.get(k).elementAt(i),
                        b = flechas.get(k).elementAt(i + 1);

                if (a.fin < b.inicio) {
                    Flecha f = new Flecha(
                            a.fin,
                            b.inicio,
                            TipoFlecha.PUNTEADA,
                            procesos.stream().filter(p -> k.equals(p.nombre)).findFirst().get());

                    flechas.get(k).insertElementAt(f, i + 1);

                    for (Linea l : lineas) {
                        if (f.inicio < l.altura && f.fin > l.altura) {
                            l.flechas.add(f);
                        }
                    }
                }
            }
        }
    }

    /**
     * Acomoda las flechas de manera que no haya
     * lineas muy cercanas entre sí.
     */
    private void alineaFlechas() {
        Vector<Integer> aEliminar = new Vector<Integer>();

        long t = lineas.size() - 1;
        for (int i = 0; i < t; ++i) {
            Linea a = lineas.elementAt(i),
                    b = lineas.elementAt(i + 1);

            if (b.altura != a.altura && b.altura - a.altura <= TOLERANCIA_MS) {
                long d = (b.altura + a.altura) / 2;
                lineas.elementAt(i).mueve(d, TOLERANCIA_MS);
                lineas.elementAt(i + 1).mueve(d, TOLERANCIA_MS);

                lineas.elementAt(i).flechas.addAll(lineas.elementAt(i + 1).flechas);
                lineas.elementAt(i + 1).flechas.clear();

                aEliminar.add(i + 1);
                i++;
            }
        }

        for (int i = aEliminar.size() - 1; i >= 0; --i) {
            lineas.remove(lineas.elementAt(aEliminar.elementAt(i)));
        }
    }

    /**
     * Dibuja una flecha simple de (x, y1) a (x, y2).
     * Requiere y1 <= y2.
     */
    private void flecha(int x, int y1, int y2, Graphics2D g) {
        assert y1 <= y2 : "Y1 siempre tiene que ser menor a Y2!";

        g.drawLine(x, y1, x, y2);
        g.drawLine(x, y2, x - amplitudFlecha, y2 - amplitudFlecha);
        g.drawLine(x, y2, x + amplitudFlecha, y2 - amplitudFlecha);
    }

    /**
     * Dibuja una flecha con linea punteada de (x, y1) a (x, y2).
     * Originalmente dibujaba una flecha con el tronco punteado.
     * Actualmente solo dibuja una linea punteada.
     * Se mantuvo el nombre por consistencia.
     */
    private void flechaPunteada(int x, int y1, int y2, Graphics2D g) {
        lineaPunteadaV(x, y1, y2, g);
    }

    /**
     * Dibuja una flecha simple gruesa de (x, y1) a (x, y2).
     */
    private void flechaCritica(int x, int y1, int y2, Graphics2D g) {
        g.setStroke(new BasicStroke(4.0f));
        flecha(x + 1, y1, y2, g);
        g.setStroke(new BasicStroke());
    }

    /**
     * Dibuja una linea punteada de (x, y1) a (x, y2).
     * Exclusivo para lineas verticales.
     * Requiere y1 <= y2.
     */
    private void lineaPunteadaV(int x, int y1, int y2, Graphics2D g) {
        assert y1 <= y2 : "Y1 siempre tiene que ser menor a Y2!";

        int b = y2 - y1;
        int y = y1;

        while ((y - y1) < b) {
            g.drawLine(x, y, x, y + pxPerIntervalo);
            y += 2 * pxPerIntervalo;
        }
    }

    /**
     * Dibuja una linea punteada de (x1, y) a (x2, y).
     * Exclusivo para lineas horizontales.
     * Requiere x1 <= x2.
     */
    private void lineaPunteadaH(int x1, int x2, int y, Graphics2D g) {
        // Este método es para lineas horizontales (hacia abajo)!
        assert x1 <= x2 : "Y1 siempre tiene que ser menor a Y2!";

        int a = x2 - x1;
        int x = x1;

        while ((x - x1) < a) {
            g.drawLine(x, y, x + pxPerIntervalo, y);
            x += 2 * pxPerIntervalo;
        }
    }

    /**
     * Convierte de ms, la medida en la que se guardan las flechas, a px.
     */
    private int escala(long v) {
        return ((int) v * (altura - margenYTop - margenYBottom) / (int) maxTiempo) + margenYTop;
    }
}

enum TipoFlecha {
    NORMAL,
    PUNTEADA,
    CRITICA
}

/**
 * Clase para almacenar los valores de las flechas.
 * Usadas durante el dibujo y la alineación.
 */
class Flecha {
    public long inicio, fin;
    public TipoFlecha tipoFlecha;
    public Proceso padre;

    /**
     * Constructor simple, esta clase funge como una tupla con nombre.
     */
    public Flecha(long inicio, long fin, TipoFlecha tipoFlecha, Proceso padre) {
        this.inicio = inicio;
        this.fin = fin;
        this.tipoFlecha = tipoFlecha;
        this.padre = padre;
    }
}

/**
 * Clase que lleva el control de la gráfica.
 * Se usa para agrupar flechas y hacer el cálculo del
 * valor del semáforo `valSem` así como la lista de
 * procesos en espera.
 */
class Linea {
    public Vector<Flecha> flechas;
    public Vector<String> procesosEsperando;
    public long altura;
    public int valSem = 0;

    /**
     * Crea una instancia de Linea sin flechas asociadas.
     * Se le pasa la altura a la que se encuentra, en términos de ms.
     */
    public Linea(long alt) {
        flechas = new Vector<Flecha>();
        procesosEsperando = new Vector<String>();
        altura = alt;
    }

    /**
     * Mueve una linea y las flechas que la componen a cierta altura.
     */
    public void mueve(long alt, long tolerancia) {
        altura = alt;
        for (int i = 0; i < flechas.size(); ++i) {
            if (flechas.elementAt(i).tipoFlecha == TipoFlecha.PUNTEADA) {
                long ini = flechas.elementAt(i).inicio,
                        fin = flechas.elementAt(i).fin;

                if (Math.abs(ini - altura) <= tolerancia) {
                    flechas.remove(flechas.elementAt(i));
                } else if (Math.abs(fin - altura) <= tolerancia) {
                    flechas.elementAt(i).fin = altura;
                }
            } else {
                flechas.elementAt(i).fin = alt;
            }
        }
    }

    /**
     * Calcula el valor del semáforo y determina la lista
     * de procesos esperando correspondiente a la linea.
     */
    public void obtenInfo(int permisos) {
        boolean criticaEscritor = false;
        int criticasLector = 0,
            punteadasLector = 0,
            punteadasEscritor = 0;

        for (Flecha f : flechas) {
            if (f.tipoFlecha == TipoFlecha.CRITICA) {
                if (f.padre.tipo == TipoProceso.ESCRITOR) {
                    criticaEscritor = true;
                } else {
                    criticasLector++;
                }
            } else if (f.tipoFlecha == TipoFlecha.PUNTEADA) {
                if (f.padre.tipo == TipoProceso.ESCRITOR) {
                    punteadasEscritor++;
                } else {
                    punteadasLector++;
                }
                procesosEsperando.add(f.padre.nombre);
            }
        }

        int p = permisos;

        if (criticaEscritor) {
            p = 0;
            p -= punteadasLector;
        } else if (criticasLector != 0) {
            p -= criticasLector;
            p -= (punteadasEscritor * permisos);
        }

        valSem = p;

        if (valSem == permisos) {
            procesosEsperando.clear();
        }
    }

    /**
     * Regresa una cadena con los nombres de los procesos en espera
     * separados por comas y espacios:
     *
     * "A1, A2, A3"
     */
    public String esperando() {
        return procesosEsperando.stream().collect(Collectors.joining(", "));
    }
}
