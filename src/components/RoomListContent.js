import React from 'react';
import { useNavigate } from 'react-router-dom';
import './RoomListContent.css'
import lollLogo from '../img/lol-logo.png';
import lockedIcon from '../img/icons8-locked-48.png';
import unlockedIcon from '../img/icons8-unlocked-48.png';

const RoomListContent = ({ room }) => {
    let { roomNum, roomName, peopleCount, degreeAvg, isLocked, isVoiceChat } = room;
    const navigate = useNavigate();

    // ****************덤프******************
    roomNum = 100240;
    roomName = '같이 5인큐 하실분~~!';
    peopleCount = 3;
    degreeAvg = 4.5;
    isLocked = false;
    isVoiceChat = true;
    // ------------------------------------
    let gameName = "League of legends";
    let maxPeopleCount = 5;
    let bangjang = "삼육대차은우";
    // ****************덤프 끝******************

    const lockedCheck = () => {
        if (isLocked) {
            return lockedIcon;
        } else {
            return unlockedIcon;
        }
    }

    const handleClick = () => {
        if (isVoiceChat) {
            navigate('/VoiceChatRoom');
        } else {
            navigate('/TextChatRoom');
        }
    };

    return (
        <div className="roomListContent" onClick={handleClick}>
            <div>
                <img className="roomListContentGameImage" src={lollLogo} alt="gameImgae"/>
            </div>
            <div>
                <div className="topOfContent">
                    <div className="gameName">{gameName}</div>
                    <div className="peopleCountContainer">
                        <div className="peopleCount">{peopleCount}/</div>
                        <div className="maxPeopleCount">{maxPeopleCount}</div>
                    </div>
                    <img className="lockImage" src={lockedCheck()} alt="lock status"></img>
                </div>

                <div className="roomNum">No.{roomNum}</div>
                <div className="roomName">{roomName}</div>
                <div className="bangjangContainer">
                    <div className="bangjangGrade"><img width="48" height="48"
                                                        src="https://img.icons8.com/emoji/48/hatching-chick--v2.png"
                                                        alt="hatching-chick--v2"/></div>
                    <div className="bangjang">{bangjang}</div>
                </div>
            </div>
        </div>
    );
};

export default RoomListContent;
