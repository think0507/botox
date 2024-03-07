import React, { useEffect, useRef, useState } from 'react';
import './TextChatRoom.css';

const Chat = (function () {
    const myName = "blue";

    function init() {
        document.addEventListener('keydown', function (e) {
            if (e.keyCode === 13 && !e.shiftKey) {
                e.preventDefault();
                const message = document.querySelector('div.input-div textarea').value;
                sendMessage(message);
                focusTextarea();
            }
        });
    }

    function createMessageTag(LR_className, senderName, message) {
        let chatLi = document.querySelector('div.chat.format ul li').cloneNode(true);
        chatLi.classList.add(LR_className);
        chatLi.querySelector('.sender span').textContent = senderName;
        chatLi.querySelector('.message span').textContent = message;
        return chatLi;
    }

    function appendMessageTag(LR_className, senderName, message) {
        const chatLi = createMessageTag(LR_className, senderName, message);
        document.querySelector('div.chat:not(.format) ul').appendChild(chatLi);
        document.querySelector('div.chat').scrollTop = document.querySelector('div.chat').scrollHeight;
    }

    function sendMessage(message) {
        if (message.trim() !== "") { // 빈 문자열이 아닌 경우에만 메시지를 전송
            const data = {
                "senderName": "은섭",
                "message": message
            };
            appendMessageTag("right", data.senderName, data.message);
            clearTextarea(); // 메시지를 보낸 후에 입력 칸 비우기
        }
    }


    function clearTextarea() {
        document.querySelector('div.input-div textarea').value = '';
    }

    function focusTextarea() {
        document.querySelector('div.input-div textarea').focus();
    }


    return {
        init: init,
        sendMessage : sendMessage
    };
})();

const VoiceChatRoom = () => {
    const [messages, setMessages] = useState([]);
    const chatContainerRef = useRef(null);
    const textareaRef = useRef(null);

    useEffect(() => {
        Chat.init();
    }, []);

    useEffect(() => {
        chatContainerRef.current.scrollTop = chatContainerRef.current.scrollHeight;
    }, [messages]);

    const handleSendMessage = () => {
        const message = textareaRef.current.value;
        if (message.trim() !== "") {
            Chat.sendMessage(message);
            textareaRef.current.value = ""; // 입력칸 클리어
        }
    };

    return (
        <div className="chat_wrap">
            <div className="header">CHAT</div>
            <div className="chat" ref={chatContainerRef}>
                <ul>
                    {messages.map((msg, index) => (
                        <li key={index} className={msg.isMyMessage ? "right" : "left"}>
                            <div className="sender"><span>{msg.senderName}</span></div>
                            <div className="message"><span>{msg.message}</span></div>
                        </li>
                    ))}
                </ul>
            </div>
            <div className="input-div">
                <textarea ref={textareaRef} placeholder="채팅을 입력해주세요."></textarea>
                <button onClick={handleSendMessage}>전송</button>
            </div>
            <div className="chat format">
                <ul>
                    <li>
                        <div className="sender">
                            <span></span>
                        </div>
                        <div className="message">
                            <span></span>
                        </div>
                    </li>
                </ul>
            </div>
        </div>
    );
}

export default VoiceChatRoom;
