package secondclass;

public class umdrehwort {
    public static String umdrehen(String s) {
        char[] zeichen = s.toCharArray();
        char[] umgedreht = new char[zeichen.length];

        for (int i = 0; i < zeichen.length; i++) {
            umgedreht[i] = zeichen[zeichen.length - 1 - i];
        }
        return new String(umgedreht);


        }
    public static void main (String[]args){
        System.out.println(umdrehen("hallo"));
    }

}