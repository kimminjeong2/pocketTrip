import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { AppDispatch, RootState } from "../../store";
import { ChangeCurrentPage } from "../../slices/currentPageSlice";
import Header from "../../components/Common/Header";
import Alert from "../../components/Common/Alert";
import TourCard from "./TourCard";
import styled from "styled-components";

const H2 = styled.h2`
  font-size: 18px;
  font-family: inherit;
  margin: 20px;
`;

export default function MainPage() {
  const dispatch: AppDispatch = useDispatch();
  const travelData = useSelector((state: RootState) => state.travel); // Redux에서 여행 데이터 가져오기

  useEffect(() => {
    dispatch(ChangeCurrentPage("home"));
  }, []);

  // 알림창 관련 로직
  const [isAlertVisible, setIsAlertVisible] = useState(false);
  const [alertMessage, setAlertMessage] = useState("");
  const [alertType, setAlertType] = useState<"success" | "error" | "info">(
    "success"
  );

  useEffect(() => {
    if (isAlertVisible) {
      const timer = setTimeout(() => {
        setIsAlertVisible(false);
      }, 2000);
      return () => clearTimeout(timer);
    }
  }, [isAlertVisible]);

  const handleAction = () => {
    setAlertMessage("작업이 성공적으로 완료되었습니다.");
    setAlertType("success");
    setIsAlertVisible(true);
  };


  // axios 요청으로 현재 날짜 기준으로 해당하는 여행 정보를 하나만 불러온다.
  const data = {
    id: 1,
    name: "일본여행지갑", // 여행지갑 이름
    selectedCountry: "일본", // 여행지 이름
    budget: "2,000,000", // 현재 누적 금액
    ImgArr: [
      "./ProfileImage.png",
      "./ProfileImage.png",
      "./ProfileImage.png",
      "./ProfileImage.png",
      "./ProfileImage.png",
      "./ProfileImage.png",
      "./ProfileImage.png",
    ], // 참여인원들 프로필 이미지 주소
    startDate: "2025-01-18", // 여행 시작일
    endDate: "2025-02-20", // 여행 종료일
    bgImg: "./japan.jpg",
  };

  const userData = {
    name: "황종현",
    profile: "ProfileImage.png",
  };

  const data = {
    ...travelData,
    budget: Number(travelData.budget),
  };

  return (
    <div>
      <Header $bgColor={"#eaf6ff"} userData={userData} />
      <H2>현재 여행중인 지역</H2>
      <TourCard Tourdata={travelData} /> {/* Redux에서 가져온 데이터 사용 */}
      {isAlertVisible && (
        <Alert
          alertState={alertType}
          message={alertMessage}
          setIsAlertVisible={setIsAlertVisible}
        />
      )}
    </div>
  );
}
