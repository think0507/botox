import React from 'react';
import './attack.css'; // 스타일 파일 import
import Search from '../img/search.png';

const Attack = () => {
    const examplePosts = [
        { id: 1, title: "보톡스 맞으실래요?" },
        { id: 2, title: "싫어요" },
        { id: 3, title: "맞으세요" }
    ];

    return (
        <div>
            <h1 id="attack_title">공략 게시판</h1>
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
            <ul className="posts">
                {examplePosts.map((post, index) => (
                    <li key={post.id} className="post">
                        <span className="post-number">{index + 1}</span>
                        <h4>{post.title}</h4>
                    </li>
                ))}
            </ul>
        </div>
    );
};

export default Attack;
