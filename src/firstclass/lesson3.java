package firstclass;

public class lesson3 {

    public static void ausgabe(int[][] mat) {
        for (int i = 0; i < mat.length; i++) {
            for (int j = 0; j < mat[i].length; j++) {
                System.out.print(mat[i][j] + " ");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        int[][] mat = {
                {1, 2, 3},
                {4, 5},
                {6, 7, 8, 9}
        };

        ausgabe(mat);
    }
}