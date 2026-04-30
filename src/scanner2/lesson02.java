import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class lesson02 {
    public static void main(String[] args) {
        int count = 0;

        try {
            Scanner s = new Scanner(new File("text.txt"));

            while (s.hasNext()) {
                s.next();
                count++;
            }

            s.close();
            System.out.println("Anzahl der Wörter: " + count);

        } catch (FileNotFoundException e) {
            System.out.println("Datei nicht gefunden!");
        }
    }
}