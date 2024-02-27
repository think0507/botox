import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Nav from './Nav';
import Main from './navbar/Main';
import Attack from './navbar/Attack';
import Login from './navbar/Login';
import RoomList from './components/RoomList';
import lolLogo from "./img/lol-logo.png";
import suddenLogo from "./img/sudden.webp";

function App() {
    return (
        <BrowserRouter>
            <div className="App">
                <Nav />
                <Routes>
                    <Route path="/" element={<Main logo={lolLogo} gameName="리그오브레전드" />}/>
                    <Route path="/about" element={<Attack logo={suddenLogo} gameName="서든어택" />}/>
                    <Route path="/contact" element={<Login />}/>
                    <Route path="/RoomList" element={<RoomList />}/>
                </Routes>
            </div>
        </BrowserRouter>
    );
}

export default App;
