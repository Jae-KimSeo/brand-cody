package org.service.brandcody.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@TestConfiguration
@EnableJpaRepositories(basePackages = "org.service.brandcody.repository")
@EntityScan(basePackages = "org.service.brandcody.domain")
@EnableTransactionManagement
public class TestConfig {
}
