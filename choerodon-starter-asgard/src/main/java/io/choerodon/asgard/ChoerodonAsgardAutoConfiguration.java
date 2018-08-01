package io.choerodon.asgard;

import io.choerodon.asgard.saga.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableConfigurationProperties(ChoerodonSagaProperties.class)
public class ChoerodonAsgardAutoConfiguration {

    @Autowired
    private ChoerodonSagaProperties choerodonSagaProperties;

    @Bean
    @ConditionalOnProperty(prefix = "choerodon.saga.consumer", name = "enabled", matchIfMissing = true)
    public SagaApplicationContextHelper sagaApplicationContextHelper() {
        return new SagaApplicationContextHelper();
    }

    @Bean
    public SagaClientCallback sagaClientCallback() {
        return new SagaClientCallback();
    }

    @Bean
    @ConditionalOnProperty(prefix = "choerodon.saga.consumer", name = "enabled", matchIfMissing = true)
    public SagaTaskProcessor sagaTaskProcessor() {
        return new SagaTaskProcessor(sagaApplicationContextHelper());
    }


    @Bean
    @ConditionalOnProperty(prefix = "choerodon.saga.consumer", name = "enabled", matchIfMissing = true)
    public Executor asyncServiceExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(choerodonSagaProperties.getThreadNum());
        executor.setMaxPoolSize(choerodonSagaProperties.getThreadNum());
        executor.setQueueCapacity(99999);
        executor.setThreadNamePrefix("saga-service-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Bean
    @ConditionalOnProperty(prefix = "choerodon.saga.consumer", name = "enabled", matchIfMissing = true)
    public SagaMonitor sagaMonitor(SagaClient sagaClient,
                                   DataSourceTransactionManager transactionManager,
                                   Environment environment) {
        return new SagaMonitor(choerodonSagaProperties, sagaClient, asyncServiceExecutor(), transactionManager, environment);
    }

}