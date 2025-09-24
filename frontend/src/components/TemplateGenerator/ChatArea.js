import React, { useState, useEffect, useRef } from 'react';

function ChatArea({ chatHistory, onSendMessage, loading }) {
  const [message, setMessage] = useState('');
  const chatEndRef = useRef(null);

  useEffect(() => {
    // Scroll to the bottom of the chat history when it updates
    chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [chatHistory]);

  const handleLocalSendMessage = () => {
    if (message.trim() && !loading) {
      onSendMessage(message);
      setMessage('');
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
      <div style={{ flexGrow: 1, overflowY: 'auto', padding: '10px' }}>
        {chatHistory.map((msg, index) => (
          <div key={index} style={{
            marginBottom: '12px',
            display: 'flex',
            justifyContent: msg.type === 'user' ? 'flex-end' : 'flex-start',
          }}>
            <div style={{
              backgroundColor: msg.type === 'user' ? '#007bff' : '#e9ecef',
              color: msg.type === 'user' ? 'white' : '#333',
              padding: '10px 15px',
              borderRadius: '18px',
              maxWidth: '75%',
              wordBreak: 'break-word',
            }}>
              {msg.text}
            </div>
          </div>
        ))}
        {loading && (
          <div style={{ display: 'flex', justifyContent: 'flex-start' }}>
            <div style={{ backgroundColor: '#e9ecef', color: '#333', padding: '10px 15px', borderRadius: '18px' }}>
              <i>템플릿을 생성 중입니다...</i>
            </div>
          </div>
        )}
        <div ref={chatEndRef} />
      </div>
      <div style={{ display: 'flex', marginTop: 'auto', padding: '10px' }}>
        <input
          type="text"
          value={message}
          onChange={(e) => setMessage(e.target.value)}
          onKeyPress={(e) => {
            if (e.key === 'Enter') {
              handleLocalSendMessage();
            }
          }}
          placeholder="메시지를 입력하세요..."
          disabled={loading}
          style={{ flexGrow: 1, padding: '10px', border: '1px solid #ccc', borderRadius: '20px' }}
        />
        <button
          onClick={handleLocalSendMessage}
          disabled={loading}
          style={{
            marginLeft: '10px',
            padding: '10px 20px',
            backgroundColor: '#007bff',
            color: 'white',
            border: 'none',
            borderRadius: '20px',
            cursor: loading ? 'not-allowed' : 'pointer',
          }}
        >
          전송
        </button>
      </div>
    </div>
  );
}

export default ChatArea;
