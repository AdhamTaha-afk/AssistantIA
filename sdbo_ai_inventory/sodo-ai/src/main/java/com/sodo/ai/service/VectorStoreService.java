package com.sodo.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorStoreService {

    private final VectorStore vectorStore;

    @org.springframework.context.annotation.Bean
    public org.springframework.boot.CommandLineRunner initRAG() {
        return args -> {
            log.info("Démarrage de l'indexation RAG Odoo 16 & Odoo 19...");
            
            List<Document> documents = new java.util.ArrayList<>(List.of(
                // 1. Module Ventes & CA (Odoo 16 & 19)
                new Document("""
                    GUIDE VENTES & FACTURATION ODOO 16 / 19 :
                    - Comment créer un devis : Allez dans Ventes > Commandes > Devis > bouton 'Nouveau' / 'Créer'. Sélectionnez le Client, ajoutez des lignes d'articles, définissez la quantité et le prix unitaire.
                    - Confirmer une vente : Cliquez sur 'Confirmer' sur le devis pour le transformer en Bon de Commande officiel (SO-xxxx).
                    - Facturer le client : Cliquez sur 'Créer une facture' (Facture normale, Acompte en % ou Montant fixe), puis validez la facture (Comptabiliser).
                    - Suivi du Chiffre d'Affaires (CA) : Rendez-vous dans Ventes > Analyse > Rapport des Ventes pour visualiser le CA par période, client ou vendeur.
                """),

                // 2. Module Stock & Inventaire (Odoo 16 & 19)
                new Document("""
                    GUIDE STOCK & GESTION DES INVENTAIRES ODOO 16 / 19 :
                    - Consulter les niveaux de stock : Menu Inventaire > Produits > Produits ou Opérations > Ajustements d'inventaire. Le champ 'Quantité en main' indique le stock physique, 'Quantité prévue' inclut les réceptions et livraisons en attente.
                    - Traiter une réception (Picking IN) : Inventaire > Opérations > Réceptions > Sélectionner le transfert > Vérifier la quantité faite > Cliquer sur 'Valider'.
                    - Traiter une livraison client (Picking OUT) : Inventaire > Opérations > Bons de livraison > Vérifier la disponibilité > Valider l'expédition.
                    - Règles de réapprovisionnement : Menu Inventaire > Configuration > Règles de réapprovisionnement. Permet de définir une quantité minimale (seuil d'alerte) et maximale pour déclencher automatiquement des demandes de prix (RFQ).
                """),

                // 3. Module Achats & Fournisseurs (Odoo 16 & 19)
                new Document("""
                    GUIDE ACHATS & FOURNISSEURS ODOO 16 / 19 :
                    - Créer une demande de prix (RFQ) : Achats > Demandes de prix > 'Nouveau'. Sélectionner le Fournisseur, ajouter les articles à commander, et envoyer par email.
                    - Valider le bon de commande (PO) : Cliquer sur 'Confirmer la commande'. Cela génère automatiquement un ordre de réception dans l'inventaire (WH/IN).
                    - Contrôler et créer la facture fournisseur : Une fois les marchandises reçues, cliquer sur 'Créer la facture' pour faire le rapprochement à 3 voies (Commande - Réception - Facture).
                """),

                // 4. Module Organisation & Projets (Odoo 16 & 19)
                new Document("""
                    GUIDE GESTION DE PROJETS & ORGANISATION ODOO 16 / 19 :
                    - Créer un projet : Menu Projet > 'Nouveau' / 'Créer'. Configurez le nom du projet, le chef de projet, et les étapes Kanban (ex: Nouveau, En cours, En attente, Terminé).
                    - Gérer les tâches : Ouvrez le projet > 'Créer une tâche'. Renseignez l'assigné, la date limite (deadline), la priorité, et la description détaillée.
                    - Feuilles de temps (Timesheets) : Enregistrez le temps passé sur chaque tâche via l'onglet Feuilles de temps pour suivre la rentabilité et le taux d'avancement.
                    - Tâches en retard : Utilisez le filtre de recherche 'En retard' ou regroupez par 'Étape' pour identifier les goulots d'étranglement.
                """),

                // 5. Monitoring Global & KPIs
                new Document("""
                    MONITORING & TABLEAUX DE BORD STRATÉGIQUES ODOO :
                    - Tableau de bord Ventes : Suivi du CA mensuel, panier moyen, taux de conversion des devis.
                    - Tableau de bord Stock : Alertes de rupture de stock, délais d'expédition, rotation des stocks.
                    - Tableau de bord Projets : Taux d'achèvement des tâches, dépassements de deadlines, charge de travail de l'équipe.
                """),

                // 6. Différences Spécifiques Odoo 16 vs Odoo 19
                new Document("""
                    SPÉCIFICITÉS & DIFFÉRENCES ODOO 16 vs ODOO 19 :
                    - Odoo 16 : Interface classique avec barre de recherche filtrée, actions en haut de formulaire, intégration standard des modules.
                    - Odoo 19 : Nouvelle ergonomie ultra-rapide, Command Palette unifiée (Ctrl + K), assistants IA intégrés pour la génération de descriptions et l'automatisation des flux de validation.
                """)
            ));

            try {
                vectorStore.add(documents);
                log.info("VectorStore (RAG) initialisé avec succès avec les connaissances Odoo 16 & 19 ({} documents).", documents.size());
            } catch (Exception e) {
                log.warn("VectorStore non initialisé via LLM (Ollama indisponible) : les services dynamiques et le contexte prendront le relais.");
            }
        };
    }

    public void addDocument(String content) {
        try {
            vectorStore.add(List.of(new Document(content)));
        } catch (Exception e) {
            log.error("Impossible d'ajouter le document au vectorStore", e);
        }
    }
}
