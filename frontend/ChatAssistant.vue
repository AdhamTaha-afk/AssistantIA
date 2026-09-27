<template>
  <div class="sodo-ai-container">
    <!-- Floating Button -->
    <button 
      @click="toggleChat" 
      class="chat-trigger"
      :class="{ 'is-active': isOpen }"
    >
      <svg v-if="!isOpen" xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m3 21 1.9-5.7a8.5 8.5 0 1 1 3.8 3.8z"/></svg>
      <svg v-else xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 6 6 18"/><path d="m6 6 12 12"/></svg>
      <span v-if="!isOpen" class="notification-badge">Odoo</span>
    </button>

    <!-- Chat Window -->
    <transition name="slide-up">
      <div v-if="isOpen" class="chat-window">
        <!-- Header -->
        <div class="chat-header">
          <div class="header-info">
            <div class="bot-avatar">
              <div class="pulse-ring"></div>
              <img src="https://api.dicebear.com/7.x/bottts/svg?seed=SODO" alt="AI Avatar">
            </div>
            <div>
              <h3>Assistant Odoo {{ odooVersion.toUpperCase() }}</h3>
              <span class="status">En ligne • Ventes, Stock, Projets, Achats & CA</span>
            </div>
          </div>
        </div>

        <!-- Suggestions Bar -->
        <div class="quick-suggestions">
          <button @click="sendSuggestion('Donne-moi le résumé des ventes et le CA')">📊 Ventes & CA</button>
          <button @click="sendSuggestion('Quel est l\'état du stock et les alertes ?')">📦 Stocks</button>
          <button @click="sendSuggestion('Montre-moi l\'avancement des projets et des tâches')">📁 Projets</button>
          <button @click="sendSuggestion('Donne-moi le dashboard de monitoring global 360°')">📈 Monitoring</button>
        </div>

        <!-- Messages Area -->
        <div class="messages-container" ref="messageScroll">
          <div v-for="(msg, index) in messages" :key="index" :class="['message-wrapper', msg.role]">
            <div class="message-bubble">
              <div v-html="formatText(msg.text)"></div>
              <div v-if="msg.redirect" class="redirect-action">
                <button @click="navigateTo(msg.redirect)" class="action-btn">
                  Ouvrir le module ({{ msg.redirect }})
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"/><polyline points="15 3 21 3 21 9"/><line x1="10" y1="14" x2="21" y2="3"/></svg>
                </button>
              </div>
            </div>
            <span class="message-time">{{ msg.time }}</span>
          </div>

          <!-- Typing Indicator -->
          <div v-if="isTyping" class="message-wrapper bot">
            <div class="message-bubble typing">
              <span></span><span></span><span></span>
            </div>
          </div>
        </div>

        <!-- Input Area -->
        <div class="chat-input-area">
          <input 
            v-model="userInput" 
            @keyup.enter="sendMessage"
            placeholder="Posez votre question sur Odoo..." 
            type="text"
          >
          <button @click="sendMessage" :disabled="!userInput.trim() || isTyping">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>
          </button>
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
import { ref, nextTick } from 'vue';
import { useRouter } from 'vue-router';

export default {
  name: 'ChatAssistant',
  props: {
    odooVersion: {
      type: String,
      default: 'v19'
    }
  },
  setup(props) {
    const router = useRouter();
    const isOpen = ref(false);
    const userInput = ref('');
    const isTyping = ref(false);
    const messages = ref([
      { 
        role: 'bot', 
        text: 'Bonjour ! Je suis votre assistant IA connecté aux bases Odoo 16 & Odoo 19. Comment puis-je vous aider sur vos ventes, stocks, projets ou achats ?', 
        time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) 
      }
    ]);
    const messageScroll = ref(null);

    const toggleChat = () => {
      isOpen.value = !isOpen.value;
      if (isOpen.value) {
        scrollToBottom();
      }
    };

    const scrollToBottom = async () => {
      await nextTick();
      if (messageScroll.value) {
        messageScroll.value.scrollTop = messageScroll.value.scrollHeight;
      }
    };

    const formatText = (text) => {
      if (!text) return '';
      return text
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
        .replace(/\n/g, '<br/>');
    };

    const sendMessage = async () => {
      if (!userInput.value.trim() || isTyping.value) return;

      const userText = userInput.value;
      messages.value.push({
        role: 'user',
        text: userText,
        time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      });

      userInput.value = '';
      isTyping.value = true;
      scrollToBottom();

      try {
        const response = await fetch('/api/ai/chat', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ 
            message: userText,
            odooVersion: props.odooVersion 
          })
        });

        const data = await response.json();

        messages.value.push({
          role: 'bot',
          text: data.answer,
          redirect: data.redirectModule,
          time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
        });

      } catch (error) {
        messages.value.push({
          role: 'bot',
          text: 'Désolé, connexion au serveur IA perdue.',
          time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
        });
      } finally {
        isTyping.value = false;
        scrollToBottom();
      }
    };

    const sendSuggestion = (text) => {
      userInput.value = text;
      sendMessage();
    };

    const navigateTo = (path) => {
      if (router) {
        router.push(path);
      } else {
        window.open(path, '_blank');
      }
      isOpen.value = false;
    };

    return {
      isOpen,
      userInput,
      messages,
      isTyping,
      messageScroll,
      toggleChat,
      sendMessage,
      sendSuggestion,
      navigateTo,
      formatText
    };
  }
};
</script>

<style scoped>
/* Utilise chat-assistant.css */
</style>
