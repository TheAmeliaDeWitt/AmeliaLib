/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.plugins.loader;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.annotation.Nonnull;

import io.amelia.lang.PluginInvalidException;
import io.amelia.plugins.Plugin;
import io.amelia.plugins.PluginMeta;
import io.amelia.support.Objs;

/**
 * A ClassLoader for plugins, to allow shared classes across multiple plugins
 */
public final class PluginClassLoader extends URLClassLoader
{
	private static final Map<Class<?>, PluginClassLoader> loaders = new WeakHashMap<>();

	static synchronized void initialize( @Nonnull Plugin plugin )
	{
		PluginClassLoader loader = loaders.get( plugin.getClass() );

		if ( loader == null )
			throw new IllegalStateException( "Plugin was not properly initialized: '" + plugin.getClass().getName() + "'." );

		if ( loader.initialized )
			throw new IllegalArgumentException( "Plugin already initialized: '" + plugin.getClass().getName() + "'." );

		plugin.init( loader.loader, loader.description, loader.dataPath, loader.pluginPath, loader );
		loader.initialized = true;
	}

	final Plugin plugin;
	private final Map<String, Class<?>> classes = new HashMap<>();
	private final Path dataPath;
	private final PluginMeta description;
	private final JavaPluginLoader loader;
	private final Path pluginPath;
	private boolean initialized = false;

	PluginClassLoader( final JavaPluginLoader loader, final ClassLoader parent, final PluginMeta description, final Path dataPath, final Path pluginPath ) throws PluginInvalidException, MalformedURLException
	{
		super( new URL[] {pluginPath.toUri().toURL()}, parent );

		Objs.notNull( loader, "Loader cannot be null" );

		this.loader = loader;
		this.description = description;
		this.dataPath = dataPath;
		this.pluginPath = pluginPath;

		try
		{


			Class<?> jarClass;
			try
			{
				jarClass = Class.forName( description.getMain(), true, this );
			}
			catch ( ClassNotFoundException ex )
			{
				throw new PluginInvalidException( "Cannot find main class `" + description.getMain() + "'", ex );
			}

			Class<? extends JavaPlugin> pluginClass;
			try
			{
				pluginClass = jarClass.asSubclass( JavaPlugin.class );
			}
			catch ( ClassCastException ex )
			{
				throw new PluginInvalidException( "main class `" + description.getMain() + "' does not extend Plugin", ex );
			}

			loaders.put( jarClass, this );

			plugin = pluginClass.newInstance();
		}
		catch ( IllegalAccessException ex )
		{
			throw new PluginInvalidException( "No public constructor", ex );
		}
		catch ( InstantiationException ex )
		{
			throw new PluginInvalidException( "Abnormal plugin type", ex );
		}
	}

	@Override
	protected Class<?> findClass( String name ) throws ClassNotFoundException
	{
		return findClass( name, true );
	}

	Class<?> findClass( String name, boolean checkGlobal ) throws ClassNotFoundException
	{
		if ( name.startsWith( "com.chiorichan." ) && !name.startsWith( "com.chiorichan.plugin." ) )
			throw new ClassNotFoundException( name );

		Class<?> result = classes.get( name );

		if ( result == null )
		{
			if ( checkGlobal )
				result = loader.getClassByName( name );

			if ( result == null )
			{
				result = super.findClass( name );

				if ( result != null )
					loader.setClass( name, result );
			}

			classes.put( name, result );
		}

		return result;
	}

	Set<String> getClasses()
	{
		return classes.keySet();
	}

	public Plugin getPlugin()
	{
		return plugin;
	}

	public PluginLoader getPluginLoader()
	{
		return loader;
	}
}
