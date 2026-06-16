package com.pentastack.skillsync.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(
    basePackages = {
        "com.pentastack.skillsync.domain.repository",
        "com.pentastack.skillsync.model.repository"
    },
    excludeFilters = {
        // This config allows both but we'll use bean naming to distinguish
    }
)
public class RepositoryConfiguration {
}
