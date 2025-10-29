package it.example.bookshop.isbn;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IsbnValidatorTest {
    @Test void ok_isbn13() { assertTrue(new IsbnValidator().isValid("9780306406157")); }
    @Test void ok_isbn10() { assertTrue(new IsbnValidator().isValid("0306406152")); }
    @Test void ko() { assertFalse(new IsbnValidator().isValid("1234567890123")); }
}