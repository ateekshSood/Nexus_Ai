import Grid from '@mui/material/Grid';
import { useState, useEffect, useRef } from 'react';
import axios from 'axios';
import ReactMarkdown from 'react-markdown';

import { 
 Box, TextField, Button,
  List, ListItemButton, 
} from '@mui/material';


function App() {
  const [sessions , setSessions] = useState([]);
  const [activeChatId , setActiveChatId] = useState(null);
  const [messages , setMessages] = useState([]);
  const [input , setInput] = useState('');

  const messagesEndRef = useRef(null);
      
  useEffect(()=>{
      messagesEndRef.current?.scrollIntoView({behavior:"smooth"});
  } , [messages])


  useEffect(()=>{

          axios.get("http://localhost:8080/api/chats")
          .then(response => {
            setSessions(response.data);
          })
          .catch(error => {
            alert("error has occured" , error);
          });

      } , []);


  function handleSelectSession(sessionId){
    setActiveChatId(sessionId);
    setMessages([]);

    axios.get(`http://localhost:8080/api/chats/${sessionId}/messages`)
    .then(response => {
      setMessages(response.data);
    })
    .catch(error => {
      alert("some error has occured" , error)
    })
  }

  function handleNewChat(){
    setActiveChatId(null);
    setMessages([]);
  }

  function handleSend(){
    if (!input.trim()) return;

    const newUserMessage = {text: input , sender:"USER"};

    setMessages([...messages , newUserMessage]);

    setInput('');

    const payload = {
      message : input,
      chatId : activeChatId
    };

    axios.post("http://localhost:8080/api/ai/chat" , 
      payload, { 
        headers: {
          "Content-Type" : "application/json" 
        }
      } 
    )
    .then(response => {

      const ai_msg = {
        text:response.data.text,
        sender:"AI"
      };

      setMessages(prevMessages => [...prevMessages , ai_msg]);

    
      if(activeChatId == null){
        setActiveChatId(response.data.chatId);

         axios.get("http://localhost:8080/api/chats")
            .then(response => {
              setSessions(response.data);
            })
            .catch(error => {
              alert("error has occured" , error);
            });
      }

    })
    .catch(error => {
      alert("some error has occured" , error);
    })

  }

  return(
    <>
      <Box sx={{ bgcolor: 'black', height: '100vh' }}>
          <Grid container spacing={2}>
            <Grid size={3} sx={{height:'100vh' , bgcolor:'#1e1e1e' , color:'white'}}>
              
              <List>
                <ListItemButton onClick={handleNewChat}
                sx={{position: 'relative',
                borderRadius: '8px',
                mb: 1,
                '@keyframes moveGradient': {
                  '0%': { backgroundPosition: '0% 50%' },
                  '50%': { backgroundPosition: '100% 50%' },
                  '100%': { backgroundPosition: '0% 50%' },
                },
                '&::before': {
                        content: '""',
                        position: 'absolute',
                        inset: 0,
                        padding: '3px',
                        borderRadius: '8px',
                        background: 'linear-gradient(45deg, red, orange, yellow, green, blue, indigo, violet)',
                        backgroundSize: '400% 400%', 
                        animation: 'moveGradient 5s ease infinite',
                        WebkitMask: 'linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0)',
                        WebkitMaskComposite: 'xor',
                        maskComposite: 'exclude',
                      },
                    }}
                  >
                  New Chat
                </ListItemButton>
                {sessions.map((session) => (
                  <ListItemButton onClick={() => handleSelectSession(session.id)}
                  key={session.id}>
                    {session.title}
                  </ListItemButton>
                ))}
              </List>
            </Grid>
            <Grid size={9}>
              <Box sx={{height:'80vh', overflowY: 'auto', p: 2, display: 'flex', flexDirection: 'column'}}>
                 {messages.map((msg, index) => (
                  <div key={index} style={{ color: msg.sender === 'USER' ? 'blue' : 'green', margin: '10px 0' }}>
                    <b>{msg.sender}:</b> 
                    <ReactMarkdown>{msg.text}</ReactMarkdown>
                  </div>
                ))}
                <div ref={messagesEndRef}></div>
              </Box>
              <TextField value={input} onChange={(e) =>
                setInput(e.target.value)
              }
              onKeyDown={(e) => {
                if (e.key === 'Enter' && !e.shiftKey) {
                  e.preventDefault();
                  handleSend();
                }
              }}
              placeholder="Type message here :D"
                  sx={{ 
                  bgcolor: 'white', 
                  borderRadius: 1, 
                  width: '80%', 
                  mr: 1 
                }}
              >

              </TextField>
              <Button onClick={handleSend} variant="contained" color="secondary">
                Send message
              </Button>
            </Grid>
          </Grid> 
      </Box>

  
    </>
  
  );
  
}

export default App;

