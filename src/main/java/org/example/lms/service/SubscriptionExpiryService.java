
package org.example.lms.service;

import org.example.lms.entity.MembershipStatus;
import org.example.lms.entity.PatronSubscription;
import org.example.lms.repository.PatronSubscriptionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@ConditionalOnProperty(name = "lms.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class SubscriptionExpiryService {

    private final PatronSubscriptionRepository subscriptionRepository;

    public SubscriptionExpiryService(
            PatronSubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Scheduled(cron = "0 0 1 * * *", zone = "Asia/Kolkata")
    @Transactional
    public void expireSubscriptions() {

        LocalDate today = LocalDate.now(
                ZoneId.of("Asia/Kolkata"));

        List<PatronSubscription> expiredSubscriptions =
                subscriptionRepository.findByStatusAndEndDateBefore(
                        MembershipStatus.ACTIVE, today);

        for (PatronSubscription subscription : expiredSubscriptions) {
            subscription.setStatus(MembershipStatus.EXPIRED);
        }

        System.out.println(
                "Expired subscriptions updated: "
                        + expiredSubscriptions.size());
    }
}
