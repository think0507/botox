import React, { useState } from 'react';
import './board.css'; // 스타일 파일 import
import Search from '../img/search.png';
import LeftSidebar from "../navbar/LeftSidebar";
import RightSidebar from "../navbar/RightSidebar";
import randomimg from "../img/randomimg.jpeg";
import Egg from "../img/egg.png";
import Polygon from "../img/Polygon 1.png";
import Pagination from './Pagination'; // Pagination 컴포넌트 가져오기

const Board = () => {
    // Pagination에 필요한 state 설정
    const [currentPage, setCurrentPage] = useState(1); // 현재 페이지 번호
    const totalItems = [
        { id: 1, title: "쵸비 VS 에디 후기 ㄷㄷㄷㄷㄷㄷ .JPG", author: "동욱", time: "2시간 전", image: randomimg }
        // 추가적인 게시글을 필요한 만큼 추가할 수 있습니다
    ];

    // Pagination 컴포넌트에서 사용할 함수
    const handlePageChange = (pageNumber) => {
        setCurrentPage(pageNumber);
    };

    return (
        <div className="Main">
            <div className="container">
                <LeftSidebar/>
                <div className="main-content">
                    <h1 id="board_title">자유 게시판</h1>
                    <div className="post-create-container">
                        <button className="post-create-button">게시글 작성</button>
                    </div>
                    <div className="search-container">
                        <div className="search-wrapper">
                            <input
                                type="text"
                                placeholder="검색어를 입력하세요"
                                className="search-input"
                            />
                            <img src={Search} alt="Search" className="search-icon"/>
                        </div>
                    </div>
                </div>
                <div className="posts-container"> {/* 포스트들을 감싸는 컨테이너 */}
                    <div className="post-plus">
                        <img className="poly" src={Polygon} alt="polygon"/>
                        <h3 className="plus">81</h3>
                    </div>
                    <div className="post-text">
                        <h2 className="post-title">쵸비 VS 에디 후기 ㄷㄷㄷㄷㄷㄷ .JPG</h2>
                        <h3 className="post-name"><img src={Egg} alt="Egg"/>동욱 2시간전</h3>
                    </div>
                    <img className="post-img" src={randomimg} alt="random"/>
                </div>
                <Pagination
                    currentPage={currentPage}
                    setCurrentPage={setCurrentPage}
                    totalItems={totalItems}
                />
            </div>
            <RightSidebar/>
        </div>
    );
};

export default Board;
