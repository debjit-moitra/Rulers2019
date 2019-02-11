package com.siemens.soarian.sf.claims.fixtures.utility;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;

import com.siemens.med.hs.logging.Logger;
import com.siemens.soarian.contract.dto.claimdto.Claim;
import com.siemens.soarian.sf.claim.constants.ClaimsBusinessConstants;

public class ECVIntegration {

  private static Connection con = null;

  private static Properties payerIdSubId = new Properties();
  private static final String EXCEPTION_CAUGHT = "Exception caught: ";
  private static final Logger LOGGER = Logger.getLogger(ECVIntegration.class);


  static {
    InputStream inputStream = ECVIntegration.class.getClassLoader().getResourceAsStream("SSI.properties");
    try {
      payerIdSubId.load(inputStream);
    } catch (IOException e) {
      LOGGER.error(EXCEPTION_CAUGHT, e);
    }
  }

  public static Connection getConnection() {

    String hostName = null;
    String dbName = null;
    String userName = null;
    String passWord = null;
    String port = null;

    try {
      hostName = FitnessePropertiesController.getDBServer();
      dbName = FitnessePropertiesController.getDBName();
      userName = FitnessePropertiesController.getUserID();
      passWord = FitnessePropertiesController.getPassword();
      port = FitnessePropertiesController.getPort();
    } catch (IOException e1) {
      e1.printStackTrace();
    }

    if (con == null) {
      try {
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        String conStr = "jdbc:sqlserver://" + hostName + ":" + port + ";databaseName=" + dbName;
        con = DriverManager.getConnection(conStr, userName, passWord);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    return con;

  }

  public static void saveXMLToDB(Claim claim, String docType, String electronicVers) {
    StringWriter writer = new StringWriter();
    String sql, pyrName = "", pyrHlthplan = "", tempString = "", XMLString, key = "";
    int priorityOfHlthPlan;
    String payerId, payerSubId;
    String payerIdSubIdValue = "";

    try {
      claim.getClaimControl().setSsiValidation(true);
      claim.getPatient().getThePersonName().setPrefix("");
      claim.getPatient().getThePersonName().setSuffix("");
      claim.getBillingProvider().setECVProviderID("");
      claim.getClaimControl().setDocType(getDocType(docType));
      claim.getClaimControl().setFormType(claim.getFormType());

      priorityOfHlthPlan = claim.getPriorityOfHealthPlan();

      DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
      for (int i = 0; i < claim.getPayers().length; i++) {
        if (claim.getPayers()[i].getPriorityNum() == priorityOfHlthPlan) {
          pyrName = claim.getPayers()[i].getPayerName();
          pyrHlthplan = claim.getPayers()[i].getHealthPlanDisplayName();
          if (docType.startsWith("837I") || docType.startsWith("UB04")) {
            key = claim.getClaimControl().getRulesID() + "_Ins";
          } else if (docType.startsWith("837P") || docType.startsWith("1500R")) {
            key = claim.getClaimControl().getRulesID() + "_Prof";
          }

          if (claim.getClaimControl().getRulesID() != null) {
            payerIdSubIdValue = getSSIPayerIdSubId(key);
            if (payerIdSubIdValue != null && !payerIdSubIdValue.equalsIgnoreCase("None")) {

              payerId = payerIdSubIdValue.substring(0, payerIdSubIdValue.indexOf("-") - 1);
              claim.getPayers()[i].setPayerIDNumber(payerId);
              payerSubId = payerIdSubIdValue.substring(payerIdSubIdValue.indexOf("-") + 1);
              claim.getPayers()[i].setPayerSubIDNumber(payerSubId);
            } else {
              claim.getPayers()[i].setPayerIDNumber("");
              claim.getPayers()[i].setPayerSubIDNumber("");
            }

          } else {
            claim.getPayers()[i].setPayerIDNumber("");
            claim.getPayers()[i].setPayerSubIDNumber("");
          }

        } else {
          claim.getPayers()[i].setPayerIDNumber("");
          claim.getPayers()[i].setPayerSubIDNumber("");
        }

      }
      Marshaller marshaller = new Marshaller(writer);
      marshaller.marshal(claim);
      tempString = writer.toString();
      XMLString = tempString.substring(tempString.indexOf("<claim"));
      writer.close();
      Statement stmt = getConnection().createStatement();
      sql = "insert into CS_VfyInputItem (ClaimId,PyrName,HlthPlanName,ElecVersText,DocTypeCd,ClaimXMLObjText,CreUserObjId,CreDTime) values ('"
              + claim.getClaimReferenceNumber() + "','" + pyrName + "','" + pyrHlthplan + "','" + electronicVers + "','"
              + docType + "','" + XMLString + "','magentateam','" + dateFormat.format(new Date()) + "')";

      claim.getClaimControl().setSsiValidation(false);
      try {
        stmt.executeUpdate(sql);
      } catch (SQLException e) {

      }

    } catch (IOException e) {
      e.printStackTrace();
    } catch (MarshalException e) {
      e.printStackTrace();
    } catch (ValidationException e) {
      e.printStackTrace();
    } catch (Exception ex) {
      ex.printStackTrace();
    }

  }

  private static String getSSIPayerIdSubId(String key) {
    String value = "";
    value = payerIdSubId.getProperty(key);
    return value;

  }

  public static String getDocType(String formType) {
    String docType = null;

    switch (formType) {
      case ClaimsBusinessConstants.FORMTYPE837I:
        docType = ClaimsBusinessConstants.DOCTYPE_837I;
        break;
      case ClaimsBusinessConstants.FORMTYPE837P:
        docType = ClaimsBusinessConstants.DOCTYPE_837P;
        break;
      case ClaimsBusinessConstants.FORMTYPE837D:
        docType = ClaimsBusinessConstants.DOCTYPE_837D;
        break;
      case ClaimsBusinessConstants.FORMTYPEUB04:
        docType = ClaimsBusinessConstants.DOCTYPE_UB04;
        break;
      case ClaimsBusinessConstants.FORMTYPE1507:
        docType = ClaimsBusinessConstants.DOCTYPE_1500;
        break;
      case ClaimsBusinessConstants.FORMTYPE1512:
        docType = ClaimsBusinessConstants.DOCTYPE_1512;
        break;
      case ClaimsBusinessConstants.FORMTYPEADA2006:
        docType = ClaimsBusinessConstants.DOCTYPEADA2006_WITHCS;
        break;
      case ClaimsBusinessConstants.FORMTYPEADA2012:
        docType = ClaimsBusinessConstants.DOCTYPEADA2012_WITHCS;
        break;
    }

    return docType;

  }

}
