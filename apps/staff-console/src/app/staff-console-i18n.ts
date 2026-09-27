export const supportedLanguages = [
  { code: "fr", label: "Français" },
  { code: "en", label: "English" },
  { code: "sw", label: "Kiswahili" }
] as const;

export type StaffConsoleLanguage = (typeof supportedLanguages)[number]["code"];

export const staffConsoleCopy: Record<StaffConsoleLanguage, Record<string, string>> = {
  fr: {
    appName: "EDRC Land GIS",
    productBoundary: "Système proposé de gestion foncière et SIG — pas un registre officiel",
    staffConsole: "Console agents",
    heroTitle: "Opérations foncières contrôlées",
    heroBody:
      "Un espace de travail sécurisé pour préparer, vérifier et auditer les dossiers fonciers. Les décisions légales finales restent réservées aux autorités humaines compétentes.",
    legalNotice:
      "Les certificats, UPI et extraits générés ici sont des artefacts de travail proposés tant qu’une autorisation officielle n’est pas configurée.",
    language: "Langue",
    quickNav: "Navigation rapide",
    connection: "Connexion",
    documents: "Documents",
    exports: "Exports",
    pilot: "Pilote",
    parcels: "Parcelles",
    workflow: "Workflow",
    apiConnection: "Connexion locale API",
    apiConnectionHint: "Paramètres de session locale. Ne jamais utiliser d’identifiants réels de production.",
    apiUrl: "URL API",
    staffEmail: "Email agent",
    password: "Mot de passe",
    operationsOverview: "Vue opérationnelle",
    loadedDocuments: "Documents chargés",
    openTasks: "Tâches ouvertes",
    notifications: "Notifications",
    documentBlockers: "Blocages documentaires",
    operationalNotifications: "Notifications opérationnelles",
    notificationHint: "Messages de service destinés uniquement à l’agent authentifié.",
    refresh: "Actualiser",
    noneLoaded: "Aucun élément chargé.",
    markRead: "Marquer comme lu",
    read: "Lu",
    documentEvidence: "Documents et preuves",
    documentIntake: "Intake documentaire contrôlé",
    documentIntakeHint:
      "Métadonnées, versions, scans, signatures et quarantaine sont gérés sans stocker les fichiers volumineux dans la base transactionnelle.",
    refreshDocument: "Actualiser document",
    dataGovernance: "Gouvernance des données",
    controlledExports: "Exports documentaires contrôlés",
    controlledExportsHint:
      "Les exports protégés exigent une demande, une revue humaine, un package traçable et aucune donnée sensible dans les canaux non sécurisés.",
    pilotReadiness: "Préparation pilote",
    formalPilot: "Dossier pilote formel",
    foundationFlows: "Flux cadastraux et registre",
    foundationFlowsHint:
      "Création administrative, brouillons de parcelles, géométries, parties, restrictions et tâches maker-checker.",
    workflowTasks: "Tâches de workflow",
    workflowHint:
      "Les transitions sensibles restent en attente jusqu’à une validation humaine explicite, auditable et autorisée."
  },
  en: {
    appName: "EDRC Land GIS",
    productBoundary: "Proposed land and GIS management system — not an official registry",
    staffConsole: "Staff console",
    heroTitle: "Controlled land operations",
    heroBody:
      "A secure workspace to prepare, verify and audit land-service records. Final legal decisions remain with authorized human institutions.",
    legalNotice:
      "Certificates, UPIs and extracts generated here are proposed workflow artifacts until formal authority is configured.",
    language: "Language",
    quickNav: "Quick navigation",
    connection: "Connection",
    documents: "Documents",
    exports: "Exports",
    pilot: "Pilot",
    parcels: "Parcels",
    workflow: "Workflow",
    apiConnection: "Local API connection",
    apiConnectionHint: "Local session settings. Never use real production credentials here.",
    apiUrl: "API URL",
    staffEmail: "Staff email",
    password: "Password",
    operationsOverview: "Operational overview",
    loadedDocuments: "Loaded documents",
    openTasks: "Open tasks",
    notifications: "Notifications",
    documentBlockers: "Document blockers",
    operationalNotifications: "Operational notifications",
    notificationHint: "Service messages for the authenticated staff user only.",
    refresh: "Refresh",
    noneLoaded: "Nothing loaded yet.",
    markRead: "Mark as read",
    read: "Read",
    documentEvidence: "Documents and evidence",
    documentIntake: "Controlled document intake",
    documentIntakeHint:
      "Metadata, versions, scans, signatures and quarantine are managed without storing large files in the transactional database.",
    refreshDocument: "Refresh document",
    dataGovernance: "Data governance",
    controlledExports: "Controlled document exports",
    controlledExportsHint:
      "Protected exports require a request, human review, traceable package and no sensitive details in unsecured channels.",
    pilotReadiness: "Pilot readiness",
    formalPilot: "Formal pilot dossier",
    foundationFlows: "Cadastral and registry flows",
    foundationFlowsHint:
      "Administrative setup, parcel drafts, geometry, parties, restrictions and maker-checker workflow tasks.",
    workflowTasks: "Workflow tasks",
    workflowHint:
      "Sensitive transitions remain pending until explicit, auditable and authorized human validation."
  },
  sw: {
    appName: "EDRC Land GIS",
    productBoundary: "Mfumo pendekezwa wa ardhi na GIS — si rejesta rasmi",
    staffConsole: "Dashibodi ya watumishi",
    heroTitle: "Uendeshaji wa ardhi unaodhibitiwa",
    heroBody:
      "Nafasi salama ya kuandaa, kuthibitisha na kukagua kumbukumbu za huduma za ardhi. Maamuzi ya mwisho ya kisheria hubaki kwa taasisi zilizoidhinishwa.",
    legalNotice:
      "Vyeti, UPI na dondoo zinazozalishwa hapa ni vielelezo vya kazi vilivyopendekezwa mpaka mamlaka rasmi isanidiwe.",
    language: "Lugha",
    quickNav: "Urambazaji wa haraka",
    connection: "Muunganisho",
    documents: "Nyaraka",
    exports: "Mauzo ya data",
    pilot: "Jaribio",
    parcels: "Viwanja",
    workflow: "Mtiririko wa kazi",
    apiConnection: "Muunganisho wa API wa ndani",
    apiConnectionHint: "Mipangilio ya kikao cha ndani. Usitumie kamwe siri halisi za uzalishaji.",
    apiUrl: "URL ya API",
    staffEmail: "Barua pepe ya mtumishi",
    password: "Nenosiri",
    operationsOverview: "Muhtasari wa uendeshaji",
    loadedDocuments: "Nyaraka zilizopakiwa",
    openTasks: "Kazi zilizo wazi",
    notifications: "Arifa",
    documentBlockers: "Vizuizi vya nyaraka",
    operationalNotifications: "Arifa za uendeshaji",
    notificationHint: "Ujumbe wa huduma kwa mtumishi aliyeingia pekee.",
    refresh: "Sasisha",
    noneLoaded: "Hakuna kilichopakiwa bado.",
    markRead: "Weka kama imesomwa",
    read: "Imesomwa",
    documentEvidence: "Nyaraka na ushahidi",
    documentIntake: "Upokeaji wa nyaraka unaodhibitiwa",
    documentIntakeHint:
      "Metadata, matoleo, skani, saini na karantini husimamiwa bila kuhifadhi faili kubwa kwenye hifadhidata ya miamala.",
    refreshDocument: "Sasisha waraka",
    dataGovernance: "Utawala wa data",
    controlledExports: "Utoaji wa nyaraka unaodhibitiwa",
    controlledExportsHint:
      "Utoaji wa data lindwa unahitaji ombi, ukaguzi wa binadamu, kifurushi kinachofuatiliwa na hakuna taarifa nyeti kwenye njia zisizo salama.",
    pilotReadiness: "Utayari wa jaribio",
    formalPilot: "Jalada rasmi la jaribio",
    foundationFlows: "Mtiririko wa kadasta na rejesta",
    foundationFlowsHint:
      "Usanidi wa utawala, rasimu za viwanja, jiometria, wahusika, vizuizi na kazi za maker-checker.",
    workflowTasks: "Kazi za mtiririko",
    workflowHint:
      "Mabadiliko nyeti husubiri uthibitisho wa wazi, unaokaguliwa na ulioidhinishwa na binadamu."
  }
};
