package com.siemens.soarian.sf.claims.fixtures;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.TreeSet;

import com.siemens.med.hs.logging.Logger;
import com.siemens.soarian.contract.dto.claimdto.Claim;
import com.siemens.soarian.sf.claims.fixtures.setUp.CopyAndUpdateClaimFixture;
import com.siemens.soarian.sf.claims.fixtures.utility.ECVIntegration;
import com.siemens.soarian.sf.claims.repository.ObjectMemoryRepository;
import com.siemens.soarian.sf.claims.repository.RepositoryObject;

import fit.ColumnFixture;
import fit.Parse;

public class VersionNeutralColumnFixture extends ColumnFixture {

  public String versionNo;
  private int versionNoColNum = -1;
  private String claimsJarVersionNumber;
  private TreeSet<String> versionMap = new TreeSet<String>();
  String claimID = "";
  String formType = "";
  public static boolean fromECVTool = false;
  ArrayList<String> formTypes = new ArrayList<String>();
  HashMap<Integer, String> columnHeaders = new HashMap<Integer, String>();
  ArrayList<String> insertedData = new ArrayList<String>();

  ArrayList<String> claimsInserted = new ArrayList<String>();
  ArrayList<String> claimsFormInserted = new ArrayList<String>();
  private static final String EXCEPTION_CAUGHT = "Exception caught: ";
  private static final Logger LOGGER = Logger.getLogger(VersionNeutralColumnFixture.class);

  static {
    Properties prop = new Properties();
    String propValues = null;
    InputStream inputStream = VersionNeutralColumnFixture.class.getClassLoader().getResourceAsStream("CS.properties");

    try {
      prop.load(inputStream);
      propValues = prop.getProperty("From_ECV");

      fromECVTool = Boolean.parseBoolean(propValues.trim());

      prop.remove("From_ECV");

    } catch (Exception e) {
      LOGGER.error(EXCEPTION_CAUGHT, e);
    }

    try {
      FileOutputStream out = new FileOutputStream("CS.properties");
      prop.remove("From_ECV");
      prop.store(out, null);
    } catch (IOException e) {
      LOGGER.error(EXCEPTION_CAUGHT, e);
    }
  }

  @Override
  public void doRows(Parse rows) {
    if (rows.parts.more == null) {
      super.doRows(rows);
      return;
    }

    for (int i = 0; i < rows.parts.more.size(); i++) {
      if (rows.at(0, i).text().equalsIgnoreCase("versionNo")) {
        versionNoColNum = i;
      }
      columnHeaders.put(i, rows.at(0, i).text());
    }

    if (versionNoColNum != -1) {
      for (int i = 1; i < rows.size(); i++) {
        versionMap.add(rows.at(i, versionNoColNum).text());

      }

    }

    super.doRows(rows);
  }

  @Override
  public void doRow(Parse row) {

    if (versionNoColNum == -1) {
      super.doRow(row);
    } else {

      try {
        claimsJarVersionNumber = FixtureUtils.getClaimsJarVersion();

        if (claimsJarVersionNumber == "" || claimsJarVersionNumber == null) {
          return;
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
      if (versionMap.contains(claimsJarVersionNumber)) {
        if (row.parts.at(versionNoColNum).text().equals(claimsJarVersionNumber)) {
          super.doRow(row);
        }
      } else {
        String testCaseVersionToBeExecuted = versionMap.floor(claimsJarVersionNumber);
        if (row.parts.at(versionNoColNum).text().equalsIgnoreCase(testCaseVersionToBeExecuted)) {
          super.doRow(row);
        }

      }

    }
  }

  public void saveFitnesseDataToDatabase(String formType) {

    final Claim originalClaim = getClaim(claimID);
    if (originalClaim == null) {
      return;
    }
    Claim claimCopy = null;
    try {
      claimCopy = CopyAndUpdateClaimFixture.copyClaim(originalClaim);
    } catch (Exception e) {
      e.printStackTrace();
    }
    final Claim claim = ValidateClaimFixture.assignClaimType(claimCopy, formType);
    if (formType.equalsIgnoreCase("837I") || formType.equalsIgnoreCase("837P") || formType.equalsIgnoreCase("837D")) {
      ECVIntegration.saveXMLToDB(claim, formType, "5010");
    } else if (formType.equalsIgnoreCase("1500R") || formType.equalsIgnoreCase("1500R12")
            || formType.equalsIgnoreCase("UB04") || formType.equalsIgnoreCase("ADA2006")
            || formType.equalsIgnoreCase("ADA2012")) {
      ECVIntegration.saveXMLToDB(claim, formType, "");
    }

  }

  private Claim getClaim(String referenceNumber) {
    RepositoryObject repoObject = ObjectMemoryRepository.getInstance().getObject(Claim.class.getName(),
            referenceNumber);
    if (repoObject != null) {
      return (Claim) repoObject.getObject();
    }
    return null;
  }

  @Override
  public void doCells(Parse cells) {
    String claimId_FormType = "";

    if (fromECVTool) {
      extractCellData(cells);
      if (claimID != "") {
        if (formType != "") {

          claimId_FormType = claimID + "_" + formType;
          if (!insertedData.contains(claimId_FormType)) {
            saveFitnesseDataToDatabase(formType);
            insertedData.add(claimId_FormType);
          }

        } else {
          if (formTypes.size() > 0) {
            for (int i = 0; i < formTypes.size(); i++) {
              claimId_FormType = claimID + "_" + formTypes.get(i);
              if (!insertedData.contains(claimId_FormType)) {
                saveFitnesseDataToDatabase(formTypes.get(i));
                insertedData.add(claimId_FormType);
              }
            }

          }

        }
        formType = "";
        claimID = "";
        return;
      }

    } else {
      super.doCells(cells);
    }

  }

  private void extractCellData(Parse cells) {
    claimID = "";
    String columnName = "";

    for (int i = 0; cells != null; i++) {
      if (i == 0) {
        claimID = cells.at(i).text();

      } else {

        if (i >= columnHeaders.size()) {
          return;
        }
        columnName = columnHeaders.get(i);
        if (columnName.equalsIgnoreCase("formType")) {

          formType = cells.at(i).text();
          return;
        } else if (columnName.equalsIgnoreCase("errorCodeForUB04?")) {
          formTypes.add("UB04");

        } else if (columnName.equalsIgnoreCase("errorCodeForADA2006?")) {
          formTypes.add("ADA2006");

        } else if (columnName.equalsIgnoreCase("errorCodeForADA2012?")) {
          formTypes.add("ADA2012");

        } else if (columnName.equalsIgnoreCase("errorCodeFor1500R?")) {
          formTypes.add("1500R");

        } else if (columnName.equalsIgnoreCase("errorCodeFor1500R12?")) {
          formTypes.add("1500R12");

        } else if (columnName.equalsIgnoreCase("errorCodeFor837I?")) {
          formTypes.add("837I");

        } else if (columnName.equalsIgnoreCase("errorCodeFor837P?")) {
          formTypes.add("837P");

        } else if (columnName.equalsIgnoreCase("errorCodeFor837D?")) {
          formTypes.add("837D");

        }
      }

    }

  }
}
