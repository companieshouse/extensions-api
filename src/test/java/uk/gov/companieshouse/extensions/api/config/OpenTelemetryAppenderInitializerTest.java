package uk.gov.companieshouse.extensions.api.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.OpenTelemetry;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

@Tag("UnitTest")
class OpenTelemetryAppenderInitializerTest {

    @Test
    void shouldInstallAppenderGivenEnabledProperty() {
        getContextRunnerWithNoOpOpenTelemetry()
            .withPropertyValues("management.opentelemetry.enabled:true")
            .run(context ->
                assertThat(context).hasSingleBean(OpenTelemetryAppenderInitializer.class));
    }

    @Test
    void shouldNotContainInitialiserGivenDisabledProperty() {
        getContextRunnerWithNoOpOpenTelemetry()
            .withPropertyValues("management.opentelemetry.enabled:false")
            .run(context ->
                assertThat(context).doesNotHaveBean(OpenTelemetryAppenderInitializer.class));
    }

    private static ApplicationContextRunner getContextRunnerWithNoOpOpenTelemetry() {
        OpenTelemetry openTelemetry = OpenTelemetry.noop();
        OpenTelemetryAppenderInitializer openTelemetryAppenderInitializer = new OpenTelemetryAppenderInitializer(openTelemetry);
        return new ApplicationContextRunner()
            .withBean(
                OpenTelemetryAppenderInitializer.class,
                () -> openTelemetryAppenderInitializer);
    }

}
