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

import com.alibaba.nacos.api.annotation.NacosProperties;
import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.config.ConfigType;
import com.alibaba.nacos.spring.context.annotation.EnableNacos;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.alibaba.nacos.api.common.Constants.DEFAULT_GROUP;
import static com.alibaba.nacos.spring.util.NacosUtils.DEFAULT_BOOLEAN_ATTRIBUTE_VALUE;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MultiNacosPropertySource {


    /**
     * The attribute name of {@link NacosPropertySource#dataId()}
     */
    String DATA_IDS_ATTRIBUTE_NAME = "dataIds";

    /**
     * The attribute name of {@link NacosPropertySource#groupId()}
     */
    String GROUP_ID_ATTRIBUTE_NAME = "groupId";

    /**
     * The attribute name of {@link NacosPropertySource#autoRefreshed()}
     */
    String AUTO_REFRESHED_ATTRIBUTE_NAME = "autoRefreshed";

    /**
     * The attribute name of {@link NacosPropertySource#properties()}
     */
    String PROPERTIES_ATTRIBUTE_NAME = "properties";

    /**
     * The attribute name of {@link NacosPropertySource#type()} ()}
     */
    String CONFIG_TYPE_ATTRIBUTE_NAME = "type";

    /**
     * Nacos Data ID
     *
     * @return required value.
     */
    String[] dataIds();

    /**
     * Nacos Group ID
     *
     * @return default value {@link Constants#DEFAULT_GROUP};
     */
    String groupId() default DEFAULT_GROUP;

    /**
     * It indicates the property source is auto-refreshed when Nacos configuration is changed.
     *
     * @return default value is <code>false</code>
     */
    boolean autoRefreshed() default DEFAULT_BOOLEAN_ATTRIBUTE_VALUE;

    /**
     * The type of config,
     * The default configuration information for all data - id
     * If data - id carry the configuration file information is
     * given priority to with data - id configuration information
     * to carry
     *
     * @return the type of config
     */
    ConfigType type() default ConfigType.PROPERTIES;

    /**
     * The {@link NacosProperties} attribute, If not specified, it will use
     * {@link EnableNacos#globalProperties() global Nacos Properties}.
     *
     * @return the default value is {@link NacosProperties}
     * @see EnableNacos#globalProperties()
     */
    NacosProperties properties() default @NacosProperties;

}
