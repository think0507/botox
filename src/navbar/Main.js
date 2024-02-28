// Main.js
import React from 'react';
import './main.css';
import './img.css';
import { Link } from 'react-router-dom'; // react-router-dom에서 Link import
import lolLogo from "../img/lol-logo.png";
import suddenLogo from "../img/sudden.webp";

function Main() {
    return (
        <div>
            <h1 className="logo">Welcome to Main!</h1>
            <h2 className="title">인기게임</h2>
            <div className="content">
                <Link to="/lol">
                    <img className="lol_img" alt="lol_logo" src={lolLogo}/>
                </Link>
                <Link to="/sudden">
                    <img className="sudden" alt="sudden_logo" src={suddenLogo}/>
                </Link>
            </div>
            <div className="text_content">
                <p className="lol_text">리그오브레전드</p>
                <p className="sudden_text">서든어택</p>
            </div>
        </div>
    );
}

export default Main;
