import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom'; // useNavigate 훅 import

import './login.css';

function Login() {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const navigate = useNavigate(); // useNavigate 훅을 사용하여 navigate 함수 가져오기

    const handleLogin = async () => {
        try {
            // 로그인 로직
            // ...

            // 로그인 성공 시 페이지 이동
            navigate("/home");
        } catch (error) {
            console.error("Error during login:", error);
            alert("무언가 잘못됐어요");
        }
    };

    return (
        <div className="login-container">
            <div className="background-overlay"></div>
            <div className="title">
                <div className="subtitle">
                    <h3>로그인</h3>
                </div>
            </div>
            <div className="form-input">
                <div>
                    <input
                        type="email"
                        placeholder="이메일"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                    />
                </div>
                <div>
                    <input
                        type="password"
                        placeholder="비밀번호"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                    />
                </div>
                <div>
                    <button className="login-btn" onClick={handleLogin}>
                        <span>로그인 하기</span>
                    </button>
                </div>
                <button className="kakao-btn" onClick={() => alert("Kakao Login")}>
                    {/*<img src={require('public/kakao_login.png')} alt="Kakao Login"/>*/}
                </button>
            </div>
            <div className="signup-text">
                <button className="signup-btn" onClick={() => navigate("/Signup")}>
                    <span>회원가입</span>
                </button>
            </div>
        </div>
    );
}

export default Login;
