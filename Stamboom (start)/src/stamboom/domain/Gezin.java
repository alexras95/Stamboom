package stamboom.domain;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import stamboom.util.StringUtilities;

public class Gezin implements java.io.Serializable{

    // *********datavelden*************************************
    private final int nr;
    private final Persoon ouder1;
    private final Persoon ouder2;
    private final List<Persoon> kinderen;
    private transient ObservableList<Persoon> obKinderen;
    /**
     * kan onbekend zijn (dan is het een ongehuwd gezin):
     */
    private Calendar huwelijksdatum;
    /**
     * kan null zijn; als huwelijksdatum null is, dan zal scheidingsdatum ook null
     * zijn; Als huwelijksdatum en scheidingsdatum bekend zijn, dan zal de
     * scheidingsdatum na het huewelijk zijn.
     */
    private Calendar scheidingsdatum;

    // *********constructoren***********************************
    /**
     * er wordt een (kinderloos) gezin met ouder1 en ouder2 als ouders
     * geregistreerd; de huwelijks-(en scheidings)datum zijn onbekend (null);
     * het gezin krijgt gezinsNr als nummer;
     *
     * @param ouder1 mag niet null zijn, moet al geboren zijn,
     * en mag geen famillie van ouder2 zijn.
     * @param ouder2 ongelijk aan ouder1, moet al geboren zijn,
     * en mag geen familie van ouder1 zijn.
     */
    public Gezin(int gezinsNr, Persoon ouder1, Persoon ouder2) {
        if (ouder1 == null) {
            throw new RuntimeException("Eerste ouder mag niet null zijn");
        }
        if (ouder1 == ouder2) {
            throw new RuntimeException("ouders hetzelfde");
        }
        if (ouder2 != null) {
            if (ouder1.getOuderlijkGezin() != null
                    && ouder1.getOuderlijkGezin().isFamilieVan(ouder2)) {
                throw new RuntimeException("ouder 2 is familie van ouder 1");
            }
            if (ouder2.getOuderlijkGezin() != null
                    && ouder2.getOuderlijkGezin().isFamilieVan(ouder1)) {
                throw new RuntimeException("ouder 1 is familie van ouder 2");
            }
        }
        if (ouder1.getGebDat().compareTo(Calendar.getInstance()) > 0){
            throw new RuntimeException("ouder1 moet nog geboren worden");
        }
        if (ouder2 != null && ouder2.getGebDat().compareTo(Calendar.getInstance()) > 0)
        {
            throw new RuntimeException("ouder2 moet nog geboren worden");
        }
        
        this.nr = gezinsNr;
        this.ouder1 = ouder1;
        this.ouder2 = ouder2;
        this.kinderen = new ArrayList<>();
        this.huwelijksdatum = null;
        this.scheidingsdatum = null;
    }

    // ********methoden*****************************************
    /**
     * @return alle kinderen uit dit gezin
     */
    public List<Persoon> getKinderen() {
        return (List<Persoon>) Collections.unmodifiableList(kinderen);
    }
    
    public ObservableList<Persoon> getObKinderen() {
                obKinderen = FXCollections.observableList(kinderen);
        return (ObservableList<Persoon>)FXCollections.unmodifiableObservableList(obKinderen);
    }

    /**
     *
     * @return het aantal kinderen in dit gezin
     */
    public int aantalKinderen() {
        return kinderen.size();
    }

    /**
     *
     * @return het nummer van dit gezin
     */
    public int getNr() {
        return nr;
    }

    /**
     * @return de eerste ouder van dit gezin
     */
    public Persoon getOuder1() {
        return ouder1;
    }

    /**
     * @return de tweede ouder van dit gezin (kan null zijn)
     */
    public Persoon getOuder2() {
        return ouder2;
    }

    /**
     *
     * @return het nr, de naam van de eerste ouder, gevolgd door de naam van de
     * eventuele tweede ouder. Als dit gezin getrouwd is, wordt ook de huwelijksdatum
     * vermeld.
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(this.nr).append(" ");
        s.append(ouder1.getNaam());
        if (ouder2 != null) {
            s.append(" met ");
            s.append(ouder2.getNaam());
        }
        if (heeftGetrouwdeOudersOp(Calendar.getInstance())) {
            s.append(" ").append(StringUtilities.datumString(huwelijksdatum));
        }
        return s.toString();
    }

    /**
     * @return de datum van het huwelijk (kan null zijn)
     */
    public Calendar getHuwelijksdatum() {
        return huwelijksdatum;
    }

    /**
     * @return de datum van scheiding (kan null zijn)
     */
    public Calendar getScheidingsdatum() {
        return scheidingsdatum;
    }

    /**
     * Als ouders zijn gehuwd, en er nog geen scheidingsdatum is dan wordt deze
     * geregistreerd.
     *
     * @param datum moet na de huwelijksdatum zijn.
     * @return true als scheiding kan worden voltrokken, anders false
     */
    public boolean setScheiding(Calendar datum) {
        if (this.scheidingsdatum == null && huwelijksdatum != null
                && datum.after(huwelijksdatum) && datum != null) {
            this.scheidingsdatum = datum;
            return true;
        } else {
            return false;
        }
    }

    /**
     * registreert het huwelijk, mits dit gezin nog geen huwelijk is en beide
     * ouders op deze datum mogen trouwen (pas op: het is mogelijk dat er al wel
     * een huwelijk staat gepland, maar nog niet is voltrokken op deze datum)
     * Mensen mogen niet trouwen voor hun achttiende.
     *
     * @param datum de huwelijksdatum
     * @return false als huwelijk niet mocht worden voltrokken, anders true
     */
    public boolean setHuwelijk(Calendar datum) {
        //todo opgave 1
        int datumyears = datum.get(Calendar.YEAR);
        int geboortejaar1 = ouder1.getGebDat().get(Calendar.YEAR);
        int geboortejaar2 = ouder2.getGebDat().get(Calendar.YEAR);
        if(datumyears - geboortejaar1 <= 18 || datumyears - geboortejaar2 <= 18){
            return false;
        }
        if(huwelijksdatum == null || huwelijksdatum.after(datum)){
            huwelijksdatum = datum;
            return true;
        }
        if(scheidingsdatum != null && scheidingsdatum.before(datum)){
            huwelijksdatum = datum;
            return true;
        }
        return false;
    }

    /**
     * @return het gezinsnummer, gevolgd door de namen van de ouder(s),
     * de eventueel bekende huwelijksdatum, (als er kinderen zijn)
     * de constante tekst '; kinderen:', en de voornamen van de
     * kinderen uit deze relatie (per kind voorafgegaan door ' -')
     */
    public String beschrijving() {
        //todo opgave 1
        // frank
        StringBuilder b = new StringBuilder();
        b.append(this.nr).append(" ");
        b.append(ouder1.getNaam());
        if (ouder2 != null) {
            b.append(" met ");
            b.append(ouder2.getNaam());
        }
        if (heeftGetrouwdeOudersOp(Calendar.getInstance())) {
            b.append(" ").append(StringUtilities.datumString(huwelijksdatum));
        }
        if(!kinderen.isEmpty())
        {
            b.append("; kinderen:");
            for(Persoon p : kinderen)
            {
                String naam = p.getVoornamen().trim();
                b.append(" -").append(naam);
            }   
        }
            
    
        return b.toString();
    }

    /**
     * Voegt kind toe aan dit gezin. Doet niets als dit kind al deel uitmaakt
     * van deze familie.
     *
     * @param kind
     */
    void breidUitMet(Persoon kind) {
        if (!kinderen.contains(kind) && !this.isFamilieVan(kind)) {
            kinderen.add(kind);
        }
    }

    /**
     * Controleert of deze familie niet al de gegeven persoon bevat.
     *
     * @param input
     * @return true als deze familie de gegeven persoon bevat.
     */
    boolean isFamilieVan(Persoon input) {
        if (this.ouder1.getNr() == input.getNr()
                || (this.ouder2 != null && this.ouder2.getNr() == input.getNr())
                || kinderen.contains(input)) {
            return true;
        }

        boolean output = this.ouder1.getOuderlijkGezin() != null
                && this.ouder1.getOuderlijkGezin().isFamilieVan(input);
        if (!output && this.ouder2 != null) {
            output = this.ouder2.getOuderlijkGezin() != null
                    && this.ouder2.getOuderlijkGezin().isFamilieVan(input);
        }
        return output;
    }

    /**
     *
     * @param datum
     * @return true als dit gezin op datum getrouwd en nog niet gescheiden is,
     * anders false
     */
    public boolean heeftGetrouwdeOudersOp(Calendar datum) {
        return isHuwelijkOp(datum)
                && (scheidingsdatum == null || scheidingsdatum.after(datum));
    }

    /**
     *
     * @param datum
     * @return true als dit gezin op of voor deze datum getrouwd is, ongeacht of
     * de ouders hierna gingen/gaan scheiden.
     */
    public boolean isHuwelijkOp(Calendar datum) {
        //todo opgave 1
        // frank
        if(datum.after(huwelijksdatum) || datum.equals(huwelijksdatum)){
            if(scheidingsdatum == null){
                return true;
            }
            else if(scheidingsdatum.after(datum)){
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @return true als de ouders van dit gezin niet getrouwd zijn, anders false
     */
    public boolean isOngehuwd() {
        return huwelijksdatum == null;
    }

    /**
     *
     * @param datum
     * @return true als dit een gescheiden huwelijk is op datum, anders false
     */
    public boolean heeftGescheidenOudersOp(Calendar datum) {
        //todo opgave 1
        // frank
        // alex: optie erbij gezet dat de scheidingsdatum voor vandaag is i.p.v. alleen precies vandaag
        
        return datum.equals(this.scheidingsdatum) || (datum.after(this.scheidingsdatum) && this.scheidingsdatum.after(this.huwelijksdatum));
    }
    
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        obKinderen = FXCollections.observableList(kinderen);
    }
}
