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
