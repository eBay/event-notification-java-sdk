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

package com.ebay.commerce.notification;

import com.ebay.commerce.notification.client.PublicKeyClient;
import com.ebay.commerce.notification.controller.EventNotificationController;
import com.ebay.commerce.notification.data.DataProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NotificationConsumerApplication.class)
class NotificationConsumerApplicationTest {

	@Inject
	private DataProvider provider;

	@MockBean
	private PublicKeyClient publicKeyClient;

	@Inject
	EventNotificationController controller;


	@Before
	public void wireMocks() throws IOException {
			Mockito.when(publicKeyClient.getPublicKey(Mockito.any(String.class))).thenReturn(provider.getMockPublicKeyResponse());
	}

	@Test
	public void testPayloadProcessingSuccess () {
		ResponseEntity actualResponse = controller.process(provider.getMockMessage(),provider.getMockXEbaySignatureHeader());
		Assert.assertEquals(HttpStatus.NO_CONTENT,actualResponse.getStatusCode());
	}

	@Test
	public void testPayLoadVerificationFailure() throws IOException {
		Mockito.when(publicKeyClient.getPublicKey(Mockito.any(String.class))).thenReturn(provider.getMockPublicKeyResponse());
		ResponseEntity actualResponse = controller.process(provider.getMockTamperedMessage(),provider.getMockXEbaySignatureHeader());
		Assert.assertEquals(HttpStatus.PRECONDITION_FAILED,actualResponse.getStatusCode());
	}
}
