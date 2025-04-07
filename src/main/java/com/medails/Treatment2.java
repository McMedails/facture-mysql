package com.medails;

import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JSlider;

import org.jfree.chart.JFreeChart;

import com.medails.database.DBConnection;

    /************************************************************ 
                        TRAITEMENT DE DONNEES
    *************************************************************/

public class Treatment2
{
    /************* Déclarations Classes ****************/
    private DBConnection db;
    private Display dp;
    private Graphic gr;

    /************************************************************ 
                            CONSTRUCTEUR
    *************************************************************/

    public Treatment2(DBConnection db, Display dp, Graphic gr)
    {
        this.db = db;
        this.dp = dp;
        this.gr = gr;  
                              
        /*********** Appels Méthodes ***************/
        actionJElements(); 
        graphDecenal();
        graphYearMonth();
        gr.updateDatasets(graphYearMonth, gr.GRAPHMONTHS, gr.CATEGORIES, gr.dataYearsPan2, gr.dataMonthsPan2);                               
    }
    

    /************************************************************ 
                            VARIABLES
    *************************************************************/

    /************************* Variables de classe **************************/
    // Données pour création graphique
    public static Double[][][][][] graphDecenal = new Double[5][5][5][5][5];
    public static Double[][][][][] graphYearMonth = new Double[12][12][12][12][12];

    /************************************************************ 
                              METHODES
    *************************************************************/

    private void actionJElements()
    {
        /*********** Partie Graphique ***************/
        dp.sliDecadePan2           .addChangeListener (e -> graphDecenal());
        dp.sliYearMonthPan2        .addChangeListener (e -> graphYearMonth());
        dp.boxYearsTotal           .addActionListener (e -> graphYearMonth());
        dp.cckTTCPan2              .addActionListener (e -> graphYearMonth());
        dp.cckTVAPan2              .addActionListener (e -> graphYearMonth());
        dp.cckHTPan2               .addActionListener (e -> graphYearMonth());
        dp.cckTaxePan2             .addActionListener (e -> graphYearMonth());
        dp.cckBenefitPan2          .addActionListener (e -> graphYearMonth());
        dp.butReset2               .addActionListener (e -> clearListener());
    }

    /*********************************************************** 
                           Onglet Décénie
    ***********************************************************/
    
    public void graphDecenal()
    {
        // Mise à jour des graphiques avec la nouvelle échelle
        slideRange(gr.chartDecadePan2, dp.sliDecadePan2);     

        // Initialisation des données graphiques
        gr.dataDecadePan2.clear();

        // Initialisation des données
        double      graphTTC        = 0.0;      
        double      graphHT         = 0.0;
        double      graphTVA        = 0.0;
        double      graphTaxe       = 0.0;
        double      graphBenefit    = 0.0;
        String      lastYear        = null;
        boolean     refreshYear     = false;

        // Récupère les données de la DB
        List<Map<String, Object>> factures = db.getFacture();

        for (Map<String, Object> facture : factures)
        {
            String   currentYear      = String.valueOf(facture.get("VersementAnnee"));
            String   currentMonth     = (String) facture.get("VersementMois");
            double   currentTTC       = (double) facture.get("TTC");
            double   currentHT        = (double) facture.get("HT");
            double   currentTVA       = (double) facture.get("TVA");
            double   currentTaxe      = (double) facture.get("Taxes");
            double   currentBenefit   = (double) facture.get("Benefices");
                                  
            // Vérification changement d'année
            if (refreshYear == false)
            { 
                lastYear = currentYear; 
                refreshYear = true; 
            }
        
            // Réinitialise si changement d'année
            if (!lastYear.equals(currentYear))
            { 
                refreshYear     = false; 
                graphTTC        = 0.0;  
                graphHT         = 0.0;  
                graphTVA        = 0.0;
                graphTaxe       = 0.0;   
                graphBenefit    = 0.0;
            }

            // Cummule des résultats
            graphTTC      +=  currentTTC;   
            graphHT       +=  currentHT;    
            graphTVA      +=  currentTVA;    
            graphTaxe     +=  currentTaxe;   
            graphBenefit  +=  currentBenefit;

            /************************* GRAPHIQUE **************************/                     
            // Trouve le mois correspondant et stocke la valeur dans le tableau
            for (int kk = 0; kk < gr.GRAPHYEARS.length; kk++)
            {
                if (gr.GRAPHYEARS[kk].equals(currentYear)) 
                {
                    graphDecenal[kk][kk][kk][kk][0] = graphTTC;      
                    graphDecenal[kk][kk][kk][kk][1] = graphTVA;     
                    graphDecenal[kk][kk][kk][kk][2] = graphHT; 
                    graphDecenal[kk][kk][kk][kk][3] = graphTaxe;   
                    graphDecenal[kk][kk][kk][kk][4] = graphBenefit;  
                }
            }
                
            // Renvoie des données calculées vers le graphique
            gr.updateDatasets(graphDecenal, gr.GRAPHYEARS, gr.CATEGORIES, gr.dataDecadePan2); 
        }
    }

    /*********************************************************** 
                    Onglet Annuel / Mensuel
    ***********************************************************/
    
    public void graphYearMonth()
    {
        // Mise à jour des graphiques avec la nouvelle échelle
        slideRange(gr.chartYearsPan2, dp.sliYearMonthPan2);
        slideRange(gr.chartMonthsPan2, dp.sliYearMonthPan2);

        // Initialisation des données graphiques
        gr.dataYearsPan2.clear();
        gr.dataMonthsPan2.clear();

        // Initialisation des données
        String      lastYear        = null;
        boolean     refreshYear     = false;

        // Récupère l'année sélectionnée dans la ComboBox
        String selectedYear = dp.boxYearsTotal.getSelectedItem().toString();

        // Récupère les données de la DB
        List<Map<String, Object>> factures = db.getFacture();

        // Réinitialisation du tableau de données
        for (int ii = 0; ii < gr.GRAPHMONTHS.length; ii++)
        {
            for (int jj = 0; jj < gr.CATEGORIES.length; jj++)
            {
                graphYearMonth[ii][ii][ii][ii][jj] = null;
            }
        }

        for (Map<String, Object> facture : factures)
        {
            String   currentYear      = String.valueOf(facture.get("VersementAnnee"));

            // Ne traiter que les données de l'année sélectionée
            if (!currentYear.equals(selectedYear))
            {   continue;   }

            String   currentMonth     = (String) facture.get("VersementMois");
            double   currentTTC       = (double) facture.get("TTC");
            double   currentHT        = (double) facture.get("HT");
            double   currentTVA       = (double) facture.get("TVA");
            double   currentTaxe      = (double) facture.get("Taxes");
            double   currentBenefit   = (double) facture.get("Benefices");
                                  
            // Vérification changement d'année
            if (refreshYear == false)
            { 
                lastYear = currentYear; 
                refreshYear = true; 
            }
        
            // Réinitialise si changement d'année
            if (!lastYear.equals(currentYear))
            { 
                refreshYear     = false; 
            }
    
            /************************* GRAPHIQUE **************************/   
            
            if (lastYear.equals(currentYear))
            {
                // Trouve le mois correspondant et stocke la valeur dans le tableau
                Object[][] filters = 
                {
                    { dp.cckTTCPan2,        0,  currentTTC},
                    { dp.cckTVAPan2,        1,  currentTVA},
                    { dp.cckHTPan2,         2,  currentHT},
                    { dp.cckTaxePan2,       3,  currentTaxe},
                    { dp.cckBenefitPan2,    4,  currentBenefit},
                };

                for (Object[] filter : filters) 
                {
                    JCheckBox checkBox = (JCheckBox) filter[0];
                    int index          = (int)       filter[1];
                    double value       = (double)    filter[2];

                    // Vérifie si le checkbox est cochée
                    if (filterGraph(checkBox))
                    {
                        // Trouve le mois correspondant et stocke la valeur dans le tableau
                        for (int kk = 0; kk < gr.GRAPHMONTHS.length; kk++)
                        {
                            if (gr.GRAPHMONTHS[kk].equals(currentMonth)) 
                            {
                                graphYearMonth[kk][kk][kk][kk][index] = value;      
                            }
                            else
                            {
                                graphYearMonth[kk][kk][kk][kk][index] = null;
                            }
                        }
                    }
                }
            }
                
            // Renvoie des données calculées vers le graphique
            gr.updateDatasets(graphYearMonth, gr.GRAPHMONTHS, gr.CATEGORIES, gr.dataYearsPan2, gr.dataMonthsPan2);
        }
    }

    /*********************************************************** 
                          Autres Méthodes
    ***********************************************************/

    // Mise à jour des graphiques avec la nouvelle échelle
    public void slideRange(JFreeChart chart, JSlider slide)
    {
        int range = slide.getValue();
        gr.updatChartRange(chart, range);
    }


    // CheckBox Graphique
    public boolean filterGraph(JCheckBox checkBox)
    { return checkBox.isSelected(); }


    // D3 -> RAZ
    public void clearListener()
    {
        /* B1 */ dp.boxYearsTotal.setSelectedItem("");
        /* C1 */ dp.txtTotalTTC.setText("");
        /* C2 */ dp.txtTotalHT.setText("");  
        /* C3 */ dp.txtTotalTVA.setText("");
        /* D1 */ dp.txtTotalTaxe.setText("");
        /* D2 */ dp.txtTotalBenefit.setText("");    
    }
}