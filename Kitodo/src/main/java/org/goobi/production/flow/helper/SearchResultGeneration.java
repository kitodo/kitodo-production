package org.goobi.production.flow.helper;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.kitodo.org
 *     		- https://github.com/goobi/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */

import de.sub.goobi.beans.Prozess;
import de.sub.goobi.beans.Prozesseigenschaft;
import de.sub.goobi.helper.Helper;

import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import org.goobi.production.flow.statistics.hibernate.IEvaluableFilter;
import org.goobi.production.flow.statistics.hibernate.UserDefinedFilter;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

public class SearchResultGeneration {

	private String filter = "";
	private boolean showClosedProcesses = false;
	private boolean showArchivedProjects = false;

	/**
	 * @param filter add description
	 * @param showClosedProcesses add description
	 * @param showArchivedProjects add description
	 */
	public SearchResultGeneration(String filter, boolean showClosedProcesses, boolean showArchivedProjects) {
		this.filter = filter;
		this.showClosedProcesses = showClosedProcesses;
		this.showArchivedProjects = showArchivedProjects;
	}

	/**
	 * @return add description
	 */
	@SuppressWarnings("deprecation")
	public HSSFWorkbook getResult() {
		IEvaluableFilter myFilteredDataSource = new UserDefinedFilter(this.filter);
		Criteria crit = myFilteredDataSource.getCriteria();
		crit.add(Restrictions.eq("istTemplate", Boolean.FALSE));
		if (!this.showClosedProcesses) {
			crit.add(Restrictions.not(Restrictions.eq("sortHelperStatus", "100000000")));
		}
		if (!this.showArchivedProjects) {
			crit.createCriteria("projekt", "proj");
			crit.add(Restrictions.not(Restrictions.eq("proj.projectIsArchived", true)));
		} else {
			crit.createCriteria("projekt", "proj");
		}
		Order order = Order.asc("titel");
		crit.addOrder(order);
		@SuppressWarnings("unchecked")
		List<Prozess> pl = crit.setFirstResult(0).setMaxResults(Integer.MAX_VALUE).list();

		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("Search results");

		HSSFRow title = sheet.createRow(0);
		HSSFCell titleCell1 = title.createCell(0);
		titleCell1.setCellValue(this.filter);
		HSSFCell titleCell2 = title.createCell(1);
		titleCell2.setCellValue("");
		HSSFCell titleCell3 = title.createCell(2);
		titleCell3.setCellValue("");
		HSSFCell titleCell4 = title.createCell(3);
		titleCell4.setCellValue("");
		HSSFCell titleCell5 = title.createCell(4);
		titleCell5.setCellValue("");
		HSSFCell titleCell6 = title.createCell(5);
		titleCell6.setCellValue("");

		HSSFCell titleCell7 = title.createCell(6);
		titleCell7.setCellValue("");
		HSSFCell titleCell8 = title.createCell(7);
		titleCell8.setCellValue("");

		HSSFCell titleCell9 = title.createCell(8);
		titleCell9.setCellValue("");

		HSSFRow row0 = sheet.createRow(1);
		HSSFCell headercell0 = row0.createCell(0);
		headercell0.setCellValue(Helper.getTranslation("title"));
		HSSFCell headercell1 = row0.createCell(1);
		headercell1.setCellValue(Helper.getTranslation("ID"));
		HSSFCell headercell2 = row0.createCell(2);
		headercell2.setCellValue(Helper.getTranslation("Datum"));
		HSSFCell headercell3 = row0.createCell(3);
		headercell3.setCellValue(Helper.getTranslation("CountImages"));
		HSSFCell headercell4 = row0.createCell(4);
		headercell4.setCellValue(Helper.getTranslation("CountMetadata"));
		HSSFCell headercell5 = row0.createCell(5);
		headercell5.setCellValue(Helper.getTranslation("Project"));
		HSSFCell headercell6 = row0.createCell(6);
		headercell6.setCellValue(Helper.getTranslation("Status"));

		HSSFCell headercell7 = row0.createCell(7);
		headercell7.setCellValue(Helper.getTranslation("AltRefNo"));

		HSSFCell headercell8 = row0.createCell(8);
		headercell8.setCellValue(Helper.getTranslation("b-number"));

		int rowcounter = 2;
		for (Prozess p : pl) {
			HSSFRow row = sheet.createRow(rowcounter);
			HSSFCell cell0 = row.createCell(0);
			cell0.setCellValue(p.getTitel());
			HSSFCell cell1 = row.createCell(1);
			cell1.setCellValue(p.getId());
			HSSFCell cell2 = row.createCell(2);
			cell2.setCellValue(p.getErstellungsdatum().toGMTString());
			HSSFCell cell3 = row.createCell(3);
			cell3.setCellValue(p.getSortHelperImages());
			HSSFCell cell4 = row.createCell(4);
			cell4.setCellValue(p.getSortHelperDocstructs());
			HSSFCell cell5 = row.createCell(5);
			cell5.setCellValue(p.getProjekt().getTitel());

			HSSFCell cell6 = row.createCell(6);

			cell6.setCellValue(p.getSortHelperStatus().substring(0, 3) + " / "
					+ p.getSortHelperStatus().substring(3, 6) + " / " + p.getSortHelperStatus().substring(6));
			HSSFCell cell7 = row.createCell(7);
			cell7.setCellValue("");
			HSSFCell cell8 = row.createCell(8);
			cell8.setCellValue("");
			if (p.getEigenschaftenList().size() > 0) {
				for (Prozesseigenschaft pe : p.getEigenschaftenList()) {
					if (pe.getTitel().equals("AltRefNo")) {
						cell7.setCellValue(pe.getWert());
					} else if (pe.getTitel().equals("b-number")) {
						cell8.setCellValue(pe.getWert());
					}
				}
			}

			rowcounter++;
		}

		return wb;
	}
}
