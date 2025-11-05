package com.example.ui_kafka_sf.course;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** CourseController provides CRUD endpoints for courses and an instructor-scoped listing. */
@RestController
@RequestMapping("/api/course")
@RequiredArgsConstructor
public class CourseController {

  private final CourseRepository repo;

  private static CourseDto toDto(Course c) {
    return new CourseDto(
        c.getId(),
        c.getCode(),
        c.getName(),
        c.getDescription(),
        c.getTerm(),
        c.getYear(),
        c.getExpired(),
        c.isStarted(),
        c.getInstructorId());
  }

  private static void apply(CourseDto dto, Course c) {
    c.setCode(dto.code());
    c.setName(dto.name());
    c.setDescription(dto.description());
    c.setTerm(dto.term());
    c.setYear(dto.year());
    c.setExpired(dto.expired());
    c.setStarted(dto.started());
    c.setInstructorId(dto.instructorId());
  }

  /** Performs a unit of domain logic; see README for the surrounding flow. */
  @GetMapping
  public List<CourseDto> all() {
    return repo.findAll().stream().map(CourseController::toDto).toList();
  }

  /** Performs a unit of domain logic; see README for the surrounding flow. */
  @GetMapping("/{id}")
  public ResponseEntity<CourseDto> get(@PathVariable String id) {
    return repo.findById(id)
        .map(c -> ResponseEntity.ok(toDto(c)))
        .orElse(ResponseEntity.notFound().build());
  }

  /** Performs a unit of domain logic; see README for the surrounding flow. */
  @GetMapping("/by_instructor/{instructorId}")
  public List<CourseDto> byInstructor(@PathVariable String instructorId) {
    return repo.findByInstructorId(instructorId).stream().map(CourseController::toDto).toList();
  }

  /** Performs a unit of domain logic; see README for the surrounding flow. */
  @PostMapping
  public ResponseEntity<CourseDto> create(@RequestBody CourseDto dto) {
    var c = new Course();
    c.setId((dto.id() == null || dto.id().isBlank()) ? UUID.randomUUID().toString() : dto.id());
    apply(dto, c);
    var saved = repo.save(c);
    return ResponseEntity.created(URI.create("/api/course/" + saved.getId())).body(toDto(saved));
  }

  /** Performs a unit of domain logic; see README for the surrounding flow. */
  @PutMapping("/{id}")
  public ResponseEntity<CourseDto> update(@PathVariable String id, @RequestBody CourseDto dto) {
    Optional<Course> existing = repo.findById(id);
    if (existing.isEmpty()) return ResponseEntity.notFound().build();
    var c = existing.get();
    apply(dto, c);
    var saved = repo.save(c);
    return ResponseEntity.ok(toDto(saved));
  }

  /** Performs a unit of domain logic; see README for the surrounding flow. */
  @DeleteMapping("/{id}")
  public ResponseEntity<?> delete(@PathVariable String id) {
    if (!repo.existsById(id)) return ResponseEntity.notFound().build();
    repo.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
