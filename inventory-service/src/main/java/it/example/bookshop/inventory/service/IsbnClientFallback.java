package it.example.bookshop.inventory.service;

import org.eclipse.microprofile.faulttolerance.ExecutionContext;
import org.eclipse.microprofile.faulttolerance.FallbackHandler;

public class IsbnClientFallback implements FallbackHandler<IsbnClient.ValidationResult> {

    @Override
    public IsbnClient.ValidationResult handle(ExecutionContext ctx) {
        // Estrae l'ISBN dal metodo validate(String isbn)
        String isbn = (String) ctx.getParameters()[0];
        boolean valid = isValidLocally(isbn);
        // Degradazione: usiamo una validazione locale per non bloccare del tutto il flusso
        return new IsbnClient.ValidationResult(isbn, valid);
    }

    // Validazione locale (checksum ISBN-10/13) – copia minimale dell'algoritmo
    private boolean isValidLocally(String isbn) {
        if (isbn == null) return false;
        String s = isbn.replaceAll("[-\\s]", "");
        if (s.length() == 10) return isValid10(s);
        if (s.length() == 13) return isValid13(s);
        return false;
    }

    private boolean isValid10(String s) {
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            char c = s.charAt(i);
            if (!Character.isDigit(c)) return false;
            sum += (c - '0') * (10 - i);
        }
        char last = s.charAt(9);
        int check = (last == 'X' || last == 'x') ? 10 : (Character.isDigit(last) ? last - '0' : -1);
        if (check < 0) return false;
        return ((sum + check) % 11) == 0;
    }

    private boolean isValid13(String s) {
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            char c = s.charAt(i);
            if (!Character.isDigit(c)) return false;
            int d = c - '0';
            sum += d * ((i % 2 == 0) ? 1 : 3);
        }
        int check = (10 - (sum % 10)) % 10;
        int last = s.charAt(12) - '0';
        return last == check;
    }
}
