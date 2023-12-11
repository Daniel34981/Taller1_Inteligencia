import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class JuegoPorTurnos {

    public static final int FILAS = 6;
    public static final int COLUMNAS = 7;
    public static final int JUGADOR_1 = 1;
    public static final int JUGADOR_2 = 2;


    public static void main(String[] args) {
        int[][] tablero = new int[FILAS][COLUMNAS];
        imprimirTablero(tablero);

        Scanner scanner = new Scanner(System.in);
        int jugadorActual = JUGADOR_1;

        while (!juegoTerminado(tablero)) {
            System.out.println("Turno del Jugador " + jugadorActual);

            if (jugadorActual == JUGADOR_1) {
                // Turno del Jugador 1 (humano)
                System.out.print("Ingrese la columna (1-" + COLUMNAS + "): ");
                int columna = scanner.nextInt();
                if (columna < 1 || columna > COLUMNAS || !columnaValida(tablero, columna - 1)) {
                    System.out.println("Movimiento no válido. Inténtalo de nuevo.");
                    continue;
                }

                realizarMovimiento(tablero, columna - 1, jugadorActual);
            } else {
                // Turno del Jugador 2 (variante Alfa-Beta)
                Nodo nodoRaiz = new Nodo(tablero, JUGADOR_2, 6); // Profundidad máxima: 3
                int mejorColumna = seleccionarMovimientoAlfaBeta(nodoRaiz);
                realizarMovimiento(tablero, mejorColumna, JUGADOR_2);
                System.out.println("Jugador 2 seleccionó la columna: " + (mejorColumna + 1));
            }

            imprimirTablero(tablero);

            if (haGanado(tablero, jugadorActual)) {
                System.out.println("¡El Jugador " + jugadorActual + " ha ganado!");
                break;
            }

            jugadorActual = (jugadorActual == JUGADOR_1) ? JUGADOR_2 : JUGADOR_1;
        }

        scanner.close();
    }
    
    public static int seleccionarMovimientoAlfaBeta(Nodo nodoRaiz) {
        int mejorColumna = -1;
        int mejorValor = Integer.MIN_VALUE;

        // Ordena las columnas por alguna heurística (por ejemplo, la cantidad de fichas ya colocadas)
        List<Integer> columnasOrdenadas = ordenarColumnas(nodoRaiz.tablero);

        for (int columna : columnasOrdenadas) {
            if (columnaValida(nodoRaiz.tablero, columna)) {
                Nodo hijo = new Nodo(cloneTablero(nodoRaiz.tablero), JUGADOR_2, 5); // Profundidad máxima: 5
                realizarMovimiento(hijo.tablero, columna, JUGADOR_2);

                int valor = podaAlfaBeta(hijo, Integer.MIN_VALUE, Integer.MAX_VALUE);
                if (valor > mejorValor) {
                    mejorValor = valor;
                    mejorColumna = columna;
                }
            }
        }

        return mejorColumna;
    }

    public static List<Integer> ordenarColumnas(int[][] tablero) {
        List<Integer> columnasOrdenadas = new ArrayList<>();
        // Ordena las columnas por la cantidad de fichas ya colocadas (de menor a mayor)
        for (int columna = 0; columna < COLUMNAS; columna++) {
            if (columnaValida(tablero, columna)) {
                columnasOrdenadas.add(columna);
            }
        }
        columnasOrdenadas.sort(Comparator.comparingInt(col -> contarFichasEnColumna(tablero, col)));
        return columnasOrdenadas;
    }

    public static int contarFichasEnColumna(int[][] tablero, int columna) {
        int contador = 0;
        for (int fila = 0; fila < FILAS; fila++) {
            if (tablero[fila][columna] != 0) {
                contador++;
            }
        }
        return contador;
    }


    public static int podaAlfaBeta(Nodo nodo, int alfa, int beta) {
        if (nodo.profundidad == 0 || juegoTerminado(nodo.tablero)) {
            nodo.valor = evaluar(nodo.tablero);
            return nodo.valor;
        }

        if (nodo.jugador == JUGADOR_1) {
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
    
    public static int evaluar(int[][] tablero) {
        int utilidad = 0;

        // Heurística: cantidad de fichas en línea para el Jugador 1
        utilidad += evaluarFichasEnLinea(tablero, JUGADOR_1);

        // Heurística: cantidad de fichas en línea para el Jugador 2
        utilidad -= evaluarFichasEnLinea(tablero, JUGADOR_2);

        // Heurística: control del centro del tablero
        utilidad += evaluarControlCentro(tablero, JUGADOR_1);
        utilidad -= evaluarControlCentro(tablero, JUGADOR_2);

        // Otros factores de evaluación pueden ser agregados aquí

        return utilidad;
    }
    
    public static int evaluarFichasEnLinea(int[][] tablero, int jugador) {
        int utilidad = 0;

        // Evaluar filas
        utilidad += evaluarFichasEnLineaEnDireccion(tablero, jugador, 1, 0); // horizontal
        // Evaluar columnas
        utilidad += evaluarFichasEnLineaEnDireccion(tablero, jugador, 0, 1); // vertical
        // Evaluar diagonales
        utilidad += evaluarFichasEnLineaEnDireccion(tablero, jugador, 1, 1); // diagonal \
        utilidad += evaluarFichasEnLineaEnDireccion(tablero, jugador, 1, -1); // diagonal /

        return utilidad;
    }

    public static int evaluarFichasEnLineaEnDireccion(int[][] tablero, int jugador, int dirFila, int dirColumna) {
        int utilidad = 0;
        int consecutivas = 0;

        for (int fila = 0; fila < FILAS; fila++) {
            for (int columna = 0; columna < COLUMNAS; columna++) {
                int i = fila;
                int j = columna;

                while (i >= 0 && i < FILAS && j >= 0 && j < COLUMNAS) {
                    if (tablero[i][j] == jugador) {
                        consecutivas++;
                        if (consecutivas >= 4) {
                            // Puntuación alta si hay 4 o más fichas en línea
                            utilidad += 1000;
                        } else {
                            // Puntuación por cantidad de fichas consecutivas (menos de 4)
                            utilidad += consecutivas * 10;
                        }
                    } else {
                        consecutivas = 0;
                    }

                    i += dirFila;
                    j += dirColumna;
                }
            }
        }

        return utilidad;
    }


    public static int evaluarControlCentro(int[][] tablero, int jugador) {
        int utilidad = 0;

        for (int fila = 0; fila < FILAS; fila++) {
            for (int columna = 0; columna < COLUMNAS; columna++) {
                if (tablero[fila][columna] == jugador) {
                    // Mayor utilidad si las fichas del jugador están cerca del centro
                    utilidad += Math.abs(fila - FILAS / 2) + Math.abs(columna - COLUMNAS / 2);
                }
            }
        }

        return utilidad;
    }

    public static List<Nodo> generarHijos(Nodo nodo) {
        List<Nodo> hijos = new ArrayList<>();

        for (int columna = 0; columna < COLUMNAS; columna++) {
            if (columnaValida(nodo.tablero, columna)) {
                int[][] nuevoTablero = cloneTablero(nodo.tablero);
                realizarMovimiento(nuevoTablero, columna, nodo.jugador);

                Nodo hijo = new Nodo(nuevoTablero, (nodo.jugador == JUGADOR_1) ? JUGADOR_2 : JUGADOR_1, nodo.profundidad - 1);
                hijos.add(hijo);
            }
        }

        return hijos;
    }
    
    public static int[][] cloneTablero(int[][] tablero) {
        int filas = tablero.length;
        int columnas = tablero[0].length;

        int[][] nuevoTablero = new int[filas][columnas];
        for (int i = 0; i < filas; i++) {
            nuevoTablero[i] = tablero[i].clone();
        }

        return nuevoTablero;
    }


    public static void imprimirTablero(int[][] tablero) {
        for (int i = 0; i < FILAS; i++) {
            for (int j = 0; j < COLUMNAS; j++) {
                System.out.print(tablero[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println("---------------------");
    }

    public static boolean columnaValida(int[][] tablero, int columna) {
        return tablero[0][columna] == 0;
    }

    public static void realizarMovimiento(int[][] tablero, int columna, int jugador) {
        for (int i = FILAS - 1; i >= 0; i--) {
            if (tablero[i][columna] == 0) {
                tablero[i][columna] = jugador;
                break;
            }
        }
    }

    public static boolean juegoTerminado(int[][] tablero) {
        return haGanado(tablero, JUGADOR_1) || haGanado(tablero, JUGADOR_2) || tableroLleno(tablero);
    }

    public static boolean tableroLleno(int[][] tablero) {
        for (int columna = 0; columna < COLUMNAS; columna++) {
            if (columnaValida(tablero, columna)) {
                return false; // Si hay al menos una columna válida, el tablero no está lleno
            }
        }
        return true; // Todas las columnas están llenas, el tablero está lleno
    }


    public static boolean haGanado(int[][] tablero, int jugador) {
        return haGanadoEnDireccion(tablero, jugador, 1, 0) ||  // horizontal
               haGanadoEnDireccion(tablero, jugador, 0, 1) ||  // vertical
               haGanadoEnDireccion(tablero, jugador, 1, 1) ||  // diagonal \
               haGanadoEnDireccion(tablero, jugador, 1, -1);   // diagonal /
    }

    public static boolean haGanadoEnDireccion(int[][] tablero, int jugador, int dirFila, int dirColumna) {
        for (int fila = 0; fila < FILAS; fila++) {
            for (int columna = 0; columna < COLUMNAS; columna++) {
                int i = fila;
                int j = columna;
                int contador = 0;

                while (i >= 0 && i < FILAS && j >= 0 && j < COLUMNAS && tablero[i][j] == jugador) {
                    contador++;
                    if (contador == 4) {
                        return true;  // Cuatro fichas consecutivas encontradas
                    }

                    i += dirFila;
                    j += dirColumna;
                }
            }
        }

        return false;  // No se encontraron cuatro fichas consecutivas en esta dirección
    }

}
