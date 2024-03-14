import React from 'react';
import {Link} from "react-router-dom";
import './RightSidebar.css';


const RightSidebar = () => {
  return (
    <div className='RightSidebar'>
        <ul>
            <li>
                <img width="48" height="48"
                     src="https://img.icons8.com/external-tanah-basah-basic-outline-tanah-basah/48/external-add-user-tanah-basah-basic-outline-tanah-basah-2.png"
                     alt="external-add-user-tanah-basah-basic-outline-tanah-basah-2"/>
                <span>친구추가</span>
            </li>
        </ul>
        <ul>
            <li>
                <h1>Online</h1>
                <h3>think0507</h3>
            </li>
            <li>
                <h1>Offline</h1>
                <h3>dongwook1234</h3>
            </li>
        </ul>
    </div>
  );
}

export default RightSidebar;