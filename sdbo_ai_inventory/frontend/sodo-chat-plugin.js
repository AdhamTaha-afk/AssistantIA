class SodoChatWidget extends HTMLElement {
    constructor() {
        super();
        this.attachShadow({ mode: 'open' });
        this.isOpen = false;
        this.odooVersion = 'v19';
        this.messages = [
            { 
                role: 'bot', 
                text: "Bonjour ! Je suis l'assistant IA Odoo 16 & 19. Je peux vous assister sur les Ventes & CA, les Stocks, les Projets, les Achats et le Monitoring. Comment puis-je vous aider ?", 
                time: this.getTime() 
            }
        ];
        this.isTyping = false;
    }

    connectedCallback() {
        this.serverUrl = this.getAttribute('server-url') || 'http://localhost:8081';
        this.botName = this.getAttribute('bot-name') || 'Assistant Odoo';
        this.odooVersion = this.getAttribute('odoo-version') || 'v19';
        this.render();
    }

    getTime() {
        return new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    }

    toggleChat() {
        this.isOpen = !this.isOpen;
        this.render();
    }

    setOdooVersion(ver) {
        this.odooVersion = ver;
        this.messages.push({
            role: 'bot',
            text: `Bascule vers **Odoo ${ver.toUpperCase()}** effectuée.`,
            time: this.getTime()
        });
        this.render();
        this.scrollToBottom();
    }

    async sendMessage(customText = null) {
        const input = this.shadowRoot.querySelector('#chat-input');
        const text = (customText || (input ? input.value : '')).trim();
        if (!text || this.isTyping) return;

        if (input) input.value = '';
        this.messages.push({ role: 'user', text, time: this.getTime() });
        this.isTyping = true;
        this.render();
        this.scrollToBottom();

        try {
            const response = await fetch(`${this.serverUrl}/api/ai/chat`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json; charset=utf-8' },
                body: JSON.stringify({ 
                    message: text,
                    odooVersion: this.odooVersion 
                })
            });

            if (!response.ok) throw new Error('Erreur serveur');
            
            const data = await response.json();
            this.messages.push({ 
                role: 'bot', 
                text: data.answer, 
                redirect: data.redirectModule,
                time: this.getTime() 
            });
        } catch (error) {
            this.messages.push({ role: 'bot', text: "Oups, je n'arrive pas à joindre le serveur.", time: this.getTime() });
        } finally {
            this.isTyping = false;
            this.render();
            this.scrollToBottom();
        }
    }

    scrollToBottom() {
        setTimeout(() => {
            const container = this.shadowRoot.querySelector('#messages');
            if (container) container.scrollTop = container.scrollHeight;
        }, 50);
    }

    formatText(text) {
        if (!text) return '';
        return text
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
            .replace(/\n/g, '<br/>');
    }

    render() {
        this.shadowRoot.innerHTML = `
        <style>
            :host { --primary: #1877f2; --bg: #f0f2f5; }
            .widget-container { position: fixed; bottom: 20px; right: 20px; font-family: 'Segoe UI', sans-serif; z-index: 9999; }
            .launcher { width: 56px; height: 56px; border-radius: 50%; background: var(--primary); color: white; border: none; cursor: pointer; display: flex; align-items: center; justify-content: center; font-weight: bold; font-size: 20px; box-shadow: 0 4px 12px rgba(0,0,0,0.2); transition: transform 0.2s; }
            .launcher:hover { transform: scale(1.05); }
            
            .window { position: absolute; bottom: 70px; right: 0; width: 380px; height: 540px; background: white; border-radius: 12px; display: ${this.isOpen ? 'flex' : 'none'}; flex-direction: column; overflow: hidden; box-shadow: 0 8px 24px rgba(0,0,0,0.15); border: 1px solid #ddd; }
            .header { background: var(--primary); color: white; padding: 12px 16px; display: flex; align-items: center; justify-content: space-between; }
            .header-left { display: flex; align-items: center; gap: 10px; }
            .header img { width: 32px; height: 32px; border-radius: 50%; background: white; }
            .header h3 { margin: 0; font-size: 15px; }
            .version-pill { font-size: 10px; background: rgba(255,255,255,0.25); padding: 2px 6px; border-radius: 10px; }
            
            .chips-bar { display: flex; gap: 6px; padding: 8px 12px; background: #f8f9fa; border-bottom: 1px solid #eee; overflow-x: auto; }
            .chip-btn { background: white; border: 1px solid #ddd; border-radius: 12px; padding: 4px 8px; font-size: 11px; cursor: pointer; white-space: nowrap; font-weight: 600; }
            .chip-btn:hover { background: var(--primary); color: white; border-color: var(--primary); }

            .messages { flex: 1; padding: 14px; overflow-y: auto; background: #f9f9f9; display: flex; flex-direction: column; gap: 10px; }
            .msg { max-width: 85%; padding: 10px 14px; border-radius: 14px; font-size: 13px; line-height: 1.45; position: relative; }
            .msg.user { align-self: flex-end; background: var(--primary); color: white; border-bottom-right-radius: 2px; }
            .msg.bot { align-self: flex-start; background: #ffffff; color: #1c1e21; border: 1px solid #e4e6eb; border-bottom-left-radius: 2px; }
            .time { font-size: 9px; color: #888; margin-top: 4px; display: block; }
            
            .input-area { padding: 10px; border-top: 1px solid #eee; display: flex; gap: 8px; background: white; }
            input { flex: 1; border: none; background: #f0f2f5; padding: 8px 12px; border-radius: 20px; outline: none; font-size: 13px; }
            button.send { background: none; border: none; color: var(--primary); cursor: pointer; font-weight: bold; }

            .typing { display: flex; gap: 4px; padding: 6px; }
            .dot { width: 6px; height: 6px; background: #90949c; border-radius: 50%; animation: bounce 1.3s infinite; }
            @keyframes bounce { 0%, 60%, 100% { transform: translateY(0); } 30% { transform: translateY(-4px); } }
            
            .redirect-btn { margin-top: 8px; display: block; padding: 6px; background: white; border: 1px solid var(--primary); color: var(--primary); border-radius: 4px; text-decoration: none; font-size: 11px; text-align: center; font-weight: 600; }
        </style>
        
        <div class="widget-container">
            <div class="window">
                <div class="header">
                    <div class="header-left">
                        <img src="https://api.dicebear.com/7.x/bottts/svg?seed=SODO" />
                        <div>
                            <h3>${this.botName}</h3>
                        </div>
                    </div>
                    <span class="version-pill">Odoo ${this.odooVersion.toUpperCase()}</span>
                </div>

                <div class="chips-bar">
                    <button class="chip-btn" id="chip-sales">📊 Ventes & CA</button>
                    <button class="chip-btn" id="chip-stock">📦 Stocks</button>
                    <button class="chip-btn" id="chip-projects">📁 Projets</button>
                    <button class="chip-btn" id="chip-monitoring">📈 Monitoring</button>
                </div>

                <div class="messages" id="messages">
                    ${this.messages.map(m => `
                        <div class="msg ${m.role}">
                            <div>${this.formatText(m.text)}</div>
                            ${m.redirect ? `<a href="${m.redirect}" target="_blank" class="redirect-btn">Ouvrir le module (${m.redirect})</a>` : ''}
                            <span class="time">${m.time}</span>
                        </div>
                    `).join('')}
                    ${this.isTyping ? '<div class="typing"><div class="dot"></div><div class="dot"></div><div class="dot"></div></div>' : ''}
                </div>
                <div class="input-area">
                    <input type="text" id="chat-input" placeholder="Posez une question sur Odoo..." />
                    <button class="send" id="send-btn">Envoyer</button>
                </div>
            </div>
            <button class="launcher" id="launcher">IA</button>
        </div>
        `;

        this.shadowRoot.querySelector('#launcher').addEventListener('click', () => this.toggleChat());
        this.shadowRoot.querySelector('#send-btn').addEventListener('click', () => this.sendMessage());
        this.shadowRoot.querySelector('#chat-input').addEventListener('keypress', (e) => {
            if (e.key === 'Enter') this.sendMessage();
        });

        const chipSales = this.shadowRoot.querySelector('#chip-sales');
        if (chipSales) chipSales.addEventListener('click', () => this.sendMessage('Donne-moi le résumé des ventes et le CA'));

        const chipStock = this.shadowRoot.querySelector('#chip-stock');
        if (chipStock) chipStock.addEventListener('click', () => this.sendMessage('Quel est l\'état du stock et les alertes ?'));

        const chipProjects = this.shadowRoot.querySelector('#chip-projects');
        if (chipProjects) chipProjects.addEventListener('click', () => this.sendMessage('Montre-moi l\'avancement des projets et des tâches'));

        const chipMonitoring = this.shadowRoot.querySelector('#chip-monitoring');
        if (chipMonitoring) chipMonitoring.addEventListener('click', () => this.sendMessage('Donne-moi le dashboard de monitoring global 360°'));
    }
}

customElements.define('sodo-chat-widget', SodoChatWidget);
