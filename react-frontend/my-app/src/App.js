import './App.css';
import { useEffect, useState } from 'react';

function App() {
  const [grid, setGrid] = useState([[]]);
  const [gridLetters, setGridLetters] = useState([[]]);
  const [gridColors, setGridColors] = useState([[]]);
  const [currentCol, setCurrentCol] = useState(0);
  const [currentRow, setCurrentRow] = useState(0);
  const [currentWord, setCurrentWord] = useState("");
  const [keyboard, setKeyboard] = useState([[]]);
  const [submittedKeys, setSubmittedKeys] = useState({});
  const [errorMessage, setErrorMessage] = useState("");
  const [gameOver, setGameOver] = useState(false);

  useEffect(() => {
    const tempGridLetters = [];
    for (var o = 0; o < 6; o++) {
      const row = [];
      for (var p = 0; p < 5; p++) {
        row.push("");
      }
      tempGridLetters.push(row);
    }
    setGridLetters(tempGridLetters);

    const tempGridColors = [];
    for (var u = 0; u < 6; u++) {
      const row = [];
      for (var v = 0; v < 5; v++) {
        row.push("white");
      }
      tempGridColors.push(row);
    }
    setGridColors(tempGridColors);
    setKeyboard([
      ["Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"],
      ["A", "S", "D", "F", "G", "H", "J", "K", "L"],
      ["Z", "X", "C", "V", "B", "N", "M"]
    ]);
  }, []);

  useEffect(() => {
    const tempGrid = [];
    for (var y = 0; y < 6; y++) {
      const row = [];
      for (var i = 0; i < 5; i++) {
        row.push(<div size={1000} style={{
          display: 'inline-block',
          width: '50px',
          height: '50px',
          marginRight: '7px',
          marginBottom: '7px',
          background: gridColors != null && gridColors[y] != null ? gridColors[y][i] : "white"
        }}>
          <p>{gridLetters != null && gridLetters[y] != null ? gridLetters[y][i] : ""}</p>
        </div>)
      }
      tempGrid.push(row);
    }
    setGrid(tempGrid);
  }, [gridLetters, gridColors]);

  useEffect(() => {
    document.addEventListener('keydown', handleKey);
    function handleKey(e) {
      if (e.key === 'Enter' && currentCol === 5) {
        //Submit
        fetch('http://localhost:8080/words/check/' + currentWord)
          .then(res => res.json())
          .then(data => {

            if (data.response === 'Can not find the word in the database. Please try a different word.') {
              setErrorMessage(data.response);
            }
            else {
              const tempGridColors = JSON.parse(JSON.stringify(gridColors));;
              for (var p = 0; p < 5; p++) {
                tempGridColors[currentRow][p] = data.response[p];
              }
              setGridColors(tempGridColors);
              setCurrentRow(currentRow + 1);
              setCurrentCol(0);
              setCurrentWord("");
              let newKeys = submittedKeys;

              //Dealing keyboard edge case
              for (let i = 0; i < currentWord.length; i++) {
                if (newKeys[currentWord.charAt(i).toUpperCase()] == null ||
                  (newKeys[currentWord.charAt(i).toUpperCase()] === 'yellow' && data.response[i] === 'green')
                ) {
                  newKeys[currentWord.charAt(i).toUpperCase()] = data.response[i];
                }
              }
              setSubmittedKeys(newKeys);
              //Manual refresh
              const tempGridLetters = JSON.parse(JSON.stringify(gridLetters));
              setGridLetters(tempGridLetters);

              setErrorMessage("");
              if (data.response.filter(x => x === 'lightgreen').length === data.response.length) {
                alert("Congratulations you have correctly guessed today's solution");
                setGameOver(true);
              }
              else if (currentRow >= 5) {
                fetch('http://localhost:8080/words/solution')
                  .then(res => res.json())
                  .then(data => {
                    alert("Unfortunately you did not guess today's word. Today's word is " + data.response.toUpperCase());
                    setGameOver(true);
                  })
              }
            }
          });
      }
      else if (e.key === 'Backspace' && !gameOver) {
        if (currentCol !== 0) {
          const tempGridLetters = JSON.parse(JSON.stringify(gridLetters));
          tempGridLetters[currentRow][currentCol - 1] = "";
          setGridLetters(tempGridLetters);
          setCurrentCol(currentCol - 1);
          setCurrentWord(currentWord.substring(0, currentWord.length - 1));
        }
      }
      else if (currentCol !== 5 && e.key.length === 1 && (/[a-zA-Z]/).test(e.key) && !gameOver) {
        const tempGridLetters = JSON.parse(JSON.stringify(gridLetters));
        tempGridLetters[currentRow][currentCol] = e.key.toUpperCase();
        setGridLetters(tempGridLetters);
        setCurrentCol(currentCol + 1);
        setCurrentWord(currentWord + e.key);
      }
    }
    return () => document.removeEventListener("keydown", handleKey);
  });

  return (
    <div className="App" tabIndex="0">
      <header className="App-header">
        Wordle
      </header>
      {grid.map((object, i) => {
        return <div style={{ flexDirection: 'row' }} key={i}>{object}</div>
      })}
      <div><p style={{ color: '#ff3636' }}>{errorMessage}</p></div>
      {keyboard.map((object, i) => {
        return <div style={{ flexDirection: 'row', display: 'flex' }} key={i}>{object.map((object, j) => {
          return <div style={{
            flexDirection: 'row',
            background: submittedKeys[object] != null ? submittedKeys[object] : "white",
            width: '25px',
            height: '25px',
            marginTop: i !== 0 ? "0px" : errorMessage.length > 0 ? '18px' : '55px',
            justifyContent: 'center',
            marginRight: '7px',
            marginBottom: '7px',
          }}>{object}</div>;
        })}</div>
      })}

    </div>
  );
}

export default App;
