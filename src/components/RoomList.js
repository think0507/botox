import React, {useRef, useState} from 'react';
import usericonURL from '../img/user-icon.png'
import './RoomList.css'
import RoomListContent from "./RoomListContent";
function RoomList() {

    // 모달창 사용을 위한 변수 선언
    const [createRoomModalOpen, createRoomSetModalOpen] = useState(false);
    const [searchRoomModalOpen, searchRoomSetModalOpen] = useState(false);

    const modalBackground = useRef();

    // ======= 페이징 =======
    const numberOfRooms = 15; // 예시로 15개의 방을 생성
    // 현재 페이지
    const [currentPage, setCurrentPage] = useState(1);
    // 페이지 당 보여줄 방의 수
    const roomsPerPage = 10;
    // 현재 페이지의 방 목록 계산
    const indexOfLastRoom = currentPage * roomsPerPage;
    const indexOfFirstRoom = indexOfLastRoom - roomsPerPage;
    const currentRooms = Array.from({ length: numberOfRooms }).slice(indexOfFirstRoom, indexOfLastRoom);
    // 페이지 변경 함수
    const paginate = pageNumber => setCurrentPage(pageNumber);
    //  ======= 페이징 종료 =======

    return (
        <div>
            <div className={'roomListNav'}>
                {/*유저 아이콘*/}
                <img className={'userIcon'} alt={"이미지 없음"} src={usericonURL}></img>

                {/*    방만들기 모달창 */}
                <modal>
                    <div className={'btn-wrapper'}>
                        <button className={'modal-open-btn'} onClick={() => createRoomSetModalOpen(true)}>
                            방 만들기 버튼
                        </button>
                    </div>
                    {
                        createRoomModalOpen &&
                        <div className={'modal-container'} ref={modalBackground} onClick={e => {
                            if (e.target === modalBackground.current) {
                                createRoomSetModalOpen(false);
                            }
                        }}>
                            <div className={'modal-content'}>
                                <p>방 만들기 모달창</p>

                                {/* 방 번호 검색, 방 이름검색, or  텍스트,음성 검색필터 들어갈수있는방 검색필터*/}
                                <input type={"text"} value={"방 이름"}></input>

                                <p>암호사용</p>
                                <input type={"checkbox"} value={"암호 사용"}></input>
                                <input type={"password"} value={"비밀번호"}></input>

                                <input type={"button"} value={'방 만들기'}></input>

                                <button className={'modal-close-btn'} onClick={() => createRoomSetModalOpen(false)}>
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

            {currentRooms.map((room, index) => (
                <RoomListContent
                    key={index}
                    roomNum={index + 1} // 예시로 방 번호를 index + 1로 지정
                    // 나머지 roomName, peoplecount 등의 속성은 서버에서 받아오는 데이터에 따라 적절히 처리
                />
            ))}

            {/*/!*    방 목록 리스트 *!/*/}
            {/*<div className={'roomListContentContainer'}>*/}
            {/*    <RoomListContent />*/}
            {/*</div>*/}

            {/* 페이징 */}
            <div>
                {Array.from({length: Math.ceil(numberOfRooms / roomsPerPage)}).map((_, index) => (
                    <button key={index} onClick={() => paginate(index + 1)}>{index + 1}</button>
                ))}
            </div>

        </div>
    )
}

export default RoomList;