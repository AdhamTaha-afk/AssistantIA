class SodoChatWidget extends HTMLElement {
        constructor() {
        super();
        this.attachShadow({ mode: 'open' });
        this.isOpen = false;
        this.odooVersion = 'v19';
        this.sessionId = (crypto.randomUUID && crypto.randomUUID()) || ('session-' + Date.now() + '-' + Math.random().toString(36).substring(2));
        this.messages = [
            { 
                role: 'bot', 
                text: "Bonjour ! Je suis l'assistant IA Odoo 16 & 19. Comment puis-je vous aider ?", 
                time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) 
            }
        ];
    }

    connectedCallback() {
        this.render();
    }

    toggleChat() {
        this.isOpen = !this.isOpen;
        this.render();
        if (this.isOpen) {
            this.scrollToBottom();
        }
    }

    async sendMessage() {
        const inputField = this.shadowRoot.querySelector('#chat-input');
        const text = inputField.value.trim();
        
        if (!text) return;

        // إضافة رسالة المستخدم إلى الواجهة
        this.messages.push({
            role: 'user',
            text: text,
            time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
        });

        inputField.value = '';
        this.render();
        this.scrollToBottom();

        // إضافة رسالة مؤقتة تدل على أن البوت يكتب (جارِ المعالجة...)
        const loadingId = 'loading-' + Date.now();
        this.messages.push({
            role: 'bot',
            text: "...",
            time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
        });
        this.render();
        this.scrollToBottom();

        try {
            // إرسال الطلب الحقيقي إلى الباك إند الخاص بك (Spring Boot / Ollama)
            const response = await fetch('http://localhost:8081/api/ai/chat', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    message: text,
                    odooVersion: this.odooVersion,
                    sessionId: this.sessionId
                })
            });

            if (!response.ok) {
                throw new Error('Erreur de communication avec le serveur AI.');
            }

            const data = await response.json();
            
            // إزالة رسالة التحميل المؤقتة
            this.messages.pop();

            // إضافة الرد الحقيقي القادم من الباك إند
            this.messages.push({
                role: 'bot',
                text: data.answer || "Désolé, je n'ai pas pu comprendre la réponse.",
                time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
            });

        } catch (error) {
            console.error('Erreur AI:', error);
            this.messages.pop();
            this.messages.push({
                role: 'bot',
                text: "Erreur de connexion avec le serveur d'intelligence artificielle.",
                time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
            });
        }

        this.render();
        this.scrollToBottom();
    }

    scrollToBottom() {
        setTimeout(() => {
            const messagesContainer = this.shadowRoot.querySelector('.messages');
            if (messagesContainer) {
                messagesContainer.scrollTop = messagesContainer.scrollHeight;
            }
        }, 50);
    }

    render() {
        this.shadowRoot.innerHTML = `
        <style>
            :host {
                position: fixed !important;
                bottom: 30px !important;
                right: 30px !important;
                z-index: 2147483647 !important;
                display: block !important;
                font-family: Arial, sans-serif;
            }
            .launcher {
                width: 60px !important;
                height: 60px !important;
                border-radius: 50% !important;
                background: #1877f2 !important;
                color: white !important;
                border: none !important;
                cursor: pointer !important;
                display: flex !important;
                align-items: center !important;
                justify-content: center !important;
                font-weight: bold !important;
                font-size: 20px !important;
                box-shadow: 0 4px 16px rgba(0,0,0,0.4) !important;
            }
            .window {
                position: absolute !important;
                bottom: 75px !important;
                right: 0 !important;
                width: 350px !important;
                height: 480px !important;
                background: white !important;
                border-radius: 12px !important;
                display: ${this.isOpen ? 'flex' : 'none'} !important;
                flex-direction: column !important;
                box-shadow: 0 8px 24px rgba(0,0,0,0.3) !important;
                border: 1px solid #ccc !important;
                overflow: hidden !important;
            }
            .header { 
                background: #1877f2; 
                color: white; 
                padding: 12px; 
                font-weight: bold; 
                font-size: 14px;
            }
            .messages { 
                flex: 1; 
                padding: 10px; 
                overflow-y: auto; 
                background: #f9f9f9; 
                display: flex;
                flex-direction: column;
            }
            .msg { 
                padding: 8px 12px; 
                border-radius: 8px; 
                margin-bottom: 8px; 
                font-size: 13px; 
                max-width: 80%; 
                word-wrap: break-word;
            }
            .msg.bot { 
                background: #e4e6eb; 
                color: black; 
                align-self: flex-start; 
            }
            .msg.user { 
                background: #1877f2; 
                color: white; 
                align-self: flex-end; 
            }
            .input-area {
                display: flex;
                padding: 10px;
                background: white;
                border-top: 1px solid #ddd;
            }
            .input-area input {
                flex: 1;
                padding: 8px 12px;
                border: 1px solid #ccc;
                border-radius: 20px;
                outline: none;
                font-size: 13px;
            }
            .input-area button {
                background: #1877f2;
                color: white;
                border: none;
                padding: 8px 14px;
                margin-left: 6px;
                border-radius: 20px;
                cursor: pointer;
                font-weight: bold;
            }
        </style>
        
        <div class="window">
            <div class="header">Assistant IA SDBO</div>
            <div class="messages">
                ${this.messages.map(m => `<div class="msg ${m.role}">${m.text}</div>`).join('')}
            </div>
            <div class="input-area">
                <input type="text" id="chat-input" placeholder="Écrivez votre message...">
                <button id="send-btn">Envoyer</button>
            </div>
        </div>
        <button class="launcher" id="launcher-btn">IA</button>
        `;

        // ربط الأحداث (Events)
        this.shadowRoot.querySelector('#launcher-btn').addEventListener('click', () => this.toggleChat());
        
        const sendBtn = this.shadowRoot.querySelector('#send-btn');
        const inputField = this.shadowRoot.querySelector('#chat-input');

        if (sendBtn && inputField) {
            sendBtn.addEventListener('click', () => this.sendMessage());
            inputField.addEventListener('keypress', (e) => {
                if (e.key === 'Enter') {
                    this.sendMessage();
                }
            });
        }
    }
}

if (!customElements.get('sodo-chat-widget')) {
    customElements.define('sodo-chat-widget', SodoChatWidget);
}

function initSodoWidget() {
    if (!document.querySelector('sodo-chat-widget')) {
        const widget = document.createElement('sodo-chat-widget');
        document.body.appendChild(widget);
        console.log("=== SDBO WIDGET INJECTED SUCCESSFULLY ===");
    }
}

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initSodoWidget);
} else {
    initSodoWidget();
}