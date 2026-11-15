package org.example.lms.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "fines",
        uniqueConstraints = @UniqueConstraint(columnNames = "borrowing_id")
)
public class Fine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "borrowing_id", nullable = false, unique = true)
    private Borrowing borrowing;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FineStatus status = FineStatus.UNPAID;

    private LocalDateTime createdAt;
    private LocalDateTime paidAt;

    public Fine() {}

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }

    public Borrowing getBorrowing() { return borrowing; }
    public void setBorrowing(Borrowing borrowing) {
        this.borrowing = borrowing;
    }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public FineStatus getStatus() { return status; }
    public void setStatus(FineStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }
}
