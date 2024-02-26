import {Link} from 'react-router-dom';
import React from 'react';
import './Nav.css';

function Nav() {
    return (
        <div>
            <div className="navbar">
                <Link className="navbarMenu" to={'/'}>홈</Link>
                <Link className="navbarMenu" to={'/about'}>공략</Link>
                <Link className="navbarMenu" to={'/contact'}>로그인</Link>
            </div>
        </div>
    )
}

export default Nav;