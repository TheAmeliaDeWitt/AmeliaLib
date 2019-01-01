/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.bindings;

import io.amelia.foundation.BaseApplication;
import io.amelia.foundation.BasePlugins;
import io.amelia.foundation.Foundation;

public class FoundationBindingResolver extends BindingResolver
{
	BasePlugins pluginServiceManager = null;

	public FoundationBindingResolver()
	{
		addAlias( DefaultPlugins.class, "plugins.manager" );
		addAlias( "plugins.mgr", "plugins.manager" );
	}

	@DynamicBinding
	public BaseApplication app()
	{
		return Foundation.getApplication();
	}

	@ProvidesBinding( "plugins.manager" )
	public BasePlugins pluginManager()
	{
		if ( pluginServiceManager == null )
			pluginServiceManager = new DefaultPlugins();
		return pluginServiceManager;
	}
}
