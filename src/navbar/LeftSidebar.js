import React from 'react';
import { Link } from "react-router-dom";
import './LeftSidebar.css';

const LeftSidebar = () => {
    const handleLogout = () => {
        // 여기에 로그아웃 로직을 추가하세요 (예: 세션 삭제, 상태 변경 등)
        alert("로그아웃 되었습니다.");
    };

    return (
        <div className='LeftSidebar'>
            <ul>
                <li>
                    <img src="https://img.icons8.com/ios/50/user--v1.png" alt="user--v1"/>
                    <span>???님</span>
                </li>
            </ul>
            <ul>
                <li><Link to="/">홈</Link></li>
                <li><Link to="/Board">게시판</Link></li>
                <li><Link to="/Login" onClick={handleLogout}>로그아웃</Link></li> {/* 로그아웃 버튼에 onClick 이벤트 핸들러 추가 */}
            </ul>
        </div>
    );
}

export default LeftSidebar;
