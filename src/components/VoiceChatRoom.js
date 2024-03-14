import React from 'react';
import './VoiceChatRoom.css';

function VoiceChatRoom() {
    // ********* 덤프 *********
    var roomName = "칼바람 하실분 구함"
    var peopleCount = 3
    var maxPeopleCount = 5
    // ********* 덤프 *********

    return (
        <div className="voiceChatRoomContainer">
            <div className="voiceChatRoomTopNav">
                <div className="voiceIcon">
                    <img width="64" height="64" src="https://img.icons8.com/wired/64/F25081/microphone.png"
                         alt="microphone"/>
                </div>
                <div className="roomName">
                    {roomName}
                </div>
                <div className="peopleCountContainer">
                    <div className="peopleCount"> {peopleCount}</div>
                    <div className="maxPeopleCount">/{maxPeopleCount}</div>
                </div>
            </div>
            <div className="voiceChatRoomContent">
                {/*<voiceChatRoomUserContent />*/}
            </div>
            <div>
                <div className="voiceChatRoomBottomNav">
                    <div className="centerIcons">
                        <div className="soundIcon"><img width="50" height="50"
                                                        src="https://img.icons8.com/ios/50/room-sound.png"
                                                        alt="room-sound"/></div>
                        <div className="micIcon"><img width="32" height="32"
                                                      src="https://img.icons8.com/external-kmg-design-basic-outline-kmg-design/32/external-mic-off-interface-essentials-kmg-design-basic-outline-kmg-design.png"
                                                      alt="external-mic-off-interface-essentials-kmg-design-basic-outline-kmg-design"/>
                        </div>
                        <div className="exitIcon"><img width="48" height="48"
                                                       src="https://img.icons8.com/sf-regular-filled/48/FA5252/phone.png"
                                                       alt="phone"/></div>
                    </div>


                    <div className="rightIcons">
                        <div className="reportIcon"><img width="48" height="48"
                                                         src="https://img.icons8.com/fluency/48/siren.png" alt="siren"/>
                        </div>
                        <div className="inviteIcon"><img width="64" height="64"
                                                         src="https://img.icons8.com/wired/64/invite.png" alt="invite"/>
                        </div>
                    </div>
                </div>
            </div>
            <div className="textChatContainer">
                {/*<TextChatRoom />*/}
            </div>
        </div>
    );
}

export default VoiceChatRoom;