package com.medails;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.medails.database.DBConnection;

    /************************************************************ 
                        TRAITEMENT DE DONNEES
    *************************************************************/

public class Treatment1
{
    /************* Déclarations Classes ****************/
    private DBConnection db;
    private Display dp;

    /************************************************************ 
                            CONSTRUCTEUR
    *************************************************************/

    public Treatment1(DBConnection db, Display dp)
    {
        this.db = db;
        this.dp = dp;
         
        /*********** Appels Méthodes ***************/
        actionJElements();                                                     
    }

    
    /************************************************************ 
                            VARIABLES
    *************************************************************/

    /************************* Variables d'instance **************************/
    // Taux de taxe
    public final Double ACRE2024 = ((2.2 + 11.6 + 0.2) / 100);  // Année 2024 (ACRE)
    public final Double ACRE2025 = ((2.2 + 12.3 + 0.2) / 100);  // Année 2025 (ACRE)
    public final Double SANS2025 = ((2.2 + 24.6 + 0.2) / 100);  // Année 2025 (sans ACRE)
    public final Double SANS20XX = ((2.2 + 26.1 + 0.2) / 100);  // Année 2026 ou plus
    public final Double TVA = 1.2;

    // Répertoire Facture 
    private final String DIRECTORY_FACTURE = "M://Multimédia/Bureau/Social/Social - Pc Bureau/01 - Professionnelle/Factures";

    // Répertoire Déclaration
    private final String DIRECTORY_DECLA = "M://Multimédia/Bureau/Social/Social - Pc Bureau/00 - Gouvernement/URSSAF/Déclarations";
     
    // Variables pour calcules
    private double currentTTC     = 0.0;
    private double currentHT      = 0.0;
    private double currentTVA     = 0.0;
    private double currentTaxe    = 0.0;
    private double currentBenefit = 0.0;

    /************************************************************ 
                              METHODES
    *************************************************************/

    private void actionJElements()
    {
        /*********** Partie Text ***************/
        dp.butOpenFacture          .addActionListener (e -> openPDF(dp.boxRep1, dp.boxPDF1));
        dp.butOpenDecla            .addActionListener (e -> openPDF(dp.boxRep2, dp.boxPDF2));
        dp.butTVA                  .addActionListener (e -> calculListener());
        dp.butSearchFacture        .addActionListener (e -> searchDirectory(dp.boxRep1, dp.boxPDF1, DIRECTORY_FACTURE));
        dp.butSearchDecla          .addActionListener (e -> searchDirectory(dp.boxRep2, dp.boxPDF2, DIRECTORY_DECLA));
        dp.butSave                 .addActionListener (e -> saveDataListener());  
        dp.butDelete               .addActionListener (e -> { deletePDF(dp.boxPDF1, "facture", "NameFacture");
                                                              deletePDF(dp.boxPDF2, "facture", "NameDecla");});                                  
        							popupListener     (dp.boxRep1, dp.boxPDF1, 
                                                        "facture", "RepFacture",
                                                        "facture", "NameFacture");
                                    popupListener     (dp.boxRep2, dp.boxPDF2, 
                                                        "facture", "RepDecla",
                                                        "facture", "NameDecla");
        dp.butReset1               .addActionListener (e -> clearListener());
    }

    /*********************************************************** 
                              PANEL 1 
    ***********************************************************/
    
    // B3 -> Calculer
    public void calculListener() 
    {
        try
        {
            // Récupération des valeurs inscrites par l'utilisateur
            String boxYear  = (String) dp.boxYears.getSelectedItem();    
            String boxMonth = (String) dp.boxMonths.getSelectedItem();     
            double txtDays  = Double.parseDouble(dp.txtDays.getText());   
            double txtTJM   = Double.parseDouble(dp.txtTJM.getText());    

            // Vérification cellules non-vide
            if ((boxYear == "") || (boxMonth == "")) 
            {
                JOptionPane.showMessageDialog(dp.fen, "Veuillez remplir les champs : \n - Année \n - Mois",
                                                      "Champs de saisie vides", 
                                                      JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Caclule des valeurs 
            currentHT   =  txtDays     *  txtTJM;
            currentTTC  =  currentHT   *  TVA;
            currentTVA  =  currentTTC  -  currentHT;

            /************************* Calcule URSSAF *************************/

            // Année 2024 (ACRE)
            if (boxYear == "2024")
            {
                currentTaxe = currentHT * ACRE2024;
            }

            // Année 2025 (ACRE)
            else if ((boxYear == "2025") && 
                        (boxMonth == "Janvier" ||
                            boxMonth == "Février"||
                                boxMonth == "Mars" ||
                                    boxMonth == "Avril")) 
            {
                currentTaxe = currentHT * ACRE2025;
            }

            // Année 2025 (sans ACRE)
            else if ((boxYear == "2025") && 
                        (boxMonth == "Mai" ||
                            boxMonth == "Juin"||
                                boxMonth == "Juillet" ||
                                    boxMonth == "Août" ||                    
                                        boxMonth == "Septembre" ||                    
                                            boxMonth == "Novembre" ||                    
                                                boxMonth == "Décembre"))                    
            {
                currentTaxe = currentHT * SANS2025;
            }

            // Année 2026 ou plus
            else
            {
                currentTaxe = currentHT * SANS20XX;
            }              

            /************************* Report *************************/

            // Report -> TTC
            String reportTTC = String.format(Locale.US, "%.2f", currentTTC);
            dp.txtTTC.setText(reportTTC);

            // Report -> HT
            String reportHT = String.format(Locale.US ,"%.2f", currentHT);
            dp.txtHT.setText(reportHT);           

            // Report -> TVA
            String reportTVA = String.format(Locale.US ,"%.2f", currentTVA);
            dp.txtTVA.setText(reportTVA);

            // Report -> URSSAF
            String reportTaxe = String.format(Locale.US, "%.2f", currentTaxe);
            dp.txtTaxe.setText(reportTaxe);

            // Report -> Bénéfice
            currentBenefit = currentHT - currentTaxe;
            String reportBenefit = String.format(Locale.US, "%.2f", currentBenefit);
            dp.txtBenefit.setText(reportBenefit);
        }
        catch (NumberFormatException ex)
        {   
            // Vérification présence chiffre
            JOptionPane.showMessageDialog(dp.fen, "Veuillez entrez des nombres valides dans les champs : \n - Jours travaillés \n - TJM",
                                                    "Format non-respecté", 
                                                    JOptionPane.WARNING_MESSAGE);
        }
    }


    // Méthode générique pour ouvrir un PDF
    public void openPDF(JComboBox<String> boxRep, JComboBox<String> boxPDF)
    {
        String selectedRep = (String) boxRep.getSelectedItem();
        String selectedPDF = (String) boxPDF.getSelectedItem();

        if (selectedRep == null || selectedPDF == null || selectedRep.isEmpty() || selectedPDF.isEmpty())
        {
            JOptionPane.showMessageDialog(dp.fen, "Veuillez sélectionner un fichier PDF dans l'onglet lien",
                                                  "Erreur", 
                                                  JOptionPane.WARNING_MESSAGE);
            return;
        }

        File file = new File(selectedRep + "/" + selectedPDF);
        try
        {
            if (file.exists())
            {
                Desktop.getDesktop().open(file);
            }
            else
            {
                throw new IOException("Fichier introuvable");
            }
        }
        catch (IOException ex)
        {
            JOptionPane.showMessageDialog(dp.fen, "Le fichier PDF : " + file.getAbsolutePath() + "est introuvable",
                                            "Erreur", 
                                            JOptionPane.WARNING_MESSAGE);
        }
    }
    

    // Méthode générique pour ouvrir un répertoire
    public void searchDirectory(JComboBox<String> boxRep, JComboBox<String> boxPDF, String directory)
    {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(directory));
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileFilter(new FileNameExtensionFilter("Type : .PDF", "pdf"));

        int result = fileChooser.showOpenDialog(dp.fen);
        File selectedRep = fileChooser.getSelectedFile();
        
        if (result == JFileChooser.APPROVE_OPTION)
        {
            if (selectedRep != null)
            {
                // MAJ des répertoires
                String parentDirectory = selectedRep.getParent();
                boxRep.removeAllItems();
                boxRep.addItem(parentDirectory);

                // MAJ des fichiers PDF
                String namePDF = selectedRep.getName();
                boxPDF.removeAllItems();
                boxPDF.addItem(namePDF);
            }
        }
    }


    // Méthode générique les onglets répertoires
    public void popupListener(JComboBox<String> boxRep, JComboBox<String> boxPDF, 
                                                    String tableRep, String columnRep,
                                                        String tablePDF, String columnPDF) 
    {
        boxRep.addPopupMenuListener(new PopupMenuListener() 
        {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) 
            {    
                // Récupération depuis MySQL
                List<String> arrayRep = db.getDirPDF(tableRep, columnRep);
                List<String> arrayPDF = db.getDirPDF(tablePDF, columnPDF); 
        
                // Mise à jour des ComboBox
                updateComboBox(boxRep, arrayRep);
                updateComboBox(boxPDF, arrayPDF);
            }
            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
    
            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {}
        });
    }
    
    public void updateComboBox (JComboBox<String> comboBox, List<String> allItems)
    {
        comboBox.removeAllItems();
        allItems.forEach(comboBox::addItem);
        comboBox.setSelectedIndex(-1);
    }
    

    // K1 -> Suppression des lignes dans la BDD
    public void deletePDF(JComboBox box, String nameTable, String nameColumn)
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

        if (confirm != JOptionPane.YES_OPTION)  {   return;   }

        // Supprimer les PDF sélectionnés un par un
        db.deleteInBDD(box, nameTable, nameColumn);
        JOptionPane.showMessageDialog(dp.fen, "Suppression des PDF sélectionnés terminée",
                                              "Opération réussie", 
                                              JOptionPane.INFORMATION_MESSAGE);
    }


    // K2 -> Enrengistrer
    public void saveDataListener()
    {
        // Vérification cellules non-vide
        if (dp.txtDays.getText().isEmpty() || dp.txtTJM.getText().isEmpty() || 
        	    dp.txtTTC.getText().isEmpty() || dp.txtHT.getText().isEmpty() ||
        	    	dp.txtTVA.getText().isEmpty() || dp.txtTaxe.getText().isEmpty() ||
        	    		dp.txtBenefit.getText().isEmpty())
        {
            JOptionPane.showMessageDialog(dp.fen, "Tous les champs doivent être renseignés",
                                                    "Champs manquants", 
                                                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try
        {
	        Date getPay = dp.datePay.getDate();
	        SimpleDateFormat sdfYear  = new SimpleDateFormat("yyyy", Locale.FRENCH);
	        SimpleDateFormat sdfMonth = new SimpleDateFormat("MMMM", Locale.FRENCH);
	        SimpleDateFormat sdfDay   = new SimpleDateFormat("dd"  , Locale.FRENCH);
	
	        // Récupération des valeurs depuis l'infercace utilisateur
	        Map<String, Object> factureData   = new HashMap<>();
	
	        /* A1 */  factureData.  put("FactureAnnee",        Integer.parseInt       ((String) dp.boxYears.getSelectedItem()));
	        /* A2 */  factureData.  put("FactureMois",                                (String) dp.boxMonths.getSelectedItem());
	        /* A3 */  factureData.  put("VersementAnnee",      Integer.parseInt       ((sdfYear.format(getPay))));
	        /* A3 */  factureData.  put("VersementMois",       sdfMonth.format        (getPay));
	        /* A3 */  factureData.  put("VersementJour",       Integer.parseInt       (sdfDay.format(getPay)));
	        /* B1 */  factureData.  put("Jours",               Double.parseDouble     (dp.txtDays.getText()));
	        /* B2 */  factureData.  put("TJM",                 Double.parseDouble     (dp.txtTJM.getText()));
	        /* C1 */  factureData.  put("TTC",                 Double.parseDouble     (dp.txtTTC.getText()));
	        /* C2 */  factureData.  put("HT",                  Double.parseDouble     (dp.txtHT.getText()));
	        /* C3 */  factureData.  put("TVA",                 Double.parseDouble     (dp.txtTVA.getText()));
	        /* D1 */  factureData.  put("Taxes",               Double.parseDouble     (dp.txtTaxe.getText()));
	        /* D2 */  factureData.  put("Benefices",           Double.parseDouble     (dp.txtBenefit.getText()));
            /* F1 */  factureData.  put("RepFacture",                                 (String) dp.boxRep1.getSelectedItem());
	        /* G1 */  factureData.  put("RepDecla",                                   (String) dp.boxRep2.getSelectedItem());
	        /* I1 */  factureData.  put("NameFacture",                                (String) dp.boxPDF1.getSelectedItem());
	        /* J1 */  factureData.  put("NameDecla",                                  (String) dp.boxPDF2.getSelectedItem());
	
	        // Vérification de l'existence du fichier 
	        List<Map<String, Object>> existingPDFData = db.getFacture();
	     	        
	        boolean exists = existingPDFData.stream().anyMatch(f -> 
	        {
	           	String nameFacture = f.containsKey("NameFacture") ? (String) f.get("NameFacture") : null;
	           	String nameDecla   = f.containsKey("NameDecla")   ? (String) f.get("NameDecla") : null;
	           	String newNameFacture  = (String) factureData.get("NameFacture");
	            String newNameDecla    = (String) factureData.get("NameDecla");
	        	        
	           // Comparaison stricte, mais en tenant compte des cas nuls
	           return (newNameFacture != null && newNameFacture.equals(nameFacture)) ||
	        		   (newNameDecla  != null && newNameDecla.equals(nameDecla));
	        });
	
	        if (exists)
	        {
	            JOptionPane.showMessageDialog(dp.fen, "Une facture pour ce mois existe déjà",
	                                                  "Doublon",
	                                                  JOptionPane.WARNING_MESSAGE);
	            return;
	        }
	
	        // Insertion dans la base de données
	        db.setFactureData(factureData);
	        JOptionPane.showMessageDialog(dp.fen, "Facture enregistrée avec succès",
	                                              "Enregistement réussi !",
	                                              JOptionPane.INFORMATION_MESSAGE);
        }
        catch (NumberFormatException e)
        {
            JOptionPane.showMessageDialog(dp.fen, "Erreur de format numérique : " + e.getMessage(),
            									  "Erreur",
            									  JOptionPane.ERROR_MESSAGE);       	
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(dp.fen, "Erreur lors de l'enregistrement : " + e.getMessage(),
					  							  "Erreur",
					  							  JOptionPane.ERROR_MESSAGE);      	
        }
    }


    // K3 -> RAZ
    public void clearListener()
    {
        /* A1 */ dp.boxYears.setSelectedItem("");
        /* A2 */ dp.boxMonths.setSelectedItem("");
        /* A3 */ dp.datePay.setDate(null); 
        /* B1 */ dp.txtDays.setText("");  
        /* B2 */ dp.txtTJM.setText("");
        /* C1 */ dp.txtTTC.setText("");
        /* C2 */ dp.txtHT.setText("");
        /* C3 */ dp.txtTVA.setText("");
        /* D1 */ dp.txtTaxe.setText("");
        /* D2 */ dp.txtBenefit.setText("");
        /* F1 */ dp.boxRep1.removeAllItems();
        /* G1 */ dp.boxPDF1.removeAllItems();
        /* I1 */ dp.boxRep2.removeAllItems();
        /* J1 */ dp.boxPDF2.removeAllItems(); 
    } 
}