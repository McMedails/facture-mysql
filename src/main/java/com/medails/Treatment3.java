package com.medails;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.JOptionPane;

import com.medails.database.DBConnection;

    /************************************************************ 
                        TRAITEMENT DE DONNEES
    *************************************************************/

public class Treatment3
{
    /************* Déclarations Classes ****************/
    private DBConnection db;
    private Display dp;
    private Graphic gr;
    private Treatment1 tr1;
    private Treatment2 tr2;

    /************************************************************ 
                            CONSTRUCTEUR
    *************************************************************/

    public Treatment3(DBConnection db, Display dp, Graphic gr, Treatment1 tr1, Treatment2 tr2)
    {
        this.db = db;
        this.dp = dp;
        this.gr = gr;   ;  
        this.tr1 = tr1;
        this.tr2 = tr2;

        /*********** Appels Méthodes ***************/
        actionJElements();
        calculListener();
        graphDecenal();
        graphYearMonth();   
        gr.updateDatasets(graphYearMonth, gr.GRAPHMONTHS, gr.SHORTCATEGORIES, gr.dataYearsPan3, gr.dataMonthsPan3);                     
    }


    /************************************************************ 
                            VARIABLES
    *************************************************************/
    
    /************************* Variables de classe **************************/
    // Données pour création graphique
    public static Double[][][] graphDecenal = new Double[5][5][5]; 
    public static Double[][][] graphYearMonth = new Double[12][12][12];      

    /************************* Variables d'instance **************************/
    // Répertoire Facture 
    private final String DIRECTORY_DEDUCTION = "M://Multimédia/Bureau/Social/Social - Pc Bureau/01 - Professionnelle/Achats";

    /************************************************************ 
                              METHODES
    *************************************************************/

    private void actionJElements()
    {
        /*********** Partie Graphique ***************/
        dp.sliDecadePan3           .addChangeListener (e -> graphDecenal());
        dp.sliYearMonthPan3        .addChangeListener (e -> graphYearMonth());

        /*********** Partie Text ***************/
        dp.txtTTCPan3              .addActionListener (e -> calculListener());
        dp.boxYearsDeduction       .addActionListener (e -> graphYearMonth());
        dp.butOpenDeduction        .addActionListener (e -> tr1.openPDF(dp.boxRepDeduction, dp.boxPDFDeduction));
        dp.butSearchDeduction      .addActionListener (e -> tr1.searchDirectory(dp.boxRepDeduction, dp.boxPDFDeduction, DIRECTORY_DEDUCTION));
        dp.butDeleteDeduction      .addActionListener (e -> tr1.deletePDF(dp.boxPDFDeduction, "deduction", "NameDeduction"));
        dp.butSaveDeduction        .addActionListener (e -> saveDataListener());
                                    tr1.popupListener (dp.boxRepDeduction, dp.boxPDFDeduction, 
                                                        "deduction", "RepDeduction",
                                                        "deduction", "NameDeduction"); 
        dp.butReset3               .addActionListener (e -> clearListener());     
    }

    /*********************************************************** 
                           Onglet Décénie
    ***********************************************************/

    public void graphDecenal()
    {
        // Mise à jour des graphiques avec la nouvelle échelle
        tr2.slideRange(gr.chartDecadePan3, dp.sliDecadePan3); 

        // Initialisation des données graphiques
        gr.dataDecadePan3.clear();

        // Initialisation des données
        double      graphTTC      = 0.0;
        double      graphHT       = 0.0;
        double      graphTVA      = 0.0;
        String      lastYear      = null;
        boolean     refreshYear   = false;

        // Récupère les données de la DB
        List<Map<String, Object>> deductions = db.getDeduction();

        for (Map<String, Object> deduction : deductions)
        {
            String   currentYear    = String.valueOf(deduction.get("DeductionAnnee"));
            String   currentMonth   = (String) deduction.get("DeductionMois");
            double   currentTTC     = (double) deduction.get("TTC");
            double   currentHT      = (double) deduction.get("HT");
            double   currentTVA     = (double) deduction.get("TVA");

            // Vérification changement d'année
            if (refreshYear == false)
            {
                lastYear = currentYear;
                refreshYear = true;
            }

            // Réinitialisation si changement d'année
            if (!lastYear.equals(currentYear))
            {
                refreshYear   = false;
                graphTTC      = 0.0;
                graphHT       = 0.0;
                graphTVA      = 0.0;         
            }

            // Cummule des résultats
            graphTTC   +=  currentTTC;
            graphHT    +=  currentHT;
            graphTVA   +=  currentTVA;


            /************************* GRAPHIQUE **************************/ 
            // Trouve le mois correspondant et stocke la valeur dans le tableau
            for (int ii = 0; ii < gr.GRAPHYEARS.length; ii++)
            {
                if (gr.GRAPHYEARS[ii].equals(currentYear))
                {
                    graphDecenal[ii][ii][0] = graphTTC;   
                    graphDecenal[ii][ii][1] = graphTVA;        
                    graphDecenal[ii][ii][2] = graphHT; 
                }
            }
            
            // Renvoie des données calculées vers le graphique
            gr.updateDatasets(graphDecenal, gr.GRAPHYEARS, gr.SHORTCATEGORIES, gr.dataDecadePan3); 
        }
    }

    /*********************************************************** 
                    Onglet Annuel / Mensuel
    ***********************************************************/

    public void graphYearMonth()
    {
        // Mise à jour des graphiques avec la nouvelle échelle
        tr2.slideRange(gr.chartYearsPan3, dp.sliYearMonthPan3);
        tr2.slideRange(gr.chartMonthsPan3, dp.sliYearMonthPan3);  

        // Initialisation des données graphiques
        gr.dataYearsPan3.clear();
        gr.dataMonthsPan3.clear();

        // Initialisation des données
        String      lastYear        = null;
        boolean     refreshYear     = false;

        // Récupère l'année sélectionnée dans la ComboBox
        String selectedYear = dp.boxYearsDeduction.getSelectedItem().toString();

        // Récupère les données de la DB
        List<Map<String, Object>> deductions = db.getDeduction();

        // Réinitialiation du tableau de données
        for (int ii = 0; ii < gr.GRAPHMONTHS.length; ii++)
        {
            for (int jj = 0; jj < gr.SHORTCATEGORIES.length; jj++)
            {
                graphYearMonth[ii][ii][jj] = null;
            }
        }

        for (Map<String, Object> deduction : deductions)
        {
            String   currentYear    = String.valueOf(deduction.get("DeductionAnnee"));

            // Ne traiter que les données de l'année sélectionnée
            if (!currentYear.equals(selectedYear))
            {   continue;   }

            String   currentMonth   = (String) deduction.get("DeductionMois");
            double   currentTTC     = (double) deduction.get("TTC");
            double   currentTVA     = (double) deduction.get("TVA");
            double   currentHT      = (double) deduction.get("HT");

            // Vérifie changement d'année
            if (refreshYear == false)
            {
                lastYear = currentYear;
                refreshYear = true;
            }

            // Réinitialise si changement d'année
            if (!lastYear.equals(currentYear))
            {
                refreshYear = false;
            }

            /************************* GRAPHIQUE **************************/  

            if (lastYear.equals(currentYear))
            {
                // Trouve le mois correspondant et stocke la valeur dans le tableau
                for (int ii = 0; ii < gr.GRAPHMONTHS.length; ii++)
                {
                    if (gr.GRAPHMONTHS[ii].equals(currentMonth))
                    {
                        graphYearMonth[ii][ii][0] = currentTTC;   
                        graphYearMonth[ii][ii][1] = currentTVA;        
                        graphYearMonth[ii][ii][2] = currentHT; 
                    }
                }
            }

            // Renvoie des données calculées vers le graphique
            gr.updateDatasets(graphYearMonth, gr.GRAPHMONTHS, gr.SHORTCATEGORIES, gr.dataYearsPan3, gr.dataMonthsPan3); 
        }
    }

    /*********************************************************** 
                          Autres Méthodes
    ***********************************************************/

    // Calcule HT/TVA
    private void calculListener()
    {
        // Vérification cellule non-vide
        if (!dp.txtTTCPan3.getText().isEmpty())
        {
            try
            {
                double TTC = Double.parseDouble(dp.txtTTCPan3.getText()); 
                double HT  = TTC / 1.2;
                double TVA = TTC - HT;
                                
                // Report -> TTC
                String repportTTC = String.format(Locale.US, "%.2f", TTC);
                dp.txtTTCPan3.setText(repportTTC);

                // Report -> HT
                String repportHT = String.format(Locale.US, "%.2f",  HT);
                dp.txtHTPan3.setText(repportHT);
                
                // Report -> TVA
                String repportTVA = String.format(Locale.US, "%.2f", TVA);
                dp.txtTVAPan3.setText(repportTVA);
            }
            catch (NumberFormatException e)
            {
                // Si le text n'est pas un nombre valide
                dp.txtTTCPan3.setText("");
                dp.txtHTPan3.setText("");
                dp.txtTVAPan3.setText("");
            }
        }
        else
        {
            // Efface si le champ TTC est vide
            dp.txtHTPan3.setText("");
            dp.txtTVAPan3.setText("");           
        }         
    }


   // F2 -> Enrengistrer
    public void saveDataListener()
    {
        // Vérification cellule non-vide : Déduction
        if (dp.dateDeduction.getDate() == null || dp.txtTTCPan3.getText().isEmpty() || 
        		dp.txtHTPan3.getText().isEmpty()  || dp.txtTVAPan3.getText().isEmpty()  || 
        			dp.boxRepDeduction.getSelectedItem() == null || dp.boxPDFDeduction.getSelectedItem() == null)
        {
            JOptionPane.showMessageDialog(null, "Tous les champs doivent être renseignés",
                                                "Champs vides", 
                                                JOptionPane.ERROR_MESSAGE);
            return;
        }

        try
        {
	        Date getPay = dp.dateDeduction.getDate();
	        SimpleDateFormat sdfYear  = new SimpleDateFormat("yyyy", Locale.FRENCH);
	        SimpleDateFormat sdfMonth = new SimpleDateFormat("MMMM", Locale.FRENCH);
	        SimpleDateFormat sdfDay   = new SimpleDateFormat("dd"  , Locale.FRENCH);
	
	        // Récupération des valeurs depuis l'infercace utilisateur
	        Map<String, Object> deductionData = new HashMap<>();
	
	        /* A1 */ deductionData.  put("DeductionAnnee",     Integer.parseInt     (sdfYear.format((getPay))));
	        /* A1 */ deductionData.  put("DeductionMois",      sdfMonth.format      (getPay));
	        /* A1 */ deductionData.  put("DeductionJour",      Integer.parseInt     (sdfDay.format(getPay)));
	        /* B1 */ deductionData.  put("TTC",                Double.parseDouble   (dp.txtTTCPan3.getText()));
	        /* B2 */ deductionData.  put("HT",                 Double.parseDouble   (dp.txtHTPan3.getText()));
	        /* B3 */ deductionData.  put("TVA",                Double.parseDouble   (dp.txtTVAPan3.getText()));
            /* D1 */ deductionData.  put("RepDeduction",                            (String) dp.boxRepDeduction.getSelectedItem());
		    /* E1 */ deductionData.  put("NameDeduction",                           (String) dp.boxPDFDeduction.getSelectedItem());
	
	        // Vérification si la ligne existe déjà
	        List<Map<String, Object>> existingPDFData = db.getDeduction();
		        
	        boolean exists = existingPDFData.stream().anyMatch(f -> 
	        {
	        	String nameDeduction = f.containsKey("NameDeduction") ? (String) f.get("NameDeduction") : null;
		        String newNameDeduction  = (String) deductionData.get("NameDeduction");
		        		        
		        // Comparaison stricte, mais en tenant compte des cas nuls
	        	return (newNameDeduction  != null && newNameDeduction.equals(nameDeduction));
	        });
	         
	        if (exists)
	        {
	            JOptionPane.showMessageDialog(dp.fen, "Une facture pour ce mois existe déjà",
	                                                  "Doublon",
	                                                  JOptionPane.WARNING_MESSAGE);
	            return;
	        }
	
	        // Insertion dans la base de données
	        db.setDeductionData(deductionData);
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
    

    // F3 -> RAZ
    private void clearListener()
    {
        /* A1 */ dp.dateDeduction.setDate(null); 
        /* B1 */ dp.txtTTCPan3.setText(""); 
        /* B2 */ dp.txtHTPan3.setText(""); 
        /* B3 */ dp.txtTVAPan3.setText(""); 
        /* D1 */ dp.boxRepDeduction.setSelectedItem("");
        /* D2 */ dp.boxPDFDeduction.setSelectedItem("");
    } 
}