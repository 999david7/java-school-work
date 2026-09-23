package firstclass;

import java.util.Random;

public class lesson4 {

    public static int[][] zMatrix(int groesse, int obergrenze) {
        Random rand = new Random();
        int[][] mat = new int[groesse][groesse];

        for (int i = 0; i < groesse; i++) {
            for (int j = 0; j < groesse; j++) {
                mat[i][j] = rand.nextInt(obergrenze + 1);
            }
        }

        return mat;
    }

    public static void main(String[] args) {
        int[][] matrix = zMatrix(3, 10);

        for (int[] row : matrix) {
            for (int val : row) {
                System.out.print(val + " ");
            }
            System.out.println();
        }
    }
}