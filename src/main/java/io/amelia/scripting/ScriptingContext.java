/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.scripting;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import io.amelia.foundation.ConfigRegistry;
import io.amelia.foundation.Kernel;
import io.amelia.injection.LibraryClassLoader;
import io.amelia.lang.ExceptionReport;
import io.amelia.lang.MultipleException;
import io.amelia.lang.ScriptingException;
import io.amelia.support.ContentTypes;
import io.amelia.support.Encrypt;
import io.amelia.support.IO;
import io.amelia.support.Objs;
import io.amelia.support.Strs;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * Provides the context to a requested eval of the EvalFactory
 */
public abstract class ScriptingContext
{
	public static ScriptingContext fromFile( final File file )
	{
		try
		{
			return fromFile( new FileInterpreter( file ) );
		}
		catch ( IOException e )
		{
			ScriptingContext context = new ScriptingContext();
			context.result.handleException( e, context );
			return context;
		}
	}

	public static ScriptingContext fromFile( final FileInterpreter fi )
	{
		ScriptingContext context = fromSource( fi.consumeBytes(), fi.getFilePath() );
		context.isVirtual = false;
		context.contentType = fi.getContentType();
		context.shell = fi.getAnnotations().get( "shell" );
		return context;
	}

	public static ScriptingContext fromSource( byte[] source )
	{
		return fromSource( source, "<no file>" );
	}

	public static ScriptingContext fromSource( final byte[] source, final File file )
	{
		return fromSource( source, file.getAbsolutePath() );
	}

	public static ScriptingContext fromSource( final byte[] source, final String filename )
	{
		ScriptingContext context = new ScriptingContext();
		context.isVirtual = true;
		context.filename = filename;
		context.write( source );
		context.setBaseSource( new String( source, context.charset ) );
		return context;
	}

	public static ScriptingContext fromSource( String source )
	{
		return fromSource( source, "" );
	}

	public static ScriptingContext fromSource( final String source, final File file )
	{
		return fromSource( source, file.getAbsolutePath() );
	}

	public static ScriptingContext fromSource( final String source, final String filename )
	{
		ScriptingContext context = fromSource( new byte[0], filename );
		context.write( source.getBytes( context.charset ) );
		return context;
	}

	public static List<String> getPreferredExtensions()
	{
		return ConfigRegistry.config.getStringList( ScriptingFactory.Config.PREFERRED_EXTENSIONS );
	}

	private Path cacheDirectory;
	private Charset charset = Charset.defaultCharset();
	private ByteBuf content = Unpooled.buffer();
	private String contentType = null;
	private String filename = null;
	private boolean isVirtual = true;
	private ScriptingResult result = null;
	private String scriptBaseClass = null;
	private String scriptName = null;
	private String scriptPackage = null;
	private ScriptingFactory scriptingFactory = null;
	private String shell = "embedded";
	private String source = null;

	public Object eval() throws ScriptingException.Error, ScriptingException.Runtime, MultipleException
	{
		ScriptingFactory scriptingFactory = getScriptingFactory();
		if ( scriptingFactory == null )
			throw new IllegalArgumentException( "Can not eval() this ScriptingContext without the ScriptingFactory." );
		result = scriptingFactory.eval( this );

		String str = result.getString();

		ExceptionReport exceptionReport = result.getExceptionReport();

		if ( exceptionReport.hasSevereExceptions() )
			exceptionReport.throwExceptions( exceptionContext -> exceptionContext.notIgnorable() && ScriptingException.isInnerClass( exceptionContext.getThrowable() ), () -> new IllegalStateException( "We found unexpected exceptions, only ScriptingExceptions are thrown here." ) );

		// TODO Wrap in <pre> if the output is HTML!
		if ( exceptionReport.hasIgnorableExceptions() )
			str = exceptionReport.printToString() + "\n\n" + str;

		scriptingFactory.print( str );
		return result.getObject();
	}

	public ByteBuf getBuffer()
	{
		return content;
	}

	public String getBufferHash()
	{
		return Encrypt.md5Hex( readBytes() );
	}

	private Path getCacheFile()
	{
		if ( getScriptClassName() == null )
			return null;
		return Paths.get( getScriptClassName().replace( '.', File.separatorChar ) + ".class" ).resolve( getCachePath() );
	}

	public Path getCachePath()
	{
		if ( cacheDirectory == null )
			cacheDirectory = getDefaultCachePath();
		if ( cacheDirectory != null )
			try
			{
				if ( !LibraryClassLoader.isPathLoaded( cacheDirectory ) )
					LibraryClassLoader.addPath( cacheDirectory );
			}
			catch ( IOException e )
			{
				ScriptingFactory.L.warning( "Failed to add " + IO.relPath( cacheDirectory ) + " to classpath.", e );
			}
		return cacheDirectory;
	}

	public ScriptingContext setCachePath( Path cache )
	{
		this.cacheDirectory = cache;
		return this;
	}

	Charset getCharset()
	{
		return charset;
	}

	void setCharset( Charset charset )
	{
		this.charset = charset;
	}

	public String getContentType()
	{
		return contentType;
	}

	public ScriptingContext setContentType( final String contentType )
	{
		this.contentType = contentType;
		return this;
	}

	protected Path getDefaultCachePath()
	{
		return Kernel.getPath( Kernel.PATH_CACHE );
	}

	public String getFileName()
	{
		return filename;
	}

	public Path getPath()
	{
		return Paths.get( getFileName() );
	}

	public ScriptingResult getResult()
	{
		if ( result == null )
			result = new ScriptingResult( this, content );
		return result;
	}

	public String getScriptBaseClass()
	{
		return scriptBaseClass;
	}

	public void setScriptBaseClass( String scriptBaseClass )
	{
		this.scriptBaseClass = scriptBaseClass;
	}

	public String getScriptClassName()
	{
		if ( getScriptPackage() == null )
			return getScriptSimpleName();
		if ( getScriptSimpleName() == null )
			return null;
		return getScriptPackage() + "." + getScriptSimpleName();
	}

	public String getScriptName()
	{
		return scriptName;
	}

	public ScriptingContext setScriptName( String scriptName )
	{
		this.scriptName = scriptName;
		return this;
	}

	/* public SQLModelBuilder model() throws ScriptingException, MultipleException
	{
		if ( request == null && scriptingFactory == null )
			throw new IllegalArgumentException( "We can't eval() this EvalContext until you provide either the request or the scriptingFactory." );
		if ( request != null && scriptingFactory == null )
			getScriptingFactory = request.getScriptingFactory();

		setScriptBaseClass( SQLModelBuilder.class.getName() );

		result = scriptingFactory.eval( this );

		String str = result.getString( false );

		if ( result.hasNonIgnorableExceptions() )
			try
			{
				ExceptionReport.throwExceptions( result.getExceptions() );
			}
			catch ( Throwable e )
			{
				if ( e instanceof ScriptingException )
					throw ( ScriptingException ) e;
				if ( e instanceof MultipleException )
					throw ( MultipleException ) e;
				throw new ScriptingException( ReportingLevel.E_ERROR, "Unrecognized exception was thrown, only ScriptingExceptions should be thrown before this point", e );
			}

		if ( result.hasIgnorableExceptions() )
			str = ExceptionReport.printExceptions( result.getIgnorableExceptions() ) + "\n" + str;

		scriptingFactory.print( str );
		return ( SQLModelBuilder ) result.getScript();
	}*/

	public String getScriptPackage()
	{
		return scriptPackage;
	}

	public ScriptingContext setScriptPackage( String scriptPackage )
	{
		this.scriptPackage = scriptPackage;
		return this;
	}

	public String getScriptSimpleName()
	{
		return scriptName == null ? null : scriptName.contains( "." ) ? scriptName.substring( 0, scriptName.lastIndexOf( "." ) ) : scriptName;
	}

	public abstract ScriptingFactory getScriptingFactory();

	ScriptingContext setScriptingFactory( final ScriptingFactory factory )
	{
		this.scriptingFactory = factory;

		if ( getContentType() == null && getFileName() != null )
			setContentType( ContentTypes.getContentTypes( getFileName() ).findFirst().orElse( null ) );

		return this;
	}

	public String getShell()
	{
		return shell;
	}

	public ScriptingContext setShell( String shell )
	{
		this.shell = shell;
		return this;
	}

	public boolean isVirtual()
	{
		return isVirtual;
	}

	public String md5Hash()
	{
		return Encrypt.md5Hex( readBytes() );
	}

	public String read() throws Exception
	{
		return read( true, false );
	}

	public String read( boolean printErrors ) throws Exception
	{
		return read( printErrors, false );
	}

	public String read( boolean printErrors, boolean dumpObject ) throws Exception
	{
		ScriptingFactory scriptingFactory = getScriptingFactory();
		if ( scriptingFactory == null )
			throw new ScriptingException.Runtime( "Can't read() script with a ScriptingFactory." );

		ScriptingResult result = scriptingFactory.eval( this );

		String strResult = result.getString();
		if ( dumpObject && result.hasObject() )
			strResult = strResult + Objs.castToString( result.getObject() );

		if ( result.getExceptionReport().hasSevereExceptions() )
			try
			{
				result.getExceptionReport().throwSevereExceptions();
			}
			catch ( Throwable e )
			{
				if ( e instanceof ScriptingException.Error )
					throw ( ScriptingException.Error ) e;
				else if ( e instanceof ScriptingException.Runtime )
					throw e;
				else if ( e instanceof MultipleException )
					throw ( MultipleException ) e;
				else
					throw new ScriptingException.Error( "That was unexpected! We should only ever throw ScriptingExceptions here!", e );
			}

		// TODO Beatify the exception outputs!
		if ( printErrors && result.getExceptionReport().hasIgnorableExceptions() )
			strResult = result.getExceptionReport().printIgnorableToString() + "\n" + strResult;

		return strResult;
	}

	public byte[] readBytes()
	{
		int inx = content.readerIndex();
		byte[] bytes = new byte[content.readableBytes()];
		content.readBytes( bytes );
		content.readerIndex( inx );
		return bytes;
	}

	public String readString()
	{
		return content.toString( charset );
	}

	public String readString( Charset charset )
	{
		return content.toString( charset );
	}

	/**
	 * Attempts to erase the entire ByteBuf content
	 */
	public void reset()
	{
		int size = content.writerIndex();
		content.clear();
		content.writeBytes( new byte[size] );
		content.clear();
	}

	public void resetAndWrite( byte... bytes )
	{
		reset();
		if ( bytes.length < 1 )
			return;
		write( bytes );
	}

	public void resetAndWrite( ByteBuf source )
	{
		reset();
		if ( source == null )
			return;
		write( source );
	}

	public void resetAndWrite( String str )
	{
		reset();
		if ( str == null )
			return;
		write( str.getBytes( charset ) );
	}

	public String setBaseSource()
	{
		return source;
	}

	public ScriptingContext setBaseSource( String source )
	{
		// TODO Presently debug source files are only created when the entire server is in debug, however, we should make it so developers can turn this feature on per webroot or source file.
		if ( Kernel.isDevelopment() )
			try
			{
				OutputStream out = Files.newOutputStream( Paths.get( scriptName + ".dbg" ).resolve( cacheDirectory ) );
				out.write( Strs.decodeUtf8( source ) );
				IO.closeQuietly( out );
			}
			catch ( Exception e )
			{
				// Do nothing since we only do this as a debug feature for developers.
			}

		this.source = source;
		return this;
	}

	@Override
	public String toString()
	{
		return String.format( "EvalExecutionContext {package=%s,name=%s,filename=%s,shell=%s,sourceSize=%s,contentType=%s}", scriptPackage, scriptName, filename, shell, content.readableBytes(), contentType );
	}

	public void write( byte... bytes )
	{
		content.writeBytes( bytes );
	}

	public void write( ByteBuf source )
	{
		content.writeBytes( source );
	}
}
