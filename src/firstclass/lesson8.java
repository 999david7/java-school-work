package firstclass;

public class lesson8 {

    public static boolean gewonnen(char[][] spielfeld) {

        int zeilen = spielfeld.length;
        int spalten = spielfeld[0].length;

        // horizontal prüfen
        for (int i = 0; i < zeilen; i++) {
            for (int j = 0; j <= spalten - 4; j++) {
                char c = spielfeld[i][j];
                if (c != ' ' &&
                        c == spielfeld[i][j + 1] &&
                        c == spielfeld[i][j + 2] &&
                        c == spielfeld[i][j + 3]) {
                    return true;
                }
            }
        }

        // vertikal prüfen
        for (int j = 0; j < spalten; j++) {
            for (int i = 0; i <= zeilen - 4; i++) {
                char c = spielfeld[i][j];
                if (c != ' ' &&
                        c == spielfeld[i + 1][j] &&
                        c == spielfeld[i + 2][j] &&
                        c == spielfeld[i + 3][j]) {
                    return true;
                }
            }
        }

        return false;
    }

    public static void main(String[] args) {
        char[][] feld = {
                {'X','X','X','X'},
                {'O','O','X','O'},
                {'X','O','O','X'}
        };

        System.out.println("Gewonnen: " + gewonnen(feld));
    }
}