package org.example.lms.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.validation.Valid;
import org.example.lms.dto.BookRequestDTO;
import org.example.lms.dto.BookResponseDTO;
import org.example.lms.dto.BookUpdateDTO;
import org.example.lms.service.BookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Books", description = "Create, retrieve, update and delete catalogue records.")
@RestController
@RequestMapping("/book")
public class BookController {

    private BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @Operation(summary = "Create a book", description = "Creates a catalogue record after ISBN duplicate validation.")
    @PostMapping
    public BookResponseDTO createBook(@Valid @RequestBody BookRequestDTO request) {
        return bookService.createBook(request);
    }

    @Operation(summary = "List books", description = "Returns the catalogue book list.")
    @GetMapping
    public List<BookResponseDTO> getAllBooks() {
        return bookService.getAllBook();
    }

    @Operation(summary = "Get a book", description = "Returns one book by its identifier.")
    @GetMapping("/{id}")
    public BookResponseDTO getBookById(@PathVariable Long id) {
        return bookService.getBookById(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public BookResponseDTO updateBook(
            @PathVariable Long id,
            @Valid @RequestBody BookUpdateDTO request){
        return bookService.updateBook(id, request);
    }

    @PutMapping("/{id}")
    public BookResponseDTO replaceBook(
            @PathVariable Long id,
            @Valid @RequestBody BookRequestDTO request
    ) {
        return bookService.replaceBook(id, request);
    }
}
