package com.medails;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
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

public class Treatment4
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

    public Treatment4(DBConnection db, Display dp, Graphic gr, Treatment1 tr1, Treatment2 tr2)
    {
        this.db = db;
        this.dp = dp;
        this.gr = gr;     
        this.tr1 = tr1;
        this.tr2 = tr2;

        /*********** Appels Méthodes ***************/
        actionJElements();
        graphDecenal();
        graphYearMonth();   
        gr.updateDatasets(graphYearMonth, gr.GRAPHMONTHS, gr.CHOMAGE, gr.dataYearsPan4, gr.dataMonthsPan4);                     
    }


    /************************************************************ 
                            VARIABLES
    *************************************************************/
    
    /************************* Variables de classe **************************/
    // Données pour création graphique
    public static Double[] graphDecenal = new Double[10]; 
    public static Double[] graphYearMonth = new Double[12];      

    /************************* Variables d'instance **************************/
    // Répertoire Facture 
    private final String DIRECTORY_CHOMAGE = "M:\\Multimédia\\Bureau\\Social\\Social - Pc Bureau\\00 - Gouvernement\\Pole Emploi\\Actualisation";

    /************************************************************ 
                              METHODES
    *************************************************************/

    private void actionJElements()
    {
        /*********** Partie Graphique ***************/
        dp.sliDecadePan4           .addChangeListener (e -> graphDecenal());
        dp.sliYearMonthPan4        .addChangeListener (e -> graphYearMonth());

        /*********** Partie Text ***************/
        dp.txtDayChomage            .addActionListener (e -> calculListener());
        dp.txtAREChomage            .addActionListener (e -> calculListener());
        dp.boxYearsChomage          .addActionListener (e -> graphYearMonth());
        dp.butOpenChomage           .addActionListener (e -> tr1.openPDF(dp.boxRepChomage, dp.boxPDFChomage));
        dp.butSearchChomage         .addActionListener (e -> tr1.searchDirectory(dp.boxRepChomage, dp.boxPDFChomage, DIRECTORY_CHOMAGE));
        dp.butDeleteChomage         .addActionListener (e -> db.deleteInBDD(dp.boxPDFChomage, "chomage", "NameChomage"));
        dp.butSaveChomage           .addActionListener (e -> saveDataListener());
                                    tr1.popupListener (dp.boxRepChomage, dp.boxPDFChomage, 
                                                        "chomage", "RepChomage",
                                                        "chomage", "NameChomage"); 
        dp.butReset4               .addActionListener (e -> clearListener());  
        

        // Solution Lambda pour mettre à jour les champs
        db.boxPDFListener(dp.boxPDFChomage, data -> 
        {
            int     annee  = (int)     data.get("ChomageAnnee");
            String  mois   = (String)  data.get("ChomageMois");
            int     jour   = (int)     data.get("ChomageJour");
            dp.dateChomage.setDate(Date.from(LocalDate.of(annee, db.convertMonth(mois), jour)
            .atStartOfDay(ZoneId.systemDefault()).toInstant()));

            dp.boxMonthsChomage.setSelectedItem (data.get("MoisActualisation"));
            dp.txtDayChomage.setText            (data.get("JoursParMois")   .toString());
            dp.txtQChomage.setText              (data.get("Coefficient")    .toString());
            dp.txtAREChomage.setText            (data.get("Montant")        .toString()); 
        });
    }

    /***********************************************************  
                           Onglet Décénie
    ***********************************************************/

    public void graphDecenal()
    {
        // Mise à jour des graphiques avec la nouvelle échelle
        tr2.slideRange(gr.chartDecadePan4, dp.sliDecadePan4); 

        // Initialisation des données graphiques
        gr.dataDecadePan4.clear();

        // Initialisation des données
        double      graphARE      = 0.0;
        String      lastYear      = null;
        boolean     refreshYear   = false;

        // Récupère les données de la DB
        List<Map<String, Object>> chomages = db.getChomage();

        for (Map<String, Object> chomage : chomages)
        {
            String   currentYear    = String.valueOf(chomage.get("ChomageAnnee"));
            String   currentMonth   = (String) chomage.get("ChomageMois");
            double   currentARE     = (double) chomage.get("Montant");

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
                graphARE      = 0.0;        
            }

            // Cummule des résultats
            graphARE   +=  currentARE;

            /************************* GRAPHIQUE **************************/ 
            // Trouve le mois correspondant et stocke la valeur dans le tableau
            for (int ii = 0; ii < gr.LONGRAPHYEARS.length; ii++)
            {
                if (gr.LONGRAPHYEARS[ii].equals(currentYear))
                {
                    graphDecenal[ii] = graphARE;   
                }
            }
            
            // Renvoie des données calculées vers le graphique
            gr.updateDatasets(graphDecenal, gr.LONGRAPHYEARS, gr.CHOMAGE, gr.dataDecadePan4); 
        }
    }

    /*********************************************************** 
                    Onglet Annuel / Mensuel
    ***********************************************************/

    public void graphYearMonth()
    {
        // Mise à jour des graphiques avec la nouvelle échelle
        tr2.slideRange(gr.chartYearsPan4, dp.sliYearMonthPan4);
        tr2.slideRange(gr.chartMonthsPan4, dp.sliYearMonthPan4);  

        // Initialisation des données graphiques
        gr.dataYearsPan4.clear();
        gr.dataMonthsPan4.clear();

        // Initialisation des données
        String      lastYear        = null;
        boolean     refreshYear     = false;

        // Récupère l'année sélectionnée dans la ComboBox
        String selectedYear = dp.boxYearsChomage.getSelectedItem().toString();

        // Récupère les données de la DB
        List<Map<String, Object>> chomages = db.getChomage();

        // Réinitialiation du tableau de données
        for (int ii = 0; ii < gr.GRAPHMONTHS.length; ii++)
        {
            for (int jj = 0; jj < gr.CHOMAGE.length; jj++)
            {
                graphYearMonth[ii] = null;
            }
        }

        for (Map<String, Object> chomage : chomages)
        {
            String   currentYear    = String.valueOf(chomage.get("ChomageAnnee"));

            // Ne traiter que les données de l'année sélectionnée
            if (!currentYear.equals(selectedYear))
            {   continue;   }

            String   currentMonth   = (String) chomage.get("ChomageMois");
            double   currentARE     = (double) chomage.get("Montant");

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
                        graphYearMonth[ii] = currentARE;   
                    }
                }
            }

            // Renvoie des données calculées vers le graphique
            gr.updateDatasets(graphYearMonth, gr.GRAPHMONTHS, gr.CHOMAGE, gr.dataYearsPan4, gr.dataMonthsPan4); 
        }
    }

    /*********************************************************** 
                          Autres Méthodes
    ***********************************************************/


   // F2 -> Enrengistrer
    public void saveDataListener()
    {
        // Vérification cellule non-vide : Déduction
        if (dp.dateChomage.getDate() == null || dp.txtDayChomage.getText().isEmpty() || 
        		dp.txtQChomage.getText().isEmpty()  || dp.txtAREChomage.getText().isEmpty()  || 
        			dp.boxRepChomage.getSelectedItem() == null || dp.boxPDFChomage.getSelectedItem() == null)
        {
            JOptionPane.showMessageDialog(null, "Tous les champs doivent être renseignés",
                                                "Champs vides", 
                                                JOptionPane.ERROR_MESSAGE);
            return;
        }

        try
        {
	        Date getPay = dp.dateChomage.getDate();
	        SimpleDateFormat sdfYear  = new SimpleDateFormat("yyyy", Locale.FRENCH);
	        SimpleDateFormat sdfMonth = new SimpleDateFormat("MMMM", Locale.FRENCH);
	        SimpleDateFormat sdfDay   = new SimpleDateFormat("dd"  , Locale.FRENCH);
	
	        // Récupération des valeurs depuis l'infercace utilisateur
	        Map<String, Object> chomageData = new HashMap<>();
	
	        /* B1 */ chomageData.  put("ChomageAnnee",       Integer.parseInt     (sdfYear.format((getPay))));
	        /* B1 */ chomageData.  put("ChomageMois",        sdfMonth.format      (getPay));
	        /* B1 */ chomageData.  put("ChomageJour",        Integer.parseInt     (sdfDay.format(getPay)));
            /* B2 */ chomageData.  put("MoisActualisation",                       (String) dp.boxMonthsChomage.getSelectedItem());
	        /* C1 */ chomageData.  put("JoursParMois",       Integer.parseInt     (dp.txtDayChomage.getText()));
	        /* C2 */ chomageData.  put("Coefficient",        Double.parseDouble   (dp.txtQChomage.getText()));
	        /* C3 */ chomageData.  put("Montant",            Double.parseDouble   (dp.txtAREChomage.getText()));
            /* D1 */ chomageData.  put("RepChomage",                              (String) dp.boxRepChomage.getSelectedItem());
		    /* E1 */ chomageData.  put("NameChomage",                             (String) dp.boxPDFChomage.getSelectedItem());
	
	        // Vérification si la ligne existe déjà
	        List<Map<String, Object>> existingPDFData = db.getChomage();
		        
	        boolean exists = existingPDFData.stream().anyMatch(f -> 
	        {
	        	String nameChomage = f.containsKey("NameChomage") ? (String) f.get("NameChomage") : null;
		        String newNameChomage  = (String) chomageData.get("NameChomage");
		        		        
		        // Comparaison stricte, mais en tenant compte des cas nuls
	        	return (newNameChomage  != null && newNameChomage.equals(nameChomage));
	        });
	         
	        if (exists)
	        {
	            JOptionPane.showMessageDialog(dp.fen, "Une actualisation pour ce mois existe déjà",
	                                                  "Doublon",
	                                                  JOptionPane.WARNING_MESSAGE);
	            return;
	        }
	
	        // Insertion dans la base de données
	        db.setChomageData(chomageData);
	        JOptionPane.showMessageDialog(dp.fen, "Actualisation enregistrée avec succès",
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
    

        // Calcule HT/TVA
        private void calculListener()
        {
            // Vérification cellule non-vide
            if (!dp.txtDayChomage.getText().isEmpty() && 
                  !dp.txtAREChomage.getText().isEmpty())
            {
                try
                {
                    double Days = Double.parseDouble(dp.txtDayChomage.getText());
                    double ARE = Double.parseDouble(dp.txtAREChomage.getText());
                    double Taux  = ARE / Days;
                                    
                    // Report -> TTC
                    String repportTaux = String.format(Locale.US, "%.2f", Taux);
                    dp.txtQChomage.setText(repportTaux);

                }
                catch (NumberFormatException e)
                {
                    // Si le text n'est pas un nombre valide
                    dp.txtDayChomage.setText("");
                    dp.txtQChomage.setText("");
                    dp.txtAREChomage.setText("");
                }
            }
            else
            {
                // Efface si le champ Jours/Montant sont vides
                dp.txtDayChomage.setText("");
                dp.txtAREChomage.setText("");           
            }         
        }


    // F3 -> RAZ
    private void clearListener()
    {
        /* B1 */ dp.dateChomage.setDate(null); 
        /* B2 */ dp.boxMonthsChomage.setSelectedItem("");
        /* C1 */ dp.txtDayChomage.setText(""); 
        /* C2 */ dp.txtQChomage.setText(""); 
        /* C3 */ dp.txtAREChomage.setText(""); 
        /* D1 */ dp.boxRepChomage.removeAllItems();
        /* E1 */ dp.boxPDFChomage.removeAllItems();
    } 
}