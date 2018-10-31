/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.credhub.core;

import org.springframework.credhub.core.certificate.CredHubCertificateOperations;
import org.springframework.credhub.core.certificate.CredHubCertificateTemplate;
import org.springframework.credhub.core.credential.CredHubCredentialOperations;
import org.springframework.credhub.core.credential.CredHubCredentialTemplate;
import org.springframework.credhub.core.info.CredHubInfoOperations;
import org.springframework.credhub.core.info.CredHubInfoTemplate;
import org.springframework.credhub.core.interpolation.CredHubInterpolationOperations;
import org.springframework.credhub.core.interpolation.CredHubInterpolationTemplate;
import org.springframework.credhub.core.permission.CredHubPermissionOperations;
import org.springframework.credhub.core.permission.CredHubPermissionTemplate;
import org.springframework.credhub.core.permissionV2.CredHubPermissionV2Operations;
import org.springframework.credhub.core.permissionV2.CredHubPermissionV2Template;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

/**
 * Implements the main interaction with CredHub.
 *
 * @author Scott Frederick 
 */
public class CredHubTemplate implements CredHubOperations {
	private final RestTemplate restTemplate;

	/**
	 * Create a new {@link CredHubTemplate} using the provided {@link RestTemplate}.
	 * Intended for internal testing only.
	 *
	 * @param restTemplate the {@link RestTemplate} to use for interactions with CredHub
	 */
	public CredHubTemplate(RestTemplate restTemplate) {
		Assert.notNull(restTemplate, "restTemplate must not be null");

		this.restTemplate = restTemplate;
	}

	/**
	 * Create a new {@link CredHubTemplate} using the provided base URI and
	 * {@link ClientHttpRequestFactory}.
	 *
	 * @param apiUriBase the base URI for the CredHub server (scheme, host, and port);
	 * must not be {@literal null}
	 * @param clientHttpRequestFactory the {@link ClientHttpRequestFactory} to use when
	 * creating new connections
	 */
	public CredHubTemplate(String apiUriBase, ClientHttpRequestFactory clientHttpRequestFactory) {
		Assert.notNull(apiUriBase, "apiUriBase must not be null");
		Assert.notNull(clientHttpRequestFactory, "clientHttpRequestFactory must not be null");

		this.restTemplate = CredHubClient.createRestTemplate(apiUriBase,
				clientHttpRequestFactory);
	}

	/**
	 * Get the operations for saving, retrieving, and deleting credentials.
	 *
	 * @return the credentials operations
	 */
	@Override
	public CredHubCredentialOperations credentials() {
		return new CredHubCredentialTemplate(this);
	}

	/**
	 * Get the operations for adding, retrieving, and deleting permissions from a credential.
	 *
	 * @return the permissions operations
	 */
	@Override
	public CredHubPermissionOperations permissions() {
		return new CredHubPermissionTemplate(this);
	}

	/**
	 * Get the operations for adding, retrieving, and deleting permissions from a credential.
	 *
	 * @return the permissions operations
	 */
	@Override
	public CredHubPermissionV2Operations permissionsV2() {
		return new CredHubPermissionV2Template(this);
	}

	/**
	 * Get the operations for retrieving, regenerating, and updating certificates.
	 *
	 * @return the certificates operations
	 */
	@Override
	public CredHubCertificateOperations certificates() {
		return new CredHubCertificateTemplate(this);
	}

	/**
	 * Get the operations for interpolating service binding credentials.
	 *
	 * @return the interpolation operations
	 */
	@Override
	public CredHubInterpolationOperations interpolation() {
		return new CredHubInterpolationTemplate(this);
	}

	/**
	 * Get the operations for retrieving CredHub server information.
	 *
	 * @return the info operations
	 */
	@Override
	public CredHubInfoOperations info() {
		return new CredHubInfoTemplate(this);
	}

	/**
	 * Allow interaction with the configured {@link RestTemplate} not provided
	 * by other methods.
	 *
	 * @param callback wrapper for the callback method
	 * @param <T> the credential implementation type
	 * @return the return value from the callback method
	 */
	@Override
	public <T> T doWithRest(RestOperationsCallback<T> callback) {
		Assert.notNull(callback, "callback must not be null");

		try {
			return callback.doWithRestOperations(restTemplate);
		}
		catch (HttpStatusCodeException e) {
			throw new CredHubException(e);
		}
	}
}
