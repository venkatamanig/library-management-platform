package org.example.lms.service;

import org.example.lms.dto.BookRequestDTO;
import org.example.lms.dto.BookResponseDTO;
import org.example.lms.dto.BookUpdateDTO;
import org.example.lms.entity.Book;
import org.example.lms.exception.BookNotFoundException;
import org.example.lms.exception.DuplicateIsbnException;
import org.example.lms.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final AuditService auditService;

    public BookService(BookRepository bookRepository, AuditService auditService) {
        this.bookRepository = bookRepository;
        this.auditService = auditService;
    }

    @Cacheable(cacheNames = "books", key = "#id")
    public BookResponseDTO getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() ->
                        new BookNotFoundException(
                                "Book not found " + id
                        )
                );

        return new BookResponseDTO(
                book.getBookId(),
                book.getTitle(),
                book.getAuthor(),
                book.getPublicationYear(),
                book.getIsbn()
        );
    }

    public boolean isIsbnExists(String isbn) {
        return bookRepository.existsByIsbn(isbn);
    }

    @CacheEvict(cacheNames = {"books", "book-lists"}, allEntries = true)
    public BookResponseDTO createBook(BookRequestDTO request) {

        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new DuplicateIsbnException(request.getIsbn());
        }

        Book book = new Book(
                request.getTitle(),
                request.getAuthor(),
                request.getPublicationYear(),
                request.getIsbn()
        );

        Book savedbook = bookRepository.save(book);
        auditService.logCurrentUser("BOOK_CREATED", "Created book " + savedbook.getBookId());

        return new BookResponseDTO(
                savedbook.getBookId(),
                savedbook.getTitle(),
                savedbook.getAuthor(),
                savedbook.getPublicationYear(),
                savedbook.getIsbn()
        );

    }

    @Cacheable(cacheNames = "book-lists", key = "'all'")
    public List<BookResponseDTO> getAllBook() {
        return bookRepository.findAll()
                .stream()
                .map(book -> new BookResponseDTO(
                        book.getBookId(),
                        book.getTitle(),
                        book.getAuthor(),
                        book.getPublicationYear(),
                        book.getIsbn()
                ))
                .toList();
    }

    @CacheEvict(cacheNames = {"books", "book-lists"}, allEntries = true)
    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() ->
                        new BookNotFoundException("Book not found" + id)
                );
        bookRepository.delete(book);
        auditService.logCurrentUser("BOOK_DELETED", "Deleted book " + id);
    }

    @CacheEvict(cacheNames = {"books", "book-lists"}, allEntries = true)
    public BookResponseDTO updateBook(Long id, BookUpdateDTO request) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() ->
                        new BookNotFoundException("Book not found " + id)
                );

        if (request.getIsbn() != null &&
                bookRepository.existsByIsbnAndBookIdNot(request.getIsbn(), id)) {
            throw new DuplicateIsbnException(request.getIsbn());
        }

        if(request.getAuthor() != null) {
            book.setAuthor(request.getAuthor());
        }

        if(request.getTitle() != null) {
            book.setTitle(request.getTitle());
        }

        if(request.getIsbn() != null) {
            book.setIsbn(request.getIsbn());
        }

        if(request.getPublicationYear() != null) {
            book.setPublicationYear(request.getPublicationYear());
        }

        Book updatedBook = bookRepository.save(book);
        auditService.logCurrentUser("BOOK_UPDATED", "Updated book " + id);

        return new BookResponseDTO(
                updatedBook.getBookId(),
                updatedBook.getTitle(),
                updatedBook.getAuthor(),
                updatedBook.getPublicationYear(),
                updatedBook.getIsbn()
        );
    }

    @CacheEvict(cacheNames = {"books", "book-lists"}, allEntries = true)
    public BookResponseDTO replaceBook(Long id, BookRequestDTO request) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() ->
                        new BookNotFoundException("Book not found " + id)
                );

        if (bookRepository.existsByIsbnAndBookIdNot(request.getIsbn(), id)) {
            throw new DuplicateIsbnException(request.getIsbn());
        }

        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setPublicationYear(request.getPublicationYear());
        book.setIsbn(request.getIsbn());

        Book updatedBook = bookRepository.save(book);
        auditService.logCurrentUser("BOOK_REPLACED", "Replaced book " + id);

        return new BookResponseDTO(
                updatedBook.getBookId(),
                updatedBook.getTitle(),
                updatedBook.getAuthor(),
                updatedBook.getPublicationYear(),
                updatedBook.getIsbn()
        );
    };
}
