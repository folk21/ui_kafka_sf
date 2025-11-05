package com.example.ui_kafka_sf.sf;

import com.example.ui_kafka_sf.auth.AppProperties;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;

/** Accepts SF-style submissions and publishes first-time ones to Kafka. */
@RestController
@RequestMapping("/api/sf")
public class SfController {
  private final KafkaTemplate<String, Object> kafka;
  private final AppProperties props;
  private final SfIdempotencyRepository idemRepo;

  public SfController(
      KafkaTemplate<String, Object> kafka, AppProperties props, SfIdempotencyRepository idemRepo) {
    this.kafka = kafka;
    this.props = props;
    this.idemRepo = idemRepo;
  }

  /** Returns {"status":"queued"} or {"status":"duplicate_ignored"} */
  @PostMapping("/submit")
  public ResponseEntity<?> submit(@Valid @RequestBody SfEvent event) {
    var hash =
        sha256(
            event.email()
                + "|"
                + nullToEmpty(event.fullName())
                + "|"
                + nullToEmpty(event.message()));
    boolean firstTime = idemRepo.tryReserveFirstSend(event.email(), hash);
    if (!firstTime) return ResponseEntity.ok(Map.of("status", "duplicate_ignored"));

    kafka.send(props.getKafka().getTopic(), event.email(), event);
    return ResponseEntity.ok(Map.of("status", "queued"));
  }

  // normalize null to empty for stable hashing
  private static String nullToEmpty(String s) {
    return s == null ? "" : s;
  }

  // sha-256 helper
  private static String sha256(String s) {
    try {
      var md = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(md.digest(s.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
