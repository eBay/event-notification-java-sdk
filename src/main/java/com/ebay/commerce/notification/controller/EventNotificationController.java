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

package com.ebay.commerce.notification.controller;

import com.ebay.commerce.notification.constants.TopicEnum;
import com.ebay.commerce.notification.exceptions.*;
import com.ebay.commerce.notification.model.ChallengeResponse;
import com.ebay.commerce.notification.processor.MessageProcessorFactory;
import com.ebay.commerce.notification.utils.EndpointValidator;
import com.ebay.commerce.notification.utils.SignatureValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.openapitools.client.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;

@RestController
@RequestMapping("/")
public class EventNotificationController {

    private Logger logger = LoggerFactory.getLogger(EventNotificationController.class);

    @Inject
    private MessageProcessorFactory processorFactory;

    @Inject
    private SignatureValidator signatureValidator;

    @Inject
    private EndpointValidator endpointValidator;

    @GetMapping("/webhook")
    public ResponseEntity validate(@RequestParam("challenge_code") String challengeCode) {
        if (challengeCode == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        ChallengeResponse challengeResponse = null;
        try {
            challengeResponse = endpointValidator.generateChallengeResponse(challengeCode);

        } catch (MissingEndpointValidationConfig | EndpointValidationException ex) {
            logger.error("Endpoint validation failure " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.ok(challengeResponse);
    }

    @PostMapping("/webhook")
    public ResponseEntity process(@RequestBody Message message, @RequestHeader(required = false, value = "X-EBAY-SIGNATURE") String signatureHeader) {
        try {
            if (signatureValidator.validate(message, signatureHeader)) {
                process(message);
                logger.info("Message processed successfully for topic:" + message.getMetadata().getTopic() + " notificationId:" + message.getNotification().getNotificationId());
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).build();
            }
        } catch (SignatureValidationException | ClientException | OAuthTokenException | PublicKeyCacheException | JsonProcessingException | ProcessorNotDefined ex) {
            logger.error("Signature validation processing failure:" + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private void process(Message message) throws JsonProcessingException {
        processorFactory.getProcessor(TopicEnum.valueOf(message.getMetadata().getTopic())).process(message);
    }

}
