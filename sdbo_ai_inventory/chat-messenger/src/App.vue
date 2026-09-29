<template>
  <div class="messenger-layout">
    <!-- Offline Banner -->
    <div v-if="!isOnline" class="offline-banner">
      ⚠️ Vous êtes actuellement en mode hors-ligne. Les requêtes IA seront synchronisées dès le retour de la connexion.
    </div>

    <!-- Sidebar -->
    <div class="sidebar">
      <div class="sidebar-header">
        <div class="brand">
          <img src="/pwa-icon.svg" alt="Logo" class="pwa-logo" />
          <div>
            <h2>Assistant Odoo</h2>
            <span class="version-badge">PWA Ready • v16 & v19</span>
          </div>
        </div>
      </div>

      <!-- PWA Install Banner if installable -->
      <div v-if="deferredPrompt" class="install-card">
        <div class="install-info">
          <strong>📱 Installer l'App</strong>
          <span>Accès direct depuis votre bureau ou smartphone</span>
        </div>
        <button class="install-btn" @click="installPwa">Installer</button>
      </div>

      <div class="version-selector-box">
        <label class="section-label">Instance Odoo Active</label>
        <div class="version-toggle">
          <button 
            :class="{ active: selectedVersion === 'v19' }" 
            @click="setVersion('v19')"
          >
            🚀 Odoo 19
          </button>
          <button 
            :class="{ active: selectedVersion === 'v16' }" 
            @click="setVersion('v16')"
          >
            🏢 Odoo 16
          </button>
        </div>
      </div>

      <div class="sidebar-nav">
        <label class="section-label">Accès Rapide Métier</label>
        <button class="nav-item" @click="askQuestion('Donne-moi le résumé des ventes et le CA')">
          <span class="nav-icon">📊</span>
          <div class="nav-info">
            <span class="nav-title">Ventes & CA</span>
            <span class="nav-sub">Chiffre d'affaires, paniers, top clients</span>
          </div>
        </button>

        <button class="nav-item" @click="askQuestion('Quel est l\'état du stock et les alertes de rupture ?')">
          <span class="nav-icon">📦</span>
          <div class="nav-info">
            <span class="nav-title">Stock & Inventaire</span>
            <span class="nav-sub">Quants, réceptions, livraisons</span>
          </div>
        </button>

        <button class="nav-item" @click="askQuestion('Montre-moi l\'état des projets et des tâches')">
          <span class="nav-icon">📁</span>
          <div class="nav-info">
            <span class="nav-title">Organisation & Projets</span>
            <span class="nav-sub">Avancement, délais, tâches en retard</span>
          </div>
        </button>

        <button class="nav-item" @click="askQuestion('Quel est le résumé des achats et fournisseurs ?')">
          <span class="nav-icon">🛒</span>
          <div class="nav-info">
            <span class="nav-title">Achats & Fournisseurs</span>
            <span class="nav-sub">Commandes fournisseurs, dépenses</span>
          </div>
        </button>

        <button class="nav-item" @click="askQuestion('Donne-moi le dashboard de monitoring global 360°')">
          <span class="nav-icon">📈</span>
          <div class="nav-info">
            <span class="nav-title">Monitoring 360°</span>
            <span class="nav-sub">KPIs stratégiques, alertes clés</span>
          </div>
        </button>

        <label class="section-label" style="margin-top: 15px;">Guides d'utilisation</label>
        <button class="nav-item guide" @click="askQuestion('Comment créer et valider un devis de vente sur ' + (selectedVersion === 'v19' ? 'Odoo 19' : 'Odoo 16') + ' ?')">
          <span class="nav-icon">💡</span>
          <div class="nav-info">
            <span class="nav-title">Guide Ventes</span>
            <span class="nav-sub">Création devis & facturation</span>
          </div>
        </button>

        <button class="nav-item guide" @click="askQuestion('Comment gérer le réapprovisionnement et les règles de stock sur ' + (selectedVersion === 'v19' ? 'Odoo 19' : 'Odoo 16') + ' ?')">
          <span class="nav-icon">💡</span>
          <div class="nav-info">
            <span class="nav-title">Guide Stock</span>
            <span class="nav-sub">Règles min/max & réceptions</span>
          </div>
        </button>
      </div>
    </div>

    <!-- Main Chat Window -->
    <div class="chat-main">
      <header class="chat-header">
        <div class="chat-target">
          <div class="avatar-small">
            <img src="https://api.dicebear.com/7.x/bottts/svg?seed=SODO" alt="AI">
          </div>
          <div class="header-text">
            <h3>Assistant SODO <span class="badge-pill">Expert Odoo {{ selectedVersion.toUpperCase() }}</span></h3>
            <p>Connecté aux bases de données Odoo 16 & Odoo 19 • Mode PWA</p>
          </div>
        </div>

        <div class="header-actions">
          <button v-if="deferredPrompt" class="header-install-btn" @click="installPwa">
            📲 Installer
          </button>
          <div :class="['status-chip', isOnline ? 'online' : 'offline']">
            <span class="dot-online"></span> {{ isOnline ? 'Base Odoo Active' : 'Hors-ligne' }}
          </div>
        </div>
      </header>

      <!-- Quick Chips bar -->
      <div class="quick-chips-bar">
        <button class="chip" @click="askQuestion('Résumé des Ventes & CA')">📊 Ventes & CA</button>
        <button class="chip" @click="askQuestion('État des stocks et alertes')">📦 Stocks</button>
        <button class="chip" @click="askQuestion('Dépenses et Achats en cours')">🛒 Achats</button>
        <button class="chip" @click="askQuestion('Suivi des Projets et tâches en retard')">📁 Projets</button>
        <button class="chip" @click="askQuestion('Monitoring Global et KPIs')">📈 Monitoring</button>
      </div>

      <!-- Messages Area -->
      <div class="messages-area" ref="scrollArea">
        <div v-for="(msg, index) in messages" :key="index" :class="['message-row', msg.role]">
          <div v-if="msg.role === 'bot'" class="bot-avatar-msg">
            <img src="https://api.dicebear.com/7.x/bottts/svg?seed=SODO" alt="AI">
          </div>
          <div class="message-bubble">
            <div v-if="msg.version" class="msg-meta-tag">
              Odoo {{ msg.version.toUpperCase() }}
            </div>
            <div class="message-text" v-html="formatMessage(msg.text)"></div>
            <div v-if="msg.redirect" class="redirect-card">
              <button @click="navigateTo(msg.redirect)">
                🔗 Ouvrir dans Odoo ({{ msg.redirect }})
              </button>
            </div>
          </div>
        </div>
        
        <!-- Typing indicator -->
        <div v-if="isTyping" class="message-row bot">
          <div class="bot-avatar-msg">
            <img src="https://api.dicebear.com/7.x/bottts/svg?seed=SODO" alt="AI">
          </div>
          <div class="message-bubble typing">
            <div class="dot"></div><div class="dot"></div><div class="dot"></div>
          </div>
        </div>
      </div>

      <footer class="chat-footer">
        <div class="input-container">
          <input 
            v-model="input" 
            @keyup.enter="send" 
            :placeholder="'Posez une question sur Odoo ' + selectedVersion.toUpperCase() + ' (Ventes, CA, Stock, Projets, Organisation, Achats)...'"
            :disabled="isTyping"
          />
          <button @click="send" :disabled="!input.trim() || isTyping">
            <svg viewBox="0 0 24 24" width="22" height="22" fill="currentColor"><path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"></path></svg>
          </button>
        </div>
      </footer>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'

const selectedVersion = ref('v19')
const isOnline = ref(navigator.onLine)
const deferredPrompt = ref(null)

const messages = ref([
  { 
    role: 'bot', 
    version: 'v19',
    text: "Bonjour ! Je suis l'Assistant SODO connecté aux bases de données Odoo 16 et Odoo 19 (PWA Progressive Web App).\n\nJe peux vous assister pour :\n- 📊 **Ventes & Chiffre d'Affaires (CA)** : Résumé financier, paniers moyens et top clients\n- 📦 **Stock & Inventaire** : Niveaux des quants, réapprovisionnement et alertes de rupture\n- 📁 **Organisation & Projets** : Suivi des tâches, planning Kanban et deadlines\n- 🛒 **Achats & Fournisseurs** : Dépenses engagées et bons de commande\n- 📈 **Monitoring 360°** : Tableau de bord de santé globale de l'entreprise\n- 💡 **Guides d'utilisation** : Aide pas-à-pas pour naviguer et utiliser les modules Odoo 16 & 19.\n\nVous pouvez installer cette application sur votre bureau ou smartphone pour un accès instantané !"
  }
])
const input = ref('')
const isTyping = ref(false)
const scrollArea = ref(null)

const setVersion = (ver) => {
  selectedVersion.value = ver
  messages.value.push({
    role: 'bot',
    version: ver,
    text: `Bascule effectuée vers **Odoo ${ver.toUpperCase()}**. Les prochaines requêtes pointeront sur cette instance.`
  })
  scrollToBottom()
}

const scrollToBottom = async () => {
  await nextTick()
  if (scrollArea.value) {
    scrollArea.value.scrollTop = scrollArea.value.scrollHeight
  }
}

const askQuestion = (text) => {
  input.value = text
  send()
}

const send = async () => {
  if (!input.value.trim() || isTyping.value) return
  
  const userText = input.value
  messages.value.push({ role: 'user', text: userText })
  input.value = ''
  isTyping.value = true
  scrollToBottom()

  try {
    const res = await fetch('/api/ai/chat', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ 
        message: userText,
        odooVersion: selectedVersion.value
      })
    })
    
    const data = await res.json()
    messages.value.push({ 
      role: 'bot', 
      version: data.odooVersion || selectedVersion.value,
      text: data.answer,
      redirect: data.redirectModule 
    })
  } catch (error) {
    messages.value.push({ 
      role: 'bot', 
      version: selectedVersion.value,
      text: "Désolé, une erreur est survenue lors de la communication avec le serveur IA." 
    })
  } finally {
    isTyping.value = false
    scrollToBottom()
  }
}

const formatMessage = (text) => {
  if (!text) return ''
  let formatted = text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
    .replace(/`([^`]+)`/g, '<code>$1</code>')
    .replace(/\n/g, '<br/>')
  return formatted
}

const navigateTo = (path) => {
  window.open(path, '_blank')
}

const installPwa = async () => {
  if (deferredPrompt.value) {
    deferredPrompt.value.prompt()
    const { outcome } = await deferredPrompt.value.userChoice
    if (outcome === 'accepted') {
      deferredPrompt.value = null
    }
  }
}

onMounted(() => {
  scrollToBottom()

  window.addEventListener('online', () => { isOnline.value = true })
  window.addEventListener('offline', () => { isOnline.value = false })

  window.addEventListener('beforeinstallprompt', (e) => {
    e.preventDefault()
    deferredPrompt.value = e
  })
})
</script>

<style>
* { box-sizing: border-box; }
body { margin: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; }

.messenger-layout {
  display: flex;
  width: 100vw;
  height: 100vh;
  background: #f0f2f5;
  position: relative;
}

.offline-banner {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  background: #f59e0b;
  color: #ffffff;
  padding: 6px 12px;
  text-align: center;
  font-size: 12px;
  font-weight: 600;
  z-index: 9999;
}

.sidebar {
  width: 320px;
  background: #ffffff;
  border-right: 1px solid #e4e6eb;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

.sidebar-header {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f2f5;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.pwa-logo {
  width: 40px;
  height: 40px;
  border-radius: 10px;
}

.sidebar-header h2 {
  margin: 0;
  font-size: 16px;
  color: #1c1e21;
  font-weight: 700;
}

.version-badge {
  background: #e7f3ff;
  color: #1877f2;
  font-size: 10px;
  font-weight: bold;
  padding: 2px 6px;
  border-radius: 10px;
}

.install-card {
  margin: 12px 16px 0 16px;
  padding: 12px;
  background: linear-gradient(135deg, #1877f2 0%, #0a4bb3 100%);
  color: #ffffff;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  box-shadow: 0 4px 10px rgba(24, 119, 242, 0.25);
}

.install-info {
  display: flex;
  flex-direction: column;
}

.install-info strong {
  font-size: 12px;
}

.install-info span {
  font-size: 10px;
  opacity: 0.9;
}

.install-btn {
  background: #ffffff;
  color: #1877f2;
  border: none;
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 700;
  cursor: pointer;
  white-space: nowrap;
}

.version-selector-box {
  padding: 14px 20px;
  background: #f8f9fa;
  border-bottom: 1px solid #e4e6eb;
  margin-top: 10px;
}

.section-label {
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: #65676b;
  font-weight: 700;
  display: block;
  margin-bottom: 8px;
}

.version-toggle {
  display: flex;
  gap: 8px;
  background: #e4e6eb;
  padding: 3px;
  border-radius: 8px;
}

.version-toggle button {
  flex: 1;
  border: none;
  background: transparent;
  padding: 8px 10px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  color: #65676b;
  transition: all 0.2s;
}

.version-toggle button.active {
  background: #ffffff;
  color: #1877f2;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.sidebar-nav {
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 8px;
  border: 1px solid transparent;
  background: transparent;
  text-align: left;
  cursor: pointer;
  transition: all 0.15s;
}

.nav-item:hover {
  background: #f0f2f5;
  border-color: #e4e6eb;
}

.nav-item.guide {
  background: #fdfdfd;
}

.nav-icon {
  font-size: 20px;
}

.nav-info {
  display: flex;
  flex-direction: column;
}

.nav-title {
  font-size: 13px;
  font-weight: 600;
  color: #1c1e21;
}

.nav-sub {
  font-size: 11px;
  color: #65676b;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #ffffff;
}

.chat-header {
  height: 65px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e4e6eb;
  box-shadow: 0 2px 6px rgba(0,0,0,0.03);
  background: #ffffff;
}

.chat-target {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar-small {
  width: 42px;
  height: 42px;
}

.avatar-small img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: #e7f3ff;
}

.header-text h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #1c1e21;
  display: flex;
  align-items: center;
  gap: 8px;
}

.badge-pill {
  background: #e7f3ff;
  color: #1877f2;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
  font-weight: 600;
}

.header-text p {
  margin: 2px 0 0 0;
  font-size: 12px;
  color: #65676b;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-install-btn {
  background: #1877f2;
  color: #ffffff;
  border: none;
  padding: 6px 14px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 2px 6px rgba(24, 119, 242, 0.3);
}

.status-chip {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 600;
  padding: 5px 12px;
  border-radius: 20px;
}

.status-chip.online {
  color: #2e7d32;
  background: #e8f5e9;
}

.status-chip.offline {
  color: #c62828;
  background: #ffebee;
}

.dot-online {
  width: 8px;
  height: 8px;
  background: #4caf50;
  border-radius: 50%;
}

.status-chip.offline .dot-online {
  background: #f44336;
}

.quick-chips-bar {
  padding: 8px 20px;
  background: #f8f9fa;
  border-bottom: 1px solid #e4e6eb;
  display: flex;
  gap: 8px;
  overflow-x: auto;
}

.chip {
  background: #ffffff;
  border: 1px solid #dcdfe4;
  color: #333;
  padding: 6px 14px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.2s;
}

.chip:hover {
  background: #1877f2;
  color: #ffffff;
  border-color: #1877f2;
}

.messages-area {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 14px;
  background: #f9fafb;
}

.message-row {
  display: flex;
  gap: 10px;
  max-width: 80%;
}

.message-row.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-row.bot {
  align-self: flex-start;
}

.bot-avatar-msg {
  width: 32px;
  height: 32px;
  align-self: flex-start;
}

.bot-avatar-msg img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: #e7f3ff;
}

.message-bubble {
  padding: 12px 16px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.5;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
}

.msg-meta-tag {
  font-size: 10px;
  font-weight: 700;
  text-transform: uppercase;
  color: #1877f2;
  margin-bottom: 6px;
  display: inline-block;
  background: #e7f3ff;
  padding: 2px 6px;
  border-radius: 4px;
}

.user .message-bubble {
  background: #1877f2;
  color: #ffffff;
  border-bottom-right-radius: 4px;
}

.bot .message-bubble {
  background: #ffffff;
  color: #1c1e21;
  border-bottom-left-radius: 4px;
  border: 1px solid #e4e6eb;
}

.message-text code {
  background: #f0f2f5;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
  font-family: monospace;
}

.redirect-card {
  margin-top: 10px;
  padding-top: 8px;
  border-top: 1px solid #eee;
}

.redirect-card button {
  padding: 8px 14px;
  background: #1877f2;
  color: #ffffff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  font-size: 12px;
  transition: opacity 0.2s;
}

.redirect-card button:hover {
  opacity: 0.9;
}

.chat-footer {
  padding: 14px 20px;
  border-top: 1px solid #e4e6eb;
  background: #ffffff;
}

.input-container {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #f0f2f5;
  padding: 4px 6px 4px 16px;
  border-radius: 24px;
}

.input-container input {
  flex: 1;
  padding: 10px 0;
  background: transparent;
  border: none;
  outline: none;
  font-size: 14px;
}

.input-container button {
  background: #1877f2;
  border: none;
  color: #ffffff;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.15s;
}

.input-container button:hover:not(:disabled) {
  transform: scale(1.05);
}

.input-container button:disabled {
  background: #bcc0c4;
  cursor: not-allowed;
}

.typing {
  display: flex;
  gap: 4px;
  padding: 14px 18px;
}

.dot {
  width: 7px;
  height: 7px;
  background: #90949c;
  border-radius: 50%;
  animation: bounce 1.3s infinite;
}

.dot:nth-child(2) { animation-delay: 0.15s; }
.dot:nth-child(3) { animation-delay: 0.3s; }

@keyframes bounce {
  0%, 60%, 100% { transform: translateY(0); }
  30% { transform: translateY(-5px); }
}

@media (max-width: 768px) {
  .sidebar { display: none; }
}
</style>
