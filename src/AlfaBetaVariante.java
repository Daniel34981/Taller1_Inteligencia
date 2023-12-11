import java.util.ArrayList;
import java.util.List;

class Nodo {
    int[][] tablero;
    int jugador;
    int profundidad;
    Integer valor; // Puede ser nulo si aún no se ha evaluado

    Nodo(int[][] tablero, int jugador, int profundidad) {
        this.tablero = tablero;
        this.jugador = jugador;
        this.profundidad = profundidad;
        this.valor = null;
    }
}

public class AlfaBetaVariante {

    public static final int JUGADOR_MAX = 1;
    public static final int JUGADOR_MIN = 2;

    public static int evaluar(int[][] tablero) {
        int utilidad = 0;

        // Evaluar la proximidad al centro del tablero
        int filas = tablero.length;
        int columnas = tablero[0].length;
        int centroFila = filas / 2;
        int centroColumna = columnas / 2;

        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                if (tablero[i][j] == JUGADOR_MAX) {
                    // Aumenta la utilidad si la ficha pertenece al jugador MAX y está cerca del centro
                    utilidad += distanciaManhattan(i, j, centroFila, centroColumna);
                } else if (tablero[i][j] == JUGADOR_MIN) {
                    // Reduce la utilidad si la ficha pertenece al jugador MIN y está cerca del centro
                    utilidad -= distanciaManhattan(i, j, centroFila, centroColumna);
                }
            }
        }

        // Otros factores de evaluación específicos del juego pueden ser considerados aquí

        return utilidad;
    }

    // Función de distancia de Manhattan entre dos puntos en el tablero
    public static int distanciaManhattan(int fila1, int columna1, int fila2, int columna2) {
        return Math.abs(fila1 - fila2) + Math.abs(columna1 - columna2);
    }


    public static int podaAlfaBeta(Nodo nodo, int alfa, int beta) {
        if (nodo.profundidad == 0 || juegoTerminado(nodo.tablero)) {
            nodo.valor = evaluar(nodo.tablero);
            return nodo.valor;
        }

        if (nodo.jugador == JUGADOR_MAX) {
            int valorMax = Integer.MIN_VALUE;
            for (Nodo hijo : generarHijos(nodo)) {
                valorMax = Math.max(valorMax, podaAlfaBeta(hijo, alfa, beta));
                alfa = Math.max(alfa, valorMax);
                if (beta <= alfa) {
                    break; // Poda beta
                }
            }
            nodo.valor = valorMax;
            return valorMax;
        } else {
            int valorMin = Integer.MAX_VALUE;
            for (Nodo hijo : generarHijos(nodo)) {
                valorMin = Math.min(valorMin, podaAlfaBeta(hijo, alfa, beta));
                beta = Math.min(beta, valorMin);
                if (beta <= alfa) {
                    break; // Poda alfa
                }
            }
            nodo.valor = valorMin;
            return valorMin;
        }
    }

    // Funciones de ayuda
    public static boolean juegoTerminado(int[][] tablero) {
        // Verificar si el juego ha terminado
        return false;
    }

    public static List<Nodo> generarHijos(Nodo nodo) {
        // Generar los posibles estados hijos del nodo
        return new ArrayList<>();
    }

    // Uso del algoritmo
    public static void main(String[] args) {
        int[][] tableroInicial = new int[6][7]; // Tablero vacío de 6x7
        Nodo nodoRaiz = new Nodo(tableroInicial, JUGADOR_MAX, 3); // Profundidad máxima: 3
        int valorOptimo = podaAlfaBeta(nodoRaiz, Integer.MIN_VALUE, Integer.MAX_VALUE);
        System.out.println("Valor óptimo: " + valorOptimo);
    }
}
