import React, { useEffect, useRef, useState } from 'react';
import './TextChatRoom.css';
import Message from '../img/message.png';
import egg from '../img/egg.png';

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
            simulateResponse(); // 상대방의 응답 시뮬레이션
        }
    }

    function clearTextarea() {
        document.querySelector('div.input-div textarea').value = '';
    }

    function focusTextarea() {
        document.querySelector('div.input-div textarea').focus();
    }

    function getRandomResponse() {
        const responses = [
            "네, 알겠습니다.",
            "고마워요!",
            "그렇군요!",
            "무슨 말인지 잘 몰라요.",
            "저도 그렇게 생각해요.",
            "정말로요?",
            "그래요?",
            "네, 알겠어요.",
            "와우!",
            "멋져요!"
        ];

        const randomIndex = Math.floor(Math.random() * responses.length);
        return responses[randomIndex];
    }

    function simulateResponse() {
        const senderName = "모르는 사람"; // 대답할 상대방의 이름
        const message = getRandomResponse();
        appendMessageTag("left", senderName, message); // left는 상대방, right는 본인의 메시지를 의미하는 클래스명입니다.
    }

    return {
        init: init,
        sendMessage : sendMessage
    };
})();

const TextChatRoom = () => {
    const [messages, setMessages] = useState([]);
    const chatContainerRef = useRef(null);
    const textareaRef = useRef(null);

    useEffect(() => {
        Chat.init();
    }, []);

    useEffect(() => {
        const chatContainer = chatContainerRef.current;
        chatContainer.scrollTop = chatContainer.scrollHeight;
        if (chatContainer.scrollHeight > chatContainer.clientHeight) {
            chatContainer.style.overflowY = 'scroll';
        } else {
            chatContainer.style.overflowY = 'hidden';
        }
    }, [messages]);

    const handleSendMessage = () => {
        const message = textareaRef.current.value;
        if (message.trim() !== "") {
            Chat.sendMessage(message);
            textareaRef.current.value = ""; // 입력칸 클리어
        }
    };

    const handleExit = () => {
        // 나가기 버튼 클릭 시 동작할 코드 작성
    };

    const handleReport = () => {
        // 신고하기 버튼 클릭 시 동작할 코드 작성
    };

    return (
        <div className="chat_wrap">
            <img className="message_img" src={Message} alt="message"/>
            <div className="header">협곡 인원 구합니다. (3/5)</div>
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
            <div className="user-container">
                <h3 className="user-title">참가중인 유저</h3>
                <div className="user-list">
                    <p><img className="user-icon" src={egg} alt="egg"/>user1</p>
                    <p><img className="user-icon" src={egg} alt="egg"/>user2</p>
                </div>
            </div>
            <div className="chat-control">
                <div className="input-div">
                    <textarea ref={textareaRef} placeholder="채팅을 입력해주세요."></textarea>
                    <button onClick={handleSendMessage}>전송</button>
                </div>
                <div className="button-container">
                    <button className="exit-button" onClick={handleExit}>나가기</button>
                    <button className="report-button" onClick={handleReport}>신고하기</button>
                </div>
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

export default TextChatRoom;
