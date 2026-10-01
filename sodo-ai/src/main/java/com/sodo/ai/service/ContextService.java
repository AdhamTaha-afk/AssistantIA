package com.sodo.ai.service;

import com.sodo.ai.odoo.OdooAnalyticsService;
import com.sodo.ai.odoo.OdooConfigProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContextService {

    private final OdooAnalyticsService odooAnalytics;
    private final OdooConfigProperties configProperties;

    public String buildBusinessContext(String userQuery) {
        return buildBusinessContext(userQuery, null);
    }

    public String buildBusinessContext(String userQuery, String explicitVersion) {
        String q = userQuery == null ? "" : userQuery.toLowerCase();
        String version = resolveOdooVersion(q, explicitVersion);

        StringBuilder context = new StringBuilder();
        context.append("INFORMATIONS SYSTÈME ODOO:\n");
        context.append("- Version Odoo ciblée : ").append(version.toUpperCase()).append("\n");
        context.append("- Instances supportées : Odoo 16 & Odoo 19 (Organisation, Stock, Ventes, Achats, Facturation, Projets, Monitoring)\n\n");

        boolean matched = false;

        // 1. Détection VENTES & CHIFFRE D'AFFAIRES (CA)
        if (q.contains("vente") || q.contains("chiffre d'affaire") || q.contains("ca") || q.contains("commande") || q.contains("devis") || q.contains("client")) {
            context.append(odooAnalytics.getSalesAndCaSummary(version)).append("\n");
            matched = true;
        }

        // 2. Détection STOCK & INVENTAIRE
        if (q.contains("stock") || q.contains("inventaire") || q.contains("quant") || q.contains("rupture") || q.contains("produit") || q.contains("livraison") || q.contains("réapprovisionnement")) {
            context.append(odooAnalytics.getStockSummary(version)).append("\n");
            matched = true;
        }

        // 3. Détection ACHATS & FOURNISSEURS
        if (q.contains("achat") || q.contains("fournisseur") || q.contains("rfq") || q.contains("po-") || q.contains("dépense")) {
            context.append(odooAnalytics.getPurchasesSummary(version)).append("\n");
            matched = true;
        }

        // 4. Détection PROJETS, TÂCHES & ORGANISATION
        if (q.contains("projet") || q.contains("organisation") || q.contains("tâche") || q.contains("task") || q.contains("planning") || q.contains("kanban") || q.contains("avancement")) {
            context.append(odooAnalytics.getProjectsAndTasksSummary(version)).append("\n");
            matched = true;
        }

        // 5. Détection MONITORING & KPIS
        if (q.contains("monitoring") || q.contains("kpi") || q.contains("tableau de bord") || q.contains("dashboard") || q.contains("santé") || q.contains("indicateur") || q.contains("résumé")) {
            context.append(odooAnalytics.getGlobalMonitoring(version)).append("\n");
            matched = true;
        }

        // 6. Détection RESSOURCES HUMAINES (RH, EMPLOYÉS, PRÉSENCES, CONGÉS)
        if (q.contains("rh") || q.contains("ressources humaines") || q.contains("employé") || q.contains("employe") || q.contains("collaborateur")
                || q.contains("présence") || q.contains("presence") || q.contains("pointage") || q.contains("heure") || q.contains("travaillé") || q.contains("travaille")
                || q.contains("congé") || q.contains("conge") || q.contains("absence") || q.contains("vacance") || q.contains("solde")
                || q.contains("logistique") || q.contains("direction") || q.contains("commercial")
                || q.contains("youssef") || q.contains("salma") || q.contains("samir") || q.contains("département") || q.contains("departement")) {
            context.append(odooAnalytics.getHrSummary(version)).append("\n");
            matched = true;
        }

        // 7. CONTRÔLE DE SÉCURITÉ & CONFIDENTIALITÉ DES DONNÉES PERSONNELLES
        if (q.contains("salaire") || q.contains("paie") || q.contains("adresse") || q.contains("téléphone") || q.contains("telephone")
                || q.contains("banque") || q.contains("rib") || q.contains("compte bancaire") || q.contains("confidentiel") || q.contains("privé") || q.contains("prive")) {
            context.append("""
            🚨 AVERTISSEMENT DE CONFIDENTIALITÉ & SÉCURITÉ :
            Les informations personnelles (adresse privée, numéro de téléphone privé, coordonnées bancaires/RIB) ainsi que la paie/salaire des employés sont STRICTEMENT CONFIDENTIELLES.
            Selon les règles de sécurité Odoo, ces données sont inaccessibles aux profils Commerciaux/Ventes ou utilisateurs non autorisés.
            Refuse impérativement de divulguer ces données privées et rappelle que seuls les gestionnaires RH disposent des autorisations requises.
            """).append("\n");
            matched = true;
        }

        // Si aucune intention spécifique n'est matchée, fournir un aperçu global consolidé
        if (!matched) {
            context.append(odooAnalytics.getGlobalMonitoring(version)).append("\n");
        }

        return context.toString();
    }

    public String resolveOdooVersion(String query, String explicitVersion) {
        if (explicitVersion != null && !explicitVersion.trim().isEmpty()) {
            if (explicitVersion.contains("16")) return "v16";
            if (explicitVersion.contains("19")) return "v19";
        }
        if (query != null) {
            if (query.contains("16") || query.contains("v16")) return "v16";
            if (query.contains("19") || query.contains("v19")) return "v19";
        }
        return configProperties.getDefaultVersion();
    }
}
