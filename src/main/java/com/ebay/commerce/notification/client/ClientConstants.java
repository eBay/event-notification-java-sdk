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

import com.ebay.commerce.notification.constants.Environment;

import java.util.HashMap;
import java.util.Map;

public class ClientConstants {
    private static Map<Environment, String> END_POINTS = new HashMap() {
        {
            put(Environment.PRODUCTION, "https://api.ebay.com/commerce/notification/v1/public_key/%s");
            put(Environment.SANDBOX, "https://api.sandbox.ebay.com/commerce/notification/v1/public_key/%s");
        }
    };

    public static final String BEARER = "bearer ";
    public static final String AUTHORIZATION = "Authorization";

    public static String getEndpointForEnvironment(Environment env) {
        return END_POINTS.get(env);
    }
}
