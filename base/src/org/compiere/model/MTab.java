/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2006 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
package org.compiere.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import org.adempiere.exceptions.FillMandatoryException;
import org.compiere.util.CCache;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;

/**
 *	Tab Model
 *	
 *  @author Jorg Janke
 *  @author victor.perez@e-evolution.com, e-Evolution
 * <li>RF [2826384] The Order and Included Columns should be to fill mandatory
 * <li>http://sourceforge.net/tracker/?func=detail&atid=879335&aid=2826384&group_id=176962
 *  @version $Id: MTab.java,v 1.2 2006/07/30 00:58:37 jjanke Exp $
 */
public class MTab extends X_AD_Tab
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4946144044358216142L;
	/**	Cache for parameters		*/
	private static CCache<String, List<MField>>	cacheASPFields = new CCache<String, List<MField>>(I_AD_Field.Table_Name, 20);
	/**	Cache						*/
	private static CCache<Integer, MTab>	s_cache	= new CCache<Integer, MTab>(Table_Name, 20);

	/**
	 * 	Standard Constructor
	 *	@param ctx context
	 *	@param AD_Tab_ID id
	 *	@param trxName transaction
	 */
	public MTab (Properties ctx, int AD_Tab_ID, String trxName)
	{
		super (ctx, AD_Tab_ID, trxName);
		if (AD_Tab_ID == 0)
		{
		//	setAD_Window_ID (0);
		//	setAD_Table_ID (0);
		//	setName (null);
			setEntityType (ENTITYTYPE_UserMaintained);	// U
			setHasTree (false);
			setIsReadOnly (false);
			setIsSingleRow (false);
			setIsSortTab (false);	// N
			setIsTranslationTab (false);
			setSeqNo (0);
			setTabLevel (0);
			setIsInsertRecord(true);
			setIsAdvancedTab(false);
		}
	}	//	M_Tab

	/**
	 * 	Load Constructor
	 *	@param ctx context
	 *	@param rs result set
	 *	@param trxName transaction
	 */
	public MTab (Properties ctx, ResultSet rs, String trxName)
	{
		super(ctx, rs, trxName);
	}	//	M_Tab

	/**
	 * 	Parent Constructor
	 *	@param parent parent
	 */
	public MTab (MWindow parent)
	{
		this (parent.getCtx(), 0, parent.get_TrxName());
		setClientOrg(parent);
		setAD_Window_ID(parent.getAD_Window_ID());
		setEntityType(parent.getEntityType());
	}	//	M_Tab

	/**
	 * 	Parent Constructor
	 *	@param parent parent
	 *	@param from copy from
	 */
	public MTab (MWindow parent, MTab from)
	{
		this (parent.getCtx(), 0, parent.get_TrxName());
		copyValues(from, this);
		setClientOrg(parent);
		setAD_Window_ID(parent.getAD_Window_ID());
		setEntityType(parent.getEntityType());
	}	//	M_Tab
	
	
	/**	The Fields						*/
	private MField[]		m_fields	= null;

	/**	Static Logger	*/
	private static CLogger	s_log	= CLogger.getCLogger (MTab.class);
	
	/**	Packages for Model Classes	*/
	/**
	 * 	Get Fields
	 *	@param reload reload data
	 *	@return array of lines
	 *	@param trxName transaction
	 */
	public MField[] getFields (boolean reload, String trxName)
	{
		if (m_fields != null && !reload)
			return m_fields;
		String sql = "SELECT * FROM AD_Field WHERE AD_Tab_ID=? ORDER BY SeqNo";
		ArrayList<MField> list = new ArrayList<MField>();
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql, trxName);
			pstmt.setInt (1, getAD_Tab_ID());
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())
				list.add (new MField (getCtx(), rs, trxName));
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, sql, e);
		}
		try
		{
			if (pstmt != null)
				pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			pstmt = null;
		}
		//
		m_fields = new MField[list.size ()];
		list.toArray (m_fields);
		return m_fields;
	}	//	getFields

	/**
	 * 	Before Save
	 *	@param newRecord new
	 *	@return true
	 */
	protected boolean beforeSave (boolean newRecord)
	{
	//	UPDATE AD_Tab SET IsInsertRecord='N' WHERE IsInsertRecord='Y' AND IsReadOnly='Y'
		if (isReadOnly() && isInsertRecord())
			setIsInsertRecord(false);
		//RF[2826384]
		if(isSortTab())
		{
			if(getAD_ColumnSortOrder_ID() == 0)
			{
				throw new FillMandatoryException("AD_ColumnSortOrder_ID");	
			}			
		}
		return true;
	}
	
	// begin e-evolution vpj-cd
	/**
	 * 	get Tab ID
	 *	@param String AD_Window_ID
	 *	@param String TabName
	 *	@return int retValue
	 */
	public static int getTab_ID(int AD_Window_ID , String TabName) {
		int retValue = 0;
		String SQL = "SELECT AD_Tab_ID FROM AD_Tab WHERE AD_Window_ID= ?  AND Name = ?";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(SQL, null);
			pstmt.setInt(1, AD_Window_ID);
			pstmt.setString(2, TabName);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			s_log.log(Level.SEVERE, SQL, e);
			retValue = -1;
		}
		return retValue;
	}
	//end vpj-cd e-evolution
	
	/**
	 * Duplicate Tab
	 * @return
	 */
	public MTab getDuplicated() {
		try {
			return (MTab) super.clone();
		} catch (CloneNotSupportedException e) {
			log.warning("Error " + e.getLocalizedMessage());
		}
		//	Default
		return null;
	}
	
	@Override
	public String toString() {
		return getAD_Tab_ID() + " " + getName() + " - " + isActive();
	}
	
	/** Get Tab from Cache
	 *	@param ctx context
	 *	@param tabId id
	 *	@return MProcess
	 */
	public static MTab get (Properties ctx, int tabId) {
		Integer key = Integer.valueOf(tabId);
		MTab retValue = (MTab) s_cache.get (key);
		if (retValue != null)
			return retValue;
		retValue = new MTab (ctx, tabId, null);
		if (retValue.get_ID () != 0)
			s_cache.put (key, retValue);
		return retValue;
	}	//	get
	
	/**
	 * Get Tabs from ASP
	 * @return
	 */
	public List<MField> getASPFields() {
		MClient client = MClient.get(getCtx());
		String key = getAD_Tab_ID() + "|" + client.getAD_Client_ID() + "|" + Env.getAD_Language(getCtx());
		List<MField> retValue = cacheASPFields.get (key);
		if (retValue != null) {
			return retValue;
		}
		StringBuffer whereClause = new StringBuffer(COLUMNNAME_AD_Tab_ID + " = ?");
		if (client.isUseASP()) {
			String aSPFilter =
					" AND ((AD_Field_ID IN ( "
					 // ASP subscribed fields for client
					 + "              SELECT f.AD_Field_ID "
					 + "                FROM ASP_Field f, ASP_Tab t, ASP_Window w, ASP_Level l, ASP_ClientLevel cl "
					 + "               WHERE w.ASP_Level_ID = l.ASP_Level_ID "
					 + "                 AND cl.AD_Client_ID = " + client.getAD_Client_ID()
					 + "                 AND cl.ASP_Level_ID = l.ASP_Level_ID "
					 + "                 AND f.ASP_Tab_ID = t.ASP_Tab_ID "
					 + "                 AND t.ASP_Window_ID = w.ASP_Window_ID "
					 + "                 AND f.IsActive = 'Y' "
					 + "                 AND t.IsActive = 'Y' "
					 + "                 AND w.IsActive = 'Y' "
					 + "                 AND l.IsActive = 'Y' "
					 + "                 AND cl.IsActive = 'Y' "
					 + "                 AND f.ASP_Status = 'S') "
					 + "        OR AD_Tab_ID IN ( "
					 // ASP subscribed fields for client
					 + "              SELECT t.AD_Tab_ID "
					 + "                FROM ASP_Tab t, ASP_Window w, ASP_Level l, ASP_ClientLevel cl "
					 + "               WHERE w.ASP_Level_ID = l.ASP_Level_ID "
					 + "                 AND cl.AD_Client_ID = " + client.getAD_Client_ID()
					 + "                 AND cl.ASP_Level_ID = l.ASP_Level_ID "
					 + "                 AND t.ASP_Window_ID = w.ASP_Window_ID "
					 + "                 AND t.IsActive = 'Y' "
					 + "                 AND w.IsActive = 'Y' "
					 + "                 AND l.IsActive = 'Y' "
					 + "                 AND cl.IsActive = 'Y' "
					 + "                 AND t.AllFields = 'Y' "
					 + "                 AND t.ASP_Status = 'S') "
					 + "        OR AD_Field_ID IN ( "
					 // ASP show exceptions for client
					 + "              SELECT AD_Field_ID "
					 + "                FROM ASP_ClientException ce "
					 + "               WHERE ce.AD_Client_ID = " + client.getAD_Client_ID()
					 + "                 AND ce.IsActive = 'Y' "
					 + "                 AND ce.AD_Field_ID IS NOT NULL "
					 + "                 AND ce.ASP_Status = 'S') "
					 + "       ) "
					 + "   AND AD_Field_ID NOT IN ( "
					 // minus ASP hide exceptions for client
					 + "          SELECT AD_Field_ID "
					 + "            FROM ASP_ClientException ce "
					 + "           WHERE ce.AD_Client_ID = " + client.getAD_Client_ID()
					 + "             AND ce.IsActive = 'Y' "
					 + "             AND ce.AD_Field_ID IS NOT NULL "
					 + "             AND ce.ASP_Status = 'H')" //	Hide
					//	Just Customization
					 + " OR EXISTS(SELECT 1 FROM ASP_Level l "
					 + "					INNER JOIN ASP_ClientLevel cl ON(cl.ASP_Level_ID = l.ASP_Level_ID) "
					 + "				WHERE cl.AD_Client_ID = " + client.getAD_Client_ID()
					 + "				AND l.IsActive = 'Y' "
					 + "				AND cl.IsActive = 'Y' "
					 + "				AND l.Type = 'C') "	//	Show
					 + ")";
			
			whereClause.append(aSPFilter);
		}
		//	Get from query
		retValue = new Query(getCtx(),I_AD_Field.Table_Name, whereClause.toString(), get_TrxName())
				.setParameters(getAD_Tab_ID())
				.setOrderBy(I_AD_Field.COLUMNNAME_SeqNo)
				.list();
		if (retValue != null
				&& retValue.size() > 0) {
			cacheASPFields.put(key, retValue);
		}
		//	Default Return
		return retValue;
	}
	
}	//	M_Tab
