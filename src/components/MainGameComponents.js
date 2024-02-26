import React from 'react'
import {useNavigate} from 'react-router-dom'

function MainGameComponents({gameName}){
    const navigate = useNavigate();
    const gameNameString = gameName.state;
    const imgURLString = gameName.imgURL;
    console.log("imgurl은"+imgURLString)

    const GoToRoomList = () => {
        navigate(`./RoomList`, {state:gameNameString}) //해당경로로 넘길때 state에 gameName 담아서 넘겨줌
    }

    return(
        <>
            <img alt={'왜로드안되지;;'} src={imgURLString} onClick={GoToRoomList}/>
            <h2>{gameNameString}</h2>
        </>
    )
}

export default MainGameComponents