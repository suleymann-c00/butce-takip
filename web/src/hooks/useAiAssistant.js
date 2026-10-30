import { useState, useCallback } from 'react';

const SYSTEM_PROMPT = `Sen yardımsever bir Türk finansal danışmanısın. Bugünün tarihi: {CURRENT_DATE} (YYYY-MM-DD formatında)
Kullanıcı işlem EKLEMEK veya SİLMEK isterse, SADECE markdown içinde JSON çıktısı VER (başka kelime etme). 
Aynı anda birden fazla işlem yapmak için "actions" dizisi (array) kullan:
\`\`\`json
{
  "actions": [
    {"action": "add", "type": "EXPENSE", "amount": 50, "category": "FOOD", "title": "Market", "currency": "TRY", "date": "2026-09-04T12:00:00.000Z"},
    {"action": "delete", "transactionId": "id-buraya"}
  ]
}
\`\`\`
- Tarihler bağlamda YYYY-MM-DD olarak gelir (Örn: 2026-09-06 = 6 Eylül 2026). Kullanıcı "6 Eylülü sil" derse, bağlamda 2026-09-06 tarihli işlemleri bulup silmelisin.
- Ekleme işleminde kullanıcı tarih belirtmezse bugünü yaz.
- Kategori anahtarları (JSON oluştururken) KESİNLİKLE ŞUNLAR OLMALIDIR: FOOD (Gıda), TRANSPORT (Ulaşım), RENT (Kira), SHOPPING (Alışveriş), BILLS (Fatura), SUBSCRIPTION (Abonelik), HEALTH (Sağlık), ENTERTAINMENT (Eğlence), EDUCATION (Eğitim), OTHER_EXPENSE (Diğer Gider), SALARY (Maaş), FREELANCE, INVESTMENT_INCOME, POCKET_MONEY.
- Kullanıcı bir işlemi GÜNCELLEMEK/DEĞİŞTİRMEK isterse, ona doğrudan güncelleme yeteneğin olmadığını, ancak eski işlemi silip yenisini aynı anda (actions dizisi ile) ekleyebileceğini söyle ve işlemi bu şekilde gerçekleştir.
- Para Birimi (currency) KESİNLİKLE ŞUNLARDAN BİRİ OLMALIDIR: TRY, USD, EUR, GOLD. (Eğer kullanıcı "2 gram altın" veya "2 gr altın" derse amount: 2, currency: "GOLD" olmalıdır. Dolar için USD, Euro için EUR kullan).
- ÖNEMLİ: Kullanıcıya doğal dilde yanıt verirken (özet, rapor veya sohbet sırasında) ASLA İngilizce kategori anahtarlarını (FOOD, HEALTH, POCKET_MONEY vb.) kullanma. Her zaman saf Türkçe karşılıklarını (Gıda, Sağlık, Harçlık vb.) kullan.

Eğer kullanıcı sadece bir soru soruyorsa JSON üretme, normal dilde sohbet et.`;

export function useAiAssistant() {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  const sendMessage = useCallback(async (userMessage, transactionContext, onAddTransaction, onDeleteTransaction) => {
    setIsLoading(true);
    setError(null);
    try {
      const apiKey = localStorage.getItem('gemini_api_key');
      if (!apiKey) {
        throw new Error('API Anahtarı bulunamadı. Lütfen ayarlardan ekleyin.');
      }

      const today = new Date().toISOString();
      let dynamicPrompt = SYSTEM_PROMPT.replace('{CURRENT_DATE}', today);
      dynamicPrompt = `${dynamicPrompt}\n\nKullanıcının Son 500 İşlemi (Bağlam Bilgisi):\n${transactionContext ? transactionContext : 'Henüz işlem yok.'}`;

      const makeRequest = async (modelName) => {
        const res = await fetch(
          `https://generativelanguage.googleapis.com/v1beta/models/${modelName}:generateContent?key=${apiKey.trim()}`,
          {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
            },
            body: JSON.stringify({
              system_instruction: {
                parts: [{ text: dynamicPrompt }]
              },
              contents: [
                {
                  role: 'user',
                  parts: [{ text: userMessage }]
                }
              ],
              generationConfig: {
                temperature: 0.2
              }
            })
          }
        );
        return res;
      };

      let response = await makeRequest('gemini-3.6-flash');

      // Eğer sunucu yoğunsa (503) veya geçici bir hata varsa diğer sürümlere geç
      if (response.status === 503 || response.status === 429) {
        console.warn("İlk model başarısız, gemini-3.7-flash modeline geçiliyor...");
        response = await makeRequest('gemini-3.7-flash');
      }
      
      if (response.status === 503 || response.status === 429) {
        console.warn("İkinci model başarısız, gemini-3.5-flash-lite modeline geçiliyor...");
        response = await makeRequest('gemini-3.5-flash-lite');
      }

      if (!response.ok) {
        let errText = await response.text();
        try {
           const errJson = JSON.parse(errText);
           errText = errJson.error?.message || errText;
        } catch(e) {}
        throw new Error(`(HTTP ${response.status}) ${errText}`);
      }

      const data = await response.json();
      const aiText = data.candidates?.[0]?.content?.parts?.[0]?.text || '';

      // Try to parse JSON from markdown code block (esnek regex kullanıldı)
      const jsonMatch = aiText.match(/```json\s+([\s\S]*?)\s+```/);
      if (jsonMatch && jsonMatch[1]) {
        try {
          const parsedData = JSON.parse(jsonMatch[1]);
          const actionsToProcess = Array.isArray(parsedData) ? parsedData : (parsedData.actions || [parsedData]);
          
          let addedCount = 0;
          let deletedCount = 0;

          for (const action of actionsToProcess) {
            if (action.action === 'add' && onAddTransaction) {
               await onAddTransaction({
                 type: action.type,
                 amount: action.amount,
                 category: action.category,
                 title: action.title,
                 currency: action.currency,
                 date: action.date || new Date().toISOString(),
               });
               addedCount++;
            } else if (action.action === 'delete' && onDeleteTransaction && action.transactionId) {
               await onDeleteTransaction(action.transactionId);
               deletedCount++;
            }
          }

          if (addedCount > 0 || deletedCount > 0) {
             let msg = [];
             if (addedCount > 0) msg.push(`${addedCount} adet işlem başarıyla eklendi! 🎉`);
             if (deletedCount > 0) msg.push(`${deletedCount} adet işlem başarıyla silindi! 🗑️`);
             return { text: msg.join('\n') };
          } else {
             return { text: "İstediğiniz kritere uygun bir işlem bulamadım veya herhangi bir değişiklik yapılamadı. Tarihi veya işlem adını kontrol edip tekrar deneyebilirsiniz. 🕵️‍♂️" };
          }
        } catch (e) {
          console.error("JSON Parse error:", e);
        }
      }

      return { text: aiText };

    } catch (err) {
      console.error('AI Error:', err);
      setError(err.message);
      return { text: `Bir hata oluştu: ${err.message}` };
    } finally {
      setIsLoading(false);
    }
  }, []);

  return {
    sendMessage,
    isLoading,
    error
  };
}
