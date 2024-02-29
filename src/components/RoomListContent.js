import React, {useState} from 'react'
import {Link} from "react-router-dom";
import {useNavigate} from 'react-router-dom';

function RoomListContent() {
    const navigate = useNavigate();




    const roomListContentDoubleClick = () => {
        // 방입장 가능한지 체크
        if (!'풀방일경우') {
            alert('풀방입니다!');
        } else if (!'암호방일경우') {
            alert('암호방입니다!');
        } else {
            const navigateToVoiceChatRoom = "/VoiceChatRoom";
            // 페이지 이동
            navigate(navigateToVoiceChatRoom);
        }
    }

    return (
        <div className={'roomListContent'} onDoubleClick={() => roomListContentDoubleClick()}>
            <div className={'roomContent roomNum'}>
                {/*    방번호*/}
                roomNum
            </div>
            <div className={'roomContent'}>
                {/*    방 제목 */}
                roomName
            </div>
            <div className={'roomContent'}>
                {/*    현재 인원수*/}
                peoplecount
            </div>
            <div className={'roomContent'}>
                {/*    평균온도 */}
                degreeAvg
            </div>
            <div className={'roomContent'}>
                {/*    잠금 여부 */}
                islocked
            </div>
        </div>
    )
}

export default RoomListContent