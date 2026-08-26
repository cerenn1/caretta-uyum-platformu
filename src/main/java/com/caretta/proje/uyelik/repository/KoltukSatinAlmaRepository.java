package com.caretta.proje.uyelik.repository;

import com.caretta.proje.uyelik.entity.KoltukSatinAlma;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KoltukSatinAlmaRepository extends JpaRepository<KoltukSatinAlma, Long> {

    Optional<KoltukSatinAlma> findByStripeCheckoutSessionId(String stripeCheckoutSessionId);
}
