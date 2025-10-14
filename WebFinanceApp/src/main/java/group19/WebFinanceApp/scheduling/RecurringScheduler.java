package group19.WebFinanceApp.scheduling;

import group19.WebFinanceApp.model.Transaction;
import group19.WebFinanceApp.service.RecurringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RecurringScheduler {

    private static final Logger log = LoggerFactory.getLogger(RecurringScheduler.class);

    private final RecurringService recurringService;

    public RecurringScheduler(RecurringService recurringService) {
        this.recurringService = recurringService;
    }

    /**
     * Jednom dnevno (00:05) pokreće generisanje svih dospelih ponavljajućih transakcija.
     * Napomena: RecurringService koristi LocalDate.today(), što je sasvim ok za dnevni ciklus.
     */
    @Scheduled(cron = "0 5 0 * * *")
    public void runDaily() {
        List<Transaction> created = recurringService.runDue();
        if (!created.isEmpty()) {
            log.info("RecurringScheduler: generisano {} transakcija (daily run).", created.size());
        }
    }

    /**
     * Opcioni “catch-up” na podizanju aplikacije – praktično odmah pozove runDue()
     * da se ne čeka sledeći dnevni termin.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        List<Transaction> created = recurringService.runDue();
        if (!created.isEmpty()) {
            log.info("Generisano {} transakcija (on startup).", created.size());
        }
    }
}