package com.shipmonk.testingday.rates.repository;

import com.shipmonk.testingday.rates.model.DailyExchangeRate;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public class ExchangeRateRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public void persist(DailyExchangeRate rate) {
        entityManager.persist(rate);
    }

    public void flush() {
        entityManager.flush();
    }

    public Optional<DailyExchangeRate> findByDate(LocalDate date, String source) {
        TypedQuery<DailyExchangeRate> query = entityManager.createQuery(
            "SELECT r FROM DailyExchangeRate r LEFT JOIN FETCH r.rates WHERE r.date = :date AND r.source = :source",
            DailyExchangeRate.class
        );
        query.setParameter("date", date);
        query.setParameter("source", source);
        query.setMaxResults(1);

        return query.getResultList().stream().findFirst();
    }

    public boolean existsByDate(LocalDate date, String source) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT count(r) FROM DailyExchangeRate r WHERE r.date = :date AND r.source = :source",
            Long.class
        );
        query.setParameter("date", date);
        query.setParameter("source", source);
        return query.getSingleResult() > 0;
    }
}
