package de.sub.goobi.Persistence.apache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.dbutils.ResultSetHandler;

public class DbUtils {

	public static SimpleDateFormat sdfShowDateTime = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");

	public static ResultSetHandler<List<StepObject>> resultSetToStepObjectListHandler = new ResultSetHandler<List<StepObject>>() {
		@Override
		public List<StepObject> handle(ResultSet rs) throws SQLException {
			List<StepObject> answer = new ArrayList<StepObject>();

			while (rs.next()) {
				StepObject o = parseStepObject(rs);
				if (o != null) {
					answer.add(o);
				}
			}
			return answer;
		}
	};

	public static ResultSetHandler<StepObject> resultSetToStepObjectHandler = new ResultSetHandler<StepObject>() {
		@Override
		public StepObject handle(ResultSet rs) throws SQLException {
			StepObject answer = null;

			if (rs.next()) {
				answer = parseStepObject(rs);

			}
			return answer;
		}
	};

	public static StepObject parseStepObject(ResultSet rs) throws SQLException {
		StepObject so = null;

		if (rs != null) {
			int id = rs.getInt("SchritteID");
			String title = rs.getString("Titel");
			int reihenfolge = rs.getInt("Reihenfolge");
			int bearbeitungsstatus = rs.getInt("bearbeitungsstatus");

			Date bearbeitungszeitpunkt = rs.getTimestamp("bearbeitungszeitpunkt");
			Date bearbeitungsbeginn = rs.getTimestamp("bearbeitungsbeginn");
			Date bearbeitungsende = rs.getTimestamp("bearbeitungsende");
//			try {
//				
//				bearbeitungszeitpunkt = sdfShowDateTime.parse(rs.getString("bearbeitungszeitpunkt"));
//				if (rs.getString("bearbeitungsbeginn") != null) {
//					bearbeitungsbeginn = sdfShowDateTime.parse(rs.getString("bearbeitungsbeginn"));
//				}
//				bearbeitungsende = sdfShowDateTime.parse(rs.getString("bearbeitungsende"));
//			} catch (ParseException e) {
//				// TODO: handle exception
//			}
			int bearbeitungsbenutzer = rs.getInt("BearbeitungsBenutzerID");
			int editType = rs.getInt("edittype");
			boolean typAutomatisch = rs.getBoolean("typAutomatisch");
			so = new StepObject(id, title, reihenfolge, bearbeitungsstatus, bearbeitungszeitpunkt, bearbeitungsbeginn, bearbeitungsende,
					bearbeitungsbenutzer, editType, typAutomatisch);
		}

		return so;
	}
}
