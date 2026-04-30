public class lesson6 {

    public static int sum(int[][] mat) {
        int summe = 0;

        for (int[] row : mat) {
            for (int val : row) {
                summe += val;
            }
        }

        return summe;
    }

    public static void main(String[] args) {
        int[][] mat = {
                {1, 2, 3},
                {4, 5},
                {6, 7, 8}
        };

        System.out.println("Summe: " + sum(mat));
    }
}