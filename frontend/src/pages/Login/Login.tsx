import React, { useState } from "react";
import axios from "axios";
import "./LoginPage.css";
import Button from "../../components/Common/Button";
import { useNavigate } from "react-router-dom";

interface dataState {
  email: string;
  id: string;
  name: string;
  password: string;
  phone: string;
  token: string;
}

interface LoginResponse {
  resultType: string;
  message: string;
  data: dataState[];
}

const LoginPage: React.FC = () => {
  // const [email, setEmailAddr] = useState<string>(""); // emailAddr의 타입을 string으로 지정
  // const [password, setPassword] = useState<string>(""); // password의 타입을 string으로 지정
  const [formData, setFormData] = useState({
    email: "",
    password: "",
  });

  const navigate = useNavigate();

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  // 로그인 함수
  const handleSubmit = (e: React.FormEvent<HTMLFormElement>): void => {
    e.preventDefault();
    axios
      .post<LoginResponse>(
        `${process.env.REACT_APP_API_BASE_URL}/auth/signin`,
        { email: formData.email, password: formData.password },
        {
          headers: {
            "Content-Type": "application/json",
          },
        }
      ) // 응답의 타입을 LoginResponse로 지정
      .then((response) => {
        if (typeof response === "undefined") {
          console.error("응답 데이터가 undefined 입니다.");
        } else if (response.status === 200) {
          // 성공 처리
          if (response.data && response.data.resultType === "success") {
            const data = response.data.data[0];

            if (data.token) {
              localStorage.setItem("accessToken", data.token);
            } else {
              console.error("토큰을 찾을 수 없습니다.");
            }
            navigate("/");
          } else {
            console.log(response.data.message);
          }
        }
      })
      .catch((error) => {
        // 400 에러 발생 시 처리
        if (error.response) {
          if (error.response.status === 400) {
            console.error("400 Error:", error.response.data.message);
          } else {
            console.error("Unexpected Error:", error.response);
          }
        } else {
          console.error("Network Error:", error.message);
        }
      });
  };

  return (
    <div className="login-page" style={{ backgroundColor: "#ffffff" }}>
      <div className="logoSz">
        <a href="/login">
          <img
            style={{ width: "100%", height: "100%" }}
            src="/airplane.png"
            alt="로고위치"
          />
        </a>
      </div>

      <form className="loginForm" id="loginForm" onSubmit={handleSubmit}>
        <label className="formLabel">아이디</label>
        <input
          type="text"
          className="email"
          name="email"
          required
          placeholder="이메일을 입력해 주세요"
          value={formData.email}
          onChange={handleChange}
        />
        <label className="formLabel">비밀번호</label>
        <input
          type="password"
          className="password"
          name="password"
          required
          placeholder="비밀번호를 입력해 주세요"
          value={formData.password}
          onChange={handleChange}
        />
        <div className="lostE">
          <p style={{ fontSize: "9px" }}>
            이메일 또는 비밀번호를 잊어버리셨나요?
          </p>
          <a className="lostEp1" href="/login/find">
            이메일 & 비밀번호 찾기
          </a>
        </div>
        <Button size="L" name="로그인" $bgColor="blue" type="submit" />
        {/* <button type="submit">로그인</button> */}
      </form>

      {/* 회원가입 페이지 만들면 경로 바꾸기! */}
      <a className="lostEp2" href="/login/register">
        회원가입
      </a>
      {/* 경로 바꾸기! */}
      <a className="noLogin" href="/">
        로그인없이 구경하기
      </a>
    </div>
  );
};

export default LoginPage;
