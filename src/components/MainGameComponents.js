// MainGameComponents.js
import React from 'react';
import { useParams } from 'react-router-dom';
import RoomList from "./RoomList";

function MainGameComponents() {
    // URL 파라미터를 가져옵니다.
    const { gameName } = useParams();

    // 게임 이름에 따른 정보를 설정합니다.
    const gameInfo = {
        'lol': ' ',
        'sudden': ' ',
    };

    return (
        <div>
            <h2>{gameName}</h2>
            <p>{gameInfo[gameName]}</p>
            <RoomList />
        </div>
    );
}

export default MainGameComponents;
