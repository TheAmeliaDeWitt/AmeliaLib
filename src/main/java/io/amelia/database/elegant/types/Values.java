/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Miss Amelia Sara (Millie) <me@missameliasara.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.database.elegant.types;

import java.util.Map;

/**
 * Provides the Skeleton Interface for SQL Queries implementing the Values Methods
 */
public interface Values<T>
{
	Map<String, Object> getValues();

	T value( String key, Object val );

	T values( String[] keys, Object[] valuesArray );

	T values( Map<String, Object> map );
}
