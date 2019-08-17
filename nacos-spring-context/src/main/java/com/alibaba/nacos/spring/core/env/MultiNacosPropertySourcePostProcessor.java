/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.nacos.spring.core.env;

import com.alibaba.nacos.api.config.ConfigType;
import com.alibaba.nacos.spring.beans.factory.annotation.ConfigServiceBeanBuilder;
import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySourceBuilder;
import com.alibaba.nacos.spring.util.NacosUtils;
import com.alibaba.nacos.spring.util.ObjectUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static com.alibaba.nacos.spring.context.annotation.config.MultiNacosPropertySource.DATA_IDS_ATTRIBUTE_NAME;
import static com.alibaba.nacos.spring.context.annotation.config.MultiNacosPropertySource.GROUP_ID_ATTRIBUTE_NAME;
import static com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource.AUTO_REFRESHED_ATTRIBUTE_NAME;
import static com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource.CONFIG_TYPE_ATTRIBUTE_NAME;
import static com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource.PROPERTIES_ATTRIBUTE_NAME;
import static com.alibaba.nacos.spring.util.GlobalNacosPropertiesSource.CONFIG;
import static com.alibaba.nacos.spring.util.NacosBeanUtils.getConfigServiceBeanBuilder;
import static com.alibaba.nacos.spring.util.NacosUtils.resolveProperties;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since
 */
public class MultiNacosPropertySourcePostProcessor implements BeanDefinitionRegistryPostProcessor, BeanFactoryPostProcessor,
        EnvironmentAware, Ordered {

    /**
     * The bean name of {@link NacosPropertySourcePostProcessor}
     */
    public static final String BEAN_NAME = "multiNacosPropertySourcePostProcessor";

    private static BeanFactory beanFactory;

    private final Set<String> processedBeanNames = new LinkedHashSet<String>();

    private ConfigurableEnvironment environment;

    private ConfigServiceBeanBuilder configServiceBeanBuilder;

    private NacosPropertySourcePostProcessor postProcessor;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        postProcessor = beanFactory.getBean(NacosPropertySourcePostProcessor.BEAN_NAME,
                NacosPropertySourcePostProcessor.class);
        MultiNacosPropertySourcePostProcessor.beanFactory = beanFactory;
        this.configServiceBeanBuilder = getConfigServiceBeanBuilder(beanFactory);

        String[] beanNames = beanFactory.getBeanDefinitionNames();

        for (String beanName : beanNames) {
            processPropertySource(beanName, beanFactory);
        }

    }

    private void processPropertySource(String beanName, ConfigurableListableBeanFactory beanFactory) {
        if (processedBeanNames.contains(beanName)) {
            return;
        }
        BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
        if (beanDefinition.getClass().isAssignableFrom(AnnotatedBeanDefinition.class)) {
            List<NacosPropertySource> propertySources = buildNacosPropertySources(beanName, (AnnotatedBeanDefinition) beanDefinition);
            // Add Orderly
            for (NacosPropertySource nacosPropertySource : propertySources) {
                postProcessor.addNacosPropertySource(nacosPropertySource);
                Properties properties = configServiceBeanBuilder.resolveProperties(nacosPropertySource.getAttributesMetadata());
                NacosPropertySourcePostProcessor.addListenerIfAutoRefreshed(nacosPropertySource, properties, environment);
            }
            processedBeanNames.add(beanName);
        }
    }

    @SuppressWarnings("all")
    private List<NacosPropertySource> buildNacosPropertySources(String beanName, AnnotatedBeanDefinition beanDefinition) {
        Map<String, Object>[] attributesArray = ObjectUtils.resolveRuntimeAttributesArray(beanDefinition);
        int size = attributesArray == null ? 0 : attributesArray.length;

        if (size == 0) {
            return Collections.emptyList();
        }
        List<NacosPropertySource> nacosPropertySources = new ArrayList<NacosPropertySource>(size);

        for (int i = 0; i < size; i ++) {
            Map<String, Object> attributes = attributesArray[i];
            if (!CollectionUtils.isEmpty(attributes)) {
                boolean autoRefreshed = Boolean.TRUE.equals(attributes.get(AUTO_REFRESHED_ATTRIBUTE_NAME));
                final String[] dataIds = (String[]) attributes.get(DATA_IDS_ATTRIBUTE_NAME);
                final String groupId = (String) attributes.get(GROUP_ID_ATTRIBUTE_NAME);
                final String type = ((ConfigType) attributes.get(CONFIG_TYPE_ATTRIBUTE_NAME)).getType();

                for (String element : dataIds) {
                    final String dataId = NacosUtils.readFromEnvironment(element, environment);
                    Map<String, Object> nacosPropertiesAttributes = (Map<String, Object>) attributes.get(PROPERTIES_ATTRIBUTE_NAME);
                    Properties nacosProperties = resolveProperties(nacosPropertiesAttributes, environment, CONFIG.getMergedGlobalProperties(beanFactory));
                    NacosPropertySource propertySource = new NacosPropertySourceBuilder()
                            .dataId(dataId)
                            .groupId(groupId)
                            .type(type)
                            .properties(nacosProperties)
                            .beanFactory(beanFactory)
                            .beanName(beanName)
                            .autoRefresh(autoRefreshed)
                            .build();
                    postProcessor.publishEvent(propertySource, beanDefinition);
                    nacosPropertySources.add(propertySource);
                }
            }
        }
        return nacosPropertySources;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = (ConfigurableEnvironment) environment;
    }

    /**
     * The order is closed to {@link ConfigurationClassPostProcessor#getOrder() HIGHEST_PRECEDENCE} almost.
     *
     * @return <code>Ordered.HIGHEST_PRECEDENCE + 1</code>
     * @see ConfigurationClassPostProcessor#getOrder()
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
