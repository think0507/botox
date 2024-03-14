// Signup.js

import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './signup.css'; // CSS 파일 import

function Signup() {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [nickname, setNickname] = useState('');
    const [isRegistered, setIsRegistered] = useState(false);
    const navigate = useNavigate();

    const handleSignup = () => {
        // 회원가입 로직 구현
        // 이메일, 비밀번호, 비밀번호 확인, 닉네임 값 활용
        // 여기서는 간단히 회원가입 완료 후 상태를 변경하여 회원가입이 성공했음을 표시합니다.
        // 실제로는 서버로 회원가입 정보를 전송하고 처리해야 합니다.
        console.log('Email:', email);
        console.log('Password:', password);
        console.log('Confirm Password:', confirmPassword);
        console.log('Nickname:', nickname);
        setIsRegistered(true); // 회원가입 완료 상태를 true로 변경
        alert('회원가입이 완료되었습니다.');
        navigate('/login');
    };

    return (
        <div className="login-container">
            <div className="title">
                <div className="subtitle">
                    <h3>회원가입</h3>
                </div>
            </div>
            <div className="form-input">
                <div>
                    <input
                        type="email"
                        placeholder="아이디"
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
                    <input
                        type="password"
                        placeholder="비밀번호 확인"
                        value={confirmPassword}
                        onChange={(e) => setConfirmPassword(e.target.value)}
                    />
                </div>
                <div>
                    <input
                        type="text"
                        placeholder="닉네임"
                        value={nickname}
                        onChange={(e) => setNickname(e.target.value)}
                    />
                </div>
                <div>
                    <button className="login-btn" onClick={handleSignup}>
                        <span>회원가입 완료</span>
                    </button>
                </div>
            </div>
        </div>
    );
}

export default Signup;
