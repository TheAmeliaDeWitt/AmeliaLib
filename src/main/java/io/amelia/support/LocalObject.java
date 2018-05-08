package io.amelia.support;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Used to track a local value using a key that is weak referenced.
 *
 * @param <T>
 */
public class LocalObject<T>
{
	private final Supplier<T> supplier;
	private volatile List<LocalObjectReference> references = new ArrayList<>();

	public LocalObject( Supplier<T> supplier )
	{
		this.supplier = supplier;
	}

	public LocalObject()
	{
		this( null );
	}

	private void clean()
	{
		synchronized ( references )
		{
			for ( LocalObjectReference ref : references )
				if ( ref.key.get() == null )
					references.remove( ref );
		}
	}

	public void clear()
	{
		references.clear();
	}

	@SuppressWarnings( "unchecked" )
	public T get( Object key )
	{
		synchronized ( references )
		{
			clean();

			for ( LocalObjectReference ref : references )
				if ( ref.key.get() == this )
					return ( T ) ref.value;

			if ( supplier != null )
			{
				T value = supplier.get();
				references.add( new LocalObjectReference( key, value ) );
				return value;
			}

			return null;
		}
	}

	public void set( Object key, T value )
	{
		synchronized ( references )
		{
			clean();

			for ( LocalObjectReference ref : references )
				if ( ref.key.get() == this )
				{
					ref.value = value;
					return;
				}

			references.add( new LocalObjectReference( key, value ) );
		}
	}

	private class LocalObjectReference
	{
		WeakReference<Object> key;
		T value;

		LocalObjectReference( Object key, T value )
		{
			this.key = new WeakReference<>( key );
			this.value = value;
		}
	}
}
