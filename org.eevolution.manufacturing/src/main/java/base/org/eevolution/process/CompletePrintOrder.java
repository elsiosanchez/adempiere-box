/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
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
 * Copyright (C) 2003-2007 e-Evolution,SC. All Rights Reserved.               *
 * Contributor(s): Victor Perez www.e-evolution.com                           *
 *                 Teo Sarca, www.arhipac.ro                                  *
 *****************************************************************************/
package org.eevolution.process;


import java.util.logging.Level;

import org.adempiere.exceptions.AdempiereException;
import org.adempiere.exceptions.FillMandatoryException;
import org.compiere.model.MTable;
import org.compiere.model.PO;
import org.compiere.print.MPrintFormat;
import org.compiere.process.ClientProcess;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.eevolution.model.I_PP_Order;
import org.eevolution.model.MPPOrder;

/**
 * Complete & Print Manufacturing Order
 * @author victor.perez@e-evolution.com
 * @author Teo Sarca, www.arhipac.ro
 * @author Yamel Senih, ysenih@erpya.com, ERPCyA http://www.erpya.com
 */
public class CompletePrintOrder extends SvrProcess
implements ClientProcess
{
	/** The Order */
	private int p_PP_Order_ID = 0;
	private boolean p_IsPrintPickList = false;
	private boolean p_IsPrintWorkflow = false;
	@SuppressWarnings("unused")
	private boolean p_IsPrintPackList = false; // for future use
	private boolean p_IsComplete = false;

	/**
	 * Prepare - e.g., get Parameters.
	 */
	protected void prepare()
	{
		for (ProcessInfoParameter para : getParameter())
		{
			String name = para.getParameterName();
			if (para.getParameter() == null)
				;
			else if (name.equals("PP_Order_ID"))
				p_PP_Order_ID = para.getParameterAsInt();
			else if (name.equals("IsPrintPickList"))
				p_IsPrintPickList = para.getParameterAsBoolean();
			else if (name.equals("IsPrintWorkflow"))
				p_IsPrintWorkflow = para.getParameterAsBoolean();
			else if (name.equals("IsPrintPackingList"))
				p_IsPrintPackList = para.getParameterAsBoolean();
			else if (name.equals("IsComplete"))
				p_IsComplete = para.getParameterAsBoolean();
			else
				log.log(Level.SEVERE, "prepare - Unknown Parameter: " + name);
		}
	} // prepare

	/**
	 * Perform process.
	 * 
	 * @return Message (clear text)
	 * @throws Exception
	 *             if not successful
	 */
	protected String doIt() throws Exception
	{

		if (p_PP_Order_ID == 0)
		{
			throw new FillMandatoryException(MPPOrder.COLUMNNAME_PP_Order_ID);
		}

		if (p_IsComplete)
		{
			MPPOrder order = new MPPOrder(getCtx(), p_PP_Order_ID, get_TrxName());
			if (!order.isAvailable())
			{
				throw new AdempiereException("@NoQtyAvailable@");
			}
			//
			// Process document
			order.setDocAction(MPPOrder.DOCACTION_Complete);
			boolean ok = order.processIt(MPPOrder.DOCACTION_Complete);
			order.saveEx();
			if (!ok)
			{
				throw new AdempiereException(order.getProcessMsg());
			}
			//
			// Document Status should be completed
			if (!MPPOrder.DOCSTATUS_Completed.equals(order.getDocStatus()))
			{
				throw new AdempiereException(order.getProcessMsg());
			}
		}
		MTable table = MTable.get(getCtx(), I_PP_Order.Table_Name);
		PO entity = table.getPO(p_PP_Order_ID, get_TrxName());
		if(entity == null) {
			addLog("@NotFound@ @PP_Order_ID@");
		} else {
			if (p_IsPrintPickList) {
				printDocument(entity, getPrintFormatId("Manufacturing_Order_BOM_Header ** TEMPLATE **", "PP_Order_BOM_Header_v"), false);
			}
			if (p_IsPrintPackList) {
				printDocument(entity, getPrintFormatId("Manufacturing_Order_BOM_Header_Packing ** TEMPLATE **", "PP_Order_BOM_Header_v"), false);
			}
			if (p_IsPrintWorkflow) {
				printDocument(entity, getPrintFormatId("Manufacturing_Order_Workflow_Header ** TEMPLATE **", "PP_Order_Workflow_Header_v"), false);
			}
		}
		return "@OK@";
	} // doIt
	
	private int getPrintFormatId(String formatName, String tableName) {
		return MPrintFormat.getPrintFormat_ID(formatName, MTable.getTable_ID(tableName), getAD_Client_ID());
	}
} // CompletePrintOrder
