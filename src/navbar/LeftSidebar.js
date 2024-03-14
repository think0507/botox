import React from 'react';
import {Link} from "react-router-dom";
import './LeftSidebar.css';

const LeftSidebar = () => {
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
            <li><Link to="/Board">로그아웃</Link></li>
        </ul>
    </div>
  );
}

export default LeftSidebar;