package com.medails;

import javax.swing.SwingUtilities;

import com.medails.database.DBConnection;

    /*********************************************************** 
                          DEMARAGE PROGRAMME
    ***********************************************************/

public class AppFacture
{                  
    public static void main (String[]args)
    {
        // Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                // Connexion à la base de données
                DBConnection.connect();

                Display display = new Display();
                Graphic graphic = new Graphic(display);
                DBConnection dbConnection = new DBConnection(display);
                Treatment1 treatment1 = new Treatment1(dbConnection, display);  
                Treatment2 treatment2 = new Treatment2(dbConnection, display, graphic); 
                Treatment3 treatment3 = new Treatment3(dbConnection, display, graphic, treatment1, treatment2); 
                Treatment4 treatment4 = new Treatment4(dbConnection, display, graphic, treatment1, treatment2); 
            }
        });
    }
}

/*             ____________________________________________________________       
              || Enregistrement|Graphique|Déduction                       ||                                   
              ||                                                          ||
              || Facture                                                  ||
              ||                                                          ||
              ||      Année             Mois         Date de paiement     ||
              ||     [_(A1)_]>        [_(A2)_]>   [_______(A3)________]>  ||                     
              ||                                                          ||
              ||                                                          ||
              || Jours travaillées      TJM                               ||
              ||     [_(B1)_]         [_(B2)_]        [Calculer](B3)      ||
              ||                                                          ||
              ||                                                          ||
              ||       TTC               HT              TVA              ||
              ||     [_(C1)_]         [_(C2)_]         [_(C3)_]           ||
              ||                                                          ||
              ||                                                          ||
              || URSSAF                                                   ||
              ||                                                          ||
              ||   Montant taxe       Bénéfices                           || 
              ||     [_(D1)_]         [_(D2)_]                            ||               
              ||                                                          ||
              ||                                                          ||
              || Liens                                                    ||
              ||                                                          ||
              ||      Facture            [Ouvrir](E1)   [Parcourir](E2)   ||
              ||     [____________________(F1)_____________________]>     ||
              ||     [____________________(G1)_____________________]>     ||
              ||                                                          ||
              ||      Déclaration        [Ouvrir](H1)   [Parcourir](H2)   ||
              ||     [____________________(I1)____________________]>      ||
              |      [____________________(J1)____________________]>      ||
              ||                                                          ||
              ||    [Supprimer](K1)	   [Enrengistrer](K2)    [RAZ](K3)    ||
              ||__________________________________________________________||

               ____________________________________________________________  
              || Enregistrement|Graphique|Déduction                       ||
              ||                                                          ||
              ||     Décénie          <>-----------------------------     || 
              ||     Déductible       <>-----------------------------     || 
              ||   ___________________________________________________    ||
              ||  |                                                   |   ||
              ||  |                                                   |   || 
              ||  |                                                   |   ||
              ||  |                                                   |   ||
              ||  |                                                   |   ||
              ||  |                                                   |   ||
              ||  |                                                   |   ||
              ||  |                                                   |   ||
              ||  |                                                   |   ||
              ||  |                                                   |   ||
              ||  |                                                   |   ||
              ||  |                                                   |   || 
              ||  |                                                   |   || 
              ||  |                                                   |   ||
              ||  |___________________________________________________|   ||
              ||   Décénie|Annuel|Mensuel    [Déduction](A1)  [_(A2)_]>   ||
              ||                                                          ||
              ||      TTC     TVA     HT      URSSAF      Bénéfices       ||  
              ||     ■(B1)    ■(B2)  ■(B3)     ■(B4)        ■(B5)         ||
              ||                                                          ||
              ||                                                          ||
              ||                                                          ||
              ||                                                          ||
              ||__________________________________________________________||    
              
               ____________________________________________________________  
              || Enregistrement|Graphique|Déduction                       || 
              ||                                                          ||
              ||     Décénie          <>-----------------------------     || 
              ||     Déductible       <>-----------------------------     || 
              ||   ___________________________________________________    ||
              ||  |                                                   |   ||
              ||  |                                                   |   || 
              ||  |                                                   |   ||
              ||  |                                                   |   ||
              ||  |                                                   |   ||
              ||  |                                                   |   ||
              ||  |                                                   |   ||
              ||  |                                                   |   ||
              ||  |                                                   |   ||
              ||  |                                                   |   ||
              ||  |                                                   |   ||
              ||  |                                                   |   || 
              ||  |                                                   |   || 
              ||  |                                                   |   ||
              ||  |___________________________________________________|   ||
              ||   Décénie|Annuel|Mensuel                   [_(A1)_]>     ||
              ||                                                          ||
              ||      Date d'achat                                        ||
              ||   [_______(B1)________]>                                 ||  
              ||                                                          ||
              ||                                                          ||
              ||      TTC              HT                     TVA         ||
              ||    [_(C1)_]        [_(C2)_]                [_(C3)_]      ||
              ||                                                          ||
              ||    Déduction                                             ||
              ||                                                          ||
              ||                         [Ouvrir](D1)   [Parcourir](D2)   ||
              ||     [____________________(E1)_____________________]>     ||
              ||     [____________________(F1)_____________________]>     ||
              ||                                                          ||
              ||    [Supprimer](G1)	   [Enrengistrer](G2)    [RAZ](G3)    ||
              ||__________________________________________________________||
 */