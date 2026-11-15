package org.example.lms.repository;

import org.example.lms.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    long count();

    boolean existsByIsbn(String isbn);

    boolean existsByIsbnAndBookIdNot(String isbn, Long bookId);

    Optional<Book> findByIsbn(String isbn);

    List<Book> findByAuthor(String Author);
}
