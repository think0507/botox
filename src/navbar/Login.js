import React, { useState } from 'react';
import './login.css';

function Login({ history }) {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');

    const handleLogin = async () => {
        try {
            // 로그인 로직
            // ...

            // 로그인 성공 시 리다이렉트
            history.push("/home");
        } catch (error) {
            console.error("Error during login:", error);
            alert("무언가 잘못됐어요");
        }
    };

    return (
        <div className="container">
            <div className="title">
                <div className="subtitle">
                    <h3>로그인을 진행해주세요</h3>
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
                        <span>로그인</span>
                    </button>
                </div>
                <div className="btn-group">
                    <div className="divider"></div>
                    <span className="or-text">OR</span>
                    <div className="divider"></div>
                </div>
                <button className="kakao-btn" onClick={() => alert("Kakao Login")}>
                    <img src={require('../img/kakao_login.png')} alt="Kakao Login"/>
                </button>
            </div>
            <div className="signup-text">
                <span>계정이 없으면 눌러주세요:</span>
                <button className="signup-btn" onClick={() => history.push("/signup")}>
                    <span>회원가입</span>
                </button>
            </div>
        </div>
    );
}

export default Login;
