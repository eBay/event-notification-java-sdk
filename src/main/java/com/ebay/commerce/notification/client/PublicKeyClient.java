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

package com.ebay.commerce.notification.client;

import com.ebay.api.client.auth.oauth2.OAuth2Api;
import com.ebay.api.client.auth.oauth2.model.AccessToken;
import com.ebay.commerce.notification.config.EventNotificationConfig;
import com.ebay.commerce.notification.constants.Constants;
import com.ebay.commerce.notification.constants.Environment;
import com.ebay.commerce.notification.exceptions.ClientException;
import com.ebay.commerce.notification.exceptions.OAuthTokenException;
import com.ebay.commerce.notification.model.PublicKey;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Optional;

@Component
public class PublicKeyClient {

    @Inject
    private EventNotificationConfig eventNotificationConfig;
    private OkHttpClient client = new OkHttpClient();
    private ObjectMapper objectMapper = new ObjectMapper();
    private OAuth2Api oAuthClient = new OAuth2Api();
    private Logger logger = LoggerFactory.getLogger(PublicKeyClient.class);


    public PublicKey getPublicKey(String keyId) throws IOException {
        String uri = String.format(ClientConstants.getEndpointForEnvironment(eventNotificationConfig.getEnvironment()), keyId);
        Request request = new Request.Builder().url(uri)
                .addHeader(ClientConstants.AUTHORIZATION, fetchToken(eventNotificationConfig.getEnvironment()))
                .build();
        Response response = client.newCall(request).execute();
        if (response.code() == HttpStatus.OK.value())
            return objectMapper.readValue(response.body().string(), PublicKey.class);
        throw new ClientException(String.format("Public Key retrieval failed with %s  for %s ", response.code(), uri));
    }

    private synchronized String fetchToken(Environment environment) throws IOException {
        com.ebay.api.client.auth.oauth2.model.Environment authEnvironment = environment == Environment.SANDBOX ? com.ebay.api.client.auth.oauth2.model.Environment.SANDBOX :
                com.ebay.api.client.auth.oauth2.model.Environment.PRODUCTION;
        Optional<AccessToken> accessToken = oAuthClient.getApplicationToken(authEnvironment, Constants.APPLICABLE_SCOPES).getAccessToken();
        if (accessToken.isPresent()) return ClientConstants.BEARER + accessToken.get().getToken();
        throw new OAuthTokenException("Retrieval of token failed for " + environment.name());
    }

}
