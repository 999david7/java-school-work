public class lesson5 {

    public static int countZeros(int[][] mat) {
        int count = 0;

        for (int[] row : mat) {
            for (int val : row) {
                if (val == 0) {
                    count++;
                }
            }
        }

        return count;
    }

    public static void main(String[] args) {
        int[][] mat = {
                {0, 1, 2},
                {0, 0, 3},
                {4, 5, 0}
        };

        System.out.println("Anzahl der 0en: " + countZeros(mat));
    }
}