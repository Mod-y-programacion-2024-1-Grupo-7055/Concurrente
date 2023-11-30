import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Clase que guarda la lógica de operaciones
 * con matrices
 */
public class Matriz{
    private int[][] valores;

    /**
     * Constructor en base a un arreglo bidimensional
     * @param valores valores de la matriz
     * @throws IllegalArgumentException si se intenta crear una matriz
     *  que no sea de tamaño n x n (cuadrada)
     */
    public Matriz(int[][] valores) throws IllegalArgumentException {
        if (valores.length != valores[0].length)
            throw new IllegalArgumentException(
                "Solo se permiten matrices de tamaño n x n"
            );
        
        this.valores = valores;
    }

    /**
     * Crea una matriz usando un archivo
     * @param ruta ruta del archivo
     */
    public Matriz(String ruta){
        try {
            File archivo = new File(ruta);          
            Scanner lector = new Scanner(archivo);            
            int i = 0;            

            if(lector.hasNextLine()){
                String datos = lector.nextLine();            
                String[] temp = datos.split(" ");
                int t = temp.length;
                valores = new int[t][t];
                this.llenarFila(datos, i);
                i++;
            }

            while (lector.hasNextLine()) {
                String datos = lector.nextLine();            
                this.llenarFila(datos, i);  
                i++;        
            }
          lector.close();
        } catch (FileNotFoundException e) {
          System.out.println("An error occurred.");
          e.printStackTrace();
        }
    }

    /**
     * Auxiliar para llenar una fila de la matriz
     * @param datos Fila de datos
     * @param i número de fila
     */
    private void llenarFila(String datos, int i){
        String[] temp = datos.split(" ");
        for (int j = 0; j < temp.length; j++) {
            valores[i][j] = Integer.parseInt(temp[j]);
        }
    }

    /**
     * Multiplica la matriz con otra dada
     * @param matriz la matriz a multiplicar
     * @return el resultado de la multiplicación
     */
    public Matriz multiplica(Matriz matriz) {
        int n = valores.length;
        int[][] resultado = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < n; k++) {
                    resultado[i][j] += valores[i][k] * matriz.valores[k][j];
                }
            }
        }
        return new Matriz(resultado);
    }

    /**
     * Multiplica de forma concurrente dos matrices
     * @param matriz la segunda matriz a multiplicar
     * @param num_hilos número de hilos que se usarán
     * @return matriz resultante
     */
    public Matriz multiplicaConcurrente(Matriz matriz, int num_hilos) {
        MultiplicacionConcurrente mc = new MultiplicacionConcurrente(this, matriz);
        List<Thread> hilosh = new ArrayList<>();
        int n = valores.length;
        int hilos = num_hilos;  
        try {
            for(int i = 0; i < n; i++){
                Thread t = new Thread(mc, i + "");
                hilosh.add(t);
                t.start();                        
                if(hilosh.size() == hilos){
                    for(var threads: hilosh){
                            threads.join();
                        }
                        hilosh.clear();
                }
            }            
            for(Thread threads: hilosh){            
                    threads.join();
            }        
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return mc.resultado();
    }


    @Override
    public String toString() {
        String resultado = "";
        for (int i = 0; i < valores.length; i++) {
            resultado += "[ " + valores[i][0];
            for (int j = 1; j < valores.length; j++) {
                resultado += ", " + valores[i][j];
            }
            resultado += " ]\n";
        }
        return resultado;
    }

    /**
     * Clase auxiliar para la multiplicación concurrente
     * de dos matrices
     */
    class MultiplicacionConcurrente implements Runnable{
        private Matriz a;
        private Matriz b;
        private Matriz c;

        public MultiplicacionConcurrente(Matriz a, Matriz b){
            this.a = a;
            this.b = b;
            int n = a.valores.length;
            this.c = new Matriz(new int[n][n]);
        }

        @Override
        public void run(){
            String celda = Thread.currentThread().getName();
            calculaFila(Integer.parseInt(celda));
        }

        /**
         * Calcula los valores de una fila de la multipliación de matrices
         * @param fila fila a calcular
         */
        public void calculaFila(int fila){
            int n = a.valores.length;
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < n; k++) {
                    c.valores[fila][j] += a.valores[fila][k] * b.valores[k][j];
                }
            }
        }

        /**
         * Obtiene la matriz resultante de la multiplicación de matrices
         * @return matriz resultante
         */
        public Matriz resultado() {
            return c;
        }
    }

    public static void main(String[] args) {
        boolean con = Boolean.parseBoolean(args[0]);
        int num_hilos = Integer.parseInt(args[1]);
        Matriz a = new Matriz("mat10");
        long timestamp = System.nanoTime();
        Matriz res = con ? a.multiplica(a): a.multiplicaConcurrente(a, num_hilos); 
        long ms = System.nanoTime() - timestamp; 
        System.out.println("Tiempo transcurrido: " + ms);
    }

}