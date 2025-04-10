package com.medails.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            Class.forName("com.mysql.cj.jdbc.Driver");                               // Class.forName("org.sqlite.JDBC");

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
                               "VersementAnnee, VersementMois, VersementJour," +
                               "Jours, TJM, TTC, HT, TVA, Taxes, Benefices FROM facture";                          

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
                factureList.add(row);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return factureList;
    }

    
    // Récupère toutes les données du tableau "directory"
    public List<Map<String, Object>> getDirectory()
    {
        List<Map<String, Object>> directoryList = new ArrayList<>();

        String query = "SELECT RepFacture, RepDecla, RepDeduction FROM directory";

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(query);
                    ResultSet rs = pstmt.executeQuery())
        {
            while (rs.next())
            {
                Map<String, Object> row = new HashMap<>();
                row.put("RepFacture",       rs.getString    ("RepFacture"));
                row.put("RepDecla",         rs.getString    ("RepDecla"));
                row.put("RepDeduction",     rs.getString    ("RepDeduction"));
                directoryList.add(row);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return directoryList;
    }

    
    // Récupère toutes les données du tableau "namePDF"
    public List<Map<String, Object>> getPDF()
    {
        List<Map<String, Object>> namePDFList = new ArrayList<>();

        String query = "SELECT NameFacture, NameDecla, NameDeduction FROM namepdf";

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(query);
                    ResultSet rs = pstmt.executeQuery())    
        {
            while (rs.next())
            {
                Map<String, Object> row = new HashMap<>();
                row.put("NameFacture",      rs.getString    ("NameFacture"));
                row.put("NameDecla",        rs.getString    ("NameDecla"));
                row.put("NameDeduction",    rs.getString    ("NameDeduction"));
                namePDFList.add(row);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return namePDFList;
    }
    
    
    // Récupère les données du tableau "deduction"
    public List<Map<String, Object>> getDedution()
    {
        List<Map<String, Object>> deductionList = new ArrayList<>();

        String query = "SELECT DeductionAnnee, DeductionMois, DeductionJour, TTC, HT, TVA FROM deduction";

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(query);
                    ResultSet rs = pstmt.executeQuery())
        {
            while (rs.next())
            {
                Map<String, Object> row = new HashMap<>();
                row.put("DeductionAnnee",    rs.getInt       ("DeductionAnnee"));
                row.put("DeductionMois",     rs.getString    ("DeductionMois"));
                row.put("DeductionJour",     rs.getInt       ("DeductionJour"));
                row.put("TTC",               rs.getDouble    ("TTC"));
                row.put("HT",                rs.getDouble    ("HT"));
                row.put("TVA",               rs.getDouble    ("TVA"));
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

    // Récupère une colonne spécifique du tableau "directory"
    public List<String> getDirectories(String column)
    {
        List<String> directories = new ArrayList<>();

        // Validation de la colonne demandée
        if (!List.of("RepFacture", "RepDecla", "RepDeduction").contains(column))
        {
            throw new IllegalArgumentException("Colonne invalide : " + column);
        }

        String query = "SELECT " + column + " FROM directory";

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(query);
                    ResultSet rs = pstmt.executeQuery())
        {
            while (rs.next())
            {
            	String value = rs.getString(column);
                if (value != null)
                {
                	directories.add(value);
                }
            }
            
            Collections.sort(directories);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return directories;
    }
    
    
    // Récupère une colonne spécifique du tableau "namePDF"
    public List<String> getPDFs(String column)
    {
        List<String> pdfs = new ArrayList<>();

        // Validation de la colonne demandée
        if (!List.of("NameFacture", "NameDecla", "NameDeduction").contains(column))
        {
            throw new IllegalArgumentException("Colonne invalide : " + column);
        }

        String query = "SELECT " + column + " FROM namepdf";

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(query);
                    ResultSet rs = pstmt.executeQuery())    
        {
            while (rs.next())
            {
            	String value = rs.getString(column);
            	if (value != null)
            	{
            		pdfs.add(value);
            	}
            }

            Collections.sort(pdfs);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return pdfs;
    }
    
    
    /************************************************************ 
                                WRITE
    *************************************************************/

    // Ajout des données dans le tableau "facture"
    public void setFactureData(Map<String, Object> factureData)
    {
    	// Vérification des valeurs obligatoires
    	if (factureData.get("FactureAnnee") == null || factureData.get("VersementAnnee") == null ||
    			factureData.get("VersementJour") == null || factureData.get("Jours") == null)
    	{
    		throw new IllegalArgumentException("Données manquantes dans factureData");
   		}
    			
        String query = "INSERT INTO facture " +
                          "(FactureAnnee, FactureMois, " +
                              "VersementAnnee, VersementMois, VersementJour, " +
                                  "Jours, TJM, TTC, HT, TVA, Taxes, Benefices) VALUES " +
                                      "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = connect();
                PreparedStatement psntmt = conn.prepareStatement(query))
        {
            psntmt.setInt       (1,  (Integer)       factureData.get("FactureAnnee"));
            psntmt.setString    (2,  (String)        factureData.get("FactureMois"));
            psntmt.setInt       (3,  (Integer)       factureData.get("VersementAnnee"));  
            psntmt.setString    (4,  (String)        factureData.get("VersementMois"));
            psntmt.setInt       (5,  (Integer)       factureData.get("VersementJour"));
            psntmt.setInt       (6,  (Integer)       factureData.get("Jours"));
            psntmt.setDouble    (7,  (Double)        factureData.get("TJM"));
            psntmt.setDouble    (8,  (Double)        factureData.get("TTC"));
            psntmt.setDouble    (9,  (Double)        factureData.get("HT"));
            psntmt.setDouble    (10, (Double)        factureData.get("TVA"));
            psntmt.setDouble    (11, (Double)        factureData.get("Taxes"));
            psntmt.setDouble    (12, (Double)        factureData.get("Benefices"));

            psntmt.executeUpdate();
            JOptionPane.showMessageDialog(dp.fen, "Facture ajoutée avec succès !",
                                                            "Enregistrement réussi",
                                                            JOptionPane.INFORMATION_MESSAGE);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de l'insertion des données de facture", e);
        }
    }


    // Ajout des données dans le tableau "directory"
    public void setDirectoryData(Map<String, Object> directoryData)
    {
        String query = "INSERT INTO directory " +
                            "(RepFacture, RepDecla, RepDeduction) VALUES " +
                                "(?, ?, ?)";

        try (Connection conn = connect();
                PreparedStatement psntmt = conn.prepareStatement(query))
        {
            psntmt.setString    (1, (String)    directoryData.get("RepFacture"));
            psntmt.setString    (2, (String)    directoryData.get("RepDecla"));
            psntmt.setString    (3, (String)    directoryData.get("RepDeduction"));

            psntmt.executeUpdate();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de l'insertion des données de directory", e);

        }
    }

 
    // Ajout des données dans le tableau "namePDF"
    public void setNamePDFData(Map<String, Object> namePDFData)
    {
        String query = "INSERT INTO namepdf " +
                            "(NameFacture, NameDecla, NameDeduction) VALUES " +
                                "(?, ?, ?)";

        try (Connection conn = connect();
                PreparedStatement psntmt = conn.prepareStatement(query))
        {
            psntmt.setString    (1, (String)    namePDFData.get("NameFacture"));
            psntmt.setString    (2, (String)    namePDFData.get("NameDecla"));
            psntmt.setString    (3, (String)    namePDFData.get("NameDeduction"));

            psntmt.executeUpdate();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de l'insertion des données de namepdf", e);

        }
    }
    

    // AJout des données dans le tableau "deduction"
    public void setDeductionData(Map<String, Object> deductionData)
    {
        String query = "INSERT INTO deduction " +
                            "(DeductionAnnee, DeductionMois, DeductionJour, TTC, HT, TVA) VALUES " +
                                "(?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = connect();
                PreparedStatement psntmt = conn.prepareStatement(query))
        {
            psntmt.setInt       (1, (Integer)       deductionData.get("DeductionAnnee"));
            psntmt.setString    (2, (String)        deductionData.get("DeductionMois"));
            psntmt.setInt       (3, (Integer)       deductionData.get("DeductionJour"));
            psntmt.setDouble    (4, (Double)        deductionData.get("TTC"));
            psntmt.setDouble    (5, (Double)        deductionData.get("HT"));
            psntmt.setDouble    (6, (Double)        deductionData.get("TVA"));

            psntmt.executeUpdate();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de l'insertion des données de déduction", e);
        } 
    }
}               
