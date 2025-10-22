package it.example.bookshop.catalog.service;

import it.example.bookshop.catalog.service.model.*;
import it.example.bookshop.common.dto.BookUpsert;
import it.example.bookshop.catalog.service.events.BookEvents;
import it.example.bookshop.catalog.service.events.BookCreatedPayload;
import jakarta.enterprise.event.Event;
import it.example.bookshop.catalog.service.isbn.IsbnClient;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import java.util.*;
import java.util.stream.Collectors;
import java.util.HashMap;

@Path("/catalog")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CatalogResource {

    @Inject
    BookEvents events;
    @Inject
    @RestClient
    IsbnClient isbnClient;
    
    @Inject
    Event<BookCreatedPayload> bookCreatedEvent;

    private Map<String, Object> bookToMap(Book b) {
        if (b == null) {
            throw new NotFoundException();
        }
        
        Map<String, Object> map = new HashMap<>();

        map.put("isbn", b.isbn);
        map.put("title", b.title);
        map.put("description", b.description);
        map.put("language", b.language);
        map.put("publishedYear", b.publishedYear);
        map.put("priceCents", b.priceCents);
        map.put("authors", b.authors.stream().map(a -> a.name).toList());
        map.put("categories", b.categories.stream().map(c -> c.slug).toList());
        
        return map;
    }

    @GET
    @Path("/books")
    public List<BookUpsert> list(@QueryParam("search") String q, @QueryParam("category") String cat,
            @QueryParam("author") String auth, @QueryParam("lang") String lang,
            @QueryParam("minYear") Integer minY, @QueryParam("maxYear") Integer maxY,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        var query = new StringBuilder("1=1");
        var params = new HashMap<String, Object>();
        if (q != null && !q.isBlank()) {
            query.append(" and (lower(title) like :q or lower(description) like :q)");
            params.put("q", "%" + q.toLowerCase() + "%");
        }
        if (lang != null && !lang.isBlank()) {
            query.append(" and language = :lang");
            params.put("lang", lang);
        }
        if (minY != null) {
            query.append(" and publishedYear >= :minY");
            params.put("minY", minY);
        }
        if (maxY != null) {
            query.append(" and publishedYear <= :maxY");
            params.put("maxY", maxY);
        }
        if (cat != null && !cat.isBlank()) {
            query.append(" and :categorySlug in (select c.slug from categories c)");
            params.put("categorySlug", cat);
        }
        if (auth != null && !auth.isBlank()) {
            query.append(" and :authorName in (select a.name from authors a)");
            params.put("authorName", auth);
        }
        List<Book> pageList = Book.find(query.toString(), params).page(page, size).list();
        return pageList.stream().map(this::toDto).collect(Collectors.toList());
    }

    @GET
    @Path("/books/{isbn}")
    public BookUpsert get(@PathParam("isbn") String isbn) {
        Book b = Book.find("isbn", isbn).firstResult();
        if (b == null) throw new NotFoundException();
        return toDto(b);
    }

    @POST
    @Path("/books")
    @Transactional
    public Response create(BookUpsert req, @Context UriInfo uri) {
        if (req == null) {
            throw new BadRequestException("Body mancante");
        }
        String isbn = (String) req.isbn();
        String title = (String) req.title();
        List<String> authors = req.authors();
        List<String> categories = req.categories();

        if (!isbnClient.validate(isbn).valid()) {
            throw new BadRequestException("ISBN non valido");
        }
        if (Book.find("isbn", isbn).firstResult() != null) {
            throw new ClientErrorException("ISBN già presente", 409);
        }
        Book b = new Book();
        b.isbn = isbn;
        b.title = title;
        b.description = (String) req.description();
        b.language = (String) req.language();
        b.publishedYear = req.publishedYear();
        b.priceCents = req.priceCents();
        b.authors = resolveAuthors(authors);
        b.categories = resolveCategories(categories);
        b.persist();
        //events.bookCreated(b.isbn, b.title, b.authors.stream().map(a -> a.name).toList());
        bookCreatedEvent.fire(new BookCreatedPayload(b.isbn, b.title, b.authors.stream().map(a -> a.name).toList()));
        return Response.created(uri.getAbsolutePathBuilder().path(b.isbn).build()).entity(bookToMap(b)).build();
    }

    @PUT
    @Path("/books/{isbn}")
    @Transactional
    public BookUpsert update(@PathParam("isbn") String isbn, BookUpsert req) {
        Book b = Book.find("isbn", isbn).firstResult();
        if (b == null) {
            throw new NotFoundException();
        }
        if (req.title() != null) {
            b.title = req.title();
        }
        if (req.description() != null) {
            b.description = req.description();
        }
        if (req.language() != null) {
            b.language = req.language();
        }
        if (req.publishedYear() != null) {
            b.publishedYear = req.publishedYear();
        }
        if (req.priceCents() != null) {
            b.priceCents = req.priceCents();
        }
        if (req.authors() != null) {
            b.authors = resolveAuthors(req.authors());
        }
        if (req.categories() != null) {
            b.categories = resolveCategories(req.categories());
        }
        return toDto(b);
    }

    private static java.util.Set<Author> resolveAuthors(java.util.List<String> names) {
        if (names == null) {
            return java.util.Set.of();
        }
        java.util.Set<Author> out = new java.util.HashSet<>();
        for (String n : names) {
            Author a = Author.<Author>find("name", n).firstResult(); // <-- tipizzato
            if (a == null) {
                a = new Author();
                a.name = n;
                a.persist();
            }
            out.add(a);
        }
        return out;
    }

    private static java.util.Set<Category> resolveCategories(java.util.List<String> slugs) {
        if (slugs == null) {
            return java.util.Set.of();
        }
        java.util.Set<Category> out = new java.util.HashSet<>();
        for (String s : slugs) {
            Category c = Category.<Category>find("slug", s).firstResult(); // <-- tipizzato
            if (c == null) {
                c = new Category();
                c.slug = s;
                c.name = Character.toUpperCase(s.charAt(0)) + s.substring(1);
                c.persist();
            }
            out.add(c);
        }
        return out;
    }

    private BookUpsert toDto(Book b) {
        if (b == null) return null;
        return new BookUpsert(
            b.isbn, b.title, b.description, b.language, b.publishedYear, b.priceCents,
            b.authors.stream().map(a -> a.name).toList(),
            b.categories.stream().map(c -> c.slug).toList()
        );
    }
}
