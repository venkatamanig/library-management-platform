
package org.example.lms.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "borrowings")
public class Borrowing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "patron_id", nullable = false)
    private AppUser patron;

    @ManyToOne(optional = false)
    @JoinColumn(name = "copy_id", nullable = false)
    private BookCopy bookCopy;

    @Column(nullable = false)
    private LocalDateTime borrowedAt;

    @Column(nullable = false)
    private LocalDateTime dueAt;

    private LocalDateTime returnedAt;

    @Column(nullable = false)
    private Integer renewalCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BorrowingStatus status = BorrowingStatus.ACTIVE;

    @Column(nullable = false)
    private boolean dueReminderSent = false;

    @Column(nullable = false)
    private boolean overdueNotificationSent = false;

    public Borrowing() {}

    @PrePersist
    public void onCreate() {
        borrowedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }

    public AppUser getPatron() { return patron; }
    public void setPatron(AppUser patron) { this.patron = patron; }

    public BookCopy getBookCopy() { return bookCopy; }
    public void setBookCopy(BookCopy bookCopy) {
        this.bookCopy = bookCopy;
    }

    public LocalDateTime getBorrowedAt() { return borrowedAt; }

    public LocalDateTime getDueAt() { return dueAt; }
    public void setDueAt(LocalDateTime dueAt) { this.dueAt = dueAt; }

    public LocalDateTime getReturnedAt() { return returnedAt; }
    public void setReturnedAt(LocalDateTime returnedAt) {
        this.returnedAt = returnedAt;
    }

    public Integer getRenewalCount() { return renewalCount; }
    public void setRenewalCount(Integer renewalCount) {
        this.renewalCount = renewalCount;
    }

    public BorrowingStatus getStatus() { return status; }
    public void setStatus(BorrowingStatus status) {
        this.status = status;
    }

    public boolean isDueReminderSent() {
        return dueReminderSent;
    }

    public void setDueReminderSent(boolean dueReminderSent) {
        this.dueReminderSent = dueReminderSent;
    }

    public boolean isOverdueNotificationSent() {
        return overdueNotificationSent;
    }

    public void setOverdueNotificationSent(boolean overdueNotificationSent) {
        this.overdueNotificationSent = overdueNotificationSent;
    }
}
