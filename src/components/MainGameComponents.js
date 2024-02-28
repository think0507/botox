// MainGameComponents.js
import React from 'react';
import { useParams } from 'react-router-dom';
import RoomList from "./RoomList";

function MainGameComponents() {
    // URL 파라미터를 가져옵니다.
    const { gameName } = useParams();

    // 게임 이름에 따른 정보를 설정합니다.
    const gameInfo = {
        'lol': '리그오브레전드에 대한 정보',
        'sudden': '서든어택에 대한 정보',
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
