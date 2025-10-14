package it.example.isbn;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class IsbnValidator {
    public boolean isValid(String isbn) {
        if (isbn == null) return false;
        String s = isbn.replaceAll("[-\\s]", "");
        return (s.length() == 10) ? isValid10(s) : (s.length()==13 && isValid13(s));
    }
    
    private boolean isValid10(String s) {
        int sum=0;
        for (int i=0;i<9;i++) {
            char c=s.charAt(i);
            if (!Character.isDigit(c)) 
                return false;
            sum+=(c-'0')*(10-i);
        }
        char last=s.charAt(9);    
        int check = (last=='X'||last=='x') ? 10:Character.isDigit(last)?last-'0':-1;
        if (check<0) 
            return false;
        return ((sum+check)%11)==0;
    }

    private boolean isValid13(String s) {
        int sum=0;
        for (int i=0;i<12;i++) {
            int d = s.charAt(i)-'0'; 
            if (d<0||d>9) 
                return false;
            sum += d * ((i%2==0)?1:3);
        }
        int check = (10 - (sum%10))%10;
        int last = s.charAt(12)-'0';
        return last==check;
    }
}