package com.example.ui_kafka_sf.course;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, String> {
  List<Course> findByInstructorId(String instructorId);
}
