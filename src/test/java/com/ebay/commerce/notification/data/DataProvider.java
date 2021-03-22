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

package com.ebay.commerce.notification.data;

import com.ebay.commerce.notification.model.PublicKey;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openapitools.client.model.Message;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
public class DataProvider {

    private ObjectMapper objectMapper = new ObjectMapper();
    private static final String MESSAGE = "src/test/java/com/ebay/commerce/notification/data/message.json";
    private static final String TAMPERED_MESSAGE = "src/test/java/com/ebay/commerce/notification/data/tampered_message.json";
    private static final String PUBLIC_KEY_RESPONSE = "src/test/java/com/ebay/commerce/notification/data/public_key_response.json";
    private static final String SIGNATURE_HEADER = "eyJhbGciOiJlY2RzYSIsImtpZCI6Ijk5MzYyNjFhLTdkN2ItNDYyMS1hMGYxLTk2Y2NiNDI4YWY0OSIsInNpZ25hdHVyZSI6Ik1FWUNJUUNmeGZJV3V4bVdjSUJRSjljNS9YN2lHREpxczJSQ0dzQkVhQWppbnlycmZBSWhBSVY2d0djVGlCdVY1S0pVaWYyaG9reXJMK1E5c3NIa2FkK214Mm5FRTI1dyIsImRpZ2VzdCI6IlNIQTEifQ==";

    public Message getMockMessage() {
        try {
            return objectMapper.readValue(readFile(MESSAGE),
                    Message.class);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Message getMockTamperedMessage() {
        try {
            return objectMapper.readValue(readFile(TAMPERED_MESSAGE),
                    Message.class);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public PublicKey getMockPublicKeyResponse() {
        try {
            return objectMapper.readValue(readFile(PUBLIC_KEY_RESPONSE),
                    PublicKey.class);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getMockXEbaySignatureHeader () {
        return SIGNATURE_HEADER;
    }

    private String readFile(String filename) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filename)));
    }
}
