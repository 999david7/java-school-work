import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class lesson10 {


    public static int[][] leseMatrix(String dateiname) throws FileNotFoundException {

        Scanner s = new Scanner(new File(dateiname));

        // erste Zeile lesen
        String ersteZeile = s.nextLine();
        Scanner s2 = new Scanner(ersteZeile);

        int zeilen = s2.nextInt();
        int spalten = s2.nextInt();

        int[][] matrix = new int[zeilen][spalten];

        int i = 0;

        while (s.hasNextLine() && i < zeilen) {
            String zeile = s.nextLine();
            Scanner zeilenScanner = new Scanner(zeile);

            int j = 0;
            while (zeilenScanner.hasNextInt() && j < spalten) {
                matrix[i][j] = zeilenScanner.nextInt();
                j++;
            }
            i++;
        }

        s.close();
        return matrix;
    }

    public static void main(String[] args) {
        try {
            int[][] mat = leseMatrix("matrix.txt");

            for (int[] row : mat) {
                for (int val : row) {
                    System.out.print(val + " ");
                }
                System.out.println();
            }

        } catch (FileNotFoundException e) {
            System.out.println("Datei nicht gefunden!");
        }
    }
}