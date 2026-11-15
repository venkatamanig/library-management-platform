
package org.example.lms.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "membership_plans")
public class MembershipPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private Integer durationDays;

    @Column(nullable = false)
    private Integer loanPeriodDays;

    @Column(nullable = false)
    private Integer maxBooks;

    @Column(nullable = false)
    private Integer maxRenewals;

    @Column(nullable = false)
    private boolean active = true;

    public MembershipPlan() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getDurationDays() { return durationDays; }
    public void setDurationDays(Integer durationDays) {
        this.durationDays = durationDays;
    }

    public Integer getLoanPeriodDays() { return loanPeriodDays; }
    public void setLoanPeriodDays(Integer loanPeriodDays) {
        this.loanPeriodDays = loanPeriodDays;
    }

    public Integer getMaxBooks() { return maxBooks; }
    public void setMaxBooks(Integer maxBooks) {
        this.maxBooks = maxBooks;
    }

    public Integer getMaxRenewals() { return maxRenewals; }
    public void setMaxRenewals(Integer maxRenewals) {
        this.maxRenewals = maxRenewals;
    }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
