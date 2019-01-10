/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.bindings;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to label a method or field as having a dynamic value inside BindingResolvers.
 * Meaning the returned value should NEVER be mapped to a namespace for reuse at a later time.
 */
@Target( {ElementType.METHOD, ElementType.FIELD} )
@Retention( RetentionPolicy.RUNTIME )
public @interface DynamicBinding
{

}
