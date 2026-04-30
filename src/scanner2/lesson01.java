import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class lesson01 {
    public static void main(String[] args) {
        try {
            Scanner s = new Scanner(new File("text.txt"));

            while (s.hasNextLine()) {
                String line = s.nextLine();
                if (!line.startsWith("#")) {
                    System.out.println(line);
                }
            }

            s.close();
        } catch (FileNotFoundException e) {
            System.out.println("Datei nicht gefunden!");
        }
    }
}