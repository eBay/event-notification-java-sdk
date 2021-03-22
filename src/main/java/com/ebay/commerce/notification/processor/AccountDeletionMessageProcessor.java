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

package com.ebay.commerce.notification.processor;

import org.openapitools.client.model.AccountDeletionData;

public class AccountDeletionMessageProcessor extends BaseMessageProcessor {

    public AccountDeletionMessageProcessor(Class type) {
        super(type);
    }

    @Override
    protected void processInternal(Object data) {
        AccountDeletionData accountDeletionData = (AccountDeletionData) data;
        // do something with the correctly serialized data for this topic.
        System.out.println("userId"+accountDeletionData.getUserId());
        System.out.println("username"+accountDeletionData.getUsername());

    }


}
