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
import java.util.function.Consumer;

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
            // Chargement du drive MySQL                                  
            Class.forName("com.mysql.cj.jdbc.Driver");                 

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
        List<String> values = new ArrayList<>();
        
        String query = String.format("SELECT DISTINCT %s FROM %s WHERE %s IS NOT NULL",
                                                             columnName, tableName, columnName);

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(query);
                    ResultSet rs = pstmt.executeQuery())
        {
            while (rs.next())
            {
                String value = rs.getString(1);

                if (value != null && !value.trim().isEmpty())
                {
                    values.add(value.trim());
                }
            }

            Collections.sort(values);

            if (columnName.equals("NameFacture") || 
                    columnName.equals("NameDecla") || 
                        columnName.equals("NameDeduction"))
            {
                List<String> num = new ArrayList<>();
                for (int ii = 0; ii < values.size(); ii++)
                {
                    num.add((ii + 1) + "-  " + values.get(ii));
                }

                return num;
            }
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
                PreparedStatement pstmt = conn.prepareStatement(query))
        {
            pstmt.setInt       	(1,  (Integer)       factureData.get("FactureAnnee"));
            pstmt.setString    	(2,  (String)        factureData.get("FactureMois"));
            pstmt.setInt       	(3,  (Integer)       factureData.get("VersementAnnee"));  
            pstmt.setString    	(4,  (String)        factureData.get("VersementMois"));
            pstmt.setInt       	(5,  (Integer)       factureData.get("VersementJour"));
            pstmt.setDouble    	(6,  (Double)        factureData.get("Jours"));
            pstmt.setDouble    	(7,  (Double)        factureData.get("TJM"));
            pstmt.setDouble    	(8,  (Double)        factureData.get("TTC"));
            pstmt.setDouble    	(9,  (Double)        factureData.get("HT"));
            pstmt.setDouble    	(10, (Double)        factureData.get("TVA"));
            pstmt.setDouble    	(11, (Double)        factureData.get("Taxes"));
            pstmt.setDouble    	(12, (Double)        factureData.get("Benefices"));
            pstmt.setString    	(13, (String)		factureData.get("RepFacture")); 
            pstmt.setString    	(14, (String)		factureData.get("RepDecla")); 
            pstmt.setString    	(15, (String)		factureData.get("NameFacture")); 
            pstmt.setString    	(16, (String)		factureData.get("NameDecla")); 
            pstmt.executeUpdate	();
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
                PreparedStatement pstmt = conn.prepareStatement(query))
        {
            pstmt.setInt       	(1, (Integer)       deductionData.get("DeductionAnnee"));
            pstmt.setString    	(2, (String)        deductionData.get("DeductionMois"));
            pstmt.setInt       	(3, (Integer)       deductionData.get("DeductionJour"));
            pstmt.setDouble    	(4, (Double)        deductionData.get("TTC"));
            pstmt.setDouble    	(5, (Double)        deductionData.get("HT"));
            pstmt.setDouble    	(6, (Double)        deductionData.get("TVA"));
            pstmt.setString    	(7, (String)	       deductionData.get("RepDeduction")); 
            pstmt.setString    	(8, (String)	       deductionData.get("NameDeduction")); 
            pstmt.executeUpdate();
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
        // Vérification que la combox n'est pas vide
        if (box.getSelectedItem() == null)
        {
            JOptionPane.showMessageDialog(dp.fen, "Veuillez sélectioner au moins un PDF à supprimer",
                                                  "Aucune sélection", 
                                                  JOptionPane.WARNING_MESSAGE);
            return;          
        }
    
        // Demande de confirmation
        int confirm = JOptionPane.showConfirmDialog(dp.fen, "Etes-vous sûr de vouloir supprimer les PDF sélectionnés ?",
                                                            "Confirmation de suppression", 
                                                            JOptionPane.YES_NO_OPTION);
    
        if (confirm == JOptionPane.YES_OPTION)  
        {     
            // Récupération du nom réel du fichier sans le préfixe
            String selectedItem  = box.getSelectedItem().toString();
            String valueToDelete = selectedItem.contains("-") ? 
                                    selectedItem.substring(selectedItem.indexOf("-") + 1).trim() : selectedItem.trim();

            String query = "DELETE FROM " + nameTable + " WHERE " + nameColumn + " = ?";

            try (Connection conn = connect();
                    PreparedStatement pstmt = conn.prepareStatement(query))
            {
                pstmt.setString         (1, valueToDelete);

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0)
                {
                    JOptionPane.showMessageDialog(dp.fen, "Suppression des PDF sélectionnés terminée",
                                                            "Opération réussie", 
                                                            JOptionPane.INFORMATION_MESSAGE);
                }
                else
                {
                    JOptionPane.showMessageDialog(dp.fen, "Aucun enregistrement correspondant trouvé en base",
                                                            "Aucune suppression", 
                                                            JOptionPane.INFORMATION_MESSAGE);                    
                }
            } 
            catch (SQLException e)                                                                                  
            {                                          
                e.printStackTrace();
                throw new RuntimeException("Erreur lors de la suppression : ", e);
            }
        }
    }


    /************************************************************ 
                       REECRITURE DANS ONGLET 1
    *************************************************************/

    // Méthode permettant la lecture avec extraction de préfixe
    public Map<String, Object> reWriteFacture(String nameFacture)
    {
        Map<String, Object> result = new HashMap<>();
        
        String query = "SELECT * FROM facture WHERE NameFacture = ? LIMIT 1";

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(query))
        {
            pstmt.setString    (1, nameFacture);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                result.put("FactureAnnee"    , rs.getInt    ("FactureAnnee"));
                result.put("FactureMois"     , rs.getString ("FactureMois"));
                result.put("VersementAnnee"  , rs.getInt    ("VersementAnnee"));
                result.put("VersementMois"   , rs.getString ("VersementMois"));
                result.put("VersementJour"   , rs.getInt    ("VersementJour"));
                result.put("Jours"           , rs.getInt    ("Jours"));
                result.put("TJM"             , rs.getDouble ("TJM"));
                result.put("TTC"             , rs.getDouble ("TTC"));
                result.put("HT"              , rs.getDouble ("HT"));
                result.put("TVA"             , rs.getDouble ("TVA"));
                result.put("Taxes"           , rs.getDouble ("Taxes"));
                result.put("Benefices"       , rs.getDouble ("Benefices"));
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return result;
    }


    /************************************************************ 
                       REECRITURE DANS ONGLET 3
    *************************************************************/

    // Méthode permettant la lecture avec extraction de préfixe
    public Map<String, Object> reWriteDeduction(String nameDeduction)
    {
        Map<String, Object> result = new HashMap<>();
        
        String query = "SELECT * FROM deduction WHERE NameDeduction = ? LIMIT 1";

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(query))
        {
            pstmt.setString    (1, nameDeduction);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                result.put("DeductionAnnee"   , rs.getInt    ("DeductionAnnee"));
                result.put("DeductionMois"    , rs.getString ("DeductionMois"));
                result.put("DeductionJour"    , rs.getInt    ("DeductionJour"));
                result.put("TTC"              , rs.getDouble ("TTC"));
                result.put("HT"               , rs.getDouble ("HT"));
                result.put("TVA"              , rs.getDouble ("TVA"));
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return result;
    }


    /************************************************************ 
                       METHODES POUR REECRITURE
    *************************************************************/

    // Méthode de controle du prefixe 
    private String cleanPrefix(String item) 
    {
        return item.replaceFirst("^\\d+-", "").trim();
    }


    public void boxPDFListener(JComboBox<String> box, Consumer<Map<String, Object>> updateUI)
    {
        box.addActionListener(e -> 
        {
            String item = (String) box.getSelectedItem();

            if (item != null && !item.isEmpty()) 
            {
                String cleanedName = cleanPrefix(item);
                Map<String, Object> dataFacture = reWriteFacture(cleanedName);
                Map<String, Object> dataDeduction = reWriteDeduction(cleanedName);
    
                if (dataFacture != null && !dataFacture.isEmpty())   { updateUI.accept(dataFacture);   }
                if (dataDeduction != null && !dataDeduction.isEmpty())   { updateUI.accept(dataDeduction);   }
            }
        });
    }


    // Reconversion du format Date 
    public int convertMonth(String mois)
    {
        Map<String, Integer> moisMap = Map.ofEntries
           (Map.entry("janvier"     , 1),
            Map.entry("février"     , 2),
            Map.entry("mars"        , 3),
            Map.entry("avril"       , 4),
            Map.entry("mai"         , 5),
            Map.entry("juin"        , 6),
            Map.entry("juillet"     , 7),
            Map.entry("août"        , 8),
            Map.entry("septembre"   , 9),
            Map.entry("octobre"     , 10),
            Map.entry("novembre"    , 11),
            Map.entry("décembre"    , 12));

        return moisMap.getOrDefault(mois, 1);
    }
}               