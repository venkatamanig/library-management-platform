
package org.example.lms.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "library_policies")
public class LibraryPolicy {

    @Id
    private Long id = 1L;

    @Column(nullable = false)
    private Integer gracePeriodDays = 0;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal finePerDay = new BigDecimal("5.00");

    @Column(precision = 10, scale = 2)
    private BigDecimal maximumFine = new BigDecimal("500.00");

    @Column(nullable = false)
    private boolean blockRenewalWithUnpaidFines = false;

    public LibraryPolicy() {}

    public Long getId() { return id; }

    public Integer getGracePeriodDays() { return gracePeriodDays; }
    public void setGracePeriodDays(Integer gracePeriodDays) {
        this.gracePeriodDays = gracePeriodDays;
    }

    public BigDecimal getFinePerDay() { return finePerDay; }
    public void setFinePerDay(BigDecimal finePerDay) {
        this.finePerDay = finePerDay;
    }

    public BigDecimal getMaximumFine() { return maximumFine; }
    public void setMaximumFine(BigDecimal maximumFine) {
        this.maximumFine = maximumFine;
    }

    public boolean isBlockRenewalWithUnpaidFines() {
        return blockRenewalWithUnpaidFines;
    }

    public void setBlockRenewalWithUnpaidFines(
            boolean blockRenewalWithUnpaidFines) {
        this.blockRenewalWithUnpaidFines =
                blockRenewalWithUnpaidFines;
    }
}
