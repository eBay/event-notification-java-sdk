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

package com.ebay.commerce.notification.utils;

import com.ebay.commerce.notification.config.EventNotificationConfig;
import com.ebay.commerce.notification.exceptions.EndpointValidationException;
import com.ebay.commerce.notification.exceptions.MissingEndpointValidationConfig;
import com.ebay.commerce.notification.model.ChallengeResponse;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class EndpointValidator {

    @Inject
    private EventNotificationConfig config;

    public ChallengeResponse generateChallengeResponse(String challengeCode) {
        if (!exists(config.getEndpoint()) || !exists(config.getVerificationToken()))
            throw new MissingEndpointValidationConfig("Endpoint and verificationToken is required");
        try {
            MessageDigest digest = MessageDigest.getInstance(MessageDigestAlgorithms.SHA_256);
            digest.update(challengeCode.getBytes(StandardCharsets.UTF_8));
            digest.update(config.getVerificationToken().getBytes(StandardCharsets.UTF_8));
            byte[] bytes = digest.digest(config.getEndpoint().getBytes(StandardCharsets.UTF_8));
            return new ChallengeResponse() {
                {
                    setChallengeResponse(Hex.encodeHexString(bytes));
                }
            };

        } catch (Exception ex) {
            // something unexpected. Catch all.
            throw new EndpointValidationException(ex.getMessage(), ex);
        }
    }


    private Boolean exists(String str) {
        return str != null && !str.isEmpty();
    }

}
