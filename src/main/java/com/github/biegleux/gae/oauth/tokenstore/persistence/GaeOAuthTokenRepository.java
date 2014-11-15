/*
 * Copyright 2014 Tibor Bombiak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.github.biegleux.gae.oauth.tokenstore.persistence;

import java.util.Collection;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.github.biegleux.gae.oauth.tokenstore.persistence.model.GaeOAuthToken;
import com.google.appengine.api.datastore.Key;

/**
 * This base class provides additional operations for managing {@link GaeOAuthToken} entities.
 *
 * @param <T> Subclass of {@link GaeOAuthToken} stored by the repository.
 */
abstract class GaeOAuthTokenRepository<T extends GaeOAuthToken<?>> extends JDORepository<T, Key> {

	public GaeOAuthTokenRepository(Class<T> clazz) {
		super(clazz);
	}

	/**
	 * Looks up the {@link GaeOAuthToken} entity with the given authentication ID.
	 * @param authenticationId Authentication ID.
	 * @return The corresponding {@link GaeOAuthToken} entity.
	 */
	@SuppressWarnings("unchecked")
	public T findByAuthenticationId(String authenticationId) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = null;
		try {
			query = pm.newQuery(clazz);
			query.setFilter("authenticationId == param");
			query.declareParameters("String param");
			Collection<T> tokens = (Collection<T>) query.execute(authenticationId);
			return tokens.isEmpty() ? null : tokens.iterator().next();
		} finally {
			if (query != null) {
				query.closeAll();
			}
			pm.close();
		}
	}

	/**
	 * Looks up the {@link GaeOAuthToken} entity with the given token ID.
	 * @param tokenId Token ID.
	 * @return The corresponding {@link GaeOAuthToken} entity.
	 */
	@SuppressWarnings("unchecked")
	public T findByTokenId(String tokenId) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = null;
		try {
			query = pm.newQuery(clazz);
			query.setFilter("tokenId == param");
			query.declareParameters("String param");
			Collection<T> tokens = (Collection<T>) query.execute(tokenId);
			return tokens.isEmpty() ? null : tokens.iterator().next();
		} finally {
			if (query != null) {
				query.closeAll();
			}
			pm.close();
		}
	}

	/**
	 * Deletes the {@link GaeOAuthToken} entities with the given token ID.
	 * @param tokenId Token ID.
	 * @return Number of {@link GaeOAuthToken} entities that were deleted.
	 */
	public long deleteByTokenId(String tokenId) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = null;
		try {
			query = pm.newQuery(clazz);
			query.setFilter("tokenId == param");
			query.declareParameters("String param");
			return query.deletePersistentAll(tokenId);
		} finally {
			if (query != null) {
				query.closeAll();
			}
			pm.close();
		}
	}
}
