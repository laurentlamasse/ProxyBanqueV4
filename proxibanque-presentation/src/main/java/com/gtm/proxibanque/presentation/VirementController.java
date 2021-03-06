package com.gtm.proxibanque.presentation;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Calendar;
import java.util.Date;

import javax.annotation.PostConstruct;

import org.primefaces.model.chart.PieChartModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import com.gtm.proxibanque.domaine.Compte;
import com.gtm.proxibanque.domaine.Virement;
import com.gtm.proxibanque.service.interfaces.ICompteService;
import com.gtm.proxibanque.service.interfaces.IVirementService;

/**
 * Classe Bean qui sera instancie par JSF et sera initialise a partir des
 * informations founies par la page JSF (exemple : les valeurs des champs d'un
 * formulaire). Cette classe permet la gestion du binding c'est a dire le
 * branchement entre l'univers web et l'univers java.
 * 
 * VirementController injecte 2 services utilises pour la gestion des virements
 * : - ICompteService compteService - IVirementService virementService
 */
@Controller
@Scope("session")
public class VirementController {

	// Propriétés
	private Virement virement;
	private ArrayList<Virement> listeVirement;
	private String numeroCompteDebiteur = "pas de numéro";
	private String numeroCompteCrediteur = "pas de numéro";
	private double montant;
	private String message;
	private Compte compteDebiteur;
	private Compte compteCrediteur;
	private PieChartModel camembert;
	private Date date1;
	private Date date2;
	private Calendar c1;
	private Calendar c2;

	public double getMontant() {
		return montant;
	}

	public void setMontant(double montant) {
		this.montant = montant;
	}

	@Autowired
	private ICompteService compteService;
	@Autowired
	private IVirementService virementService;

	// Constructeur
	public VirementController() {
	}

	/**
	 * Methode d'initialisation appelee automatiquement apres l'instanciation de la
	 * classe VirementController.
	 */
	@PostConstruct
	public void init() {
		virement = new Virement();
		listeVirement = new ArrayList<Virement>();
		listeVirement = (ArrayList<Virement>) virementService.listerVirements();
		c1 = Calendar.getInstance();
		c2 = Calendar.getInstance();
		c1.setTime(new Date());
		date2 = c1.getTime();
		c2.setTime(new Date());
		c2.add(Calendar.DATE, -30);
		date1 = c2.getTime();
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		System.out.println("dans le controller" + dateFormat.format(date1));
		System.out.println("dans le controller" + dateFormat.format(date2));
		createCamembert();
	}

	// Getters & Setters
	public Virement getVirement() {
		return virement;
	}

	public void setVirement(Virement Virement) {
		this.virement = Virement;
	}

	public ArrayList<Virement> getListeVirement() {
		return (ArrayList<Virement>) virementService.listerVirements();
	}

	public void setListeVirement(ArrayList<Virement> listeVirement) {
		this.listeVirement = listeVirement;
	}

	public PieChartModel getCamembert() {
		return camembert;
	}

	public Date getDate1() {
		return date1;
	}

	public void setDate1(Date date1) {
		this.date1 = date1;
	}

	public Date getDate2() {
		return date2;
	}

	public void setDate2(Date date2) {
		this.date2 = date2;
	}

	/**
	 * Cree un virement entre 2 comptes et l'enregistre en base de donnees
	 * 
	 * @return
	 */
	public String creerVirement() {
		// Compte compteDebiteur =
		// compteService.trouverCompteAvecNumero(numeroCompteDebiteur);
		// Compte compteCrediteur =
		// compteService.trouverCompteAvecNumero(numeroCompteCrediteur);
		Virement virement = new Virement(compteDebiteur, compteCrediteur, montant, message);
		virementService.createVirement(virement);
		return "listeClients";
	}

	/**
	 * Verifie si le virement est possible entre les 2 comptes.
	 * 
	 * @return
	 */
	public String verificationVirement() {
		compteDebiteur = compteService.trouverCompteAvecNumero(numeroCompteDebiteur);
		compteCrediteur = compteService.trouverCompteAvecNumero(numeroCompteCrediteur);
		return "validationVirement";
	}

	/**
	 * Permet de choisir le compte debiteur
	 * 
	 * @param numeroCompteDebiteur
	 * @return
	 */
	public String choixCompteDebite(String numeroCompteDebiteur) {
		this.numeroCompteDebiteur = numeroCompteDebiteur;
		Compte compteDebiteur = compteService.trouverCompteAvecNumero(numeroCompteDebiteur);
		virement.setCompteDebite(compteDebiteur);
		return "preparationVirement";
	}

	/**
	 * Permet de choisir le compte crediteur
	 * 
	 * @param numeroCompteCrediteur
	 * @return
	 */
	public String choixCompteCredite(String numeroCompteCrediteur) {
		this.numeroCompteCrediteur = numeroCompteCrediteur;
		Compte compteCrediteur = compteService.trouverCompteAvecNumero(numeroCompteCrediteur);
		virement.setCompteCredite(compteCrediteur);
		return "montantVirement";
	}

	/**
	 * Recupere la liste des numeros de comptes
	 * 
	 * @return
	 */
	public ArrayList<String> getListeNumeroCompte() {
		ArrayList<String> listeNumero = (ArrayList<String>) compteService.listerComptes().stream()
				.map(c -> c.getNumeroCompte()).sorted().collect(Collectors.toList());
		String numero = "";
		for (String s : listeNumero) {
			if (s.equals(numeroCompteDebiteur))
				numero = s;
		}
		listeNumero.remove(numero);
		return listeNumero;
	}

	public String validationMontant() {
		virement.setMontant(montant);
		return "validationVirement";
	}

	/**
	 * Methode qui cree un diagramme circulaire sur les virements
	 */
	public void createCamembert() {
		camembert = new PieChartModel();
		ArrayList<Long> listeSection = virementService.getSectionPourCamembert(date1, date2);
		camembert.set("entre 0 et 200€", listeSection.get(0));
		camembert.set("entre 200 et 500€", listeSection.get(1));
		camembert.set("entre 500 et 1000€", listeSection.get(2));
		camembert.set("entre 1000 et 5000€", listeSection.get(3));
		camembert.set("plus de 5000€", listeSection.get(4));

		camembert.setLegendPosition("w");

	}

	public String getNumeroCompteDebiteur() {
		return numeroCompteDebiteur;
	}

	public void setNumeroCompteDebiteur(String numeroCompteDebiteur) {
		this.numeroCompteDebiteur = numeroCompteDebiteur;
	}

	public String getNumeroCompteCrediteur() {
		return numeroCompteCrediteur;
	}

	public void setNumeroCompteCrediteur(String numeroCompteCrediteur) {
		this.numeroCompteCrediteur = numeroCompteCrediteur;
	}

	public ICompteService getCompteService() {
		return compteService;
	}

	public void setCompteService(ICompteService compteService) {
		this.compteService = compteService;
	}

	public IVirementService getVirementService() {
		return virementService;
	}

	public void setVirementService(IVirementService virementService) {
		this.virementService = virementService;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Compte getCompteDebiteur() {
		return compteDebiteur;
	}

	public void setCompteDebiteur(Compte compteDebiteur) {
		this.compteDebiteur = compteDebiteur;
	}

	public Compte getCompteCrediteur() {
		return compteCrediteur;
	}

	public void setCompteCrediteur(Compte compteCrediteur) {
		this.compteCrediteur = compteCrediteur;
	}
}
