import React from 'react';
import { useLocation } from 'react-router-dom';

function RoomList() {
    const {state} = useLocation(); //useLocation()훅으로 state를 받음
    console.log(state)
    return(
        <>
            <p>{state}</p>
        </>
    )
}
export default RoomList;