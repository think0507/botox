import {Link} from 'react-router-dom';
import React from 'react';
import './Nav.css';

function Nav() {
    return (
            <div className="navbar">
                <Link className="navbarMenu" to={'/'}>홈</Link>
                <Link className="navbarMenu" to={'/Board'}>게시판</Link>
                <Link className="navbarMenu" to={'/Login'}>로그인</Link>
            </div>
    )
}

export default Nav;