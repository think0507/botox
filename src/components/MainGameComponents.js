// MainGameComponents.js

import React from 'react';

function MainGameComponents({ match }) {
    const { game } = match.params; // URL에서 게임 이름을 가져옴

    let gameContent;
    if (game === 'lol') {
        gameContent = <h2>리그오브레전드 컴포넌트</h2>;
    } else if (game === 'sudden') {
        gameContent = <h2>서든어택 컴포넌트</h2>;
    } else {
        gameContent = <h2>게임을 선택하세요.</h2>;
    }

    return (
        <div>
            {gameContent}
        </div>
    );
}

export default MainGameComponents;
