import React, {useRef, useState} from 'react';
import {useLocation} from 'react-router-dom';
import usericonURL from '../img/user-icon.png'
import './RoomList.css'
function RoomList() {
    const {state} = useLocation(); //useLocation()훅으로 state를 받음
    console.log(state)
    console.log('this')

    // 모달창 사용을 위한 변수 선언
    const [creteRoomModalOpen, creteRoomSetModalOpen] = useState(false);
    const [searchRoomModalOpen, searchRoomSetModalOpen] = useState(false);
    const modalBackground = useRef();

    return (
        <div>
            <div className={'roomListNav'}>
                {/*유저 아이콘*/}
                <img className={'userIcon'} alt={"이미지 없음"} src={usericonURL}></img>

                {/*    방만들기 모달창 */}
                <modal>
                    <div className={'btn-wrapper'}>
                        <button className={'modal-open-btn'} onClick={() => creteRoomSetModalOpen(true)}>
                            방 만들기 버튼
                        </button>
                    </div>
                    {
                        creteRoomModalOpen &&
                        <div className={'modal-container'} ref={modalBackground} onClick={e => {
                            if (e.target === modalBackground.current) {
                                creteRoomSetModalOpen(false);
                            }
                        }}>
                            <div className={'modal-content'}>
                                <p>방 만들기 모달창</p>

                                {/* 방 번호 검색, 방 이름검색, or  텍스트,음성 검색필터 들어갈수있는방 검색필터*/}
                                <input type={"text"} value={"방 이름"}></input>

                                <input type={"button"} value={'방 만들기'}></input>

                                <button className={'modal-close-btn'} onClick={() => creteRoomSetModalOpen(false)}>
                                    모달 닫기
                                </button>
                            </div>
                        </div>
                    }
                </modal>


                <div className={'checkBoxContainer'}>
                    {/*    검색필터 체크박스 */}
                    <p>들어갈수있는방만 보기</p>
                    <input className={'checkBox'} type={"checkbox"} value={"체크박스1"}></input>
                    <p>보이스 방만 보기</p>
                    <input className={'checkBox'} type={"checkbox"}></input>
                </div>



                {/*    검색창 모달창 */}
                <modal>
                    <div className={'btn-wrapper'}>
                        <button className={'modal-open-btn'} onClick={() => searchRoomSetModalOpen(true)}>
                            방 검색 모달 버튼
                        </button>
                    </div>
                    {
                        searchRoomModalOpen &&
                        <div className={'modal-container'} ref={modalBackground} onClick={e => {
                            if (e.target === modalBackground.current) {
                                searchRoomSetModalOpen(false);
                            }
                        }}>
                            <div className={'modal-content'}>
                                <p>방 검색 모달창</p>

                                {/* 방 번호 검색, 방 이름검색, or  텍스트,음성 검색필터 들어갈수있는방 검색필터*/}
                                <input type={"text"} value={"방번호 검색창"}></input>

                                <input type={"button"} value={'제출버튼'}></input>

                                <button className={'modal-close-btn'} onClick={() => searchRoomSetModalOpen(false)}>
                                    모달 닫기
                                </button>
                            </div>
                        </div>
                    }
                </modal>
            </div>



            {/*    방 목록 리스트 */}
            <div className={'roomListContentContainer'}>
                <div className={'roomListContent'}>
                    <div className={'content'}>
                        {/*    방번호*/}
                        roomNum
                    </div>
                    <div className={'content'}>
                        {/*    방 제목 */}
                        roomName
                    </div>
                    <div className={'content'}>
                        {/*    현재 인원수*/}
                        peoplecount
                    </div>
                    <div className={'content'}>
                        {/*    평균온도 */}
                        degreeAvg
                    </div>
                    <div className={'content'}>
                        {/*    잠금 여부 */}
                        islocked
                    </div>
                </div>
            </div>
        </div>
    )
}

export default RoomList;