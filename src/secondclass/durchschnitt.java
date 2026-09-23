package secondclass;

public class durchschnitt {
    public static void main(String[] args){
        int[] werte = {122, 133, 190, 310};
        double ergebnis = durchschnitt(werte);
        System.out.println(ergebnis);
    }

public static double durchschnitt(int[] werte){
        int summe = 0;

        for (int i = 0; i < werte.length; i++){
            summe = summe + werte[i];
        }
        return summe / (double) werte.length;
}
}
