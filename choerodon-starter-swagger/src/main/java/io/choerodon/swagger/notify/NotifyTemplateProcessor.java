package io.choerodon.swagger.notify;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class NotifyTemplateProcessor implements BeanPostProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotifyTemplateProcessor.class);

    private final Set<NotifyTemplateScanData> scanDataSet = new HashSet<>(1 << 3);

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof NotifyTemplate) {
            NotifyTemplate template = (NotifyTemplate) bean;
            NotifyTemplateScanData scanData = new NotifyTemplateScanData(template.businessTypeCode(),
                    template.code(), template.name(), template.title(), template.content(),template.type());
            if (validNotifyTemplate(scanData)) {
                scanDataSet.add(scanData);
            }
        }
        return bean;
    }

    public Set<NotifyTemplateScanData> getScanDataSet() {
        return scanDataSet;
    }

    private boolean validNotifyTemplate(final NotifyTemplateScanData template) {
        if (StringUtils.isEmpty(template.getBusinessType())) {
            LOGGER.error("error.notifyTemplate.businessTypeCodeEmpty {}", template);
            return false;
        }
        if (StringUtils.isEmpty(template.getCode())) {
            LOGGER.error("error.notifyTemplate.codeEmpty {}", template);
            return false;
        }
        if (StringUtils.isEmpty(template.getName())) {
            LOGGER.error("error.notifyTemplate.nameEmpty {}", template);
            return false;
        }
        if (StringUtils.isEmpty(template.getTitle())) {
            LOGGER.error("error.notifyTemplate.titleEmpty {}", template);
            return false;
        }
        if (StringUtils.isEmpty(template.getContent())) {
            LOGGER.error("error.notifyTemplate.contentEmpty {}", template);
            return false;
        }
        template.setContent(readTemplate(template.getContent()));
        return true;
    }

    private String readTemplate(final String contentPath) {
        String trimContentPath = contentPath.trim();
        if (!trimContentPath.startsWith("classpath://")) {
            return trimContentPath;
        }
        StringBuilder sb = new StringBuilder();
        trimContentPath = trimContentPath.substring(12, trimContentPath.length());
        ClassPathResource templateResource = new ClassPathResource(trimContentPath);
        try (InputStreamReader reader = new InputStreamReader(templateResource.getInputStream());
             BufferedReader bufferedReader = new BufferedReader(reader)) {
            String s;
            while ((s = bufferedReader.readLine()) != null) {
                sb.append(s).append("\n");
            }
        } catch (IOException e) {
            LOGGER.warn("error.NotifyTemplateProcessor.readTemplate.IOException {}", e);
            return trimContentPath;
        }
        return sb.toString();
    }
}
