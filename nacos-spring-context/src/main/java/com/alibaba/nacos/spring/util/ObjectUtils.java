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
package com.alibaba.nacos.spring.util;

import com.alibaba.nacos.api.config.annotation.NacosIgnore;
import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySources;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.3.0
 */
public abstract class ObjectUtils {

    public static void cleanMapOrCollectionField(final Object bean) {
        ReflectionUtils.doWithFields(bean.getClass(), new ReflectionUtils.FieldCallback() {

            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                field.setAccessible(true);
                if (field.isAnnotationPresent(NacosIgnore.class)) {
                    return;
                }
                Class<?> type = field.getType();
                if (type.isAssignableFrom(Map.class) || Collection.class.isAssignableFrom(type)) {
                    field.set(bean, null);
                }
            }
        });
    }

    public static Map<String, Object>[] resolveRuntimeAttributesArray(AnnotatedBeanDefinition beanDefinition) {
        // Get AnnotationMetadata
        AnnotationMetadata metadata = beanDefinition.getMetadata();

        Set<String> annotationTypes = metadata.getAnnotationTypes();

        List<Map<String, Object>> annotationAttributesList = new LinkedList<Map<String, Object>>();

        for (String annotationType : annotationTypes) {
            annotationAttributesList.addAll(getAnnotationAttributesList(metadata, annotationType));
        }

        return annotationAttributesList.toArray(new Map[0]);
    }

    private static List<Map<String, Object>> getAnnotationAttributesList(AnnotationMetadata metadata, String annotationType) {

        List<Map<String, Object>> annotationAttributesList = new LinkedList<Map<String, Object>>();

        if (NacosPropertySources.class.getName().equals(annotationType)) {
            Map<String, Object> annotationAttributes = metadata.getAnnotationAttributes(annotationType);
            if (annotationAttributes != null) {
                annotationAttributesList.addAll(Arrays.asList((Map<String, Object>[]) annotationAttributes.get("value")));
            }
        } else if (com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource.class.getName().equals(annotationType)) {
            annotationAttributesList.add(metadata.getAnnotationAttributes(annotationType));
        }
        return annotationAttributesList;
    }

}
