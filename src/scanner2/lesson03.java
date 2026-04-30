import java.util.Scanner;

public class lesson03 {
    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);
        int summe = 0;
        int zahl;

        do {
            System.out.print("Bitte eine Ganzzahl eingeben: ");
            zahl = s.nextInt();
            summe += zahl;
        } while (zahl > 0);

        System.out.println("Die Summe ist: " + summe);
        s.close();
    }
}