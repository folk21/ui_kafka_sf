package com.example.ui_kafka_sf.sf;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository for storing idempotency keys of SF-style submissions.
 *
 * <p>What this improves:
 * <ul>
 *   <li><b>Cross-instance idempotency:</b> persistence replaces in-memory dedupe, so duplicates
 *       are suppressed across app restarts and multiple nodes.</li>
 *   <li><b>Race-safe "insert-only" reservation:</b> we avoid the classic
 *       "check-then-insert" race by trying a single INSERT and interpreting a unique constraint
 *       violation as "not first" — no explicit locks.</li>
 *   <li><b>At-most-once publish trigger:</b> upstream controllers/services can gate Kafka publishing
 *       on this reservation; only the first submit wins.</li>
 *   <li><b>Operational transparency:</b> the boolean return value is an explicit signal for
 *       the caller to choose a response (e.g., 200 queued vs. duplicate_ignored).</li>
 * </ul>
 *
 * <p>Schema expectations:
 * <ul>
 *   <li>{@code SfIdempotencyEntity.keyHash} (or the PK) must be <b>unique</b> to ensure
 *       constraint violations on duplicates.</li>
 *   <li>The caller is responsible for computing a <b>stable</b> hash from normalized fields
 *       (e.g., {@code email|fullName|message}).</li>
 *   <li>(Optional) add a retention job to purge old rows to cap storage.</li>
 * </ul>
 */
public interface SfIdempotencyRepository extends JpaRepository<SfIdempotencyEntity, String> {

  /**
   * Attempts to reserve a "first send" for the given logical submission key.
   *
   * <p>How it works:
   * <ul>
   *   <li>Tries to INSERT a new idempotency row ({@code keyHash}, {@code email}).</li>
   *   <li>If INSERT succeeds ⇒ this is the <b>first</b> occurrence ⇒ return {@code true}.</li>
   *   <li>If database raises {@link DataIntegrityViolationException} (unique hit) ⇒
   *       a prior reservation exists ⇒ return {@code false}.</li>
   * </ul>
   *
   * <p>Why this is better:
   * <ul>
   *   <li>No "check-then-insert" race, no explicit locks, scales horizontally.</li>
   *   <li>Caller logic stays simple: <code>if (tryReserveFirstSend) publish(); else ignore()</code>.</li>
   * </ul>
   *
   * @param email   optional context for observability/debug (who sent)
   * @param keyHash stable, collision-resistant hash that identifies the logical submission
   * @return {@code true} if the reservation is new (first send), {@code false} if duplicate
   */
  @Transactional
  default boolean tryReserveFirstSend(String email, String keyHash) {
    try {
      var e = SfIdempotencyEntity.builder().keyHash(keyHash).email(email).build();
      save(e);
      return true;
    } catch (DataIntegrityViolationException duplicate) {
      return false;
    }
  }
}
