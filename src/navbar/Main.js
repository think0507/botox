// Main.js
import React, { useState, useEffect } from 'react';
import './main.css';
import './img.css';
import { Link } from 'react-router-dom'; // react-router-dom에서 Link import
import lolLogo from "../img/lol-logo.png";
import suddenLogo from "../img/sudden.webp";
import userIcon from "../img/user-icon.png";

function Main() {
    const [userCount, setUserCount] = useState(0);

    // useEffect를 사용하여 컴포넌트가 마운트될 때 사용자 수를 가져오는 비동기 작업을 수행합니다.
    useEffect(() => {
        // 실제로는 서버에서 사용자 수를 가져오는 비동기 작업을 수행해야 합니다.
        // 여기서는 임시적으로 setTimeout을 사용하여 3초마다 랜덤한 숫자로 사용자 수를 갱신합니다.
        const intervalId = setInterval(() => {
            const randomCount = Math.floor(Math.random() * 1000); // 랜덤한 숫자 생성
            setUserCount(randomCount);
        }, 3000);

        // 컴포넌트가 언마운트될 때 interval을 정리합니다.
        return () => clearInterval(intervalId);
    }, []); // 빈 배열을 전달하여 컴포넌트가 마운트될 때 한 번만 실행되도록 합니다.

    return (
        <div>
            <h1 className="logo">Welcome to Main!</h1>
            <h2 className="title">인기게임</h2>
            <div className="content">
                {/* 리그오브레전드 이미지와 제목 */}
                <div className="image-container">
                    <Link to="/lol">
                        <img className="lol_img" alt="lol_logo" src={lolLogo}/>
                        <div className="title-overlay">리그오브레전드</div>
                    </Link>
                    <div className="lol_user_count">
                        <img className="user-icon" src={userIcon} alt="user"/> : {userCount}
                    </div>
                </div>
                {/* 서든어택 이미지와 제목 */}
                <div className="image-container">
                    <Link to="/sudden">
                        <img className="sudden" alt="sudden_logo" src={suddenLogo}/>
                        <div className="title-overlay">서든어택</div>
                    </Link>
                    <div className="sudden_user_count">
                        <img className="user-icon" src={userIcon} alt="user"/> : {userCount}
                    </div>
                </div>
            </div>
        </div>
    );
}

export default Main;
