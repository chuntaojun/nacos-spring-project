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

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since
 */
public class AnnotationUtils {

    public static Map<String, Object>[] resolveRuntimeAttributesArray(AnnotatedBeanDefinition beanDefinition) {
        return resolveRuntimeAttributesArray(beanDefinition, null);
    }

    public static Map<String, Object>[] resolveRuntimeAttributesArray(AnnotatedBeanDefinition beanDefinition, Compute compute) {
        // Get AnnotationMetadata
        AnnotationMetadata metadata = beanDefinition.getMetadata();

        Set<String> annotationTypes = metadata.getAnnotationTypes();

        List<Map<String, Object>> annotationAttributesList = new LinkedList<Map<String, Object>>();

        for (String annotationType : annotationTypes) {
            annotationAttributesList.addAll(getAnnotationAttributesList(metadata, annotationType, compute));
        }

        return annotationAttributesList.toArray(new Map[0]);
    }

    private static List<Map<String, Object>> getAnnotationAttributesList(AnnotationMetadata metadata, String annotationType, Compute compute) {
        Map<String, Object>[] maps = compute.apply(metadata, annotationType);
        if (maps != null && maps.length != 0) {
            return new LinkedList<Map<String, Object>>(Arrays.asList(maps));
        }
        return Collections.emptyList();
    }

}
