import axios from 'axios';

const AI_API_BASE_URL = 'http://localhost:8000' ;

const apiAi = axios.create({
    baseURL: AI_API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

export default apiAi;
