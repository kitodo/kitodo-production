package de.sub.goobi.Persistence.apache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.dbutils.ResultSetHandler;

import de.sub.goobi.Beans.Regelsatz;

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

	public static ResultSetHandler<Regelsatz> resultSetToRulesetHandler = new ResultSetHandler<Regelsatz>() {
		@Override
		public Regelsatz handle(ResultSet rs) throws SQLException {
			if (rs.next()) {
				int id = rs.getInt("MetadatenKonfigurationID");
				String title = rs.getString("Titel");
				String file = rs.getString("Datei");
				boolean order = rs.getBoolean("orderMetadataByRuleset");
				Regelsatz r = new Regelsatz();
				r.setId(id);
				r.setTitel(title);
				r.setDatei(file);
				r.setOrderMetadataByRuleset(order);
				return r;
			}
			return null;
		}
	};

	public static ResultSetHandler<List<Property>> resultSetToProcessPropertyListHandler = new ResultSetHandler<List<Property>>() {
		@Override
		public List<Property> handle(ResultSet rs) throws SQLException {
			List<Property> answer = new ArrayList<Property>();
			while (rs.next()) {
				int id =  rs.getInt("prozesseeigenschaftenID");
				String title = rs.getString("Titel");
				String value = rs.getString("Wert");
				boolean isObligatorisch = rs.getBoolean("IstObligatorisch");
				int datentypenID = rs.getInt("DatentypenID");
				String auswahl = rs.getString("Auswahl");
				Date creationDate = rs.getTimestamp("creationDate");
				int container = rs.getInt("container");
				Property prop = new Property(id, title, value, isObligatorisch, datentypenID, auswahl, creationDate, container);
				answer.add(prop);	
			}
			return answer;
		}
	};
	
	
	public static ResultSetHandler<List<Property>> resultSetToTemplatePropertyListHandler = new ResultSetHandler<List<Property>>() {
		@Override
		public List<Property> handle(ResultSet rs) throws SQLException {
			List<Property> answer = new ArrayList<Property>();
			while (rs.next()) {
				int id =  rs.getInt("vorlageneigenschaftenID");
				String title = rs.getString("Titel");
				String value = rs.getString("Wert");
				boolean isObligatorisch = rs.getBoolean("IstObligatorisch");
				int datentypenID = rs.getInt("DatentypenID");
				String auswahl = rs.getString("Auswahl");
				Date creationDate = rs.getTimestamp("creationDate");
				int container = rs.getInt("container");
				Property prop = new Property(id, title, value, isObligatorisch, datentypenID, auswahl, creationDate, container);
				answer.add(prop);	
			}
			return answer;
		}
	};
	
	public static ResultSetHandler<List<Property>> resultSetToProductPropertyListHandler = new ResultSetHandler<List<Property>>() {
		@Override
		public List<Property> handle(ResultSet rs) throws SQLException {
			List<Property> answer = new ArrayList<Property>();
			while (rs.next()) {
				int id =  rs.getInt("werkstueckeeigenschaftenID");
				String title = rs.getString("Titel");
				String value = rs.getString("Wert");
				boolean isObligatorisch = rs.getBoolean("IstObligatorisch");
				int datentypenID = rs.getInt("DatentypenID");
				String auswahl = rs.getString("Auswahl");
				Date creationDate = rs.getTimestamp("creationDate");
				int container = rs.getInt("container");
				Property prop = new Property(id, title, value, isObligatorisch, datentypenID, auswahl, creationDate, container);
				answer.add(prop);	
			}
			return answer;
		}
	};
	
	public static ResultSetHandler<ProcessObject> resultSetToProcessHandler = new ResultSetHandler<ProcessObject>() {

		@Override
		public ProcessObject handle(ResultSet rs) throws SQLException {
			if (rs.next()) {
				int processId = rs.getInt("ProzesseID");
				String title = rs.getString("Titel");
				String ausgabename = rs.getString("ausgabename");
				boolean isTemplate = rs.getBoolean("IstTemplate");
				boolean swappedOut = rs.getBoolean("swappedOut");
				boolean inAuswahllisteAnzeigen = rs.getBoolean("inAuswahllisteAnzeigen");
				String sortHelperStatus = rs.getString("sortHelperStatus");
				int sortHelperImages = rs.getInt("sortHelperImages");
				int sortHelperArticles = rs.getInt("sortHelperArticles");
				Date erstellungsdatum = rs.getTimestamp("erstellungsdatum");
				int projekteID = rs.getInt("ProjekteID");
				int metadatenKonfigurationID = rs.getInt("MetadatenKonfigurationID");
				int sortHelperDocstructs = rs.getInt("sortHelperDocstructs");
				int sortHelperMetadata = rs.getInt("sortHelperMetadata");
				String wikifield = rs.getString("wikifield");
				int batchID = rs.getInt("batchID");
				ProcessObject po = new ProcessObject(processId, title, ausgabename, isTemplate, swappedOut, inAuswahllisteAnzeigen, sortHelperStatus,
						sortHelperImages, sortHelperArticles, erstellungsdatum, projekteID, metadatenKonfigurationID, sortHelperDocstructs,
						sortHelperMetadata, wikifield, batchID);
				return po;
			}
			return null;
		}

	};

	public static ResultSetHandler<List<String>> resultSetToScriptsHandler = new ResultSetHandler<List<String>>() {
		@Override
		public List<String> handle(ResultSet rs) throws SQLException {
			List<String> answer = new ArrayList<String>();
			if (rs.next()) {
				if (rs.getString("typAutomatischScriptpfad") != null && rs.getString("typAutomatischScriptpfad").length() > 0) {
					answer.add(rs.getString("typAutomatischScriptpfad"));
				}
				if (rs.getString("typAutomatischScriptpfad2") != null && rs.getString("typAutomatischScriptpfad2").length() > 0) {
					answer.add(rs.getString("typAutomatischScriptpfad2"));
				}
				if (rs.getString("typAutomatischScriptpfad3") != null && rs.getString("typAutomatischScriptpfad3").length() > 0) {
					answer.add(rs.getString("typAutomatischScriptpfad3"));
				}
				if (rs.getString("typAutomatischScriptpfad4") != null && rs.getString("typAutomatischScriptpfad4").length() > 0) {
					answer.add(rs.getString("typAutomatischScriptpfad4"));
				}
				if (rs.getString("typAutomatischScriptpfad5") != null && rs.getString("typAutomatischScriptpfad5").length() > 0) {
					answer.add(rs.getString("typAutomatischScriptpfad5"));
				}
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
			int processId = rs.getInt("ProzesseID");
			int bearbeitungsbenutzer = rs.getInt("BearbeitungsBenutzerID");
			int editType = rs.getInt("edittype");
			boolean typExport = rs.getBoolean("typExportDMS");
			boolean typAutomatisch = rs.getBoolean("typAutomatisch");
			so = new StepObject(id, title, reihenfolge, bearbeitungsstatus, bearbeitungszeitpunkt, bearbeitungsbeginn, bearbeitungsende,
					bearbeitungsbenutzer, editType, typExport, typAutomatisch, processId);
		}

		return so;
	}
}
