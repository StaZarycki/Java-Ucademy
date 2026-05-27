package com.example.ucademy.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(25) DEFAULT 'USER'")
    private Role role = Role.USER;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CourseProgress> courseProgresses = new HashSet<>();

    public void enrollInCourse(Course course) {
        CourseProgress courseProgress = new CourseProgress();
        courseProgress.setUser(this);
        courseProgress.setCourse(course);

        this.courseProgresses.add(courseProgress);
        course.getCourseProgresses().add(courseProgress);
    }

    public void removeCourse(Course course) {
        CourseProgress toRemove = this.courseProgresses.stream()
                .filter(p -> p.getCourse().equals(course))
                .findFirst()
                .orElse(null);

        if (toRemove != null) {
            this.courseProgresses.remove(toRemove);
            course.getCourseProgresses().remove(toRemove);
            toRemove.setUser(null);
            toRemove.setCourse(null);
        }
    }
}
