package com.alura.literalura;

import com.alura.literalura.model.Book;
import com.alura.literalura.model.Author;
import com.alura.literalura.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

@SpringBootApplication
public class LiteraluraApplication implements CommandLineRunner {
	@Autowired
	private BookService bookService;

	private final Scanner scanner = new Scanner(System.in);

	public static void main(String[] args) {
		SpringApplication.run(LiteraluraApplication.class, args);
	}

	@Override
	public void run(String... args) {
		while (true) {
			System.out.println("\n=== LiterAlura - Catálogo de Libros ===");
			System.out.println("1. Buscar libro por título");
			System.out.println("2. Listar todos los libros");
			System.out.println("3. Listar autores");
			System.out.println("4. Buscar libros por idioma");
			System.out.println("5. Listar autores vivos en un año");
			System.out.println("6. Salir");

			int option = scanner.nextInt();
			scanner.nextLine(); // Consumir el salto de línea

			switch (option) {
				case 1:
					searchBookByTitle();
					break;
				case 2:
					listAllBooks();
					break;
				case 3:
					listAuthors();
					break;
				case 4:
					listBooksByLanguage();
					break;
				case 5:
					listAuthorsAliveInYear();
					break;
				case 6:
					return;
				default:
					System.out.println("Opción inválida");
			}
		}
	}

	private void searchBookByTitle() {
		System.out.println("Ingrese el título del libro a buscar:");
		String title = scanner.nextLine();

		try {
			Book book = bookService.searchAndSaveBookByTitle(title);
			if (book != null) {
				System.out.println("Libro encontrado y guardado:");
				showBookDetails(book);
			} else {
				System.out.println("No se encontró ningún libro con el título: " + title);
				System.out.println("Sugerencias:");
				System.out.println("- Verifica la ortografía");
				System.out.println("- Intenta con un título más general");
				System.out.println("- Prueba con el título en inglés");
			}
		} catch (Exception e) {
			System.out.println("Ocurrió un error al buscar el libro: " + e.getMessage());
		}
	}

	private void listAllBooks() {
		List<Book> books = bookService.getAllBooks();
		if (books.isEmpty()) {
			System.out.println("No hay libros en la base de datos.");
			return;
		}

		System.out.println("\nListado de libros:");
		books.forEach(this::showBookDetails);
	}

	private void listAuthors() {
		List<Author> authors = bookService.getAllAuthors();
		if (authors.isEmpty()) {
			System.out.println("No hay autores en la base de datos.");
			return;
		}

		System.out.println("\nListado de autores:");
		for (Author author : authors) {
			System.out.printf("\nNombre: %s (%d - %s)\n",
					author.getName(),
					author.getBirthYear() != null ? author.getBirthYear() : 0,
					author.getDeathYear() != null ? author.getDeathYear() : "presente");
		}
	}

	private void listBooksByLanguage() {
		// Mostrar idiomas disponibles
		List<String> uniqueLanguages = bookService.getUniqueLanguages();

		if (uniqueLanguages.isEmpty()) {
			System.out.println("No hay libros guardados en la base de datos.");
			return;
		}

		System.out.println("Idiomas disponibles:");
		uniqueLanguages.forEach(System.out::println);

		System.out.println("Ingrese el código de idioma (por ejemplo, en, es, fr):");
		String language = scanner.nextLine();

		int bookCount = bookService.countBooksByLanguage(language);
		System.out.println("Número de libros en " + language + ": " + bookCount);

		// Mostrar los libros de ese idioma
		List<Book> languageBooks = bookService.getAllBooks().stream()
				.filter(book -> book.getLanguage().equals(language))
				.collect(Collectors.toList());

		if (!languageBooks.isEmpty()) {
			System.out.println("\nLibros en " + language + ":");
			languageBooks.forEach(this::showBookDetails);
		}
	}

	private void listAuthorsAliveInYear() {
		System.out.println("Ingrese el año para buscar autores vivos:");

		try {
			int year = scanner.nextInt();
			scanner.nextLine(); // Consumir salto de línea

			List<Author> authorsAlive = bookService.findAuthorsAliveInYear(year);

			if (authorsAlive.isEmpty()) {
				System.out.println("No se encontraron autores vivos en el año " + year);
			} else {
				System.out.println("Autores vivos en " + year + ":");
				authorsAlive.forEach(author -> {
					System.out.printf("Nombre: %s (Nacimiento: %d, Muerte: %s)\n",
							author.getName(),
							author.getBirthYear(),
							author.getDeathYear() != null ? author.getDeathYear() : "Presente");
				});
			}
		} catch (Exception e) {
			System.out.println("Por favor, ingrese un año válido.");
			scanner.nextLine(); // Limpiar el buffer del scanner
		}
	}

	private void showBookDetails(Book book) {
		System.out.println("\nTítulo: " + book.getTitle());
		if (book.getAuthor() != null) {
			System.out.println("Autor: " + book.getAuthor().getName());
		}
		System.out.println("Descargas: " + book.getDownloadCount());
		System.out.println("Idioma: " + book.getLanguage());
	}
}