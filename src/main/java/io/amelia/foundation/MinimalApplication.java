/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation;

import io.amelia.data.parcel.ParcelCarrier;
import io.amelia.lang.ApplicationException;
import io.amelia.lang.ExceptionReport;
import io.amelia.lang.ParcelException;

public class MinimalApplication extends ApplicationInterface
{
	@Override
	public void fatalError( ExceptionReport report, boolean crashOnError )
	{

	}

	@Override
	public void handleParcel( ParcelCarrier parcelCarrier ) throws ParcelException.Error
	{

	}

	@Override
	public void onRunlevelChange( Runlevel previousRunlevel, Runlevel currentRunlevel ) throws ApplicationException.Error
	{

	}

	@Override
	void parse() throws Exception
	{

	}

	@Override
	public void sendToAll( ParcelCarrier parcel )
	{

	}
}
