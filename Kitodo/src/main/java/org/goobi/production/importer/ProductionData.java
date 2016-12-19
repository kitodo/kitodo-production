/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.goobi.production.importer;

import java.sql.Date;

public class ProductionData {
	/**
	 * simple POJO class
	 * @author Robert Sehr
	 */
	private Integer WERKNR;
	private String WERKATS;
	private Integer NAMEWV;
	private String WERKPPNANALOG;
	private String WERKPPNDIGITAL;
	private String AUFTRAGSNUMMER;
	private String SCANPFAD;
	private String WERKSIGNATUR;
	private String XSLSHEET;
	private Date DATUMAUFNAHMEWERK;
	private String BEMERKUNG;
	private String KOMMENTAR;
	private Integer WERKSCANSEITEN;
	private Integer WERKMB;
	private Date WERKSCANDATUM;
	private Date WERKQKONTROLLDATUM;
	private String WERKQKONTROLLOK;
	private String WERKKONTROLLNR;
	private String XLSSTRUKTURGEPRUEFT;
	private String XLSMETADATENEINGETRAGEN;
	private Integer METADATENZUMDIENSTLEISTER;
	private String SCANNERTYP;
	private String BITONALIMAGENACHBEARBEITUNG;
	private String GRAUIMAGENACHBEARBEITUNG;
	private String FARBEIMAGENACHBEARBEITUNG;
	private String DRUCKQUALITAET;
	private String WERKBATCH;
	private Integer FARBGRAUABB;
	private String DOWNLOADERSTELLT;
	private String DIGDOC2XML;
	private String MERGER;
	private Integer WERKZURUECKAN;
	private String WERKZURUECKWER;
	private Date WERKZURUECKWANN;
	private String WERKPROJEKT;
	private String CDSICHERUNG;
	private Integer IMPORT;
	private Integer RÜCKLAUFZAEHLERIMPORT;
	private String FEHLERKOMMENTAR;
	private String MAARCHIV;
	private String SEITENFORMATPROBLEM;
	private Integer BEMERKUNG2;
	private Integer AUFTRAGGEBER;
	private Date Zeitpunkt;
	private Date ImageNachbearbBitonalDatum;
	private String ImageNachbearbBitonalPerson;
	private Date ImageNachbearbGrauDatum;
	private String ImageNachbearbGrauPerson;
	private Date ImageNachbearbFarbeDatum;
	private String ImageNachbearbFarbePerson;
	private Date ImportDatum;
	private Boolean Patennennung;
	private String Patenname;
	private Boolean StempelGesetzt;
	private Boolean xmlTag;
	private String otrsID;
	private Boolean versandErfolgt;
	private Boolean pdfErstellt;
	private Integer istNachscan;
	private String NachscannAufnahmeWann;
	private String NachscannAufnahmeDurch;
	private Date NachscannRueckgabeWann;
	private String NachscannRueckgabeDurch;
	private Integer NachscannScanner;

	public Integer getRÜCKLAUFZAEHLERIMPORT() {
		return RÜCKLAUFZAEHLERIMPORT;
	}

	public void setRÜCKLAUFZAEHLERIMPORT(Integer rÜCKLAUFZAEHLERIMPORT) {
		RÜCKLAUFZAEHLERIMPORT = rÜCKLAUFZAEHLERIMPORT;
	}

	public String getFEHLERKOMMENTAR() {
		return FEHLERKOMMENTAR;
	}

	public void setFEHLERKOMMENTAR(String fEHLERKOMMENTAR) {
		FEHLERKOMMENTAR = fEHLERKOMMENTAR;
	}

	public String getMAARCHIV() {
		return MAARCHIV;
	}

	public void setMAARCHIV(String mAARCHIV) {
		MAARCHIV = mAARCHIV;
	}

	public String getSEITENFORMATPROBLEM() {
		return SEITENFORMATPROBLEM;
	}

	public void setSEITENFORMATPROBLEM(String sEITENFORMATPROBLEM) {
		SEITENFORMATPROBLEM = sEITENFORMATPROBLEM;
	}

	public Integer getBEMERKUNG2() {
		return BEMERKUNG2;
	}

	public void setBEMERKUNG2(Integer bEMERKUNG2) {
		BEMERKUNG2 = bEMERKUNG2;
	}

	public Integer getAUFTRAGGEBER() {
		return AUFTRAGGEBER;
	}

	public void setAUFTRAGGEBER(Integer aUFTRAGGEBER) {
		AUFTRAGGEBER = aUFTRAGGEBER;
	}

	public Date getZeitpunkt() {
		return Zeitpunkt;
	}

	public void setZeitpunkt(Date zeitpunkt) {
		Zeitpunkt = zeitpunkt;
	}

	public Date getImageNachbearbBitonalDatum() {
		return ImageNachbearbBitonalDatum;
	}

	public void setImageNachbearbBitonalDatum(Date imageNachbearbBitonalDatum) {
		ImageNachbearbBitonalDatum = imageNachbearbBitonalDatum;
	}

	public String getImageNachbearbBitonalPerson() {
		return ImageNachbearbBitonalPerson;
	}

	public void setImageNachbearbBitonalPerson(String imageNachbearbBitonalPerson) {
		ImageNachbearbBitonalPerson = imageNachbearbBitonalPerson;
	}

	public Date getImageNachbearbGrauDatum() {
		return ImageNachbearbGrauDatum;
	}

	public void setImageNachbearbGrauDatum(Date imageNachbearbGrauDatum) {
		ImageNachbearbGrauDatum = imageNachbearbGrauDatum;
	}

	public String getImageNachbearbGrauPerson() {
		return ImageNachbearbGrauPerson;
	}

	public void setImageNachbearbGrauPerson(String imageNachbearbGrauPerson) {
		ImageNachbearbGrauPerson = imageNachbearbGrauPerson;
	}

	public Date getImageNachbearbFarbeDatum() {
		return ImageNachbearbFarbeDatum;
	}

	public void setImageNachbearbFarbeDatum(Date imageNachbearbFarbeDatum) {
		ImageNachbearbFarbeDatum = imageNachbearbFarbeDatum;
	}

	public String getImageNachbearbFarbePerson() {
		return ImageNachbearbFarbePerson;
	}

	public void setImageNachbearbFarbePerson(String imageNachbearbFarbePerson) {
		ImageNachbearbFarbePerson = imageNachbearbFarbePerson;
	}

	public Date getImportDatum() {
		return ImportDatum;
	}

	public void setImportDatum(Date importDatum) {
		ImportDatum = importDatum;
	}

	public Boolean getPatennennung() {
		return Patennennung;
	}

	public void setPatennennung(Boolean patennennung) {
		Patennennung = patennennung;
	}

	public String getPatenname() {
		return Patenname;
	}

	public void setPatenname(String patenname) {
		Patenname = patenname;
	}

	public Boolean getStempelGesetzt() {
		return StempelGesetzt;
	}

	public void setStempelGesetzt(Boolean stempelGesetzt) {
		StempelGesetzt = stempelGesetzt;
	}

	public Boolean getXmlTag() {
		return xmlTag;
	}

	public void setXmlTag(Boolean xmlTag) {
		this.xmlTag = xmlTag;
	}

	public String getOtrsID() {
		return otrsID;
	}

	public void setOtrsID(String otrsID) {
		this.otrsID = otrsID;
	}

	public Boolean getVersandErfolgt() {
		return versandErfolgt;
	}

	public void setVersandErfolgt(Boolean versandErfolgt) {
		this.versandErfolgt = versandErfolgt;
	}

	public Boolean getPdfErstellt() {
		return pdfErstellt;
	}

	public void setPdfErstellt(Boolean pdfErstellt) {
		this.pdfErstellt = pdfErstellt;
	}

	public Integer getIstNachscan() {
		return istNachscan;
	}

	public void setIstNachscan(Integer istNachscan) {
		this.istNachscan = istNachscan;
	}

	public String getNachscannAufnahmeWann() {
		return NachscannAufnahmeWann;
	}

	public void setNachscannAufnahmeWann(String timestamp) {
		NachscannAufnahmeWann = timestamp;
	}

	public String getNachscannAufnahmeDurch() {
		return NachscannAufnahmeDurch;
	}

	public void setNachscannAufnahmeDurch(String nachscannAufnahmeDurch) {
		NachscannAufnahmeDurch = nachscannAufnahmeDurch;
	}

	public Date getNachscannRueckgabeWann() {
		return NachscannRueckgabeWann;
	}

	public void setNachscannRueckgabeWann(Date nachscannRueckgabeWann) {
		NachscannRueckgabeWann = nachscannRueckgabeWann;
	}

	public String getNachscannRueckgabeDurch() {
		return NachscannRueckgabeDurch;
	}

	public void setNachscannRueckgabeDurch(String nachscannRueckgabeDurch) {
		NachscannRueckgabeDurch = nachscannRueckgabeDurch;
	}

	public Integer getNachscannScanner() {
		return NachscannScanner;
	}

	public void setNachscannScanner(Integer nachscannScanner) {
		NachscannScanner = nachscannScanner;
	}

	public void setWERKZURUECKWANN(Date wERKZURUECKWANN) {
		WERKZURUECKWANN = wERKZURUECKWANN;
	}

	public String getWERKATS() {
		return WERKATS;
	}

	public void setWERKATS(String wERKATS) {
		WERKATS = wERKATS;
	}

	public Integer getNAMEWV() {
		return NAMEWV;
	}

	public void setNAMEWV(Integer nAMEWV) {
		NAMEWV = nAMEWV;
	}

	public String getWERKPPNANALOG() {
		return WERKPPNANALOG;
	}

	public void setWERKPPNANALOG(String wERKPPNANALOG) {
		WERKPPNANALOG = wERKPPNANALOG;
	}

	public String getWERKPPNDIGITAL() {
		return WERKPPNDIGITAL;
	}

	public void setWERKPPNDIGITAL(String wERKPPNDIGITAL) {
		WERKPPNDIGITAL = wERKPPNDIGITAL;
	}

	public String getAUFTRAGSNUMMER() {
		return AUFTRAGSNUMMER;
	}

	public void setAUFTRAGSNUMMER(String aUFTRAGSNUMMER) {
		AUFTRAGSNUMMER = aUFTRAGSNUMMER;
	}

	public String getSCANPFAD() {
		return SCANPFAD;
	}

	public void setSCANPFAD(String sCANPFAD) {
		SCANPFAD = sCANPFAD;
	}

	public String getWERKSIGNATUR() {
		return WERKSIGNATUR;
	}

	public void setWERKSIGNATUR(String wERKSIGNATUR) {
		WERKSIGNATUR = wERKSIGNATUR;
	}

	public String getXSLSHEET() {
		return XSLSHEET;
	}

	public void setXSLSHEET(String xSLSHEET) {
		XSLSHEET = xSLSHEET;
	}

	public Date getDATUMAUFNAHMEWERK() {
		return DATUMAUFNAHMEWERK;
	}

	public void setDATUMAUFNAHMEWERK(Date dATUMAUFNAHMEWERK) {
		DATUMAUFNAHMEWERK = dATUMAUFNAHMEWERK;
	}

	public String getBEMERKUNG() {
		return BEMERKUNG;
	}

	public void setBEMERKUNG(String bEMERKUNG) {
		BEMERKUNG = bEMERKUNG;
	}

	public String getKOMMENTAR() {
		return KOMMENTAR;
	}

	public void setKOMMENTAR(String kOMMENTAR) {
		KOMMENTAR = kOMMENTAR;
	}

	public Integer getWERKSCANSEITEN() {
		return WERKSCANSEITEN;
	}

	public void setWERKSCANSEITEN(Integer wERKSCANSEITEN) {
		WERKSCANSEITEN = wERKSCANSEITEN;
	}

	public Integer getWERKMB() {
		return WERKMB;
	}

	public void setWERKMB(Integer wERKMB) {
		WERKMB = wERKMB;
	}

	public Date getWERKSCANDATUM() {
		return WERKSCANDATUM;
	}

	public void setWERKSCANDATUM(Date wERKSCANDATUM) {
		WERKSCANDATUM = wERKSCANDATUM;
	}

	public Date getWERKQKONTROLLDATUM() {
		return WERKQKONTROLLDATUM;
	}

	public void setWERKQKONTROLLDATUM(Date wERKQKONTROLLDATUM) {
		WERKQKONTROLLDATUM = wERKQKONTROLLDATUM;
	}

	public String getWERKQKONTROLLOK() {
		return WERKQKONTROLLOK;
	}

	public void setWERKQKONTROLLOK(String wERKQKONTROLLOK) {
		WERKQKONTROLLOK = wERKQKONTROLLOK;
	}

	public String getWERKKONTROLLNR() {
		return WERKKONTROLLNR;
	}

	public void setWERKKONTROLLNR(String wERKKONTROLLNR) {
		WERKKONTROLLNR = wERKKONTROLLNR;
	}

	public String getXLSSTRUKTURGEPRUEFT() {
		return XLSSTRUKTURGEPRUEFT;
	}

	public void setXLSSTRUKTURGEPRUEFT(String xLSSTRUKTURGEPRUEFT) {
		XLSSTRUKTURGEPRUEFT = xLSSTRUKTURGEPRUEFT;
	}

	public String getXLSMETADATENEINGETRAGEN() {
		return XLSMETADATENEINGETRAGEN;
	}

	public void setXLSMETADATENEINGETRAGEN(String xLSMETADATENEINGETRAGEN) {
		XLSMETADATENEINGETRAGEN = xLSMETADATENEINGETRAGEN;
	}

	public Integer getMETADATENZUMDIENSTLEISTER() {
		return METADATENZUMDIENSTLEISTER;
	}

	public void setMETADATENZUMDIENSTLEISTER(Integer mETADATENZUMDIENSTLEISTER) {
		METADATENZUMDIENSTLEISTER = mETADATENZUMDIENSTLEISTER;
	}

	public String getSCANNERTYP() {
		return SCANNERTYP;
	}

	public void setSCANNERTYP(String string) {
		SCANNERTYP = string;
	}

	public String getBITONALIMAGENACHBEARBEITUNG() {
		return BITONALIMAGENACHBEARBEITUNG;
	}

	public void setBITONALIMAGENACHBEARBEITUNG(String bITONALIMAGENACHBEARBEITUNG) {
		BITONALIMAGENACHBEARBEITUNG = bITONALIMAGENACHBEARBEITUNG;
	}

	public String getGRAUIMAGENACHBEARBEITUNG() {
		return GRAUIMAGENACHBEARBEITUNG;
	}

	public void setGRAUIMAGENACHBEARBEITUNG(String gRAUIMAGENACHBEARBEITUNG) {
		GRAUIMAGENACHBEARBEITUNG = gRAUIMAGENACHBEARBEITUNG;
	}

	public String getFARBEIMAGENACHBEARBEITUNG() {
		return FARBEIMAGENACHBEARBEITUNG;
	}

	public void setFARBEIMAGENACHBEARBEITUNG(String fARBEIMAGENACHBEARBEITUNG) {
		FARBEIMAGENACHBEARBEITUNG = fARBEIMAGENACHBEARBEITUNG;
	}

	public String getDRUCKQUALITAET() {
		return DRUCKQUALITAET;
	}

	public void setDRUCKQUALITAET(String dRUCKQUALITAET) {
		DRUCKQUALITAET = dRUCKQUALITAET;
	}

	public String getWERKBATCH() {
		return WERKBATCH;
	}

	public void setWERKBATCH(String wERKBATCH) {
		WERKBATCH = wERKBATCH;
	}

	public Integer getFARBGRAUABB() {
		return FARBGRAUABB;
	}

	public void setFARBGRAUABB(Integer fARBGRAUABB) {
		FARBGRAUABB = fARBGRAUABB;
	}

	public String getDOWNLOADERSTELLT() {
		return DOWNLOADERSTELLT;
	}

	public void setDOWNLOADERSTELLT(String dOWNLOADERSTELLT) {
		DOWNLOADERSTELLT = dOWNLOADERSTELLT;
	}

	public String getDIGDOC2XML() {
		return DIGDOC2XML;
	}

	public void setDIGDOC2XML(String dIGDOC2XML) {
		DIGDOC2XML = dIGDOC2XML;
	}

	public String getMERGER() {
		return MERGER;
	}

	public void setMERGER(String mERGER) {
		MERGER = mERGER;
	}

	public Integer getWERKZURUECKAN() {
		return WERKZURUECKAN;
	}

	public void setWERKZURUECKAN(Integer wERKZURUECKAN) {
		WERKZURUECKAN = wERKZURUECKAN;
	}

	public String getWERKZURUECKWER() {
		return WERKZURUECKWER;
	}

	public void setWERKZURUECKWER(String wERKZURUECKWER) {
		WERKZURUECKWER = wERKZURUECKWER;
	}

	public Date getWERKZURUECKWANN() {
		return WERKZURUECKWANN;
	}

	public void setWERKPROJEKT(String wERKPROJEKT) {
		WERKPROJEKT = wERKPROJEKT;
	}

	public String getWERKPROJEKT() {
		return WERKPROJEKT;
	}

	public void setCDSICHERUNG(String cDSICHERUNG) {
		CDSICHERUNG = cDSICHERUNG;
	}

	public String getCDSICHERUNG() {
		return CDSICHERUNG;
	}

	public void setIMPORT(Integer iMPORT) {
		IMPORT = iMPORT;
	}

	public Integer getIMPORT() {
		return IMPORT;
	}

	public void setWERKNR(int i) {
		WERKNR = i;
	}

	public Integer getWERKNR() {
		return WERKNR;
	}

}
