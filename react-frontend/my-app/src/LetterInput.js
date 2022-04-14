import {useState} from 'react'
export function LetterInput(letter) {
    return (
        <div size={1000} style={{
            display: 'inline-block', 
            width: '50px', 
            height: '50px', 
            marginRight: '7px', 
            marginBottom: '2px',
            background: 'white'
        }}>
            <p>{letter}</p>
        </div>
    );
}