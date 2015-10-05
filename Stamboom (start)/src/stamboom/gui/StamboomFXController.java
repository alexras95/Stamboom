/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stamboom.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import stamboom.controller.StamboomController;
import stamboom.domain.Administratie;
import stamboom.domain.Geslacht;
import stamboom.domain.Gezin;
import stamboom.domain.Persoon;
import stamboom.util.StringUtilities;

/**
 *
 * @author frankpeeters
 */
public class StamboomFXController extends StamboomController implements Initializable {

    //MENUs en TABs
    @FXML MenuBar menuBar;
    @FXML MenuItem miNew;
    @FXML MenuItem miOpen;
    @FXML MenuItem miSave;
    @FXML CheckMenuItem cmDatabase;
    @FXML MenuItem miClose;
    @FXML Tab tabPersoon;
    @FXML Tab tabGezin;
    @FXML Tab tabPersoonInvoer;
    @FXML Tab tabGezinInvoer;

    //PERSOON
    @FXML ComboBox cbPersonen;
    @FXML TextField tfPersoonNr;
    @FXML TextField tfVoornamen;
    @FXML TextField tfTussenvoegsel;
    @FXML TextField tfAchternaam;
    @FXML TextField tfGeslacht;
    @FXML TextField tfGebDatum;
    @FXML TextField tfGebPlaats;
    @FXML ComboBox cbOuderlijkGezin;
    @FXML ListView lvAlsOuderBetrokkenBij;
    @FXML Button btStamboom;

    //INVOER GEZIN
    @FXML ComboBox cbOuder1Invoer;
    @FXML ComboBox cbOuder2Invoer;
    @FXML TextField tfHuwelijkInvoer;
    @FXML TextField tfScheidingInvoer;
    @FXML Button btOKGezinInvoer;
    @FXML Button btCancelGezinInvoer;
    
    // NIEUW PERSOON
    @FXML TextField tfNieuwPersVoornamen;
    @FXML TextField tfNieuwPersTsv;
    @FXML TextField tfNieuwPersAnaam;
    @FXML TextField tfNieuwPersGebDat;
    @FXML TextField tfNieuwPersGebPl;
    @FXML ComboBox cbNieuwPersGeslacht;
    @FXML ComboBox cbNieuwPersOudGez;
    @FXML Button btnOkNieuwPersoon;
    @FXML Button btnCancelNieuwPersoon;
    
    // GEZIN
    @FXML ComboBox cbKiesGezin;
    @FXML TextField tfGezinNr;
    @FXML TextField tfSelOuder1;
    @FXML TextField tfSelOuder2;
    @FXML TextField tfGezHuwelijk;
    @FXML TextField tfGezScheiding;
    @FXML ListView lvGezKinderen;
    
    //opgave 4
    private boolean withDatabase;
    
 
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        admin = new Administratie();
        initComboboxes();
        withDatabase = false;
        // frank : testdata
        Persoon piet = admin.addPersoon(Geslacht.MAN, new String[]{"Piet", "Franciscus"}, "Swinkels",
                "", new GregorianCalendar(1950, Calendar.APRIL, 23), "ede", null);
        Persoon teuntje = admin.addPersoon(Geslacht.VROUW, new String[]{"Teuntje"}, "Vries", "de",
                new GregorianCalendar(1949, Calendar.MAY, 5), "Amersfoort", null);
        Gezin pietEnTeuntje = admin.addHuwelijk(piet, teuntje, new GregorianCalendar(1970, Calendar.MAY, 23));
        Persoon henkie = admin.addPersoon(Geslacht.MAN, new String[]{"Henkie", "Franciscus"}, "Swinkels",
                "", new GregorianCalendar(1980, Calendar.APRIL, 23), "ede", pietEnTeuntje);
        Persoon henk = admin.addPersoon(Geslacht.MAN, new String[]{"Penk", "Franciscus"}, "Swinkels",
                "", new GregorianCalendar(1985, Calendar.APRIL, 23), "ede", pietEnTeuntje);
        Persoon veerle = admin.addPersoon(Geslacht.VROUW, new String[]{"Veerle"}, "Slippens",
                "", new GregorianCalendar(1996, Calendar.MARCH, 13), "Veghel", null);
        
    }

    private void initComboboxes() {
        //todo opgave 3    
        cbPersonen.setItems(admin.getPersonen());
        cbKiesGezin.setItems(admin.getGezinnen());
        cbNieuwPersOudGez.setItems(admin.getGezinnen());
        cbOuder1Invoer.setItems(admin.getPersonen());
        cbOuder2Invoer.setItems(admin.getPersonen());
        cbOuderlijkGezin.setItems(admin.getGezinnen());
    }

    public void selectPersoon(Event evt) {
        Persoon persoon = (Persoon) cbPersonen.getSelectionModel().getSelectedItem();
        showPersoon(persoon);
    }

    private void showPersoon(Persoon persoon) {
        if (persoon == null) {
            clearTabPersoon();
        } else {
            tfPersoonNr.setText(persoon.getNr() + "");
            tfVoornamen.setText(persoon.getVoornamen());
            tfTussenvoegsel.setText(persoon.getTussenvoegsel());
            tfAchternaam.setText(persoon.getAchternaam());
            tfGeslacht.setText(persoon.getGeslacht().toString());
            tfGebDatum.setText(StringUtilities.datumString(persoon.getGebDat()));
            tfGebPlaats.setText(persoon.getGebPlaats());
            if (persoon.getOuderlijkGezin() != null) {
                cbOuderlijkGezin.getSelectionModel().select(persoon.getOuderlijkGezin());
            } else {
                cbOuderlijkGezin.getSelectionModel().clearSelection();
            }

            //todo opgave 3
            //
            lvAlsOuderBetrokkenBij.setItems(persoon.getObAlsOuderBetrokkenIn());
        }
    }

    public void setOuders(Event evt) {
        if (tfPersoonNr.getText().isEmpty()) {
            return;
        }
        Gezin ouderlijkGezin = (Gezin) cbOuderlijkGezin.getSelectionModel().getSelectedItem();
        if (ouderlijkGezin == null) {
            return;
        }

        int nr = Integer.parseInt(tfPersoonNr.getText());
        Persoon p = admin.getPersoon(nr);
        if(admin.setOuders(p, ouderlijkGezin)){
            showDialog("Success", ouderlijkGezin.toString()
                + " is nu het ouderlijk gezin van " + p.getNaam());
        }
        
    }

    public void selectGezin(Event evt) {
        // todo opgave 3
        // frank
        Gezin gezin = (Gezin)cbKiesGezin.getSelectionModel().getSelectedItem();
        showGezin(gezin);
    }

    private void showGezin(Gezin gezin) {
        // todo opgave 3
        // frank
        if(gezin == null){
            clearTabGezin();
        }
        else{
            tfGezinNr.setText(String.valueOf(gezin.getNr()));
            tfSelOuder1.setText(gezin.getOuder1().standaardgegevens());
            tfSelOuder2.setText(gezin.getOuder2().standaardgegevens());
            tfGezHuwelijk.setText(StringUtilities.datumString(gezin.getHuwelijksdatum()));
            if(gezin.getScheidingsdatum() != null){
                tfGezScheiding.setText(gezin.getScheidingsdatum().toString());
            }
            if(gezin.getObKinderen().size() != 0){
               lvGezKinderen.setItems(gezin.getObKinderen()); 
            }
            
        }

    }   

    public void setHuwdatum(Event evt) {
        // todo opgave 3
        // frank
        Gezin huwGezin = (Gezin)cbKiesGezin.getSelectionModel().getSelectedItem();
        String huwDatum[] = tfGezHuwelijk.getText().split("-");
        int dag = Integer.parseInt(huwDatum[0]);
        int maand = Integer.parseInt(huwDatum[1]);
        int jaar = Integer.parseInt(huwDatum[2]);
        GregorianCalendar gc = new GregorianCalendar(dag, maand, jaar);
        admin.setHuwelijk(huwGezin, gc);
    }

    public void setScheidingsdatum(Event evt) {
        // todo opgave 3
        // frank
        Gezin scheidingGezin = (Gezin)cbKiesGezin.getSelectionModel().getSelectedItem();
        String scheidingDatum[] = tfGezScheiding.getText().split("-");
        int dag = Integer.parseInt(scheidingDatum[0]);
        int maand = Integer.parseInt(scheidingDatum[1]);
        int jaar = Integer.parseInt(scheidingDatum[2]);
        GregorianCalendar gc = new GregorianCalendar(dag, maand, jaar);
        admin.setScheiding(scheidingGezin, gc);
    }

    public void cancelPersoonInvoer(Event evt) {
        // todo opgave 3
        // frank
        clearTabPersoonInvoer();
    }

    public void okPersoonInvoer(Event evt) {
        // todo opgave 3
        // frank
        String tussenVoegsel = "";
        if(!tfNieuwPersVoornamen.getText().equals("")){
            String[] vnamen = tfNieuwPersVoornamen.getText().split(" ");           
            if(!tfNieuwPersTsv.getText().equals("")){
                tussenVoegsel = tfNieuwPersTsv.getText();        
            }
            if(!tfNieuwPersAnaam.getText().equals("")){
                if(!tfNieuwPersGebDat.getText().equals("")){
                    Calendar calendar = Calendar.getInstance();
                    String[] gebdat = tfNieuwPersGebDat.getText().split("-");
                    if(gebdat.length == 3){
                        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(gebdat[0]));
                        calendar.set(Calendar.MONTH, Integer.parseInt(gebdat[1]));
                        calendar.set(Calendar.YEAR, Integer.parseInt(gebdat[2]));
                    }
                    else{
                        showDialog("Warning", "geboortedatum bestaat niet uit 3 delen");
                        return;
                    }
                    if(!tfNieuwPersGebPl.getText().equals("")){
                        Geslacht geslacht = (Geslacht) Geslacht.valueOf(cbNieuwPersGeslacht.getSelectionModel().getSelectedItem().toString());
                        Gezin ouderlijkGezin = (Gezin) cbNieuwPersOudGez.getSelectionModel().getSelectedItem();                                     
                        Persoon p = admin.addPersoon(geslacht, vnamen, tfNieuwPersAnaam.getText(), tussenVoegsel, calendar, tfNieuwPersGebPl.getText(), ouderlijkGezin);
                        if(p != null){
                            showDialog("Warning", "Persoon succesvol aangemaakt");
                        }                                                                                                                   
                        else{
                            showDialog("Warning", "geen geslacht ingevoerd");
                        }    
                    }
                    else{
                        showDialog("Warning", "geen geboorteplaats ingevoerd");
                    }

                }
                else{
                    showDialog("Warning", "geen geboortedatum ingevoerd");
                }
            }
            else{
                showDialog("Warning", "Geen Achternaam Ingevoerd");
            }            
        }
        else{
            showDialog("Warning", "Geen Voornamen Ingegeven");
        }
        
        clearTabPersoonInvoer();
        initComboboxes();
        
    }

    public void okGezinInvoer(Event evt) {
        Persoon ouder1 = (Persoon) cbOuder1Invoer.getSelectionModel().getSelectedItem();
        if (ouder1 == null) {
            showDialog("Warning", "eerste ouder is niet ingevoerd");
            return;
        }
        Persoon ouder2 = (Persoon) cbOuder2Invoer.getSelectionModel().getSelectedItem();
        Calendar huwdatum;
        try {
            huwdatum = StringUtilities.datum(tfHuwelijkInvoer.getText());
        } catch (IllegalArgumentException exc) {
            showDialog("Warning", "huwelijksdatum :" + exc.getMessage());
            return;
        }
        Gezin g;
        if (huwdatum != null) {
            g = admin.addHuwelijk(ouder1, ouder2, huwdatum);
            if (g == null) {
                showDialog("Warning", "Invoer huwelijk is niet geaccepteerd");
            } else {
                Calendar scheidingsdatum;
                try {
                    scheidingsdatum = StringUtilities.datum(tfScheidingInvoer.getText());
                    if(scheidingsdatum != null){
                        admin.setScheiding(g, scheidingsdatum);
                    }
                } catch (IllegalArgumentException exc) {
                    showDialog("Warning", "scheidingsdatum :" + exc.getMessage());
                }
            }
        } else {
            g = admin.addOngehuwdGezin(ouder1, ouder2);
            if (g == null) {
                showDialog("Warning", "Invoer ongehuwd gezin is niet geaccepteerd");
            }
        }

        clearTabGezinInvoer();
        initComboboxes();
    }

    public void cancelGezinInvoer(Event evt) {
        clearTabGezinInvoer();
    }

    
    public void showStamboom(Event evt) {
        // todo opgave 3
        // frank
        Persoon persoon = (Persoon) cbPersonen.getSelectionModel().getSelectedItem();
        if(persoon != null){
            showDialog("Stamboom " + persoon.standaardgegevens(), persoon.stamboomAlsString());
        }
        else{
            showDialog("Error", "Selecteer een persoon");
        }
            
        
    }

    public void createEmptyStamboom(Event evt) {
        //alex: Stamboom wordt simpelweg opnieuw geïnstantieerd waardoor hij leeg is.
        admin = new Administratie();
        clearTabs();
        initComboboxes();
    }

    
    public void openStamboom(Event evt) throws IOException, ClassNotFoundException {
        // todo opgave 3
       FileChooser filechooser = new FileChooser();
       filechooser.setTitle("Open File");
       File file = filechooser.showOpenDialog(getStage());
       if(file != null){
           //admin = this.deserialize(file);
           FileInputStream fin = new FileInputStream(file.getAbsolutePath());
           System.out.println(file.getAbsolutePath());
           try (ObjectInputStream ois = new ObjectInputStream(fin)) {
               admin = (Administratie) ois.readObject();
               ois.close();
           }
           initComboboxes();
       }
        
       
    }

    
    public void saveStamboom(Event evt) throws IOException {
        // todo opgave 3
        FileChooser filechooser = new FileChooser();
        filechooser.setTitle("Save File");
        File file = filechooser.showSaveDialog(getStage());
        if(file != null){
            this.serialize(file);
        }
        
        
       
    }

    
    public void closeApplication(Event evt) throws IOException {
        saveStamboom(evt);
        getStage().close();
    }

   
    public void configureStorage(Event evt) {
        withDatabase = cmDatabase.isSelected();
    }

 
    public void selectTab(Event evt) {
        Object source = evt.getSource();
        if (source == tabPersoon) {
            clearTabPersoon();
        } else if (source == tabGezin) {
            clearTabGezin();
        } else if (source == tabPersoonInvoer) {
            clearTabPersoonInvoer();
        } else if (source == tabGezinInvoer) {
            clearTabGezinInvoer();
        }
    }

    private void clearTabs() {
        clearTabPersoon();
        clearTabPersoonInvoer();
        clearTabGezin();
        clearTabGezinInvoer();
    }

    
    private void clearTabPersoonInvoer() {
        //todo opgave 3
        // frank
        tfNieuwPersVoornamen.clear();
        tfNieuwPersTsv.clear();
        tfNieuwPersAnaam.clear();
        tfNieuwPersGebDat.clear();
        tfNieuwPersGebPl.clear();
        cbNieuwPersGeslacht.getSelectionModel().clearSelection();
        cbNieuwPersOudGez.getSelectionModel().clearSelection();
    }

    
    private void clearTabGezinInvoer() {
        //todo opgave 3
        // frank
        cbOuder1Invoer.getSelectionModel().clearSelection();
        cbOuder2Invoer.getSelectionModel().clearSelection();
        tfHuwelijkInvoer.clear();
        tfScheidingInvoer.clear();
    }

    private void clearTabPersoon() {
        cbPersonen.getSelectionModel().clearSelection();
        tfPersoonNr.clear();
        tfVoornamen.clear();
        tfTussenvoegsel.clear();
        tfAchternaam.clear();
        tfGeslacht.clear();
        tfGebDatum.clear();
        tfGebPlaats.clear();
        cbOuderlijkGezin.getSelectionModel().clearSelection();
        lvAlsOuderBetrokkenBij.setItems(FXCollections.emptyObservableList());
    }

    
    private void clearTabGezin() {
        // todo opgave 3
        // frank
       tfGezinNr.clear();
       tfSelOuder1.clear();
       tfSelOuder2.clear();
       tfGezHuwelijk.clear();
       tfGezScheiding.clear();
       lvGezKinderen.setItems(FXCollections.emptyObservableList());
    }

    private void showDialog(String type, String message) {
        Stage myDialog = new Dialog(getStage(), type, message);
        myDialog.show();
    }

    private Stage getStage() {
        return (Stage) menuBar.getScene().getWindow();
    }

}
