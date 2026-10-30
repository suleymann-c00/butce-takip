import React, { useState, useRef, useEffect } from 'react';
import { Bot, X, Send, User, ChevronUp } from 'lucide-react';
import { useAiAssistant } from '../hooks/useAiAssistant';
import { useTransactions } from '../hooks/useTransactions';

// Kendi hafif Markdown yorumlayıcımız (Kütüphane kullanmadan çökme riskini sıfırlıyoruz)
const formatMarkdown = (text) => {
  if (!text) return '';
  return text
    // Başlıklar (### Başlık)
    .replace(/^###\s+(.*?)$/gm, '<h3 class="text-neonGreen font-bold mt-4 mb-1 text-base">$1</h3>')
    // Kalın (Bold) yazılar (**yazı**)
    .replace(/\*\*(.*?)\*\*/g, '<strong class="text-white font-bold">$1</strong>')
    // Eğik (Italic) yazılar (*yazı*)
    .replace(/\*(.*?)\*/g, '<em class="text-gray-300 italic">$1</em>')
    // Ayırıcı çizgi (---)
    .replace(/^---$/gm, '<hr class="border-white/10 my-3" />')
    // Madde işaretli liste (* madde veya - madde)
    .replace(/^[\*-]\s+(.*?)$/gm, '<li class="ml-5 list-disc marker:text-neonGreen">$1</li>')
    // Numaralı liste (1. madde)
    .replace(/^\d+\.\s+(.*?)$/gm, (match) => {
      const content = match.replace(/^\d+\.\s+/, '');
      return `<li class="ml-5 list-decimal marker:text-neonGreen">${content}</li>`;
    });
};

export default function AiAssistantChat() {
  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState([
    { role: 'ai', text: 'Merhaba! Ben sizin finansal asistanınızım. Size nasıl yardımcı olabilirim? (Örn: "Bugün markete 500 TL harcadım")' }
  ]);
  const [inputValue, setInputValue] = useState('');
  
  const { sendMessage, isLoading } = useAiAssistant();
  const { addTransaction, deleteTransaction, transactions } = useTransactions();
  const messagesEndRef = useRef(null);

  const hasApiKey = !!localStorage.getItem('gemini_api_key');

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    if (isOpen) {
      scrollToBottom();
    }
  }, [messages, isOpen]);

  const handleSend = async (e) => {
    e.preventDefault();
    if (!inputValue.trim() || isLoading) return;

    const userText = inputValue.trim();
    setInputValue('');
    setMessages(prev => [...prev, { role: 'user', text: userText }]);

    // Son 1 yılın işlemlerini filtrele
    const oneYearAgo = Date.now() - 365 * 24 * 60 * 60 * 1000;
    const recentTransactions = (transactions || [])
      .filter(t => t.date >= oneYearAgo)
      .slice(0, 1000)
      .map(t => {
        let dateStr = '';
        try { 
          const d = new Date(t.date);
          const year = d.getFullYear();
          const month = String(d.getMonth() + 1).padStart(2, '0');
          const day = String(d.getDate()).padStart(2, '0');
          dateStr = `${year}-${month}-${day}`;
        } catch(e) {}
        return `ID:${t.id} | Tarih:${dateStr} | ${t.type === 'INCOME' ? '+' : '-'}${t.amount} ${t.currency} | Kategori:${t.category} (${t.title})`;
      }).join('\n');

    const response = await sendMessage(userText, recentTransactions, addTransaction, deleteTransaction);
    
    setMessages(prev => [...prev, { role: 'ai', text: response.text }]);
  };

  return (
    <div className="fixed bottom-24 right-4 sm:bottom-6 sm:right-24 z-50 flex flex-col items-end">
      {/* Chat Window */}
      {isOpen && (
        <div className="mb-4 w-[90vw] sm:w-[350px] h-[500px] max-h-[70vh] bg-cardDark border border-white/10 shadow-2xl rounded-2xl flex flex-col overflow-hidden animate-fade-in">
          {/* Header */}
          <div className="bg-white/5 border-b border-white/10 p-4 flex items-center justify-between">
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 rounded-full bg-neonGreen/20 flex items-center justify-center text-neonGreen">
                <Bot size={18} />
              </div>
              <span className="font-semibold text-white">Yapay Zeka Asistanı</span>
            </div>
            <button 
              onClick={() => setIsOpen(false)}
              className="text-gray-400 hover:text-white transition-colors"
            >
              <X size={20} />
            </button>
          </div>

          {/* Messages */}
          <div className="flex-1 overflow-y-auto p-4 space-y-4">
            {!hasApiKey ? (
              <div className="bg-expenseRed/10 border border-expenseRed/20 text-expenseRed text-sm p-4 rounded-xl text-center">
                Yapay zeka asistanını kullanabilmek için Ayarlar sayfasından Gemini API anahtarınızı eklemelisiniz.
              </div>
            ) : (
              messages.map((msg, idx) => (
                <div key={idx} className={`flex ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}>
                  <div 
                    className={`max-w-[85%] rounded-2xl p-3 text-sm whitespace-pre-wrap ${
                      msg.role === 'user' 
                        ? 'bg-neonGreen text-deepNight rounded-tr-sm' 
                        : 'bg-white/10 text-white/80 rounded-tl-sm'
                    }`}
                    dangerouslySetInnerHTML={{ __html: msg.role === 'ai' ? formatMarkdown(msg.text) : msg.text }}
                  />
                </div>
              ))
            )}
            {isLoading && (
              <div className="flex justify-start">
                <div className="max-w-[85%] rounded-2xl p-3 text-sm bg-white/10 text-white rounded-tl-sm flex gap-1 items-center">
                  <span className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{animationDelay: '0ms'}}></span>
                  <span className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{animationDelay: '150ms'}}></span>
                  <span className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{animationDelay: '300ms'}}></span>
                </div>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>

          {/* Input Area */}
          <form onSubmit={handleSend} className="p-3 border-t border-white/10 bg-white/5 flex gap-2">
            <input
              type="text"
              value={inputValue}
              onChange={(e) => setInputValue(e.target.value)}
              placeholder="Bir mesaj yazın..."
              disabled={!hasApiKey || isLoading}
              className="flex-1 bg-deepNight border border-white/10 rounded-xl px-4 py-2 text-sm text-white placeholder-gray-500 focus:outline-none focus:border-neonGreen/50"
            />
            <button
              type="submit"
              disabled={!hasApiKey || isLoading || !inputValue.trim()}
              className="w-10 h-10 rounded-xl bg-neonGreen text-deepNight flex items-center justify-center disabled:opacity-50 disabled:cursor-not-allowed transition-all"
            >
              <Send size={16} />
            </button>
          </form>
        </div>
      )}

      {/* Floating Button */}
      {!isOpen && (
        <button
          onClick={() => setIsOpen(true)}
          className="w-14 h-14 rounded-full bg-neonGreen hover:bg-green-400 text-deepNight flex items-center justify-center shadow-lg shadow-neonGreen/20 transition-all hover:scale-105"
        >
          <Bot size={24} />
        </button>
      )}
    </div>
  );
}
