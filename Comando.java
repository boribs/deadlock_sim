package lectores_escritores;

/**
 * Tupla con nombre.
 * Almacena el tipo de comando y un valor.
 * No todos los comandos tienen un valor asociado.
 *
 * La utilizan los Procesos (`Proceso.java`).
 */
public class Comando {
    public Cmd tipo;
    public int valor;

    public Comando(Cmd t, int v) {
        tipo = t;
        valor = v;
    }
}
