import React, { useState } from 'react';

function ChatArea() {
  const [message, setMessage] = useState('');
  const [chatHistory, setChatHistory] = useState([
    { type: 'bot', text: '안녕하세요. 무엇을 도와드릴까요?' },
  ]);

  const handleSendMessage = () => {
    if (message.trim()) {
      setChatHistory([...chatHistory, { type: 'user', text: message }]);
      // Placeholder for bot response
      setTimeout(() => {
        setChatHistory(prevHistory => [...prevHistory, { type: 'bot', text: `"${message}" 요청을 받았습니다. 템플릿을 생성 중입니다.` }]);
      }, 500);
      setMessage('');
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
      <div style={{ flexGrow: 1, overflowY: 'auto', paddingBottom: '10px' }}>
        {chatHistory.map((msg, index) => (
          <div key={index} style={{
            marginBottom: '10px',
            display: 'flex', // Use flex to align the bubble
            justifyContent: msg.type === 'user' ? 'flex-end' : 'flex-start', // Align bubble to right for user, left for bot
          }}>
            <div style={{
              backgroundColor: msg.type === 'user' ? '#e0f7fa' : '#f1f0f0', // Light blue for user, light grey for bot
              color: '#333',
              padding: '10px 15px',
              borderRadius: '18px',
              maxWidth: '70%', // Limit bubble width
              wordBreak: 'break-word', // Break long words
            }}>
              {msg.text}
            </div>
          </div>
        ))}
      </div>
      <div style={{ display: 'flex', marginTop: 'auto' }}>
        <input
          type="text"
          value={message}
          onChange={(e) => setMessage(e.target.value)}
          onKeyPress={(e) => {
            if (e.key === 'Enter') {
              handleSendMessage();
            }
          }}
          placeholder="메시지 입력창"
          style={{ flexGrow: 1, padding: '8px', border: '1px solid #ccc', borderRadius: '4px' }}
        />
        <button
          onClick={handleSendMessage}
          style={{ marginLeft: '10px', padding: '8px 15px', backgroundColor: '#007bff', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
        >
          전송
        </button>
      </div>
    </div>
  );
}

export default ChatArea;
