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

import com.alibaba.nacos.api.config.annotation.NacosConfigListener;
import com.alibaba.nacos.api.config.annotation.NacosConfigurationProperties;
import com.alibaba.nacos.api.config.annotation.NacosInjected;
import com.alibaba.nacos.api.config.annotation.NacosProperties;
import com.alibaba.nacos.spring.context.annotation.NacosBeanDefinitionRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Annotation for enabling Nacos Config features.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see NacosBeanDefinitionRegistrar
 * @since 0.2.0
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(NacosConfigBeanDefinitionRegistrar.class)
public @interface EnableNacosConfig {

    /**
     * The prefix of property name of Nacos Config
     */
    String PREFIX = NacosProperties.PREFIX + "config";

    /**
     * Global {@link NacosProperties Nacos Properties}
     *
     * @return required
     * @see NacosInjected#properties()
     * @see NacosConfigListener#properties()
     * @see NacosConfigurationProperties#properties()
     */
    NacosProperties globalProperties() default @NacosProperties();
}