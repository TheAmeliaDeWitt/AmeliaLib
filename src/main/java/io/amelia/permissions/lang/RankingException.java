/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.permissions.lang;

import io.amelia.permissions.PermissibleEntity;

public class RankingException extends PermissionException
{
	private static final long serialVersionUID = -328357153481259189L;

	protected PermissibleEntity promoter = null;
	protected PermissibleEntity target = null;

	public RankingException( String message, PermissibleEntity target, PermissibleEntity promoter )
	{
		super( message );
		this.target = target;
		this.promoter = promoter;
	}

	public PermissibleEntity getPromoter()
	{
		return promoter;
	}

	public PermissibleEntity getTarget()
	{
		return target;
	}
}
