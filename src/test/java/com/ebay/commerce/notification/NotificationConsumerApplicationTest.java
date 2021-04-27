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
import com.ebay.commerce.notification.model.ChallengeResponse;
import org.apache.commons.codec.binary.Hex;
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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NotificationConsumerApplication.class)
class NotificationConsumerApplicationTest {

	@Inject
	private DataProvider provider;

	@MockBean
	private PublicKeyClient publicKeyClient;

	@Inject
	private EventNotificationController controller;

	@Inject
	private EventNotificationController eventNotificationController;


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

	@Test
	public void testVerification() throws NoSuchAlgorithmException {
		String challengeCode= "a8628072-3d33-45ee-9004-bee86830a22d";
		String verificationToken = "71745723-d031-455c-bfa5-f90d11b4f20a";
		String endpoint = "http://www.testendpoint.com/webhook";
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		digest.update(challengeCode.getBytes(StandardCharsets.UTF_8));
		digest.update(verificationToken.getBytes(StandardCharsets.UTF_8));
		byte[] bytes = digest.digest(endpoint.getBytes(StandardCharsets.UTF_8));
		String expectedChallengeResponse = Hex.encodeHexString( bytes ) ;
		ResponseEntity responseEntity =eventNotificationController.validate("a8628072-3d33-45ee-9004-bee86830a22d");
		ChallengeResponse challengeResponse = (ChallengeResponse) responseEntity.getBody();
		Assert.assertEquals(expectedChallengeResponse,challengeResponse.getChallengeResponse());
	}

}
