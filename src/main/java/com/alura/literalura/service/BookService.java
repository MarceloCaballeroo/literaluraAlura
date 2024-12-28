package com.alura.literalura.service;

import com.alura.literalura.model.*;
import com.alura.literalura.repository.BookRepository;
import com.alura.literalura.repository.AuthorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookService {
    private static final Logger logger = LoggerFactory.getLogger(BookService.class);
    private static final String API_URL = "https://gutendex.com/books";
    private final RestTemplate restTemplate;
    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;

    @Autowired
    public BookService(BookRepository bookRepository, AuthorRepository authorRepository) {
        this.restTemplate = new RestTemplate();
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
    }

    public int countBooksByLanguage(String language) {
        return bookRepository.findByLanguage(language).size();
    }

    public List<Author> findAuthorsAliveInYear(Integer year) {
        return authorRepository.findAuthorsAliveInYear(year);
    }

    public List<String> getUniqueLanguages() {
        return bookRepository.findAll().stream()
                .map(Book::getLanguage)
                .distinct()
                .collect(Collectors.toList());
    }

    public Book searchAndSaveBookByTitle(String title) {
        try {
            String encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8);
            String url = API_URL + "?search=" + encodedTitle;

            logger.info("Buscando libro con URL: {}", url);

            GutendexResponse response = restTemplate.getForObject(url, GutendexResponse.class);

            if (response != null && response.getResults() != null && !response.getResults().isEmpty()) {
                logger.info("Se encontraron {} libros", response.getResults().size());

                Optional<GutendexBook> matchedBook = response.getResults().stream()
                        .filter(book -> book.getTitle().toLowerCase().contains(title.toLowerCase()))
                        .findFirst()
                        .or(() -> Optional.ofNullable(response.getResults().get(0)));

                return matchedBook.map(this::convertAndSaveBook).orElse(null);
            } else {
                logger.warn("No se encontraron libros para el título: {}", title);
                return null;
            }
        } catch (Exception e) {
            logger.error("Error al buscar el libro", e);
            return null;
        }
    }

    private Book convertAndSaveBook(GutendexBook gutendexBook) {
        Book book = new Book();
        book.setTitle(gutendexBook.getTitle());
        book.setDownloadCount(gutendexBook.getDownload_count());

        // Manejar idiomas
        if (gutendexBook.getLanguages() != null && !gutendexBook.getLanguages().isEmpty()) {
            book.setLanguage(gutendexBook.getLanguages().get(0));
        }

        // Manejar autores
        if (gutendexBook.getAuthors() != null && !gutendexBook.getAuthors().isEmpty()) {
            GutendexPerson gutendexAuthor = gutendexBook.getAuthors().get(0);
            Author author = new Author();
            author.setName(gutendexAuthor.getName()); // Manejar años de nacimiento y muerte
            if (gutendexAuthor.getBirth_year() != null) {
                author.setBirthYear(gutendexAuthor.getBirth_year());
            }
            if (gutendexAuthor.getDeath_year() != null) {
                author.setDeathYear(gutendexAuthor.getDeath_year());
            }

            book.setAuthor(author);
        }

        return bookRepository.save(book);
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public List<Author> getAllAuthors() {
        return authorRepository.findAll();
    }
}