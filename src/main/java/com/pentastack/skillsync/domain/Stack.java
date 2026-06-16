package com.pentastack.skillsync.domain;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
public class Stack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    protected Stack() {}

    public Stack(String name, String description) {
        this.name = name;
        this.description = description;
    }

}

