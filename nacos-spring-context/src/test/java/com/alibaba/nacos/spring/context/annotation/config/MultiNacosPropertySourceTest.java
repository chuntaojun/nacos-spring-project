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

import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.annotation.NacosProperties;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.ConfigType;
import com.alibaba.nacos.api.config.annotation.NacosConfigListener;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.embedded.web.server.EmbeddedNacosHttpServer;
import com.alibaba.nacos.spring.beans.factory.annotation.AnnotationNacosInjectedBeanPostProcessor;
import com.alibaba.nacos.spring.beans.factory.annotation.ConfigServiceBeanBuilder;
import com.alibaba.nacos.spring.context.annotation.EnableNacos;
import com.alibaba.nacos.spring.factory.ApplicationContextHolder;
import com.alibaba.nacos.spring.test.AbstractNacosHttpServerTestExecutionListener;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import java.util.HashMap;
import java.util.Map;

import static com.alibaba.nacos.api.common.Constants.DEFAULT_GROUP;
import static com.alibaba.nacos.embedded.web.server.NacosConfigHttpHandler.CONTENT_PARAM_NAME;
import static com.alibaba.nacos.embedded.web.server.NacosConfigHttpHandler.DATA_ID_PARAM_NAME;
import static com.alibaba.nacos.embedded.web.server.NacosConfigHttpHandler.GROUP_ID_PARAM_NAME;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        ConfigServiceBeanBuilder.class,
        AnnotationNacosInjectedBeanPostProcessor.class,
        NacosConfigListenerMethodProcessor.class,
        MultiNacosPropertySourceTest.class
})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class, MultiNacosPropertySourceTest.class})
@MultiNacosPropertySource(dataIds = {"test-1.yml", "test-2", "test-3.json"}, autoRefreshed = true, type = ConfigType.PROPERTIES)
@EnableNacos(globalProperties = @NacosProperties(serverAddr = "${server.addr}"))
public class MultiNacosPropertySourceTest extends AbstractNacosHttpServerTestExecutionListener {

    private static final String DATA_ID_TEST_1 = "test-1.yml";

    private static final String DATA_ID_1_CONTENT = "students:\n" +
            "    - {name: lct-1,num: 12}\n" +
            "    - {name: lct-2,num: 13}\n" +
            "    - {name: lct-3,num: 14}";

    private static final String DATA_ID_1_CONTENT_NEW = "students:\n" +
            "    - {name: lct-1,num: 12}\n" +
            "    - {name: lct-2,num: 13}";

    private static final String DATA_ID_TEST_2 = "test-2";

    private static final String DATA_ID_2_CONTENT = "KEY=V\nVALUE=K";

    private static final String DATA_ID_2_CONTENT_NEW = "KEY=222\nVALUE=111";

    private static final String DATA_ID_TEST_3 = "test-3.json";

    private static final String DATA_ID_3_CONTENT = "{\n" +
            "    \"people\":{\n" +
            "        \"a\":\"liaochuntao\",\n" +
            "        \"b\":\"this is test\"\n" +
            "    }\n" +
            "}";

    private static final String DATA_ID_3_CONTENT_NEW = "{\"just\":\"ok\"}";

    private static volatile String msg1 = null;
    private static volatile String msg2 = null;
    private static volatile String msg3 = null;

    @Override
    protected String getServerAddressPropertyName() {
        return "server.addr";
    }

    @Bean(name = ApplicationContextHolder.BEAN_NAME)
    public ApplicationContextHolder applicationContextHolder(ApplicationContext applicationContext) {
        ApplicationContextHolder applicationContextHolder = new ApplicationContextHolder();
        applicationContextHolder.setApplicationContext(applicationContext);
        return applicationContextHolder;
    }

    @NacosInjected
    private ConfigService configService;

    @NacosConfigListener(dataId = DATA_ID_TEST_1)
    public void onMessage1(String config) {
        System.out.println("onMessage1 : " + config);
        msg1 = config;
    }

    @NacosConfigListener(dataId = DATA_ID_TEST_2)
    public void onMessage2(String config) {
        System.out.println("onMessage2 : " + config);
        msg2 = config;
    }

    @NacosConfigListener(dataId = DATA_ID_TEST_3)
    public void onMessage3(String config) {
        System.out.println("onMessage3: " + config);
        msg3 = config;
    }

    @Test
    public void testMultiPropertySource() throws InterruptedException, NacosException {

        configService.publishConfig(DATA_ID_TEST_1, DEFAULT_GROUP, DATA_ID_1_CONTENT);
        configService.publishConfig(DATA_ID_TEST_2, DEFAULT_GROUP, DATA_ID_2_CONTENT);
        configService.publishConfig(DATA_ID_TEST_3, DEFAULT_GROUP, DATA_ID_3_CONTENT);

        Thread.sleep(1000);
        Assert.assertEquals(DATA_ID_1_CONTENT, msg1);
        Assert.assertEquals(DATA_ID_2_CONTENT, msg2);
        Assert.assertEquals(DATA_ID_3_CONTENT, msg3);

        configService.publishConfig(DATA_ID_TEST_1, DEFAULT_GROUP, DATA_ID_1_CONTENT_NEW);
        configService.publishConfig(DATA_ID_TEST_2, DEFAULT_GROUP, DATA_ID_2_CONTENT_NEW);
        configService.publishConfig(DATA_ID_TEST_3, DEFAULT_GROUP, DATA_ID_3_CONTENT_NEW);

        Thread.sleep(1000);

        Assert.assertEquals(DATA_ID_1_CONTENT_NEW, msg1);
        Assert.assertEquals(DATA_ID_2_CONTENT_NEW, msg2);
        Assert.assertEquals(DATA_ID_3_CONTENT_NEW, msg3);
    }

}
