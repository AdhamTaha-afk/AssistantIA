package com.sodo.ai.service;

import com.sodo.ai.odoo.OdooAnalyticsService;
import com.sodo.ai.odoo.OdooConfigProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContextService {

    private final OdooAnalyticsService odooAnalytics;
    private final OdooConfigProperties configProperties;

    private static final Pattern THRESHOLD_PATTERN = Pattern.compile("(?:moins de|inf[ée]rieur[e]? [àa])\\s+(\\d+)");

    public String buildBusinessContext(String userQuery) {
        return buildBusinessContext(userQuery, null, null);
    }

    public String buildBusinessContext(String userQuery, String explicitVersion) {
        return buildBusinessContext(userQuery, explicitVersion, null);
    }

    public String buildBusinessContext(String userQuery, String explicitVersion, String historyText) {
        String q = userQuery == null ? "" : userQuery.toLowerCase();
        String version = resolveOdooVersion(q, explicitVersion);

        StringBuilder context = new StringBuilder();
        context.append("INFORMATIONS SYSTÈME ODOO:\n");
        context.append("- Version Odoo ciblée : ").append(version.toUpperCase()).append("\n\n");

        // 1. Question sur un seuil de stock ("produits ayant moins de X unités")
        Matcher m = THRESHOLD_PATTERN.matcher(q);
        if (m.find()) {
            double threshold = Double.parseDouble(m.group(1));
            context.append(odooAnalytics.getProductsBelowThreshold(version, threshold)).append("\n");
            return context.toString();
        }

        // 2. Détection d'un produit mentionné explicitement dans la question
        String matchedProduct = detectMostRecentProductName(q, version);

        // 2bis. Question de suivi sans produit cité : on cherche le produit le plus récent dans l'historique de la session
        boolean isFollowUp = q.contains("colis") || q.contains("mouvement") || q.contains("expliquent") || q.contains("passage");
        if (matchedProduct == null && isFollowUp && historyText != null && !historyText.isBlank()) {
            matchedProduct = detectMostRecentProductName(historyText.toLowerCase(), version);
        }

        if (matchedProduct != null) {
            if (q.contains("colis")) {
                context.append(odooAnalytics.getProductPackagesByName(version, matchedProduct)).append("\n");
            } else if (q.contains("mouvement") || q.contains("expliquent") || q.contains("passage")) {
                context.append(odooAnalytics.getProductMovementsByName(version, matchedProduct)).append("\n");
            } else {
                context.append(odooAnalytics.getProductStockByName(version, matchedProduct)).append("\n");
            }
            return context.toString();
        }

        // 3. Repli sur les rapports généraux existants (comportement d'origine)
        boolean matched = false;
        if (q.contains("vente") || q.contains("chiffre d'affaire") || q.contains("commande") || q.contains("devis") || q.contains("client")) {
            context.append(odooAnalytics.getSalesAndCaSummary(version)).append("\n");
            matched = true;
        }
        if (q.contains("stock") || q.contains("inventaire") || q.contains("quant") || q.contains("rupture") || q.contains("produit") || q.contains("livraison") || q.contains("réapprovisionnement")) {
            context.append(odooAnalytics.getStockSummary(version)).append("\n");
            matched = true;
        }
        if (q.contains("achat") || q.contains("fournisseur") || q.contains("rfq") || q.contains("po-") || q.contains("dépense")) {
            context.append(odooAnalytics.getPurchasesSummary(version)).append("\n");
            matched = true;
        }
        if (q.contains("projet") || q.contains("organisation") || q.contains("tâche") || q.contains("task") || q.contains("planning") || q.contains("kanban") || q.contains("avancement")) {
            context.append(odooAnalytics.getProjectsAndTasksSummary(version)).append("\n");
            matched = true;
        }
        if (q.contains("monitoring") || q.contains("kpi") || q.contains("tableau de bord") || q.contains("dashboard") || q.contains("santé") || q.contains("indicateur") || q.contains("résumé")) {
            context.append(odooAnalytics.getGlobalMonitoring(version)).append("\n");
            matched = true;
        }
        if (!matched) {
            context.append(odooAnalytics.getGlobalMonitoring(version)).append("\n");
        }

        return context.toString();
    }

    /**
     * Cherche, parmi les produits existants, celui dont le nom apparaît le plus tard dans le texte donné.
     */
    private String detectMostRecentProductName(String text, String version) {
        List<String> names = odooAnalytics.getAllProductNames(version);
        String best = null;
        int bestIndex = -1;
        for (String name : names) {
            int idx = text.lastIndexOf(name.toLowerCase());
            if (idx > bestIndex) {
                bestIndex = idx;
                best = name;
            }
        }
        return best;
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