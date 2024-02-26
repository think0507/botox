import React from 'react';
import './main.css';
import './img.css';
import MainGameComponents from "../components/MainGameComponents";
import roomList from "../components/RoomList";

function Main() {
    return (
        <div>
            <h1 className="logo">Welcome to Main!</h1>
            <h2 className="title">인기게임</h2>

            <MainGameComponents gameName={{ id: roomList, state: "LeagueOfLegend" , imgURL:"lol-logo.png"}} />

        </div>
    )
}

export default Main;