import React from 'react';
import './main.css';
import './img.css';
import lolLogo from "../img/lol-logo.png";
import suddenLogo from "../img/sudden.webp";
function Main() {
    return (
        <div>
            <h1 className="logo">Welcome to Main!</h1>
            <h2 className="title">인기게임</h2>
            <div className="content">
                <img className="lol_img" alt="lol_logo" src={lolLogo}/>
                <img className="sudden" alt="sudden_logo" src={suddenLogo}/>
            </div>
        </div>
    )
}

export default Main;