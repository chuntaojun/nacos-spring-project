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
package com.alibaba.nacos.spring.context.annotation.config;

import com.alibaba.nacos.spring.factory.NacosServiceFactory;
import com.alibaba.nacos.spring.util.NacosUtils;
import com.alibaba.nacos.spring.util.config.NacosConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.util.StringUtils;

import java.util.Properties;

import static com.alibaba.nacos.spring.util.NacosBeanUtils.getNacosServiceFactoryBean;
import static com.alibaba.nacos.spring.util.NacosUtils.buildDefaultPropertySourceName;
import static com.alibaba.nacos.spring.util.NacosUtils.toProperties;
import static java.lang.String.format;
import static org.springframework.util.ClassUtils.resolveClassName;

/**
 * Nacos {@link PropertySource} Builder
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see NacosPropertySource
 * @since 0.1.0
 */
public class NacosPropertySourceBuilder {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String name;

    private String dataId;

    private String groupId;

    private String type;

    private String beanName;

    private String beanClassName;

    private boolean autoRefresh;

    private Properties nacosProperties;

    private ConfigurableEnvironment environment;

    private BeanFactory beanFactory;

    private NacosConfigLoader nacosConfigLoader;

    private ClassLoader classLoader;

    private NacosPropertySourceBuilder() {
    }

    public static NacosPropertySourceBuilder newInstance() {
        return new NacosPropertySourceBuilder();
    }

    public NacosPropertySourceBuilder name(String name) {
        this.name = name;
        return this;
    }

    public NacosPropertySourceBuilder dataId(String dataId) {
        this.dataId = dataId;
        return this;
    }

    public NacosPropertySourceBuilder groupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public NacosPropertySourceBuilder properties(Properties properties) {
        this.nacosProperties = properties;
        return this;
    }

    public NacosPropertySourceBuilder type(String type) {
        this.type = type;
        return this;
    }

    public NacosPropertySourceBuilder beanName(String beanName) {
        this.beanName = beanName;
        return this;
    }

    public NacosPropertySourceBuilder beanClassName(String beanClassName) {
        this.beanClassName = beanClassName;
        return this;
    }

    public NacosPropertySourceBuilder classLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        return this;
    }

    public NacosPropertySourceBuilder autoRefresh(boolean autoRefresh) {
        this.autoRefresh = autoRefresh;
        return this;
    }

    public NacosPropertySourceBuilder environment(ConfigurableEnvironment environment) {
        this.environment = environment;
        return this;
    }

    public NacosPropertySourceBuilder beanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
        return this;
    }

    /**
     * Build Nacos {@link PropertySource}
     *
     * @return if Nacos config is absent , return <code>null</code>
     */
    public com.alibaba.nacos.spring.core.env.NacosPropertySource build() {

        nacosConfigLoader = new NacosConfigLoader(environment);

        NacosServiceFactory nacosServiceFactory = getNacosServiceFactoryBean(beanFactory);

        nacosConfigLoader.setNacosServiceFactory(nacosServiceFactory);

        dataId = NacosUtils.readFromEnvironment(dataId, environment);
        groupId = NacosUtils.readFromEnvironment(groupId, environment);
        type = StringUtils.isEmpty(NacosUtils.readTypeFromDataId(dataId)) ? type : NacosUtils.readTypeFromDataId(dataId);

        String config = nacosConfigLoader.load(dataId, groupId, nacosProperties);

        if (!StringUtils.hasText(config)) {
            if (logger.isWarnEnabled()) {
                logger.warn(format("There is no content for Nacos PropertySource from dataId[%s] , groupId[%s] , properties[%s].",
                        dataId,
                        groupId,
                        nacosProperties));
            }
            return null;
        }

        if (!StringUtils.hasText(name)) {
            name = buildDefaultPropertySourceName(dataId, groupId, nacosProperties);
        }

        com.alibaba.nacos.spring.core.env.NacosPropertySource propertySource =
                new com.alibaba.nacos.spring.core.env.NacosPropertySource(dataId, groupId, name, config, type);
        propertySource.setAutoRefreshed(autoRefresh);
        propertySource.setBeanName(beanName);

        if (StringUtils.hasText(beanClassName)) {
            propertySource.setBeanType(resolveClassName(beanClassName, classLoader));
        }

        return propertySource;
    }

    public NacosConfigLoader getNacosConfigLoader() {
        return nacosConfigLoader;
    }

}
