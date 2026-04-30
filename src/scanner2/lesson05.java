import java.util.Scanner;

public class lesson05 {
    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);
        int lZahl = (int)(Math.random() * 100) + 1;

        int eingabe;
        int versuche = 0;

        do {
            System.out.print("Geben Sie bitte eine Zahl ein: ");
            eingabe = s.nextInt();
            versuche++;

            if (eingabe < lZahl) {
                System.out.println("Die Zahl ist größer!");
            } else if (eingabe > lZahl) {
                System.out.println("Die Zahl ist kleiner!");
            }

        } while (eingabe != lZahl);

        System.out.println("Gratuliere!! Es waren " + versuche + " Versuche notwendig.");
        s.close();
    }
}