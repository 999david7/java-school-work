package firstclass;

public class lesson7 {

    public static int[] zeile(int[][] mat, int zeilenNr) {
        if (zeilenNr < 0 || zeilenNr >= mat.length) {
            return new int[0]; // leeres Array
        }
        return mat[zeilenNr];
    }

    public static void main(String[] args) {
        int[][] mat = {
                {1, 2, 3},
                {4, 5},
                {6, 7, 8}
        };

        int[] result = zeile(mat, 1);

        for (int val : result) {
            System.out.print(val + " ");
        }
    }
}