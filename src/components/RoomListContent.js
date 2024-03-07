import React from 'react';
import { useNavigate } from 'react-router-dom';
const RoomListContent = ({ room }) => {
    const { roomNum, roomName, peopleCount, degreeAvg, isLocked, isVoiceChat } = room;
    const navigate = useNavigate();

    const handleClick = () => {
        if (isVoiceChat) {
            navigate('/VoiceChatRoom');
        } else {
            navigate('/TextChatRoom');
        }
    };

    return (
        <div className="roomListContent" onClick={handleClick}>
            <div className="roomContent roomNum">{roomNum}</div>
            <div className="roomContent">{roomName}</div>
            <div className="roomContent">{peopleCount}</div>
            <div className="roomContent">{degreeAvg}</div>
            <div className="roomContent">{isLocked ? '잠김' : '열림'}</div>
        </div>
    );
};

export default RoomListContent;
