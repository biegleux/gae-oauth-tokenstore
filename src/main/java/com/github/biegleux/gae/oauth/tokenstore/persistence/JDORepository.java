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

import java.util.ArrayList;
import java.util.Collection;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;

/**
 * This base class provides basic operations for managing persistent JDO entities.
 *
 * @param <T> The type of Object stored by the repository.
 * @param <ID> The type of ID used by the stored object.
 */
abstract class JDORepository<T, ID> {

	protected Class<T> clazz;

	public JDORepository(Class<T> clazz) {
		this.clazz = clazz;
	}

	/**
	 * Looks up the entity with the given key.
	 * @param id An object representation of a single field identity key.
	 * @return The corresponding persistent entity.
	 */
	public T get(ID id) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			T object = pm.getObjectById(clazz, id);
			return object;
		} finally {
			pm.close();
		}
	}

	/**
	 * Looks up all entities.
	 * @return Persistent entities.
	 */
	@SuppressWarnings("unchecked")
	public Collection<T> getAll() {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = null;
		try {
			query = pm.newQuery(clazz);
			return new ArrayList<T> ((Collection<T>) query.execute());
		} finally {
			if (query != null) {
				query.closeAll();
			}
			pm.close();
		}
	}

	/**
	 * Makes the given entity persistent.
	 * @param entity Persistent capable entity.
	 * @return Corresponding persistent entity.
	 */
	public T save(T entity) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Transaction tx = pm.currentTransaction();
		try {
			tx.begin();
			T result = pm.makePersistent(entity);
			tx.commit();
			return result;
		} finally {
			if (tx.isActive()) {
				tx.rollback();
			}
			pm.close();
		}
	}
}
