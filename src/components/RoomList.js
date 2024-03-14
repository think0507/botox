import React, { useRef, useState, useEffect } from 'react';
import usericonURL from '../img/user-icon.png';
import './RoomList.css';
import RoomListContent from "./RoomListContent";
import { useNavigate } from 'react-router-dom';
import LeftSidebar from "../navbar/LeftSidebar";
import RightSidebar from "../navbar/RightSidebar";

const RoomList = () => {
    const [createRoomModalOpen, setCreateRoomModalOpen] = useState(false);
    const [rooms, setRooms] = useState([]); // 방 목록을 관리할 상태 추가
    const [roomInfo, setRoomInfo] = useState({
        title: '',
        capacity: '',
        password: '',
        isVoiceChat: false
    });
    const modalBackground = useRef();
    const navigate = useNavigate();

    // ======= 페이징 =======
    const [currentPage, setCurrentPage] = useState(1);
    const roomsPerPage = 5; // 페이지 당 보여줄 방의 수 변경
    const indexOfLastRoom = currentPage * roomsPerPage;
    const indexOfFirstRoom = indexOfLastRoom - roomsPerPage;
    const currentRooms = rooms.slice(indexOfFirstRoom, indexOfLastRoom); // 페이지에 맞게 방 목록 슬라이싱

    const paginate = pageNumber => setCurrentPage(pageNumber);
    //  ======= 페이징 종료 =======

    // 로컬 스토리지에서 방 목록을 가져와 초기화
    useEffect(() => {
        const savedRooms = JSON.parse(localStorage.getItem('rooms')) || [];
        setRooms(savedRooms);
    }, []);

    // 방 만들기 모달 열기
    const openCreateRoomModal = () => {
        setCreateRoomModalOpen(true);
    };

    // 방 만들기 모달 닫기
    const closeCreateRoomModal = () => {
        setCreateRoomModalOpen(false);
    };

    // 방 만들기
    const createRoom = () => {
        const roomId = Math.floor(Math.random() * 1000000); // 랜덤한 방 번호 생성
        const newRoom = { ...roomInfo, id: roomId, title: `${roomId} - ${roomInfo.title}` }; // 방 번호를 제목에 추가
        const updatedRooms = [...rooms, newRoom];
        setRooms(updatedRooms);
        localStorage.setItem('rooms', JSON.stringify(updatedRooms)); // 로컬 스토리지에 저장
        closeCreateRoomModal();
    };

    // 방 목록으로 이동
    // const goToRoomList = () => {
    //     navigate('/roomlist');
    // };

    // 방 삭제
    const deleteRoom = (roomId) => {
        const updatedRooms = rooms.filter(room => room.id !== roomId);
        setRooms(updatedRooms);
        localStorage.setItem('rooms', JSON.stringify(updatedRooms)); // 로컬 스토리지에 저장
    };

    // 방 목록에서 방을 클릭했을 때의 이벤트 핸들러
    const handleRoomClick = (roomId) => {
        navigate(`/room/${roomId}`);
    };

    return (
        <div className="RoomList">
            <LeftSidebar />
            <RightSidebar />
            <div className="roomListNav">
                {/* 유저 아이콘 */}
                {/*<img className="userIcon" alt="user icon" src={usericonURL} />*/}
                {/* 방 만들기 버튼 */}
                <div className="roomListFunctionContainer">
                    <button className="modal-open-btn" onClick={openCreateRoomModal}>
                        방 만들기 버튼
                    </button>
                </div>


                {/* 방 만들기 모달 */}
                {createRoomModalOpen && (
                    <div className="modal-container" ref={modalBackground} onClick={(e) => {
                        if (e.target === modalBackground.current) {
                            closeCreateRoomModal();
                        }
                    }}>
                        <div className="modal-content">
                            <p>방 만들기 모달창</p>
                            {/* 제목 입력 */}
                            <input type="text" placeholder="방 제목" value={roomInfo.title} onChange={(e) => setRoomInfo({ ...roomInfo, title: e.target.value })} />
                            {/* 인원 수 입력 */}
                            <input type="number" placeholder="인원 수" value={roomInfo.capacity} onChange={(e) => setRoomInfo({ ...roomInfo, capacity: e.target.value })} />
                            {/* 비밀번호 입력 */}
                            <input type="password" placeholder="비밀번호" value={roomInfo.password} onChange={(e) => setRoomInfo({ ...roomInfo, password: e.target.value })} />
                            {/* 보이스 채팅 체크박스 */}
                            <label>
                                <input type="checkbox" checked={roomInfo.isVoiceChat} onChange={(e) => setRoomInfo({ ...roomInfo, isVoiceChat: e.target.checked })} />
                                보이스 채팅
                            </label>
                            {/* 방 만들기 버튼 */}
                            <input type="button" value="방 만들기" onClick={createRoom} />
                            <button className="modal-close-btn" onClick={closeCreateRoomModal}>
                                모달 닫기
                            </button>
                        </div>
                    </div>
                )}
            </div>

            {/* 방 목록 */}
            <div className="roomListContentContainer">
                {currentRooms.map((room, index) => (
                    <div key={index} className="roomListItem">
                        <RoomListContent
                            room={room} // 방 정보를 props로 전달
                            onClick={() => handleRoomClick(room.id)}
                        />
                        <button onClick={() => deleteRoom(room.id)}>방 삭제</button>
                    </div>
                ))}
            </div>

            {/* 페이징 */}
            <div>
                {Array.from({ length: Math.ceil(rooms.length / roomsPerPage) }).map((_, index) => (
                    <button key={index} onClick={() => paginate(index + 1)}>{index + 1}</button>
                ))}
            </div>
        </div>
    );

};

export default RoomList;
