/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.scripting.event;

import io.amelia.scripting.ScriptingContext;

/**
 * Provides an interface which allows the ScriptingEngine to pass events into scripts, such as, pre-eval, post-eval and that exceptions were thrown.
 */
public interface ScriptingEvents
{
	void onAfterExecute( ScriptingContext context );

	void onBeforeExecute( ScriptingContext context );

	void onException( ScriptingContext context, Throwable throwable );
}
