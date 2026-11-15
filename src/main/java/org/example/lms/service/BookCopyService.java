package org.example.lms.service;

import org.example.lms.dto.BookCopyRequestDTO;
import org.example.lms.dto.BookCopyResponseDTO;
import org.example.lms.dto.BookCopyStatusUpdateDTO;
import org.example.lms.entity.Book;
import org.example.lms.entity.BookCopy;
import org.example.lms.entity.BookCopyStatus;
import org.example.lms.repository.BookCopyRepository;
import org.example.lms.repository.BookRepository;
import org.example.lms.repository.BorrowingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class BookCopyService {
    private final BookCopyRepository copyRepository;
    private final BookRepository bookRepository;
    private final AuditService auditService;
    private final BorrowingRepository borrowingRepository;

    public BookCopyService(BookCopyRepository copyRepository,
                           BookRepository bookRepository,
                           AuditService auditService,
                           BorrowingRepository borrowingRepository) {
        this.copyRepository = copyRepository;
        this.bookRepository = bookRepository;
        this.auditService = auditService;
        this.borrowingRepository = borrowingRepository;
    }

    @Transactional
    public BookCopyResponseDTO addCopy(Long bookId, BookCopyRequestDTO request,
                                       String performedBy) {
        Book book = getBook(bookId);
        if (copyRepository.existsByBook_BookIdAndCopyNumber(bookId, request.copyNumber())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "That copy number already exists for this book");
        }
        BookCopy copy = new BookCopy();
        copy.setBook(book);
        copy.setCopyNumber(request.copyNumber());
        copy.setStatus(BookCopyStatus.AVAILABLE);
        BookCopy saved = copyRepository.save(copy);
        auditService.log(performedBy, "BOOK_COPY_CREATED",
                "Created copy " + saved.getId() + " for book " + bookId);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<BookCopyResponseDTO> getCopies(Long bookId) {
        getBook(bookId);
        return copyRepository.findByBook_BookIdOrderByCopyNumberAsc(bookId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public BookCopyResponseDTO updateStatus(Long bookId, Long copyId,
                                             BookCopyStatusUpdateDTO request,
                                             String performedBy) {
        BookCopy copy = getCopy(copyId);
        if (!copy.getBook().getBookId().equals(bookId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Book copy not found for this book");
        }
        BookCopyStatus requested = request.status();
        if (requested == BookCopyStatus.BORROWED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A copy can only become BORROWED through the borrowing process");
        }
        if (copy.getStatus() == BookCopyStatus.BORROWED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A borrowed copy cannot be manually reclassified");
        }
        BookCopyStatus previous = copy.getStatus();
        copy.setStatus(requested);
        auditService.log(performedBy, "BOOK_COPY_STATUS_UPDATED",
                "Copy " + copyId + ": " + previous + " -> " + requested);
        return toResponse(copy);
    }

    @Transactional
    public void deleteCopy(Long bookId, Long copyId, String performedBy) {
        BookCopy copy = getCopy(copyId);
        if (!copy.getBook().getBookId().equals(bookId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Book copy not found for this book");
        }
        if (copy.getStatus() == BookCopyStatus.BORROWED ||
                borrowingRepository.existsByBookCopy_Id(copyId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A copy with a current or historical borrowing record cannot be deleted");
        }
        copyRepository.delete(copy);
        auditService.log(performedBy, "BOOK_COPY_DELETED",
                "Deleted copy " + copyId + " from book " + bookId);
    }

    private Book getBook(Long id) {
        return bookRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));
    }

    private BookCopy getCopy(Long id) {
        return copyRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Book copy not found"));
    }

    private BookCopyResponseDTO toResponse(BookCopy copy) {
        return new BookCopyResponseDTO(copy.getId(), copy.getBook().getBookId(),
                copy.getBook().getTitle(), copy.getCopyNumber(), copy.getStatus());
    }
}
