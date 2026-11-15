
package org.example.lms.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "book_copies",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"book_id", "copy_number"}
        )
)
public class BookCopy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer copyNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookCopyStatus status = BookCopyStatus.AVAILABLE;

    public BookCopy() {}

    public Long getId() {
        return id;
    }

    public Integer getCopyNumber() {
        return copyNumber;
    }

    public void setCopyNumber(Integer copyNumber) {
        this.copyNumber = copyNumber;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public BookCopyStatus getStatus() {
        return status;
    }

    public void setStatus(BookCopyStatus status) {
        this.status = status;
    }
}
