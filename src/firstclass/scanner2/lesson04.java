package firstclass.scanner2;

import java.util.Scanner;

public class lesson04 {

    public static int[] lottoZahlen() {
        int[] ziehung = new int[6];

        for (int i = 0; i < 6; i++) {
            ziehung[i] = (int)(Math.random() * 49) + 1;
        }
        return ziehung;
    }

    public static int lottoTip(int[] tip, int[] ziehung) {
        int richtige = 0;

        for (int i = 0; i < tip.length; i++) {
            for (int j = 0; j < ziehung.length; j++) {
                if (tip[i] == ziehung[j]) {
                    richtige++;
                }
            }
        }
        return richtige;
    }

    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);
        int[] tip = new int[6];

        System.out.println("Geben Sie 6 Lottozahlen ein:");

        for (int i = 0; i < 6; i++) {
            System.out.print("Zahl " + (i + 1) + ": ");
            tip[i] = s.nextInt();
        }

        int[] ziehung = lottoZahlen();
        int richtige = lottoTip(tip, ziehung);

        System.out.println("Richtige Zahlen: " + richtige);
        s.close();
    }
}