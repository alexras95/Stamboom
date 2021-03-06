package stamboom.domain;

import com.sun.deploy.Environment;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import stamboom.util.StringUtilities;

public class Persoon implements java.io.Serializable{

    // ********datavelden**************************************
    private final int nr;
    private final String[] voornamen;
    private final String achternaam;
    private final String tussenvoegsel;
    private final Calendar gebDat;
    private final String gebPlaats;
    private Gezin ouderlijkGezin;
    private ArrayList<Gezin> alsOuderBetrokkenIn;
    private transient ObservableList<Gezin> obAlsOuderBetrokkenIn;
    private final Geslacht geslacht;
    private int afmeting = 1;

    // ********constructoren***********************************
    /**
     * er wordt een persoon gecreeerd met persoonsnummer persNr en met als
     * voornamen vnamen, achternaam anaam, tussenvoegsel tvoegsel, geboortedatum
     * gebdat, gebplaats, geslacht g en een gegeven ouderlijk gezin (mag null
     * (=onbekend) zijn); NB. de eerste letter van een voornaam, achternaam en
     * gebplaats wordt naar een hoofdletter omgezet, alle andere letters zijn
     * kleine letters; het tussenvoegsel is zo nodig in zijn geheel
     * geconverteerd naar kleine letters.
     *
     */
    public Persoon(int persNr, String[] vnamen, String anaam, String tvoegsel,
            Calendar gebdat, String gebplaats, Geslacht g, Gezin ouderlijkGezin) {
        //TODO opgave 1
        char first = Character.toUpperCase(anaam.charAt(0));
        this.achternaam = first + anaam.toLowerCase().substring(1);
        this.nr = persNr;
        String[] tempvoornamen = new String[vnamen.length];
        int i = 0;
        for(String s: vnamen)
        {           
            String tempVnaam = s.replaceAll("\\s", "");
            String nieuw;
            first = Character.toUpperCase(tempVnaam.charAt(0));
            nieuw = first + tempVnaam.toLowerCase().substring(1);
            tempvoornamen[i] = nieuw;
            i++;
        }
        this.voornamen = tempvoornamen;
        this.tussenvoegsel = tvoegsel.toLowerCase();
        this.gebDat = gebdat;
        first = Character.toUpperCase(gebplaats.charAt(0));
        this.gebPlaats = first + gebplaats.toLowerCase().substring(1);
        this.ouderlijkGezin = ouderlijkGezin;
        this.geslacht = g;
        // frank: onderste zin ben ik niet zeker van

        this.alsOuderBetrokkenIn = new ArrayList<>();
        obAlsOuderBetrokkenIn = FXCollections.observableList(alsOuderBetrokkenIn);  
        

    }

    // ********methoden****************************************
    /**
     * @return de achternaam van deze persoon
     */
    public String getAchternaam() {
        return achternaam;
    }

    /**
     * @return de geboortedatum van deze persoon
     */
    public Calendar getGebDat() {
        return gebDat;
    }

    /**
     *
     * @return de geboorteplaats van deze persoon
     */
    public String getGebPlaats() {
        return gebPlaats;
    }

    /**
     *
     * @return het geslacht van deze persoon
     */
    public Geslacht getGeslacht() {
        return geslacht;
    }

    /**
     *
     * @return de voorletters van de voornamen; elke voorletter wordt gevolgd
     * door een punt
     */
    public String getInitialen() {
        //todo opgave 1
        String initialen = "";
        for(String s: voornamen)
        {
            initialen = initialen + s.charAt(0) + ".";
        }
        return initialen;
    }

    /**
     *
     * @return de initialen gevolgd door een eventueel tussenvoegsel en
     * afgesloten door de achternaam; initialen, voorzetsel en achternaam zijn
     * gescheiden door een spatie
     */
    public String getNaam() {
        //todo opgave 1
        String naam;
        if(tussenvoegsel != null && !tussenvoegsel.isEmpty())
        {
            naam = getInitialen() + " " + tussenvoegsel + " " + achternaam;
        }
        else
        {
            naam = getInitialen() + " " + achternaam;
        }
        //naam = getInitialen() + " "
        return naam;
    }

    /**
     * @return het nummer van deze persoon
     */
    public int getNr() {
        return nr;
    }

    /**
     * @return het ouderlijk gezin van deze persoon, indien bekend, anders null
     */
    public Gezin getOuderlijkGezin() {
        return ouderlijkGezin;
    }

    /**
     * @return het tussenvoegsel van de naam van deze persoon (kan een lege
     * string zijn)
     */
    public String getTussenvoegsel() {
        return tussenvoegsel;
    }

    /**
     * @return alle voornamen onderling gescheiden door een spatie
     */
    public String getVoornamen() {
        StringBuilder init = new StringBuilder();
        for (String s : voornamen) {
            init.append(s).append(' ');
        }
        return init.toString().trim();
    }

    /**
     * @return de standaardgegevens van deze mens: naam (geslacht) geboortedatum
     */
    public String standaardgegevens() {
        return getNaam() + " (" + getGeslacht() + ") " + StringUtilities.datumString(gebDat);
    }

    @Override
    public String toString() {
        return standaardgegevens();
    }

    /**
     * @return de gezinnen waar deze persoon bij betrokken is
     */
    public List<Gezin> getAlsOuderBetrokkenIn() {
        return (List<Gezin>) Collections.unmodifiableList(alsOuderBetrokkenIn);
    }
    
    public ObservableList<Gezin> getObAlsOuderBetrokkenIn() {
        return (ObservableList<Gezin>)FXCollections.unmodifiableObservableList(obAlsOuderBetrokkenIn);
    }
    
    

    /**
     * Als het ouderlijk gezin van deze persoon nog onbekend is dan wordt deze
     * persoon een kind van ouderlijkGezin en tevens wordt deze persoon als kind
     * in dat gezin geregistreerd. Als de ouders bij aanroep al bekend zijn,
     * verandert er niets
     *
     * @param ouderlijkGezin
     * @return of ouderlijk gezin kon worden toegevoegd
     */
    public boolean setOuders(Gezin ouderlijkGezin) {
        //todo opgave 1
        if(this.ouderlijkGezin == null){
            this.ouderlijkGezin = ouderlijkGezin;
            ouderlijkGezin.breidUitMet(this);
            return true;
        }
        else{
            return false;
        }
    }

    /**
     * @return voornamen, eventueel tussenvoegsel en achternaam, geslacht,
     * geboortedatum, namen van de ouders, mits bekend, en nummers van de
     * gezinnen waarbij deze persoon betrokken is (geweest)
     */
    public String beschrijving() {
        StringBuilder sb = new StringBuilder();

        sb.append(standaardgegevens());

        if (ouderlijkGezin != null) {
            sb.append("; 1e ouder: ").append(ouderlijkGezin.getOuder1().getNaam());
            if (ouderlijkGezin.getOuder2() != null) {
                sb.append("; 2e ouder: ").append(ouderlijkGezin.getOuder2().getNaam());
            }
        }
        if (!alsOuderBetrokkenIn.isEmpty()) {
            sb.append("; is ouder in gezin ");

            for (Gezin g : alsOuderBetrokkenIn) {
                sb.append(g.getNr()).append(" ");
            }
        }

        return sb.toString();
    }

    /**
     * als g nog niet bij deze persoon staat geregistreerd wordt g bij deze
     * persoon geregistreerd en anders verandert er niets
     *
     * @param g een nieuw gezin waarin deze persoon een ouder is
     *
     */
    void wordtOuderIn(Gezin g) {
        if (!alsOuderBetrokkenIn.contains(g)) {
            alsOuderBetrokkenIn.add(g);
        }
    }

    /**
     *
     *
     * @param andereOuder mag null zijn
     * @return het ongehuwde gezin met de andere ouder ; mits bestaand anders
     * null
     */
    public Gezin heeftOngehuwdGezinMet(Persoon andereOuder) {
        //todo opgave 1
        for(Gezin g : alsOuderBetrokkenIn){           
            if(g.getOuder1() == this || g.getOuder2() == this)
            {
                if(g.getOuder1() == andereOuder || g.getOuder2() == andereOuder)
                {                   
                    if(g.isOngehuwd()){
                        return g;
                    }
                    else{
                        return null;
                    }                   
                }
            }
        }
        return null;        
    }

    /**
     *
     * @param datum
     * @return true als persoon op datum getrouwd is, anders false
     */
    public boolean isGetrouwdOp(Calendar datum) {
        for (Gezin gezin : alsOuderBetrokkenIn) {
            if (gezin.heeftGetrouwdeOudersOp(datum)) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param datum
     * @return true als de persoon kan trouwen op datum, hierbij wordt rekening
     * gehouden met huwelijken in het verleden en in de toekomst
     * Alleen meerderjarige (18+) personen kunnen trouwen.
     */
    public boolean kanTrouwenOp(Calendar datum) {
        Calendar meerderjarigDatum = ((GregorianCalendar)this.gebDat.clone());
        meerderjarigDatum.add(Calendar.YEAR, 18);
        if(datum.compareTo(meerderjarigDatum) < 1){
            return false;
        }

        for (Gezin gezin : alsOuderBetrokkenIn) {
            if (gezin.heeftGetrouwdeOudersOp(datum)) {
                return false;
            } else {
                Calendar huwdatum = gezin.getHuwelijksdatum();
                if (huwdatum != null && huwdatum.after(datum)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     *
     * @param datum
     * @return true als persoon op datum gescheiden is, anders false
     */
    public boolean isGescheidenOp(Calendar datum) {
        for(Gezin gezin: alsOuderBetrokkenIn){
            if(gezin.heeftGescheidenOudersOp(datum)){
                return true;
            }
        }
        return false;
    }

    /**
     * ********* de rest wordt in opgave 2 verwerkt ****************
     */
    /**
     *
     * @return het aantal personen in de stamboom van deze persoon (ouders,
     * grootouders etc); de persoon zelf telt ook mee
     */
    public int afmetingStamboom() {
        //todo opgave 2
        BerekenAfmeting(this);
        return afmeting;
    }
    
    public void BerekenAfmeting(Persoon p)
    {
         /*
        while(persoon heeft ouders)
        {
            if(persoonHeeftOuders)
        {
            afmeting += 2
            BerekenAfmeting(ouder1);
            BerekenAfmeting(ouder2);
        }
            else
        {
            persoon heeft geen ouders (uit loop)
        }
        }
        */
        if(p.ouderlijkGezin != null)
        {
            if(p.ouderlijkGezin.getOuder1() != null && p.ouderlijkGezin.getOuder2() != null)
            {
                afmeting += 2;
                BerekenAfmeting(p.ouderlijkGezin.getOuder1());
                BerekenAfmeting(p.ouderlijkGezin.getOuder2());
            }
            else if(p.ouderlijkGezin.getOuder1() != null)
            {
                afmeting += 1;
                BerekenAfmeting(p.ouderlijkGezin.getOuder1());
            }
            else
            {
                afmeting += 1;
                BerekenAfmeting(p.ouderlijkGezin.getOuder2());
            }            
        }
        
    }

    /**
     * de lijst met de items uit de stamboom van deze persoon wordt toegevoegd
     * aan lijst, dat wil zeggen dat begint met de toevoeging van de
     * standaardgegevens van deze persoon behorende bij generatie g gevolgd door
     * de items uit de lijst met de stamboom van de eerste ouder (mits bekend)
     * en gevolgd door de items uit de lijst met de stamboom van de tweede ouder
     * (mits bekend) (het generatienummer van de ouders is steeds 1 hoger)
     *
     * @param lijst
     * @param g >=0, het nummer van de generatie waaraan deze persoon is
     * toegewezen;
     */
    void voegJouwStamboomToe(ArrayList<PersoonMetGeneratie> lijst, int g) {
        //todo opgave 2
        // frank: methode om voor een persoon een lijst 
        // met zijn stamboom te genereren
        lijst.add(new PersoonMetGeneratie(this.standaardgegevens(), 0));
        voegOudersToe(lijst, g, this);
    }
    
    void voegOudersToe(ArrayList<PersoonMetGeneratie> lijst, int g, Persoon p)
    {
        // frank: vervolg op voeg stamboom toe
        if(p.ouderlijkGezin != null)
        {
            g += 1;
            if(p.ouderlijkGezin.getOuder1() != null && p.ouderlijkGezin.getOuder2() != null)
            {   
                lijst.add(new PersoonMetGeneratie(p.ouderlijkGezin.getOuder1().standaardgegevens(), g));
                voegOudersToe(lijst, g, p.ouderlijkGezin.getOuder1());
                lijst.add(new PersoonMetGeneratie(p.ouderlijkGezin.getOuder2().standaardgegevens(), g));
                voegOudersToe(lijst, g, p.ouderlijkGezin.getOuder2());
            }
            else if(p.ouderlijkGezin.getOuder1() != null)
            {
                lijst.add(new PersoonMetGeneratie(p.ouderlijkGezin.getOuder1().standaardgegevens(), g));
                voegOudersToe(lijst, g, p.ouderlijkGezin.getOuder1());
            }
            else
            {
                lijst.add(new PersoonMetGeneratie(p.ouderlijkGezin.getOuder2().standaardgegevens(), g));
                voegOudersToe(lijst, g, p.ouderlijkGezin.getOuder2());
            }            
        }
        else
        {
            g -= 1;
        }
    }
    /**
     *
     * @return de stamboomgegevens van deze persoon in de vorm van een String:
     * op de eerste regel de standaardgegevens van deze persoon, gevolgd door de
     * stamboomgegevens van de eerste ouder (mits bekend) en gevolgd door de
     * stamboomgegevens van de tweede ouder (mits bekend); formattering: iedere
     * persoon staat op een aparte regel en afhankelijk van het
     * generatieverschil worden er per persoon 2*generatieverschil spaties
     * ingesprongen;
     *
     * bijv voor M.G. Pieterse met ouders, grootouders en overgrootouders,
     * inspringen is in dit geval met underscore gemarkeerd: <br>
     *
     * M.G. Pieterse (VROUW) 5-5-2004<br>
     * __L. van Maestricht (MAN) 27-6-1953<br>
     * ____A.G. von Bisterfeld (VROUW) 13-4-1911<br>
     * ______I.T.M.A. Goldsmid (VROUW) 22-12-1876<br>
     * ______F.A.I. von Bisterfeld (MAN) 27-6-1874<br>
     * ____H.C. van Maestricht (MAN) 17-2-1909<br>
     * __J.A. Pieterse (MAN) 23-6-1964<br>
     * ____M.A.C. Hagel (VROUW) 12-0-1943<br>
     * ____J.A. Pieterse (MAN) 4-8-1923<br>
     */
    public String stamboomAlsString() {
        StringBuilder builder = new StringBuilder();
        //todo opgave 2
        // frank: hij lijkt goed te zijn ik snap de unittest niet...
        ArrayList<PersoonMetGeneratie> stamboom = new ArrayList<>();
        voegJouwStamboomToe(stamboom, 0);
        int listCounter = 0;
        for(PersoonMetGeneratie p : stamboom)
        {
            listCounter ++;
            String spaces = "";
            for(int i = 0 ; i < p.getGeneratie() ; i++)
            {
                spaces += "  ";
            }
            builder.append(spaces);
            builder.append(p.getPersoonsgegevens());
            if(stamboom.size() != listCounter)
            {
                builder.append(System.getProperty("line.separator"));
            }           
        }
        System.out.println(builder.toString());
        return builder.toString();
    }
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        obAlsOuderBetrokkenIn = FXCollections.observableList(alsOuderBetrokkenIn);
    }
}
