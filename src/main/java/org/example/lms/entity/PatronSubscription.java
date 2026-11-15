
package org.example.lms.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "patron_subscriptions")
public class PatronSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "patron_id", nullable = false)
    private AppUser patron;

    @ManyToOne(optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private MembershipPlan plan;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MembershipStatus status = MembershipStatus.ACTIVE;

    public PatronSubscription() {}

    @PrePersist
    public void onCreate() {
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        if (endDate == null && plan != null) {
            endDate = startDate.plusDays(plan.getDurationDays());
        }
    }

    public Long getId() { return id; }
    public AppUser getPatron() { return patron; }
    public void setPatron(AppUser patron) { this.patron = patron; }

    public MembershipPlan getPlan() { return plan; }
    public void setPlan(MembershipPlan plan) { this.plan = plan; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public MembershipStatus getStatus() { return status; }
    public void setStatus(MembershipStatus status) {
        this.status = status;
    }
}
