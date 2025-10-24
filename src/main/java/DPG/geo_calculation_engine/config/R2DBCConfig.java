package DPG.geo_calculation_engine.config;

import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.r2dbc.mapping.R2dbcMappingContext;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.core.DatabaseClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration class for setting up multiple R2DBC connections (one primary for configuration,
 * one secondary for calculations) and related Spring Data R2DBC beans.
 * Enables R2DBC repositories found in the specified base package.
 */
@Configuration
@EnableR2dbcRepositories(basePackages = "DPG.geo_calculation_engine.repository")
public class R2DBCConfig {

    private static final Logger log = LoggerFactory.getLogger(R2DBCConfig.class);

    // URL for the primary/configuration database (e.g., calculator_engine)
    @Value("${spring.r2dbc.url}")
    private String configDbUrl;

    // URL for the secondary/calculations database (e.g., postgis_calculator)
    @Value("${spring.r2dbc.additional-datasources.calc.url}")
    private String calculationsDbUrl;

    /**
     * Creates the primary R2DBC ConnectionFactory bean, designated for the configuration database.
     * Marked as @Primary to be the default choice for autowiring ConnectionFactory.
     * Identified by the "configConnectionFactory" qualifier.
     * Reads the connection URL from the "spring.r2dbc.url" property.
     *
     * @return The primary ConnectionFactory bean.
     */
    @Bean
    @Primary
    @Qualifier("configConnectionFactory")
    public ConnectionFactory configConnectionFactory() {
        log.info("Creating primary configConnectionFactory from URL: {}", configDbUrl);
        return ConnectionFactories.get(configDbUrl);
    }

    /**
     * Creates the secondary R2DBC ConnectionFactory bean, designated for the calculations database.
     * Identified by the "calculationsConnectionFactory" qualifier.
     * Reads the connection URL from the "spring.r2dbc.additional-datasources.calc.url" property.
     *
     * @return The secondary ConnectionFactory bean.
     */
    @Bean
    @Qualifier("calculationsConnectionFactory")
    public ConnectionFactory calculationsConnectionFactory() {
        log.info("Creating calculationsConnectionFactory from URL: {}", calculationsDbUrl);
        return ConnectionFactories.get(calculationsDbUrl);
    }

    // --- R2DBC Mapping Context ---

    /**
     * Creates the R2dbcMappingContext bean.
     * This context is essential for Spring Data R2DBC to understand how to map
     * Java entities (@Table, @Column, @Id) to database tables and columns.
     * Uses the default naming strategy (usually camelCase to snake_case).
     * This single context can often be shared by multiple data sources if the mapping rules are the same.
     *
     * @return The R2dbcMappingContext bean.
     */
    @Bean
    public R2dbcMappingContext r2dbcMappingContext() {
        log.info("Creating R2dbcMappingContext bean (using default naming strategy)");
        return new R2dbcMappingContext();
    }

    // --- Database Clients ---

    /**
     * Creates the primary DatabaseClient bean, associated with the configuration database connection.
     * Marked as @Primary to be the default choice for autowiring DatabaseClient.
     * Identified by the "configDatabaseClient" qualifier.
     * Useful for direct database interactions using a fluent API.
     *
     * @param connectionFactory The primary ConnectionFactory ("configConnectionFactory").
     * @return The primary DatabaseClient bean.
     */
    @Bean
    @Primary
    @Qualifier("configDatabaseClient")
    public DatabaseClient configDatabaseClient(@Qualifier("configConnectionFactory") ConnectionFactory connectionFactory) {
        log.info("Creating configDatabaseClient");
        return DatabaseClient.create(connectionFactory);
    }

    /**
     * Creates the secondary DatabaseClient bean, associated with the calculations database connection.
     * Identified by the "calculationsDatabaseClient" qualifier.
     * Used specifically by services needing to interact with the calculation database.
     *
     * @param connectionFactory The secondary ConnectionFactory ("calculationsConnectionFactory").
     * @return The secondary DatabaseClient bean.
     */
    @Bean
    @Qualifier("calculationsDatabaseClient")
    public DatabaseClient calculationsDatabaseClient(@Qualifier("calculationsConnectionFactory") ConnectionFactory connectionFactory) {
        log.info("Creating calculationsDatabaseClient");
        return DatabaseClient.create(connectionFactory);
    }
}