package com.medails.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;

import com.medails.Display;

    /*********************************************************** 
                             BDD MySQL
    ***********************************************************/

public class DBConnection
{
    /************* Déclarations Classes ****************/
    private Display dp;

    /************************************************************ 
                            CONSTRUCTEUR
    *************************************************************/
    
    public DBConnection (Display dp)
    {
        this.dp = dp;
    }

    /************************************************************ 
                            VARIABLES
    *************************************************************/

    // private static final String URL = "jdbc:sqlite:target/classes/db.sqlite";
    private static final String URL = "jdbc:mysql://localhost:3306/AppFacture?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    /************************************************************ 
                            CONNEXION
    *************************************************************/
    
    public static Connection connect()
    {
        Connection conn = null;

        try
        {
            // Chargement du drive MySQL                                             // Chargement du driver SQLite  
            Class.forName("com.mysql.cj.jdbc.Driver");                     // Class.forName("org.sqlite.JDBC");

            // Etablir la connexion
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connexion à MySQL établie.");
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
            System.out.println("Erreur : Le driver MySQL est introuvable.");
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            System.out.println("Erreur de connexion à la base MySQL.");
        }
        return conn;
    }


    /************************************************************ 
                                READ
    *************************************************************/

    /****************** List<Map<String, Object> ****************/
 
    // Récupère les données du tableau "facture"
    public List<Map<String, Object>> getFacture()
    {
        List<Map<String, Object>> factureList = new ArrayList<>();

        String query = "SELECT FactureAnnee, FactureMois," + 
                               "VersementAnnee, VersementMois, VersementJour, " +
                               	 "Jours, TJM, TTC, HT, TVA, Taxes, Benefices, " +
                               	   "RepFacture, RepDecla, " + 
                                     "NameFacture, NameDecla FROM facture";                          

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(query);
                    ResultSet rs = pstmt.executeQuery())
        {
            while (rs.next())
            {
                Map<String, Object> row = new HashMap<>();
                row.put("FactureAnnee"    , rs.getInt      ("FactureAnnee"));
                row.put("FactureMois"     , rs.getString   ("FactureMois"));
                row.put("VersementAnnee"  , rs.getInt      ("VersementAnnee"));
                row.put("VersementMois"   , rs.getString   ("VersementMois"));
                row.put("VersementJour"   , rs.getInt      ("VersementJour"));
                row.put("Jours"           , rs.getInt      ("Jours"));
                row.put("TJM"             , rs.getDouble   ("TJM"));
                row.put("TTC"             , rs.getDouble   ("TTC"));
                row.put("HT"              , rs.getDouble   ("HT"));
                row.put("TVA"             , rs.getDouble   ("TVA"));
                row.put("Taxes"           , rs.getDouble   ("Taxes"));
                row.put("Benefices"       , rs.getDouble   ("Benefices"));
                row.put("RepFacture"      , rs.getString   ("RepFacture"));
                row.put("RepDecla"        , rs.getString   ("RepDecla"));
                row.put("NameFacture"	  , rs.getString   ("NameFacture")); 
                row.put("NameDecla"		  , rs.getString   ("NameDecla")); 
                factureList.add(row);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return factureList;
    }

   
    // Récupère les données du tableau "deduction"
    public List<Map<String, Object>> getDeduction()
    {
        List<Map<String, Object>> deductionList = new ArrayList<>();

        String query = "SELECT DeductionAnnee, DeductionMois, DeductionJour," +
        			   "TTC, HT, TVA, RepDeduction, NameDeduction FROM deduction";

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(query);
                    ResultSet rs = pstmt.executeQuery())
        {
            while (rs.next())
            {
                Map<String, Object> row = new HashMap<>();
                row.put("DeductionAnnee"   , rs.getInt       ("DeductionAnnee"));
                row.put("DeductionMois"    , rs.getString    ("DeductionMois"));
                row.put("DeductionJour"    , rs.getInt       ("DeductionJour"));
                row.put("TTC"			   , rs.getDouble    ("TTC"));
                row.put("HT"			   , rs.getDouble    ("HT"));
                row.put("TVA"			   , rs.getDouble    ("TVA"));
                row.put("RepDeduction"     , rs.getString    ("RepDeduction"));
                row.put("NameDeduction"	   , rs.getString    ("NameDeduction")); 
                deductionList.add(row);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return deductionList;
    }
    

    /******************** List<String, Object> ******************/

    // Récupère les colonnes Répertoire des tableaux SQL
    public List<String> getDirPDF(String tableName, String columnName)
    {
        // Validation des paramètres
        Map<String, List<String>> validColumns = new HashMap<>();
        validColumns.put("facture", Arrays.asList("RepFacture", "RepDecla", 
                                                            "NameFacture", "NameDecla"));
        validColumns.put("deduction", Arrays.asList("RepDeduction", "NameDeduction"));

        if (!validColumns.containsKey(tableName))
        {
            throw new IllegalArgumentException("Table invalide : " + tableName);
        }

        if (!validColumns.get(tableName).contains(columnName))
        {
            throw new IllegalArgumentException(String.format
                ("Colonne invalide pour la table %s: %s", tableName, columnName));
        }

        List<String> values = new ArrayList<>();

        String query = String.format
            ("SELECT DISTINCT %s FROM %s WHERE %s IS NOT NULL", columnName, tableName, columnName);

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(query);
                    ResultSet rs = pstmt.executeQuery())
        {
            while (rs.next())
            {
                String value = rs.getString(1);
                if (value != null && !value.trim().isEmpty())
                {
                    values.add(value);
                }
            }
            
            Collections.sort(values);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            System.err.printf("Erreur lors de la récupération de %s.%s; %s%n", 
                                                tableName, columnName, e.getMessage());
        }
        return values;
    }
        

    /************************************************************ 
                                WRITE
    *************************************************************/

    // Ajout des données dans le tableau "facture"
    public void setFactureData(Map<String, Object> factureData)
    {		
        String query = "INSERT INTO facture " +
                          "(FactureAnnee, FactureMois, " +
                              "VersementAnnee, VersementMois, VersementJour, " +
                                  "Jours, TJM, TTC, HT, TVA, Taxes, Benefices, " + 
                                  	  "RepFacture, RepDecla, " +
                                         "NameFacture, NameDecla) VALUES " +
                                           "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = connect();
                PreparedStatement psntmt = conn.prepareStatement(query))
        {
            psntmt.setInt       	(1,  (Integer)       factureData.get("FactureAnnee"));
            psntmt.setString    	(2,  (String)        factureData.get("FactureMois"));
            psntmt.setInt       	(3,  (Integer)       factureData.get("VersementAnnee"));  
            psntmt.setString    	(4,  (String)        factureData.get("VersementMois"));
            psntmt.setInt       	(5,  (Integer)       factureData.get("VersementJour"));
            psntmt.setDouble    	(6,  (Double)        factureData.get("Jours"));
            psntmt.setDouble    	(7,  (Double)        factureData.get("TJM"));
            psntmt.setDouble    	(8,  (Double)        factureData.get("TTC"));
            psntmt.setDouble    	(9,  (Double)        factureData.get("HT"));
            psntmt.setDouble    	(10, (Double)        factureData.get("TVA"));
            psntmt.setDouble    	(11, (Double)        factureData.get("Taxes"));
            psntmt.setDouble    	(12, (Double)        factureData.get("Benefices"));
            psntmt.setString    	(13, (String)		factureData.get("RepFacture")); 
            psntmt.setString    	(14, (String)		factureData.get("RepDecla")); 
            psntmt.setString    	(15, (String)		factureData.get("NameFacture")); 
            psntmt.setString    	(16, (String)		factureData.get("NameDecla")); 
            psntmt.executeUpdate	();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de l'insertion des données de facture", e);
        }
    }

 
    // AJout des données dans le tableau "deduction"
    public void setDeductionData(Map<String, Object> deductionData)
    {
        String query = "INSERT INTO deduction " +
                            "(DeductionAnnee, DeductionMois, DeductionJour, TTC, HT, TVA, RepDeduction, NameDeduction) VALUES " +
                                "(?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = connect();
                PreparedStatement psntmt = conn.prepareStatement(query))
        {
            psntmt.setInt       	(1, (Integer)       deductionData.get("DeductionAnnee"));
            psntmt.setString    	(2, (String)        deductionData.get("DeductionMois"));
            psntmt.setInt       	(3, (Integer)       deductionData.get("DeductionJour"));
            psntmt.setDouble    	(4, (Double)        deductionData.get("TTC"));
            psntmt.setDouble    	(5, (Double)        deductionData.get("HT"));
            psntmt.setDouble    	(6, (Double)        deductionData.get("TVA"));
            psntmt.setString    	(7, (String)	       deductionData.get("RepDeduction")); 
            psntmt.setString    	(8, (String)	       deductionData.get("NameDeduction")); 
            psntmt.executeUpdate();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de l'insertion des données de déduction", e);
        } 
    }


    /************************************************************ 
                                DELETE
    *************************************************************/

    public void deleteInBDD(JComboBox box, String nameTable, String nameColumn)
    {
        String query = "DELETE FROM " + nameTable + " WHERE " + nameColumn + " = ?";

        try (Connection conn = connect();
                PreparedStatement psntmt = conn.prepareStatement(query))
        {
            psntmt.setString    (1, box.getSelectedItem().toString());
            psntmt.executeUpdate();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la suppression : ", e);
        }
    }
}               