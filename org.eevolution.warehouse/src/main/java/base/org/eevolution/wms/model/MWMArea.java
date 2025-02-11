/**********************************************************************
 * This file is part of Adempiere ERP Bazaar                          * 
 * http://www.adempiere.org                                           * 
 *                                                                    * 
 * Copyright (C) Victor Perez	                                      * 
 * Copyright (C) Contributors                                         * 
 *                                                                    * 
 * This program is free software; you can redistribute it and/or      * 
 * modify it under the terms of the GNU General Public License        * 
 * as published by the Free Software Foundation; either version 2     * 
 * of the License, or (at your option) any later version.             * 
 *                                                                    * 
 * This program is distributed in the hope that it will be useful,    * 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of     * 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the       * 
 * GNU General Public License for more details.                       * 
 *                                                                    * 
 * You should have received a copy of the GNU General Public License  * 
 * along with this program; if not, write to the Free Software        * 
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,         * 
 * MA 02110-1301, USA.                                                * 
 *                                                                    * 
 * Contributors:                                                      * 
 *  - Victor Perez (victor.perez@e-evolution.com	 )                *
 *                                                                    *
 * Sponsors:                                                          *
 *  - e-Evolution (http://www.e-evolution.com/)                       *
 **********************************************************************/
package org.eevolution.wms.model;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.compiere.model.Query;
import org.compiere.util.CLogger;

/**
 * Class Model Warehouse Area
 * @author victor.perez@e-evoluton.com, e-Evolution
 *
 */
public class MWMArea extends X_WM_Area
{
	/**
	 * Get Warehouse Area Type
	 * @param ctx
	 * @param warehouseId
	 * @param trxName
     * @return
     */
	public static List<MWMArea> getByWarehouse(Properties ctx, int warehouseId, String trxName)
	{
		final String whereClause =	MWMArea.COLUMNNAME_M_Warehouse_ID + "=?";
		return new Query(ctx, MWMArea.Table_Name, whereClause, trxName)
					.setClient_ID()
					.setOnlyActiveRecords(true)
					.setParameters(warehouseId)
					.list();
	}

	/**
	 * Get Area Type based on warehouse id and area type id
	 * @param ctx
	 * @param warehouseId
	 * @param warehouseAreaTypeId
	 * @param trxName
     * @return
     */
	public static List<MWMArea> getByWarehouseAndAreaType(Properties ctx, int warehouseId, int warehouseAreaTypeId, String trxName)
	{
		ArrayList<Object> parameters = new ArrayList();
		StringBuffer whereClause = new StringBuffer(MWMArea.COLUMNNAME_M_Warehouse_ID).append("= ?");
		parameters.add(warehouseId);
		if(warehouseAreaTypeId > 0)
		{
			whereClause.append("AND WM_Area_Type_ID = ?");
			parameters.add(warehouseAreaTypeId);
		}
	
	return  new Query(ctx, MWMArea.Table_Name,whereClause.toString(), trxName)
	.setOnlyActiveRecords(true)
	.setParameters(parameters)
	.list();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -3526627130289068011L;
	/**	Logger	**/
	private static CLogger	s_log = CLogger.getCLogger (MWMArea.class);
	
	/**************************************************************************
	 * 	Warehouse Area
	 *	@param ctx context
	 *	@param WM_Rue_ID  
	 *	@param trxName transaction name 
	 */
	public MWMArea (Properties ctx, int WM_Area_ID, String trxName)
	{
		super (ctx, WM_Area_ID, trxName);
		if (WM_Area_ID == 0)
		{
		}
	}

	/**
	 * 	Warehouse Area
	 *	@param ctx context
	 *	@param WM_Area_ID Cahs Flow ID
	 */
	public MWMArea (Properties ctx, int WM_Area_ID)
	{
		this (ctx, WM_Area_ID, null);
	}

	
	
	/**
	 *  Load Constructor
	 *  @param ctx context
	 *  @param rs result set record
	 *	@param trxName transaction
	 */
	public MWMArea (Properties ctx, ResultSet rs, String trxName)
	{
		super(ctx, rs, trxName);
	}	//	MAsset

	/**
	 * 	String representation
	 *	@return info
	 */
	@Override
	public String toString ()
	{
		StringBuffer sb = new StringBuffer ("MWMArea[")
			.append (get_ID ())
			.append ("-")
			.append (getName())
			.append ("]");
		return sb.toString ();
	}	//	toString

	public List <MWMSection> getBySectionType(int warehouseSectionTypeId)
	{
		final StringBuffer whereClause = new StringBuffer(MWMSection.COLUMNNAME_WM_Area_ID + "=? ");
		ArrayList<Object> parameters = new ArrayList();
		parameters.add(getWM_Area_ID());
		if( warehouseSectionTypeId > 0)
		{
			whereClause.append("AND ");
			whereClause.append(MWMSection.COLUMNNAME_WM_Section_Type_ID + "=?");
			parameters.add(warehouseSectionTypeId);
		}
		return new Query(getCtx(), MWMSection.Table_Name, whereClause.toString(), get_TrxName())
				.setClient_ID()
				.setOnlyActiveRecords(true)
				.setParameters(parameters)
				.list();
	}
}	
