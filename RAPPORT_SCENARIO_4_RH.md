# 📋 RAPPORT DE TRANSMISSION — SCÉNARIO 4 : RH / EMPLOYÉS / PRÉSENCES / CONGÉS

- **Responsable :** Saad
- **Environnement :** Odoo 19.0 (PostgreSQL 16)
- **Base de données :** `odoo19` (dupliquée depuis `sodo_hr_db`)
- **Statut :** Validé & Conforme (100%)

---

## 🏢 1. Départements Créés (Étape 1)
| Département | Statut |
| :--- | :--- |
| **Direction** | Créé & Actif |
| **Logistique** | Créé & Actif |
| **Commercial** | Créé & Actif |

---

## 👥 2. Références des Employés (Étape 2)
| Employé | Email Professionnel | Département | Intitulé du Poste | Responsable |
| :--- | :--- | :--- | :--- | :--- |
| **Samir Démo** | `samir@magasin.example` | Direction | Responsable magasin | — |
| **Youssef Démo** | `youssef@magasin.example` | Logistique | Magasinier | Samir Démo |
| **Salma Démo** | `salma@magasin.example` | Commercial | Vendeuse | Samir Démo |

---

## ⏰ 3. Horaire de Travail Utilisé (Étape 3)
- **Nom du calendrier :** `Magasin — 40 h de démonstration`
- **Fuseau horaire :** `Africa/Casablanca`
- **Jours travaillés :** Du Lundi au Vendredi
  - **Matin :** 08:00 → 12:00 (4h)
  - **Après-midi :** 13:00 → 17:00 (4h)
- **Week-end :** Samedi & Dimanche non travaillés (0h)
- **Affectation :** Associé aux 3 employés (Samir Démo, Youssef Démo, Salma Démo).

---

## ⏱️ 4. Enregistrements de Présences du 21/09/2026 (Étape 4)
| Employé | Date | Pointage Entrée | Pointage Sortie | Heures Calculées |
| :--- | :--- | :--- | :--- | :--- |
| **Youssef Démo** | 21/09/2026 | 08:00:00 | 12:00:00 | 4.0 h |
| **Youssef Démo** | 21/09/2026 | 13:00:00 | 17:00:00 | 4.0 h |
| **Total Youssef** | **21/09/2026** | — | — | **8.0 heures** |
| **Salma Démo** | 21/09/2026 | 08:00:00 | 12:00:00 | 4.0 h |
| **Salma Démo** | 21/09/2026 | 13:00:00 | 17:00:00 | 4.0 h |
| **Total Salma** | **21/09/2026** | — | — | **8.0 heures** |

---

## 🌴 5. Configuration & Cycle des Congés (Étapes 5, 6, 7)

### A. Type de Congé Créé
- **Nom :** `Congé annuel — Démo`
- **Unité de décompte :** Jours
- **Allocation nécessaire :** Oui
- **Validation :** Par le responsable des congés (Mitchell Admin)

### B. Allocation de Congés (Youssef Démo)
- **Bénéficiaire :** Youssef Démo
- **Nombre de jours alloués :** 5 jours
- **Période de validité :** À partir du 21/09/2026
- **Statut de l'allocation :** **Approuvé / Validé**

### C. Demande de Congé & Décompte
- **Bénéficiaire :** Youssef Démo
- **Période demandée :** Du 22/09/2026 au 22/09/2026
- **Durée calculée :** 1 jour ouvré
- **Statut :** **Approuvé / Validé**
- **Vérification présence 22/09/2026 :** Aucune présence enregistrée pour Youssef le 22/09/2026.

### D. Bilan du Solde de Congé
$$\text{Solde Restant} = 5\text{ jours alloués} - 1\text{ jour pris} = \mathbf{4\text{ jours restants}}$$

---

## 🔒 6. Contrôle des Droits d'Accès (Sécurité RH)
- **Utilisateur testé :** `Marc Demo` (Profil Ventes / Commercial uniquement).
- **Observations vérifiées :**
  - ❌ **Informations privées :** Aucune visibilité sur l'adresse personnelle, téléphone privé ou coordonnées bancaires de Youssef Démo. Seule l'adresse publique du siège de l'entreprise est affichée.
  - ❌ **Onglet Paie / Contrats :** Onglet « Paie / Payroll » totalement masqué pour l'utilisateur commercial.
  - ❌ **Gestion des congés :** Menu « Gestion » et tableau d'allocations inaccessibles (impossible de voir ou modifier le solde de Youssef Démo).
- **Conclusion :** Respect strict du cloisonnement des données RH conforme aux exigences Odoo et au cahier des charges.

---

## 🤖 7. Réponses Cibles pour le Test Assistant IA
| Question | Réponse Fournie par les Données Odoo 19 |
| :--- | :--- |
| **« Qui travaille dans le département Logistique ? »** | **Youssef Démo** (Poste : Magasinier) |
| **« Combien d'heures Youssef Démo a-t-il effectuées le 21 septembre 2026 ? »** | **8 heures** (08:00-12:00 et 13:00-17:00) |
| **« Qui est en congé le 22 septembre 2026 ? »** | **Youssef Démo** (Congé annuel — Démo, 1 jour) |
| **« Quel est le solde de congés de Youssef Démo ? »** | **4 jours restants** (sur 5 jours alloués) |
