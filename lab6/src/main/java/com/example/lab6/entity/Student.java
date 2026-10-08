package com.example.lab6.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "students")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Positive
    private Integer id;

    @NotBlank
    @Size(max = 15)
    @Column(nullable = false, length = 15)
    private String name;

    @NotBlank
    @Size(max = 25)
    @Column(nullable = false, length = 25)
    private String surname;

    @NotBlank
    @Size(max = 20)
    @Column(nullable = false, length = 20)
    private String faculty;

    @NotNull
    @Min(1)
    @Max(120)
    @Column(nullable = false)
    private Integer age;
}
