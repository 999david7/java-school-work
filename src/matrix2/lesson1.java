public class lesson1 {
    public static void main(String[] args) {
        int[][] mat = {
                {1, 2, 3},
                {4, 5, 7},
                {6, 7, 8}
        };

        int zeilen = mat.length;

        System.out.println("Anzahl Zeilen: " + zeilen);

        for (int i = 0; i < zeilen; i++) {
            System.out.println("Spalten in Zeile " + i + ": " + mat[i].length);
        }
    }
}