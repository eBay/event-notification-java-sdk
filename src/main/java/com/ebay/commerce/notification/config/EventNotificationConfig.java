/*
 * Copyright (c) 2021 eBay Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.ebay.commerce.notification.config;

import com.ebay.api.client.auth.oauth2.CredentialUtil;
import com.ebay.commerce.notification.constants.Environment;
import com.ebay.commerce.notification.constants.TopicEnum;
import com.ebay.commerce.notification.exceptions.InitializationException;
import com.ebay.commerce.notification.processor.AccountDeletionMessageProcessor;
import com.ebay.commerce.notification.processor.MessageProcessorFactory;
import org.openapitools.client.model.AccountDeletionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;

import javax.inject.Inject;
import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class EventNotificationConfig
        implements ApplicationListener<ApplicationReadyEvent> {

    private Logger logger = LoggerFactory.getLogger(EventNotificationConfig.class);

    @Value("${client.credentials.file}")
    private String clientCredentialFilePath;

    @Value("${environment}")
    private String environment;

    @Value("${endpoint}")
    private String endpoint;

    @Value("${verificationToken}")
    private String verificationToken;


    @Inject
    private MessageProcessorFactory processorFactory;

    public Environment getEnvironment() {
        return Environment.valueOf(environment);
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getVerificationToken() {
        return verificationToken;
    }

    public void setVerificationToken(String verificationToken) {
        this.verificationToken = verificationToken;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        setOauthClientCredentials();
        registerMessageProcessors();
    }

    private void setOauthClientCredentials() {
        try {
            CredentialUtil.load(new FileInputStream(clientCredentialFilePath));

        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new InitializationException(e.getMessage());
        }
    }

    private void registerMessageProcessors() {
        processorFactory.register(TopicEnum.MARKETPLACE_ACCOUNT_DELETION, new AccountDeletionMessageProcessor(AccountDeletionData.class));
        // register other use case specific message processors here
    }

}


