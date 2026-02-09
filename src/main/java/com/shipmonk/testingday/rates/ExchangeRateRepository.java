package com.shipmonk.testingday.rates;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public class ExchangeRateRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public void persist(DailyExchangeRate rate) {
        entityManager.persist(rate);
    }

    @Transactional(readOnly = true)
    public Optional<DailyExchangeRate> findByDate(LocalDate date) {
        TypedQuery<DailyExchangeRate> query = entityManager.createQuery(
            "SELECT r FROM DailyExchangeRate r LEFT JOIN FETCH r.rates WHERE r.date = :date",
            DailyExchangeRate.class
        );
        query.setParameter("date", date);

        return query.getResultStream().findFirst();
    }

    @Transactional(readOnly = true)
    public boolean existsByDate(LocalDate date) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT count(r) FROM DailyExchangeRate r WHERE r.date = :date",
            Long.class
        );
        query.setParameter("date", date);
        return query.getSingleResult() > 0;
    }
}
