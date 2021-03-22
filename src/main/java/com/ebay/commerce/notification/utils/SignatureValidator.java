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

import com.ebay.commerce.notification.constants.Constants;
import com.ebay.commerce.notification.exceptions.InitializationException;
import com.ebay.commerce.notification.exceptions.SignatureValidationException;
import com.ebay.commerce.notification.model.PublicKey;
import com.ebay.commerce.notification.model.XeBaySignature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openapitools.client.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.regex.Matcher;

@Component
public class SignatureValidator {

    @Inject
    private PublicKeyCache cache;
    private Logger logger = LoggerFactory.getLogger(SignatureValidator.class);
    ObjectMapper mapper = new ObjectMapper();
    private KeyFactory keyFactory = getKeyFactory();


    public Boolean validate(Message message, String signatureHeader) {
        try {
            XeBaySignature xeBaySignature = getXeBaySignatureHeader(signatureHeader);
            PublicKey publicKey = cache.getPublicKey(xeBaySignature.getKid());
            java.security.PublicKey pk = keyFactory.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(getRawKey(publicKey.getKey()))));
            Signature sig = Signature.getInstance(String.format(Constants.ALGO, publicKey.getDigest(), publicKey.getAlgorithm()));
            sig.initVerify(pk);
            sig.update(mapper.writeValueAsString(message).getBytes());
            Boolean validationResult = sig.verify(Base64.getDecoder().decode(xeBaySignature.getSignature()));
            if(validationResult==Boolean.FALSE) logger.error("Signature mismatch for payload:"+mapper.writeValueAsString(message)+" with signature:"+signatureHeader);
            return validationResult;
        } catch (InvalidKeySpecException | NoSuchAlgorithmException | InvalidKeyException | SignatureException | JsonProcessingException ex) {
            throw new SignatureValidationException(ex.getMessage());
        }
    }

    private String getRawKey(String key) {
        Matcher matcher = Constants.KEY_PATTERN.matcher(key);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return key;
    }

    private XeBaySignature getXeBaySignatureHeader(String signatureHeader) {
        try {
            return mapper.readValue(new String(Base64.getDecoder().decode(signatureHeader)), XeBaySignature.class);
        } catch (JsonProcessingException e) {
            logger.error("Parsing falied for signature header " + signatureHeader);
            throw new RuntimeException();
        }
    }

    private KeyFactory getKeyFactory() {
        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance("EC");
        } catch (NoSuchAlgorithmException e) {
            logger.error("Error initializing KeyFactory for EC");
            throw new InitializationException(e.getMessage());
        }
        return keyFactory;
    }
}
